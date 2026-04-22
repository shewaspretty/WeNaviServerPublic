package com.hrlee.transnaviserver.springboot.osm.node;

import com.hrlee.transnaviserver.springboot.osm.entity.NodeImpl;
import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.NodeWeight;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.*;

@RequiredArgsConstructor
public class NodeWrapper implements Node {

    @Getter
    private final long id;
    @Getter
    private final double latitude;
    @Getter
    private final double longitude;
    @Getter(value = AccessLevel.PROTECTED)
    private final ArrayList<WayWrapper> ways = new ArrayList<>();

    private Map<String, Double> comparableValues = null;
    @Nullable
    @Getter
    @Setter
    private NodeWeight attachedNodeWeight = null;
    @Getter
    @Setter
    private boolean isVisited = false;

    public NodeWrapper(NodeImpl rawNode) {
        this.id = rawNode.getId();
        this.latitude = rawNode.getLatitude();
        this.longitude = rawNode.getLongitude();
    }

    /**
     * IMPORTANT: CACHE-ING IS NOT ALLOWED
     * as it has to be separated with this object,
     * and also treated as a TEMPORARY OBJECT which is used once or more as it's needed and to be DROPPED after use.
     * data(lat,lon) from the coordinate object that is generated from here MUST NOT INFLUENCE to the data of THIS OBJECT
     */
    public Coordinate generateNewCoordinate() {
        return new Coordinate(latitude, longitude);
    }

    public List<NodeWrapper.ReachableNode> getReachableNodes() {
        ArrayList<NodeWrapper.ReachableNode> returnAble = new ArrayList<>();
        WayWrapper waysIt = null;

        for(int i=0; i<ways.size(); i++) {
            waysIt = ways.get(i);
            NodeWrapper[] reachableNodes = waysIt.getReachableNodes(this);
            if(reachableNodes == null)
                continue;

            NodeWrapper reachableNodesIt = null;
            for(int j=0; j<reachableNodes.length; j++) {
                reachableNodesIt = reachableNodes[j];
                if(reachableNodesIt == null)
                    continue;
                returnAble.add(new ReachableNode(reachableNodesIt, this, waysIt));
            }
        }
        return returnAble;
    }

    @Nullable
    public Double getComparableValue(String key) {
        if(comparableValues == null)
            return null;
        return comparableValues.get(key);
    }

    public void addComparableValue(String key, double value) {
        if(comparableValues == null)
            comparableValues = new HashMap<>();
        comparableValues.put(key, value);
    }

    public void attachWay(WayWrapper attachAble) {
       if(hasWay(attachAble))
           return;
       ways.add(attachAble);
    }

    public boolean hasWay(WayWrapper comparable) {
        for(int i=0; i<ways.size(); i++) {
            if(ways.get(i).getId() != comparable.getId())
                continue;
            return true;
        }
        return false;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ReachableNode {

        @NonNull
        private final NodeWrapper node;
        @NonNull
        private final NodeWrapper fromNode;
        @NonNull
        private final WayWrapper connectedWay;
    }
}
