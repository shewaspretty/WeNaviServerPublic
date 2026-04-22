package com.hrlee.transnaviserver.springboot.service.route.factory;

import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.NodeWeight;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  Use once by instantiated
 */
public class RouteSummaryFactory {

    private static final int NUMBER_OF_DIVISION = 3;

    private final WayDistanceHolder[] wayDistanceHolders = new WayDistanceHolder[NUMBER_OF_DIVISION]; // Replaced from HashMap
    private final int maxNodeCnt;

    private int currentDivisionPtr = 0;

    protected RouteSummaryFactory(int maxNodeDepth) {
        for(int i=0; i<NUMBER_OF_DIVISION; i++)
            wayDistanceHolders[i] = new WayDistanceHolder();

        this.maxNodeCnt = maxNodeDepth; // Because the start node doesn't have fromWay
    }

    public void record(@NonNull NodeWrapper targetNode) {
        if(wayDistanceHolders[currentDivisionPtr].roadRecordedCnt >= maxNodeCnt / NUMBER_OF_DIVISION)
            if(currentDivisionPtr +1 < NUMBER_OF_DIVISION)
                currentDivisionPtr++;

        NodeWeight targetNodeWeight = targetNode.getAttachedNodeWeight();
        if(targetNodeWeight == null) return;

        wayDistanceHolders[currentDivisionPtr].record(targetNodeWeight);
    }

    @NonNull
    public List<String> getRouteSummary() {
        ArrayList<String> returnAble = new ArrayList<>(NUMBER_OF_DIVISION);

        String appendableToReturnAble = null;
        for(int i = 0; i< wayDistanceHolders.length; i++) {
            if((appendableToReturnAble = wayDistanceHolders[i].getMostLongestWayName(returnAble)) == null)
                continue;
            returnAble.add(appendableToReturnAble);
        }

        return returnAble;
    }

    private static class WayDistanceHolder {
        private final ArrayList<WayDistance> wayDistances = new ArrayList<>();
        private int roadRecordedCnt = 0;

        private void record(NodeWeight targetNodeWeight) {
            roadRecordedCnt++;
            WayWrapper fromWay = targetNodeWeight.getFromWay();
            if(fromWay == null) return;

            String wayName = fromWay.getWayName();
            if(wayName == null) return;

            double distanceToFromNode = targetNodeWeight.getDistanceToFromNodeMeter();
            if(distanceToFromNode < 0) return;

            WayDistance wayDistancesIt = null;
            for(int i = 0; i< wayDistances.size(); i++) {
                wayDistancesIt = wayDistances.get(i);
                if(!wayDistancesIt.wayName.equals(wayName))
                    continue;

                wayDistancesIt.totalDistance += distanceToFromNode;
                return;
            }
            wayDistances.add(new WayDistance(wayName, distanceToFromNode));
        }

        @Nullable
        private String getMostLongestWayName(@NonNull List<String> overlappingWayNames) {
            if(wayDistances.isEmpty())
                return null;

            int[] wayDistanceIndexOrderDescByDistance = getIndexOrderDescByDistance(overlappingWayNames.size() +1);
            if(wayDistanceIndexOrderDescByDistance == null)
                return null;

            if(overlappingWayNames.isEmpty())
                return wayDistances.get(wayDistanceIndexOrderDescByDistance[0]).wayName;

            WayDistance returnAbleIt = null;
            for(int i=0; i<wayDistanceIndexOrderDescByDistance.length; i++) {
                if(wayDistanceIndexOrderDescByDistance[i] < 0)
                    continue;

                returnAbleIt = wayDistances.get(wayDistanceIndexOrderDescByDistance[i]);
                boolean isOverLapped = false;
                
                for(int j=0; j<overlappingWayNames.size(); j++) {
                    if(!returnAbleIt.wayName.equals(overlappingWayNames.get(j)))
                        continue;
                    isOverLapped = true;
                    break;
                }

                if(isOverLapped)
                    continue;
                return returnAbleIt.wayName;
            }
            return null;
        }

        @Nullable
        private int[] getIndexOrderDescByDistance(int size) {
            int[] returnAble = new int[size];
            Arrays.fill(returnAble, -1);

            WayDistance wayDistanceIt = null;
            for(int i = 0; i< wayDistances.size(); i++) {
                wayDistanceIt = wayDistances.get(i);

                int descIndexOrderIt = -1;
                WayDistance wayDistanceDescOrderIt = null;

                for(int j=0; j<returnAble.length; j++) {
                    descIndexOrderIt = returnAble[j];
                    if(descIndexOrderIt < 0) {
                        returnAble[j] = i;
                        break;
                    }

                    wayDistanceDescOrderIt = wayDistances.get(descIndexOrderIt);
                    if(wayDistanceIt.totalDistance <= wayDistanceDescOrderIt.totalDistance)
                        continue;

                    int wayDistanceIndexOrderSortingPtr = returnAble.length;
                    while(--wayDistanceIndexOrderSortingPtr > j)
                        returnAble[wayDistanceIndexOrderSortingPtr] = returnAble[wayDistanceIndexOrderSortingPtr -1];
                    returnAble[j] = i;
                    break;
                }
            }

            if(returnAble[0] < 0)
                return null;
            return returnAble;
        }

        @AllArgsConstructor
        private static class WayDistance {
            private final String wayName;
            private double totalDistance;
        }
    }
}
