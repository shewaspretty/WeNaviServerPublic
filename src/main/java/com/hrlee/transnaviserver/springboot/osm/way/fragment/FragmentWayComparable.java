package com.hrlee.transnaviserver.springboot.osm.way.fragment;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public final class FragmentWayComparable {

    @NonNull
    private final Coordinate[] points = new Coordinate[2];
    private int nearestCoordinatePtrToTarget = -1;

    @NonNull
    private final Coordinate targetCoordinateComparable;
    @Getter
    private double distanceComparableMeter;

    @Getter
    private final NodeWrapper[] originNodes = new NodeWrapper[2];

    @NonNull
    private final CoordinateTool coordinateTool;

    @Getter
    @Setter
    private boolean isCutInHalfDisabled = false;

    public FragmentWayComparable(@NonNull NodeWrapper node1, @NonNull NodeWrapper node2, @NonNull Coordinate targetCoordinateComparable, @NonNull CoordinateTool coordinateTool) {
        originNodes[0] = node1;
        originNodes[1] = node2;
        points[0] = node1.generateNewCoordinate();
        points[1] = node2.generateNewCoordinate();
        this.targetCoordinateComparable = targetCoordinateComparable;
        this.coordinateTool = coordinateTool;

        setNearestCoordinateByDistanceToTarget();
    }

    public boolean cutInHalfByDistanceFromTarget() {
        double originDistanceBetweenPoints = coordinateTool.getDistanceMeter(points[0], points[1]);
        if(originDistanceBetweenPoints <= 1) {
            isCutInHalfDisabled = true;
            return false;
        }

        int furtherCoordinatePtrToTarget = 0;
        if(nearestCoordinatePtrToTarget < 1)
            furtherCoordinatePtrToTarget = 1;

        Coordinate coordinateBeforeMove = points[furtherCoordinatePtrToTarget].duplicate();
        double distanceComparableBeforeMove = distanceComparableMeter;

        coordinateTool.moveCoordinate(points[furtherCoordinatePtrToTarget],
                coordinateTool.getAzimuth(points[furtherCoordinatePtrToTarget], points[nearestCoordinatePtrToTarget]),
                originDistanceBetweenPoints /2);
        setNearestCoordinateByDistanceToTarget();

        if(!(distanceComparableBeforeMove < distanceComparableMeter))
            return true;

        points[furtherCoordinatePtrToTarget] = coordinateBeforeMove;
        distanceComparableMeter = distanceComparableBeforeMove;
        isCutInHalfDisabled = true;
        return false;
    }

    public boolean equals(Coordinate point1, Coordinate point2) {
        return (this.points[0].isSameLocation(point1) || this.points[0].isSameLocation(point2))
                && (this.points[1].isSameLocation(point1) || this.points[1].isSameLocation(point2));
    }

    public Coordinate getNearestCoordinateToTarget() { return points[nearestCoordinatePtrToTarget]; }

    private void setNearestCoordinateByDistanceToTarget() {
        double distancePoint1 = coordinateTool.getDistanceMeter(points[0], targetCoordinateComparable);
        double distancePoint2 = coordinateTool.getDistanceMeter(points[1], targetCoordinateComparable);

        if(distancePoint2 < distancePoint1) {
            nearestCoordinatePtrToTarget = 1;
            distanceComparableMeter = distancePoint2;
            return;
        }
        nearestCoordinatePtrToTarget = 0;
        distanceComparableMeter = distancePoint1;
    }
}
