package com.hrlee.transnaviserver.springboot.service.route.factory;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.NodeWeight;
import com.hrlee.transnaviserver.springboot.service.route.handler.WayChangedDetectable;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Designed for being used once by instantiated.
 * Not Thread-Safe
 */
public final class TagFactory implements LoggAble, WayChangedDetectable {

    @Nullable
    private NodeWrapper currentNode = null;
    @Nullable
    private WayWrapper currentWay = null;
    @Nullable
    private HashMap<String, String> currentWayTags = null;

    private final DirectionTagFactory directionTagFactory = new DirectionTagFactory();


    public HashMap<String, String> getTags(NodeWrapper targetNode) {
        NodeWeight targetNodeWeight = targetNode.getAttachedNodeWeight();
        if(targetNodeWeight == null)
            return null;

        WayWrapper targetWay = targetNodeWeight.getFromWay();
        if(targetWay == null) {
            if(currentWay == null || currentWayTags == null)
                return null;
            return currentWayTags;
        }

        NodeWrapper prevNode = currentNode;
        WayWrapper prevWay = currentWay;

        currentNode = targetNode;
        currentWay = targetWay;

        if(prevWay != null && currentWayTags != null)
            if(prevWay == currentWay)
                return currentWayTags;

        currentWayTags = getNewWayTags(currentWay);

        if(prevWay == null) {
            HashMap<String, String> arrivedTagAppendable = new HashMap<>(currentWayTags);
            directionTagFactory.addArrivedTag(arrivedTagAppendable);
            return arrivedTagAppendable;
        }

        if(!isWayChanged(prevWay, currentWay))
            return currentWayTags;

        HashMap<String, String> directionTags = directionTagFactory.getNewDirectionTags(targetNodeWeight.getFromNode(), currentNode, prevNode, targetNodeWeight.getFromWay());
        if(directionTags == null)
            return null;
        directionTags.putAll(currentWayTags);
        return directionTags;
    }

    private class DirectionTagFactory {
        private final CoordinateTool coordinateTool = CoordinateTool.getInstance();
        @Nullable
        private NodeWrapper roundaboutExitNode = null;

        private void addArrivedTag(Map<String, String> targetTags) {
            addDirectionTagsByDescriptionDirection(DescriptionDirection.ARRIVE, targetTags);
        }

        @Nullable
        private HashMap<String, String> getNewDirectionTags(NodeWrapper prevNode, NodeWrapper currentNode, NodeWrapper nextNode, WayWrapper currentWay) {
            HashMap<String, String> returnAble = new HashMap<>();

            if(currentWay.isRoundabout()) {
                roundaboutExitNode = currentNode;
                addDirectionTagsByDescriptionDirection(DescriptionDirection.ROUNDABOUT_EXIT, returnAble);
            } else if(roundaboutExitNode != null && !currentWay.isRoundabout()) {
                double azimuthFromRoundaboutEntranceToExit = getAzimuthFromPrevWayToCurrentWay(prevNode, currentNode, roundaboutExitNode);
                int directionToRoundaboutExitWithClock = 0;

                if(azimuthFromRoundaboutEntranceToExit != 0 && azimuthFromRoundaboutEntranceToExit != 360)
                    directionToRoundaboutExitWithClock = (int)(azimuthFromRoundaboutEntranceToExit / 30);

                if(directionToRoundaboutExitWithClock == 0)
                    directionToRoundaboutExitWithClock = 12;

                addDirectionTags(directionToRoundaboutExitWithClock + "시 방향 회전교차로 출구", DescriptionDirection.ROUNDABOUT.ordinal(), returnAble);
                roundaboutExitNode = null;
            } else {
                double azimuthFromPrevWayToCurrentWay = getAzimuthFromPrevWayToCurrentWay(prevNode, currentNode, nextNode);
                DescriptionDirection descriptionDirection = DescriptionDirection.getByStandardAzimuth(azimuthFromPrevWayToCurrentWay);

                if(descriptionDirection == null) {
                    getLogger().error("descriptionDirection found NULL, route failed");
                    return null;
                }
                addDirectionTagsByDescriptionDirection(descriptionDirection, returnAble);
            }

            addTagOfDirectionTo(nextNode, returnAble);
            return returnAble;
        }

        private void addTagOfDirectionTo(NodeWrapper nextNode, Map<String, String> targetDirectionTags) {
            NodeWeight nextNodeWeight = nextNode.getAttachedNodeWeight();
            if(nextNodeWeight == null) return;

            WayWrapper nextNodeFromWay = nextNodeWeight.getFromWay();
            if(nextNodeFromWay == null) return;

            String destinationInNextNodeFromWayTag = nextNodeFromWay.getWayTags().get("destination");
            if(destinationInNextNodeFromWayTag != null) {
                addDirectionToTag(getAppropriateDestinationPrintableFromOsmTag(destinationInNextNodeFromWayTag), targetDirectionTags);
                return;
            }

            String nextNodeFromWayName = nextNodeFromWay.getWayName();
            if(nextNodeFromWayName == null)
                return;
            addDirectionToTag(nextNodeFromWayName, targetDirectionTags);
        }

        private double getAzimuthFromPrevWayToCurrentWay(NodeWrapper prevNode, NodeWrapper currentNode, NodeWrapper nextNode) {
            double azimuthFromPrevNodeToCurrentNode = coordinateTool.getAzimuth(prevNode.generateNewCoordinate(), currentNode.generateNewCoordinate());
            double azimuthFromCurrentNodeToNextNode = coordinateTool.getAzimuth(currentNode.generateNewCoordinate(), nextNode.generateNewCoordinate());

            return coordinateTool.convertToStandardAzimuth(azimuthFromCurrentNodeToNextNode - azimuthFromPrevNodeToCurrentNode);
        }

        private void addDirectionTagsByDescriptionDirection(DescriptionDirection descriptionDirection, Map<String, String> targetDirectionTags) {
            addDirectionTags(descriptionDirection.printable, descriptionDirection.ordinal(), targetDirectionTags);
        }

        private void addDirectionTags(String direction, int directionCode, Map<String, String> targetDirectionTags) {
            targetDirectionTags.put("direction", direction);
            targetDirectionTags.put("directionCode", String.valueOf(directionCode));
        }

        private void addDirectionToTag(String directionTo, Map<String, String> targetDirectionTags) {
            targetDirectionTags.put("directionTo", directionTo);
        }

        private String getAppropriateDestinationPrintableFromOsmTag(@NonNull String destinationFromOsmTag) {
            StringBuilder returnable = new StringBuilder();

            for(int i=0; i<destinationFromOsmTag.length(); i++) {
                if(destinationFromOsmTag.charAt(i) == ';') {
                    returnable.append(',');
                    continue;
                }
                returnable.append(destinationFromOsmTag.charAt(i));
            }
            return returnable.toString();
        }
    }

    @NonNull
    private HashMap<String, String> getNewWayTags(WayWrapper targetWay) {
        HashMap<String, String> returnAble = new HashMap<>();
        returnAble.put("type", targetWay.getHighway());

        String wayName = targetWay.getWayName();
        if (wayName != null)
            returnAble.put("roadName", wayName);
        return returnAble;
    }


    @RequiredArgsConstructor
    private static enum DescriptionDirection {
        STRAIGHT_FORWARD("계속 직진", 0, 0),
        U_TURN("U턴", 180, 180),
        BEAR_LEFT("왼쪽 방향 직진", 360 -30, 360),
        BEAR_RIGHT("오른쪽 방향 직진", 0, 30),
        LEFT("좌회전", 360 -110, 360 -30),
        RIGHT("우회전", 30, 110),
        SHARP_LEFT("큰 좌회전", 360 -180, 360 -110),
        SHARP_RIGHT("큰 우회전", 110, 180),
        ARRIVE("목적지 도착", 0 ,0),
        ROUNDABOUT("회전 교차로", 0, 0),
        ROUNDABOUT_EXIT("회전 교차로 출구", 0, 0);

        private final String printable;
        private final double minAzimuth;
        private final double maxAzimuth;

        @Nullable
        private static DescriptionDirection getByStandardAzimuth(double azimuth) {
            DescriptionDirection[] values = DescriptionDirection.values();
            DescriptionDirection valueIt = null;

            for(int i=0; i<values.length; i++) {
                valueIt = values[i];
                if(!(valueIt.minAzimuth <= azimuth) || !(azimuth <= valueIt.maxAzimuth))
                    continue;
                return valueIt;
            }
            return null;
        }
    }
}
