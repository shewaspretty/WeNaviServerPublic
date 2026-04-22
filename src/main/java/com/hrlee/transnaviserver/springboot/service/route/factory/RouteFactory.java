package com.hrlee.transnaviserver.springboot.service.route.factory;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.dto.route.Route;
import com.hrlee.transnaviserver.springboot.dto.route.RouteNode;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.NodeWeight;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public final class RouteFactory implements LoggAble {

    @Nullable
    public Route backTrackAndGetExposableRoute(@NonNull NodeWrapper destinationReached) {
        List<RouteNode> routeNodes = new ArrayList<>();
        TagFactory tagFactory = new TagFactory();

        NodeWeight destinationNodeWeight = destinationReached.getAttachedNodeWeight();
        if(destinationNodeWeight == null) {
            getLogger().error("destination nodeWeight NULL, route factory stopped");
            return null;
        }

        RouteSummaryFactory routeSummaryFactory = new RouteSummaryFactory(destinationNodeWeight.getDepth());
        RouteTotalDistanceFactory routeTotalDistanceFactory = new RouteTotalDistanceFactory();

        NodeWrapper currentNode = destinationReached;
        NodeWeight currentNodeWeight = null;
        HashMap<String, String> tagsAppendable = null;

        while(currentNode != null) {
            currentNodeWeight = currentNode.getAttachedNodeWeight();
            if(currentNodeWeight == null) {
                getLogger().error("current nodeWeight NULL, route factory failed");
                return null;
            }

            tagsAppendable = tagFactory.getTags(currentNode);
            if(tagsAppendable == null)
                return null;

            routeSummaryFactory.record(currentNode);
            routeTotalDistanceFactory.record(currentNode);

            routeNodes.add(new RouteNode(currentNode, currentNodeWeight.getEstimatedTimeArrivalMs(), tagsAppendable));
            currentNode = currentNodeWeight.getFromNode();
        }

        return new Route(routeNodes, routeNodes.get(0).getEtaMs(), routeSummaryFactory.getRouteSummary(), routeTotalDistanceFactory.getTotalDistancePrintable());
    }
}
