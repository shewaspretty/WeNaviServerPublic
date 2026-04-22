package com.hrlee.transnaviserver.springboot.service.route.dijkstra;

import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NodeWeight implements PriorityQueueElement {

    private final NodeWrapper attachedNode;
    @Nullable
    private NodeWrapper fromNode;
    @Nullable
    private WayWrapper fromWay;

    private long estimatedTimeArrivalMs;
    private long etaMsForWeight;

    private int indexInPriorityQueue = -1;

    private int depth = 0;
    private double distanceToFromNodeMeter = -1.0;

    private NodeWeight(@NonNull NodeWrapper attachedNode, @Nullable NodeWrapper fromNode, @Nullable WayWrapper fromWay, long estimatedTimeArrivalMs, long additionalEtaForWeight,
                       double distanceToFromNodeMeter) {
        this.attachedNode = attachedNode;
        update(fromNode, fromWay, estimatedTimeArrivalMs, additionalEtaForWeight, distanceToFromNodeMeter);
        attachedNode.setAttachedNodeWeight(this);
    }

    public static NodeWeight generateAndAttachToNode(NodeWrapper.ReachableNode reachableNode, long estimatedTimeArrivalMs, long additionalEtaForWeight, double distanceToFromNode) {
        return new NodeWeight(reachableNode.getNode(), reachableNode.getFromNode(), reachableNode.getConnectedWay(), estimatedTimeArrivalMs, additionalEtaForWeight,
                distanceToFromNode);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static NodeWeight generateAndAttachToNode(@NonNull NodeWrapper attachedNode, long estimatedTimeArrivalMs) {
        return new NodeWeight(attachedNode, null, null, estimatedTimeArrivalMs, 0,-1);
    }

    public void update(@NonNull NodeWrapper.ReachableNode reachableNode, long estimatedTimeArrivalMs, long additionalEtaForWeight, double distanceToFromNodeMeter) {
        update(reachableNode.getFromNode(), reachableNode.getConnectedWay(), estimatedTimeArrivalMs, additionalEtaForWeight, distanceToFromNodeMeter);
    }

    private void update(@Nullable NodeWrapper fromNode, @Nullable WayWrapper fromWay, long estimatedTimeArrivalMs, long additionalEtaForWeight, double distanceToFromNodeMeter) {
        this.fromNode = fromNode;
        this.fromWay = fromWay;

        this.estimatedTimeArrivalMs = estimatedTimeArrivalMs;
        this.etaMsForWeight = estimatedTimeArrivalMs + additionalEtaForWeight;

        if(fromNode == null)
            return;

        NodeWeight fromNodeWeight = fromNode.getAttachedNodeWeight();
        if(fromNodeWeight == null)
            return; // TODO: ERROR?
        this.depth = fromNodeWeight.depth +1;

        if(distanceToFromNodeMeter < 0)
            return;
        this.distanceToFromNodeMeter = distanceToFromNodeMeter;
    }

    @Override
    public boolean setIndexInPriorityQueue(int index) {
        indexInPriorityQueue = index;
        return true;
    }
}
