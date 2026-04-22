package com.hrlee.transnaviserver.springboot.service.route.factory;

import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.service.route.dijkstra.NodeWeight;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Use only once by instantiated
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteTotalDistanceFactory {

    private double totalDistance = 0.0d;

    public void record(NodeWrapper currentNode) {
        NodeWeight nodeWeight = currentNode.getAttachedNodeWeight();
        if(nodeWeight == null) return;

        double distanceToFromNode = nodeWeight.getDistanceToFromNodeMeter();
        if(distanceToFromNode < 0)
            return;
        totalDistance += nodeWeight.getDistanceToFromNodeMeter();
    }

    public String getTotalDistancePrintable() {
        if(totalDistance < 1000)
            return (int)totalDistance + "m";

        double totalDistance = this.totalDistance;
        totalDistance /= 1000;

        BigDecimal bigDecimal = new BigDecimal(Double.toString(totalDistance)).setScale(0, RoundingMode.HALF_UP);
        return bigDecimal.toString() + "km";
    }
}
