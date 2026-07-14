package com.hrlee.transnaviserver.springboot.service.route.finder;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import lombok.*;
import org.geotools.referencing.GeodeticCalculator;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class NearbyNodeFinder {

    private final List<NodeWrapper> allNodes;
    private final CoordinateTool coordinateTool;

    private static final int DISTANCE_MAX_EXPANDABLE_BOUND_METER = 1000;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class Result {
        private final ArrayList<NodeWrapper> nodesNearStart = new ArrayList<>();
        private final ArrayList<NodeWrapper> nodesNearDestination = new ArrayList<>();
    }

    public Result getNearbyNodes(Coordinate start, Coordinate destination) {
        GeodeticCalculator geodeticCalculator = coordinateTool.createNewGeodeticCalculator();
        Result returnable = new Result();

        NodeWrapper nodeIt;
        double distanceFromNodeItToStart = -1.0;
        double distanceFromNodeItToDestination = -1.0;

        for(int i=0; i<allNodes.size(); i++) {
            nodeIt = allNodes.get(i);
            distanceFromNodeItToStart = coordinateTool.getDistanceMeter(nodeIt.generateNewCoordinate(), start, geodeticCalculator);
            distanceFromNodeItToDestination = coordinateTool.getDistanceMeter(nodeIt.generateNewCoordinate(), destination, geodeticCalculator);

            if(distanceFromNodeItToStart > DISTANCE_MAX_EXPANDABLE_BOUND_METER && distanceFromNodeItToDestination > DISTANCE_MAX_EXPANDABLE_BOUND_METER)
                continue;

            if(distanceFromNodeItToStart <= DISTANCE_MAX_EXPANDABLE_BOUND_METER)
                returnable.nodesNearStart.add(nodeIt);

            if(distanceFromNodeItToDestination > DISTANCE_MAX_EXPANDABLE_BOUND_METER)
                continue;
            returnable.nodesNearDestination.add(nodeIt);
        }

        return returnable;
    }
}
