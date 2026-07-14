// local variable (resigned) color hex 3BFCF2
package com.hrlee.transnaviserver.springboot.service.route;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.dto.route.Route;
import com.hrlee.transnaviserver.springboot.osm.node.virtual.VirtualNodeFactory;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.NodeWeight;
import com.hrlee.transnaviserver.springboot.service.route.param.type.AbstractRouteType;
import com.hrlee.transnaviserver.springboot.service.route.param.type.DrivingRouteType;
import com.hrlee.transnaviserver.springboot.service.route.param.type.WalkingRouteType;
import com.hrlee.transnaviserver.springboot.osm.jdbc.OsmDataFactory;
import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.node.virtual.VirtualNode;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.Dijkstra;
import com.hrlee.transnaviserver.springboot.service.route.factory.RouteFactory;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService implements LoggAble {

    private final JdbcTemplate jdbcTemplate;

    private final CoordinateTool coordinateTool = CoordinateTool.getInstance();
    private static final int DISTANCE_MAX_EXPANDABLE_FOR_ROUTE_BOUND_METER = 3000;

    @RequiredArgsConstructor
    public static enum Type {
        WALKING(WalkingRouteType.class, "walking"),
        DRIVING(DrivingRouteType.class, "driving");

        private final Class<? extends AbstractRouteType> routeType;
        private final String controllerIdentifier;

        @Nullable
        public static AbstractRouteType generateRouteType(String controllerIdentifier, @Nullable int[] activatedOptionsOrdinals) {
            Type[] values = values();
            for(int i=0; i<values.length; i++) {
                if(!values[i].controllerIdentifier.equals(controllerIdentifier))
                    continue;
                try {
                    return values[i].routeType.getDeclaredConstructor(int[].class).newInstance(activatedOptionsOrdinals);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
    }

    @Nullable
    public Route getRoute(Coordinate start, Coordinate destination, AbstractRouteType routeType) {
        ReferencedEnvelope extendedBound = getExtendedBound(start, destination);

        OsmDataFactory osmDataFactory = new OsmDataFactory();
        List<NodeWrapper> allNodes = getAllOsmNodesInBoundFromDB(extendedBound, routeType, osmDataFactory);
        if(allNodes == null)
            return null;

        VirtualNodeFactory virtualNodeFactory = VirtualNodeFactory.create(allNodes, coordinateTool, start, destination);
        VirtualNode startNode = null;
        VirtualNode destinationNode = null;

        boolean needsNewStartNode = false;
        boolean needsNewDestinationNode = false;

        NodeWrapper destinationNodeReached = null;

        while(true) {
            if(startNode == null || needsNewStartNode) {
                startNode = virtualNodeFactory.createNewVirtualNode(VirtualNodeFactory.Type.START_VIRTUAL_NODE, startNode);
                needsNewStartNode = false;
            }
            if(destinationNode == null || needsNewDestinationNode) {
                destinationNode = virtualNodeFactory.createNewVirtualNode(VirtualNodeFactory.Type.DESTINATION_VIRTUAL_NODE, destinationNode);
                needsNewDestinationNode = false;
            }

            if(startNode == null || destinationNode == null)
                return null;

            Dijkstra dijkstra = new Dijkstra(startNode, destinationNode, System.currentTimeMillis(), routeType);
            destinationNodeReached = dijkstra.process();
            if(destinationNodeReached != null)
                break;

            if(dijkstra.getVisitedNodesCnt() < allNodes.size() / 2)
                needsNewStartNode = true;
            else
                needsNewDestinationNode = true;

            for(int i=0; i<allNodes.size(); i++)
                allNodes.get(i).clearVisitedHistory();
        }

        return new RouteFactory().backTrackAndGetExposableRoute(destinationNodeReached);
    }

    @Nullable
    private List<NodeWrapper> getAllOsmNodesInBoundFromDB(ReferencedEnvelope bounding, AbstractRouteType routeType, OsmDataFactory osmDataFactory) {
        String query =
                "WITH Node AS ( " +
                        "SELECT * FROM node AS n" +
                        " WHERE n.latitude>=" + bounding.getMinY() +
                        " AND n.longitude>=" + bounding.getMinX() +
                        " AND n.latitude<= " + bounding.getMaxY() +
                        " AND n.longitude<=" + bounding.getMaxX() +
                        "), " +
                "WayNode AS ( " +
                        "SELECT Node.*, way_id, order_in_way FROM Node " +
                        "INNER JOIN way_node " +
                        "ON Node.id=way_node.node_id" +
                        "), " +
                "HighWay AS ( " +
                        "SELECT WayNode.*, tag_value as highway, way_tag.id AS highway_id FROM WayNode " +
                        "STRAIGHT_JOIN way_tag " +
                        "ON way_tag.id=WayNode.way_id " +
                        "AND way_tag.tag_key=\"highway\" "+
                        "AND " + routeType.getHighwayConditionQuery() +
                        "), " +
                "WayTag AS ( " +
                        "SELECT HighWay.*, tag_key AS way_tag_key, tag_value AS way_tag_value FROM HighWay " +
                        "STRAIGHT_JOIN way_tag " +
                        "ON way_tag.id=HighWay.highway_id" +
                        ") " +
                "SELECT * FROM WayTag";

        List<NodeWrapper> nodes = jdbcTemplate.query(query, osmDataFactory);
        if(nodes == null || nodes.isEmpty())
            return null;
        return nodes;
    }

    private ReferencedEnvelope getExtendedBound(Coordinate startCoordinate, Coordinate endCoordinate) {
        ReferencedEnvelope pureBound = coordinateTool.getBoundingBox(startCoordinate, endCoordinate);
        Coordinate minCoordinate = Coordinate.getMinFromBound(pureBound);
        Coordinate maxCoordinate = Coordinate.getMaxFromBound(pureBound);

        coordinateTool.moveCoordinate(minCoordinate, CoordinateTool.Direction.WEST, DISTANCE_MAX_EXPANDABLE_FOR_ROUTE_BOUND_METER);
        coordinateTool.moveCoordinate(minCoordinate, CoordinateTool.Direction.SOUTH, DISTANCE_MAX_EXPANDABLE_FOR_ROUTE_BOUND_METER);

        coordinateTool.moveCoordinate(maxCoordinate, CoordinateTool.Direction.NORTH, DISTANCE_MAX_EXPANDABLE_FOR_ROUTE_BOUND_METER);
        coordinateTool.moveCoordinate(maxCoordinate, CoordinateTool.Direction.EAST, DISTANCE_MAX_EXPANDABLE_FOR_ROUTE_BOUND_METER);

        return coordinateTool.getBoundingBox(minCoordinate, maxCoordinate);
    }

}
