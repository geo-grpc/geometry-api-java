package com.esri.core.geometry;

import org.proj4.*;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;

/**
 * Created by davidraleigh on 11/3/16.
 */
public class TestProjection extends TestCase {

    static {
        System.loadLibrary("proj");
    }
    private static final double PRECESISSION = 1e-3;

//    private final List<Projection> inversableProjections = new ArrayList<Projection>();

    @Before
    public void setUp() {
//        for (final String projectionName : ProjectionFactory.getOrderedProjectionNames()) {
//            final Projection projection = ProjectionFactory.getNamedProjection(projectionName);
//            if (!projection.hasInverse()) {
//                System.out.println("INFO: Ignore not inverable projection: " + projection);
//                continue;
//            }
//            inversableProjections.add(projection);
//        }
//        System.out.println();
//        System.out.flush();
    }

    @Test
    public void testProjectionAndInverseAreTheSame() throws Exception {
//        for (final Projection projection : inversableProjections) {
//            try {
//                final java.awt.geom.Point2D.Double sourceLL = new java.awt.geom.Point2D.Double(0, 1);
//                final java.awt.geom.Point2D.Double projectedXY = projection.project(sourceLL.getX(), sourceLL.getY(), new Point2D.Double());
//                final java.awt.geom.Point2D.Double sourceLL2 = projection.projectInverse(projectedXY.getX(), projectedXY.getX(), new java.awt.geom.Point2D.Double());
//
//
//                System.out.println("INFO: Test inverable projection: " + projection);
//                checkAndPrintErrorForProjection(projection, sourceLL.getX(), sourceLL2.getX());
//                checkAndPrintErrorForProjection(projection, sourceLL.getY(), sourceLL2.getY());
//            } catch (final Exception e) {
//                System.err.println("ERROR: Exception for  " + projection);
//                e.printStackTrace(System.err);
//            }
//        }
    }

    @Test
    public void testProj4() throws Exception {
        PJ sourcePJ = new PJ("+init=epsg:32632");                   // (x,y) axis order
        PJ targetPJ = new PJ("+proj=latlong +datum=WGS84");         // (λ,φ) axis order
        PJ sourcePJ_utm = new PJ( "+proj=utm +zone=32 +datum=WGS84 +units=m +no_defs ");
        PJ targetPJ_4326 = new PJ("+init=epsg:4326");

        double[] coordinates_32632 = {
                500000,       0,   // First coordinate
                400000,  100000,   // Second coordinate
                600000, -100000    // Third coordinate
        };

        double[] coordinates_WGS84 = Arrays.copyOf(coordinates_32632,coordinates_32632.length);
        double[] coordinates_4326 = Arrays.copyOf(coordinates_32632,coordinates_32632.length);

        sourcePJ.transform(targetPJ, 2, coordinates_WGS84, 0, 3);
        sourcePJ.transform(targetPJ_4326, 2, coordinates_4326, 0, 3);

        for (int i = 0; i < coordinates_WGS84.length; i++) {
            if (i == 1)
                continue;

            assertTrue((Math.abs(coordinates_WGS84[i] - coordinates_32632[i])) > 1);
            assertTrue((Math.abs(coordinates_4326[i] - coordinates_32632[i])) > 1);
            assertEquals(coordinates_4326[i], coordinates_WGS84[i]);
        }

//        targetPJ.transform(sourcePJ, 2, coordinates_32632, 0, 3);
//
//        for (int i = 0; i < coordinates_WGS84.length; i++) {
//            assertEquals(coordinates_WGS84[i], coordinates_32632[i], .001);
//        }
    }

    @Test
    public void testProj4Strings() {
        SpatialReference spatialReference = SpatialReference.create(4326);
        assertEquals("+init=epsg:4326", spatialReference.getProj4());

        spatialReference = SpatialReference.create(32632);
        assertEquals("+init=epsg:32632", spatialReference.getProj4());
    }

    @Test
    public void testProjectionCursor_1() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(32632), SpatialReference.create(4326));
        Point point = new Point(500000,       0);
        Point pointOut = (Point)OperatorProject.local().execute(point, projectionTransformation, null);
        assertNotNull(pointOut);
        assertTrue(Math.abs(point.getX() - pointOut.getY()) > 1);
        projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.create(32632));
        Point originalPoint = (Point)OperatorProject.local().execute(pointOut, projectionTransformation, null);
        assertEquals(originalPoint.getX(), point.getX());
        assertEquals(originalPoint.getY(), point.getY());
    }

    @Test
    public void testProjectMultiPoint() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(32632), SpatialReference.create(4326));
        MultiPoint multiPoint = new MultiPoint();
        multiPoint.add( 500000,       0);
        multiPoint.add(400000,  100000);
        multiPoint.add(600000, -100000);
        MultiPoint multiPointOut = (MultiPoint)OperatorProject.local().execute(multiPoint, projectionTransformation, null);
        assertNotNull(multiPointOut);
        assertFalse(multiPointOut.equals(multiPoint));
        assertEquals(multiPoint.getPointCount(), multiPointOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.create(32632));
        MultiPoint originalMultiPoint = (MultiPoint)OperatorProject.local().execute(multiPointOut, projectionTransformation, null);

        for (int i = 0; i < multiPoint.getPointCount(); i++) {
            assertEquals(multiPoint.getPoint(i).getX(), originalMultiPoint.getPoint(i).getX(), 1e-10);
            assertEquals(multiPoint.getPoint(i).getY(), originalMultiPoint.getPoint(i).getY(), 1e-10);
        }
    }

    @Test
    public void testProjectPolyline() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(32632), SpatialReference.create(4326));
        Polyline polyline = new Polyline();
        polyline.startPath( 500000,       0);
        polyline.lineTo(400000,  100000);
        polyline.lineTo(600000, -100000);
        Polyline polylineOut = (Polyline)OperatorProject.local().execute(polyline, projectionTransformation, null);
        assertNotNull(polylineOut);
        assertFalse(polylineOut.equals(polyline));
        assertEquals(polyline.getPointCount(), polylineOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.create(32632));
        Polyline originalPolyline = (Polyline)OperatorProject.local().execute(polylineOut, projectionTransformation, null);

        for (int i = 0; i < polyline.getPointCount(); i++) {
            assertEquals(polyline.getPoint(i).getX(), originalPolyline.getPoint(i).getX(), 1e-10);
            assertEquals(polyline.getPoint(i).getY(), originalPolyline.getPoint(i).getY(), 1e-10);
        }
    }

    @Test
    public void testProjectPolygon() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(32632), SpatialReference.create(4326));
        Polygon polygon = new Polygon();
        polygon.startPath( 500000,       0);
        polygon.lineTo(400000,  100000);
        polygon.lineTo(600000, -100000);
        polygon.closeAllPaths();
        Polygon polygonOut = (Polygon)OperatorProject.local().execute(polygon, projectionTransformation, null);
        assertNotNull(polygonOut);
        assertFalse(polygonOut.equals(polygon));
        assertEquals(polygon.getPointCount(), polygonOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.create(32632));
        Polygon originalPolygon = (Polygon)OperatorProject.local().execute(polygonOut, projectionTransformation, null);

        for (int i = 0; i < polygon.getPointCount(); i++) {
            assertEquals(polygon.getPoint(i).getX(), originalPolygon.getPoint(i).getX(), 1e-10);
            assertEquals(polygon.getPoint(i).getY(), originalPolygon.getPoint(i).getY(), 1e-10);
        }
    }


//    private void checkAndPrintErrorForProjection(final Projection projection, final double expected, final double actual) {
//        if (Math.abs(expected - actual) > PRECESISSION) {
//            System.err.println("ERROR: Expected " + expected + ", but was " + actual + " for " + projection);
//        }
//    }
}