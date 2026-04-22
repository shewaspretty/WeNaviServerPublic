package com.hrlee.transnaviserver.springboot.osm.coordinate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.locationtech.jts.geom.Envelope;

import java.awt.geom.Point2D;

@AllArgsConstructor
@Getter
@Setter
public class Coordinate {

    private double latitude;
    private double longitude;

    public double getY() { return latitude; }
    public double getX() { return longitude; }

    public void set(Point2D point) { this.latitude = point.getY(); this.longitude = point.getX(); }

    public Coordinate(@NonNull Point2D coordinate) { latitude = coordinate.getY(); longitude = coordinate.getX(); }

    public Coordinate duplicate() { return new Coordinate(latitude, longitude); }

    public boolean isSameLocation(Coordinate target) { return target.latitude == latitude && target.longitude == longitude; }

    public static Coordinate getMinFromBound(Envelope envelope) { return new Coordinate(envelope.getMinY(), envelope.getMinX()); }
    public static Coordinate getMaxFromBound(Envelope envelope) { return new Coordinate(envelope.getMaxY(), envelope.getMaxX()); }
}
