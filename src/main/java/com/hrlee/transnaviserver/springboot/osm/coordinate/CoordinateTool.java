package com.hrlee.transnaviserver.springboot.osm.coordinate;
import jakarta.annotation.Nullable;
import lombok.*;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Envelope;

import java.awt.geom.Point2D;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoordinateTool {

    @Nullable
    private static CoordinateTool instance = null;

    @NonNull
    public static synchronized CoordinateTool getInstance() {
        if(instance != null)
            return instance;

        instance = new CoordinateTool();
        return instance;
    }

    @RequiredArgsConstructor
    @Getter
    public enum Direction {
        WEST(-90),
        SOUTH(-180),
        EAST(90),
        NORTH(0);

        private final double azimuth;
    }

    public void moveCoordinate(@NonNull Coordinate coordinate, double azimuth, double distanceMeter) {
        GeodeticCalculator geodeticCalculator = createNewGeodeticCalculator();

        geodeticCalculator.setStartingGeographicPoint(coordinate.getLongitude(), coordinate.getLatitude());
        geodeticCalculator.setDirection(azimuth, distanceMeter);

        Point2D destination = geodeticCalculator.getDestinationGeographicPoint();
        coordinate.set(destination);
    }

    public void moveCoordinate(@NonNull Coordinate coordinate, @NonNull Direction direction, double distanceMeter) {
        moveCoordinate(coordinate, direction.azimuth, distanceMeter);
    }

    public ReferencedEnvelope getBoundingBox(@NonNull Coordinate point1, @NonNull Coordinate point2) {
        return new ReferencedEnvelope(point1.getX(), point2.getX(), point1.getY(), point2.getY(), DefaultGeographicCRS.WGS84);

        /*GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

        org.locationtech.jts.geom.Coordinate[] jtsCoordinates = new org.locationtech.jts.geom.Coordinate[coordinates.length];
        for(int i=0; i<coordinates.length; i++)
            jtsCoordinates[i] = coordinates[i].getJtsCoordinate();

        Envelope returnAble = geometryFactory.createMultiPointFromCoords(jtsCoordinates).getEnvelopeInternal();
        return returnAble;*/
    }

    public double getAzimuth(Coordinate target, Coordinate destination) {
        GeodeticCalculator geodeticCalculator = createNewGeodeticCalculator();
        setPoints(geodeticCalculator, target, destination);
        return geodeticCalculator.getAzimuth();
    }

    public double getDistanceMeter(Coordinate center, Coordinate target) {
        GeodeticCalculator geodeticCalculator = createNewGeodeticCalculator();
        return getDistanceMeter(center, target, geodeticCalculator);
    }

    public double getDistanceMeter(Coordinate center, Coordinate target, GeodeticCalculator geodeticCalculator) {
        setPoints(geodeticCalculator, center, target);
        return geodeticCalculator.getOrthodromicDistance();
    }

    private void setPoints(GeodeticCalculator geodeticCalculator, Coordinate target, Coordinate destination) {
        geodeticCalculator.setStartingGeographicPoint(target.getLongitude(), target.getLatitude());
        geodeticCalculator.setDestinationGeographicPoint(destination.getLongitude(), destination.getLatitude());
    }

    public GeodeticCalculator createNewGeodeticCalculator() { return new GeodeticCalculator(DefaultGeographicCRS.WGS84); }

    public double convertToStandardAzimuth(double azimuth) {
        if(!(azimuth < 0))
            return azimuth;
        return azimuth + 360;
    }
}
