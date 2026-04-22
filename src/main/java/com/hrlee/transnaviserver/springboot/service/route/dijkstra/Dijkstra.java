package com.hrlee.transnaviserver.springboot.service.route.dijkstra;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import com.hrlee.transnaviserver.springboot.service.route.handler.WayChangedDetectable;
import com.hrlee.transnaviserver.springboot.service.route.param.type.AbstractRouteType;
import com.hrlee.transnaviserver.springboot.service.route.param.type.WalkingRouteType;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * to be used once by instantiated.
 */
@AllArgsConstructor
public final class Dijkstra implements LoggAble, WayChangedDetectable {

    private final PriorityQueue nodeWeights = new PriorityQueue();

    @NonNull
    private final NodeWrapper startNode;
    @NonNull
    private final NodeWrapper endNode;
    private final long departureTimeMs;

    @SuppressWarnings("FieldCanBeLocal")
    private final double walkingSpeedKph = 5.2;
    private final AbstractRouteType routeType;

    private final AdditionalEtaFactory additionalEtaFactory = new AdditionalEtaFactory();


    @Nullable
    public NodeWrapper process() {
        CoordinateTool coordinateTool = CoordinateTool.getInstance();
        NodeWrapper currentNode = startNode;
        NodeWeight.generateAndAttachToNode(currentNode, departureTimeMs); // NECESSARY, creating for currentNode

        return dijkstra(currentNode, coordinateTool);
    }

    @Nullable
    private NodeWrapper dijkstra(NodeWrapper currentNode, CoordinateTool coordinateTool) {
        NodeWeight currentNodeWeight = null;

        while(currentNode.getId() != endNode.getId()) {
            currentNode.setVisited(true);

            if((currentNodeWeight = currentNode.getAttachedNodeWeight()) == null) {
                getLogger().error(currentNode.getId() + " attachedNodeWeight Null");
                return null;
            }

            List<NodeWrapper.ReachableNode> reachableNodes = currentNode.getReachableNodes();
            NodeWrapper.ReachableNode reachableNodesIt = null;
            NodeWrapper reachableNodesItNodeWrapper = null;

            for(int i=0; i<reachableNodes.size(); i++) {
                reachableNodesIt = reachableNodes.get(i);
                reachableNodesItNodeWrapper = reachableNodesIt.getNode();

                if(reachableNodesItNodeWrapper.isVisited())
                    continue;

                if(!routeType.isWayTagConditionPassed(reachableNodesIt.getConnectedWay().getWayTags()))
                    continue;

                double distanceMeter = coordinateTool.getDistanceMeter(currentNode.generateNewCoordinate(), reachableNodesItNodeWrapper.generateNewCoordinate());
                long estimatedArrivalTimeToReachableNodesIt =
                        getEstimatedTimeArrivalToNodeMs(reachableNodesIt, currentNodeWeight, distanceMeter);
                long additionalEtaForWeight = additionalEtaFactory.getAdditionalEtaForWeightMs(reachableNodesIt, currentNodeWeight, reachableNodes.size());

                NodeWeight reachableNodesItWeight = reachableNodesItNodeWrapper.getAttachedNodeWeight();
                if(reachableNodesItWeight == null) {
                    nodeWeights.insert(NodeWeight.generateAndAttachToNode(reachableNodesIt, estimatedArrivalTimeToReachableNodesIt, additionalEtaForWeight, distanceMeter));
                    continue;
                }

                if(!(reachableNodesItWeight.getEtaMsForWeight() > estimatedArrivalTimeToReachableNodesIt + additionalEtaForWeight))
                    continue;

                reachableNodesItWeight.update(reachableNodesIt, estimatedArrivalTimeToReachableNodesIt, additionalEtaForWeight, distanceMeter);
                nodeWeights.onValuePriorityUpdated(reachableNodesItWeight);
            }

            NodeWeight nextVisitNodeWeight = nodeWeights.pop();
            if(nextVisitNodeWeight == null)
                return null;
            currentNode = nextVisitNodeWeight.getAttachedNode();
        }
        return currentNode;
    }

    @Deprecated(forRemoval = true)
    private List<NodeWrapper> backTrackAndSerialize(NodeWrapper currentNode) {
        List<NodeWrapper> returnAble = new ArrayList<>();
        while(true) {
            returnAble.add(currentNode);
            NodeWeight attachedNodeWeight = currentNode.getAttachedNodeWeight();
            if(attachedNodeWeight == null) {
                getLogger().error("get attached nodeWeight null " + currentNode.getId());
                return null;
            }
            NodeWrapper fromNode = attachedNodeWeight.getFromNode();
            if(fromNode == null)
                break;
            currentNode = fromNode;
        }
        return returnAble;
    }

    private long getEstimatedTimeArrivalToNodeMs(@NonNull NodeWrapper.ReachableNode currentReachableNode, @NonNull NodeWeight currentNodeWeight, double distanceMeter) {
        long startTimeMs = currentNodeWeight.getEstimatedTimeArrivalMs();
        double speedKph = 0.0;

        if(routeType instanceof WalkingRouteType)
            speedKph = walkingSpeedKph;
        else {
            String maxSpeedOsmTag = currentReachableNode.getConnectedWay().getWayTags().get("maxspeed");
            if(maxSpeedOsmTag != null)
                speedKph = Integer.parseInt(maxSpeedOsmTag);
            else {
                switch (currentReachableNode.getConnectedWay().getHighway()) {
                    case "motorway":
                        speedKph = 100.0;
                        break;
                    case "trunk":
                        speedKph = 70.0;
                        break;
                    case "primary":
                        speedKph = 60.0;
                        break;
                    case "tertiary":
                        speedKph = 40.0;
                        break;
                    case "secondary":
                        speedKph = 30.0;
                        break;
                    case "residential":
                        speedKph = 20.0;
                        break;
                    case "service":
                        speedKph = 10.0;
                        break;
                    default:
                        speedKph = 7.0;
                }
            }
        }
        return (long) (startTimeMs + (distanceMeter / (speedKph * 1000 / 60 / 60 / 1000)));
    }

    private class AdditionalEtaFactory {
        private long getAdditionalEtaForWeightMs(NodeWrapper.ReachableNode currentReachableNode, NodeWeight currentNodeWeight, int sizeReachableNodesForCurrentNode) {
            return getAdditionalEtaByTurn(currentReachableNode, currentNodeWeight) + getAdditionalEtaByIntersectionMs(sizeReachableNodesForCurrentNode);
        }

        private long getAdditionalEtaByTurn(NodeWrapper.ReachableNode currentReachableNode, NodeWeight currentNodeWeight) {
            WayWrapper fromNodeFromWay = currentNodeWeight.getFromWay();
            if(fromNodeFromWay == null)
                return 0;

            if(!isWayChanged(fromNodeFromWay, currentReachableNode.getConnectedWay()))
                return 0;
            return routeType.getAdditionalEtaMsByTurn();
        }

        private long getAdditionalEtaByIntersectionMs(int sizeReachableNodesForCurrentNode) {
            if(sizeReachableNodesForCurrentNode <= 1)
                return 0;
            return 80 * 1000;
        }
    }

}
