package com.hrlee.transnaviserver.springboot.service.route.factory.deprecated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.dto.route.RouteNode;
import com.hrlee.transnaviserver.springboot.dto.route.Route;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.NodeWeight;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@RequiredArgsConstructor
public final class RouteSerializerDeprecated implements LoggAble {

    private final List<NodeWrapper> nodes;

    public static class CurrentPointHolder {
        @NonNull
        private final List<NodeWrapper> nodes;

        private final CurrentWayWrapper currentWay = new CurrentWayWrapper();
        private final NextInterSectionWrapper nextInterSection = new NextInterSectionWrapper();
        @Getter
        private int currentNodeIndex;

        public CurrentPointHolder(List<NodeWrapper> nodes) {
            this.nodes = nodes;
            currentNodeIndex = nodes.size();
        }

        private boolean movePointNext() {
            if(!hasNextNode(currentNodeIndex))
                return false;
            currentNodeIndex = getNextNodeIndex(currentNodeIndex);
            return currentWay.update() && nextInterSection.update();
        }

        @SuppressWarnings("all")
        private boolean hasNextNode(int targetIndex) { return existsAt(getNextNodeIndex(targetIndex)); }
        private int getNextNodeIndex(int targetIndex) { return targetIndex -1;}
        private int getPrevNodeIndex(int targetIndex) { return targetIndex +1;}

        private boolean existsAt(int targetIndex) { return targetIndex >= 0 && targetIndex < nodes.size(); }
        private boolean isAheadOfCurrentPoint(int targetIndex) {  return targetIndex < currentNodeIndex; }

        private class CurrentWayWrapper {
            private WayWrapper way = null;
            private boolean isChanged = false;

            private boolean update() {
                if(isChanged)
                    isChanged = false;

                if(way == null)
                    return setWay(getWayBetweenNodes(currentNodeIndex, getNextNodeIndex(currentNodeIndex)));

                if(!nextInterSection.isSet())
                    return false;
                if(nextInterSection.isAheadOfCurrentNode())
                    return true;

                return setWay(nextInterSection.nextInterSectionWay);
            }

            private boolean setWay(@Nullable WayWrapper updatable) {
                if(updatable == null)
                    return false;
                way = updatable;
                return (isChanged = true);
            }
        }

        private class NextInterSectionWrapper {
            private int index = -1;
            private WayWrapper nextInterSectionWay = null;

            private boolean isSet() { return !(index < 0); }
            private boolean isAheadOfCurrentNode() { return isAheadOfCurrentPoint(index) || isLastNode();}
            private boolean update() {
                if(isSet() && isAheadOfCurrentNode())
                    return true;

                int nextInterSectionPtr = currentNodeIndex;
                WayWrapper ptrWayIt = null;

                while(hasNextNode(nextInterSectionPtr)) {
                    nextInterSectionPtr = getNextNodeIndex(nextInterSectionPtr);
                    ptrWayIt = getWayBetweenNodes(nextInterSectionPtr, getNextNodeIndex(nextInterSectionPtr));

                    if(ptrWayIt == null) {
                        if(hasNextNode(nextInterSectionPtr))
                            return false;
                        if((ptrWayIt = getWayBetweenNodes(nextInterSectionPtr, getPrevNodeIndex(nextInterSectionPtr))) == null) // necessary
                            return false;
                    }

                    if(ptrWayIt.getId() == currentWay.way.getId())
                        continue;

                    String currentWayName = currentWay.way.getWayName();
                    String ptrWayItName = ptrWayIt.getWayName();
                    if(currentWayName != null && ptrWayItName != null)
                        if(currentWayName.equals(ptrWayItName))
                            continue;

                    index = nextInterSectionPtr;
                    nextInterSectionWay = ptrWayIt;
                    return true;
                }

                index = nextInterSectionPtr;
                return true;
            }
            @Nullable
            private NodeWrapper get() {
                if(!isSet())
                    return null;
                return nodes.get(index);
            }
            @Nullable
            private NodeWrapper getNextNodeOfNextInterSection() {
                int nextNodeIndex = getNextNodeIndex(index);
                if(!existsAt(nextNodeIndex))
                    return null;

                return nodes.get(nextNodeIndex);
            }
            private boolean isLastNode() { return isSet() && !hasNextNode(index); }
        }

        @Nullable
        private WayWrapper getWayBetweenNodes(int targetNodeIndex, int nextNodeIndex) {
            if(!existsAt(targetNodeIndex) || !existsAt(nextNodeIndex))
                return null;

            List<WayWrapper> targetNodeAttachedWays = null;// = nodes.get(targetNodeIndex).();
            if(targetNodeAttachedWays.isEmpty())
                return null;

            if(targetNodeAttachedWays.size() == 1)
                return targetNodeAttachedWays.get(0);

            List<WayWrapper> nextNodeAttachedWays = null;// nodes.get(nextNodeIndex).getAllAttachedWays();
            if(nextNodeAttachedWays.isEmpty())
                return null;

            for(int i=0; i<targetNodeAttachedWays.size(); i++)
                for(int j=0; j<nextNodeAttachedWays.size(); j++) {
                    if(targetNodeAttachedWays.get(i).getId() != nextNodeAttachedWays.get(j).getId())
                        continue;
                    return targetNodeAttachedWays.get(i);
                }

            return null;
        }

        public NodeWrapper getCurrentNode() { return nodes.get(currentNodeIndex); }
        public boolean isLast() { return !hasNextNode(currentNodeIndex); }
        public WayWrapper getCurrentWay() { return currentWay.way; }

        @Nullable
        public NodeWrapper getNextInterSection() { return nextInterSection.get(); }
        @Nullable
        public NodeWrapper getNextNodeOfNextInterSection() { return nextInterSection.getNextNodeOfNextInterSection(); }
        public boolean isNextInterSectionLastNode() { return nextInterSection.isLastNode(); }

        public boolean isWayChanged() { return currentWay.isChanged; }
    }

    @Nullable
    public String serialize() {
        List<RouteNode> returnAbleRouteNodes = new ArrayList<>();
        CurrentPointHolder currentPointHolder = new CurrentPointHolder(nodes);
        TagsFactory tagsFactory = new TagsFactory(nodes, CoordinateTool.getInstance());

        NodeWrapper currentNode = null;
        NodeWeight currentNodeWeight = null;

        while(currentPointHolder.movePointNext()) {
            currentNode = currentPointHolder.getCurrentNode();
            currentNodeWeight = currentPointHolder.getCurrentNode().getAttachedNodeWeight();

            if(currentNodeWeight == null) {
                getLogger().error("attached node weight null " + currentPointHolder.getCurrentNode());
                return null;
            }

            ///returnAbleRouteNodes.add(
              //      new RouteNode(currentNode, currentNodeWeight.getEstimatedTimeArrivalMs(),
             //               tagsFactory.generate(currentPointHolder)));
        }

        //try {
            //return new ObjectMapper().writeValueAsString(
                    //new Route(returnAbleRouteNodes, returnAbleRouteNodes.get(returnAbleRouteNodes.size() -1).getEtaMs()));
        //} catch (JsonProcessingException e) {
           // getLogger().error(e.getMessage());
       // }
        return null;
    }
}
