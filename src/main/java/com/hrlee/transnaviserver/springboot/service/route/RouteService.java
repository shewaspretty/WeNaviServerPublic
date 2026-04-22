// local variable (resigned) color hex 3BFCF2
package com.hrlee.transnaviserver.springboot.service.route;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.dto.route.Route;
import com.hrlee.transnaviserver.springboot.service.route.param.type.AbstractRouteType;
import com.hrlee.transnaviserver.springboot.service.route.param.type.DrivingRouteType;
import com.hrlee.transnaviserver.springboot.service.route.param.type.WalkingRouteType;
import com.hrlee.transnaviserver.springboot.osm.jdbc.OsmDataFactory;
import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.node.VirtualNode;
import com.hrlee.transnaviserver.springboot.osm.way.fragment.FragmentWayComparable;
import com.hrlee.transnaviserver.springboot.osm.way.fragment.FragmentWayFactory;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.Dijkstra;
import com.hrlee.transnaviserver.springboot.service.route.factory.RouteFactory;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.GeodeticCalculator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService implements LoggAble {

    private final JdbcTemplate jdbcTemplate;

    private final CoordinateTool coordinateTool = CoordinateTool.getInstance();
    private static final int DISTANCE_MAX_EXPANDABLE_FOR_BOUND_METER = 3000;
    private static final int DISTANCE_MAX_EXPANDABLE_FOR_VIRTUAL_NODE_BOUND_METER = 1000;

    private static final String COMPARABLE_KEY_ORDER_BY_NEAREST_START = "start";
    private static final String COMPARABLE_KEY_ORDER_BY_NEAREST_DESTINATION = "destination";

    private static final long START_VIRTUAL_NODE_ID = -1;
    private static final long END_VIRTUAL_NODE_ID = -2;

    @RequiredArgsConstructor
    public static enum Type {
        WALKING(WalkingRouteType.class, "walking"),
        DRIVING(DrivingRouteType.class, "driving");

        @Getter
        private final Class<? extends AbstractRouteType> routeType;
        private final String controllerIdentifier;

        @Nullable
        public static AbstractRouteType generateRouteType(String controllerIdentifier, @Nullable int[] activatedOptionsOrdinals) {
            Type[] values = values();
            for(int i=0; i<values.length; i++) {
                if(!values[i].controllerIdentifier.equals(controllerIdentifier))
                    continue;
                try {
                    return values[i].getRouteType().getDeclaredConstructor(int[].class).newInstance(activatedOptionsOrdinals);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
    }

    @Nullable
    public Route getRoute(Coordinate start, Coordinate destination, AbstractRouteType type) {
        long currentTimeForLogging = -1;
        logInfo("start to getting route");
        ReferencedEnvelope extendedBound = getExtendedBound(start, destination);

        logUsedMemoryInfo();
        logInfo("trying to get nodes from db.. ");
        currentTimeForLogging = System.currentTimeMillis();
        OsmDataFactory osmDataFactory = new OsmDataFactory();
        List<NodeWrapper> allNodes = getAllOsmNodesInBound(extendedBound, type, osmDataFactory);
        if(allNodes == null)
            return null;

        /*logUsedMemoryInfo();
        logInfo("convert raw nodes to wrappers.. ");
        OsmDataHolder osmData = new OsmDataFactory(allRawNodes).generate();
        List<NodeWrapper> allNodes = osmData.getNodes();
        //List<WayWrapper> allWays = osmData.getWays();*/

        logInfo("takes " + (System.currentTimeMillis() - currentTimeForLogging) / 1000 + "secs");
        logUsedMemoryInfo();
        logInfo("creating index asc order by start location..");
        List<Integer> nodesIndexOrderByNearestStart = getIndexOrderAscByDistanceWithDistanceLimit(allNodes, start, COMPARABLE_KEY_ORDER_BY_NEAREST_START);
        if(nodesIndexOrderByNearestStart == null)
            return null;

        logUsedMemoryInfo();
        logInfo("creating index asc order by destination..");
        List<Integer> nodesIndexOrderByNearestDestination = getIndexOrderAscByDistanceWithDistanceLimit(allNodes, destination, COMPARABLE_KEY_ORDER_BY_NEAREST_DESTINATION);
        if(nodesIndexOrderByNearestDestination == null)
            return null;

        logUsedMemoryInfo();
        logInfo("getting fragment ways in all ways..");
        currentTimeForLogging = System.currentTimeMillis();
        FragmentWayComparable startCoordinateOnRoad = getNearestCoordinateInRoad(allNodes, nodesIndexOrderByNearestStart, start);
        FragmentWayComparable endCoordinateOnRoad = getNearestCoordinateInRoad(allNodes, nodesIndexOrderByNearestDestination, destination);

        allNodes.add(new VirtualNode(START_VIRTUAL_NODE_ID, startCoordinateOnRoad.getNearestCoordinateToTarget(), startCoordinateOnRoad.getOriginNodes()));
        allNodes.add(new VirtualNode(END_VIRTUAL_NODE_ID, endCoordinateOnRoad.getNearestCoordinateToTarget(), endCoordinateOnRoad.getOriginNodes()));

        logInfo("takes " + (System.currentTimeMillis() - currentTimeForLogging) / 1000 + "secs");
        logUsedMemoryInfo();
        logInfo("processing Dijkstra..");
        Dijkstra dijkstra = new Dijkstra(allNodes.get(allNodes.size() -2), allNodes.get(allNodes.size() -1), System.currentTimeMillis(), type);
        NodeWrapper destinationNodeReached = dijkstra.process();
        if(destinationNodeReached == null)
            return null;

        logUsedMemoryInfo();
        logInfo("serializing route..");
        return new RouteFactory().backTrackAndGetExposableRoute(destinationNodeReached);
    }


    private FragmentWayComparable getNearestCoordinateInRoad(List<NodeWrapper> nodes, List<Integer> nodesIndexOrderBy, Coordinate target) {
        List<FragmentWayComparable> fragmentWays = new FragmentWayFactory(nodes, nodesIndexOrderBy, coordinateTool).getComparableList(target);
        FragmentWayComparable fragmentWaysIt = null;

        while(true) {
            int fragmentWaysPtr = -1;
            int cutInHalfDisabledCnt = 0;

            while(++fragmentWaysPtr < fragmentWays.size()) {
                fragmentWaysIt = fragmentWays.get(fragmentWaysPtr);
                if(fragmentWaysIt.isCutInHalfDisabled()) {
                    cutInHalfDisabledCnt++;
                    continue;
                }
                fragmentWaysIt.cutInHalfByDistanceFromTarget();
            }
            if(cutInHalfDisabledCnt == fragmentWays.size())
                break;
        }

        double minDistance = fragmentWays.get(0).getDistanceComparableMeter();
        int minPtr = 0;
        for(int i=1; i<fragmentWays.size(); i++) {
            if(!(fragmentWays.get(i).getDistanceComparableMeter() < minDistance))
                continue;
            minDistance = fragmentWays.get(i).getDistanceComparableMeter();
            minPtr = i;
        }

        return fragmentWays.get(minPtr);
    }

    @Nullable
    private List<Integer> getIndexOrderAscByDistanceWithDistanceLimit(List<NodeWrapper> nodes, Coordinate fromLocation, String comparableValueKey) {
        ArrayList<Integer> nodeIndexOrderAsc = new ArrayList<>();
        GeodeticCalculator geodeticCalculator = coordinateTool.createNewGeodeticCalculator();

        NodeWrapper nodeIt;
        for(int i=0; i<nodes.size(); i++) {
            nodeIt = nodes.get(i);

            double distanceMeter = coordinateTool.getDistanceMeter(fromLocation, nodeIt.generateNewCoordinate(), geodeticCalculator);
            if(distanceMeter > DISTANCE_MAX_EXPANDABLE_FOR_VIRTUAL_NODE_BOUND_METER)
                continue;
            nodeIt.addComparableValue(comparableValueKey, distanceMeter);

            if(nodeIndexOrderAsc.isEmpty()) {
                nodeIndexOrderAsc.add(i);
                continue;
            }

            int nodeIndexOrderAscPtr = -1;
            Double comparableDistanceAtIndexIt = null;

            while(++nodeIndexOrderAscPtr < nodeIndexOrderAsc.size()) {
                comparableDistanceAtIndexIt = nodes.get(nodeIndexOrderAsc.get(nodeIndexOrderAscPtr)).getComparableValue(comparableValueKey);
                if(comparableDistanceAtIndexIt == null)
                    return null;

                if(comparableDistanceAtIndexIt < distanceMeter)
                    continue;

                nodeIndexOrderAsc.add(nodeIndexOrderAscPtr, i);
                break;
            }

            if(!(nodeIndexOrderAscPtr >= nodeIndexOrderAsc.size()))
                continue;
            nodeIndexOrderAsc.add(i);
        }
        return nodeIndexOrderAsc;
    }

    @Nullable
    private List<NodeWrapper> getAllOsmNodesInBound(ReferencedEnvelope bounding, AbstractRouteType routeType, OsmDataFactory osmDataFactory) {
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

        coordinateTool.moveCoordinate(minCoordinate, CoordinateTool.Direction.WEST, DISTANCE_MAX_EXPANDABLE_FOR_BOUND_METER);
        coordinateTool.moveCoordinate(minCoordinate, CoordinateTool.Direction.SOUTH, DISTANCE_MAX_EXPANDABLE_FOR_BOUND_METER);

        coordinateTool.moveCoordinate(maxCoordinate, CoordinateTool.Direction.NORTH, DISTANCE_MAX_EXPANDABLE_FOR_BOUND_METER);
        coordinateTool.moveCoordinate(maxCoordinate, CoordinateTool.Direction.EAST, DISTANCE_MAX_EXPANDABLE_FOR_BOUND_METER);

        return coordinateTool.getBoundingBox(minCoordinate, maxCoordinate);
    }


}
