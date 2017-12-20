package com.esri.core.geometry;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by davidraleigh on 5/10/17.
 */
public class TestRandomPoints extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testPointCreate() {
        OperatorRandomPoints operatorRandomPoints = OperatorRandomPoints.local();
        Polygon poly = new Polygon();
        poly.startPath(0, 0);
        poly.lineTo(0, 10);
        poly.lineTo(10, 10);
        poly.closePathWithLine();
        SpatialReference sr = SpatialReference.create(4326);
        MultiPoint geometry = (MultiPoint) operatorRandomPoints.execute(poly, .0013, 1977, sr, null);
        assertNotNull(geometry);
        assertEquals(geometry.getPointCount(), 793194);
        assertNotNull(geometry.getXY(0));
        assertNotNull(geometry.getXY(geometry.getPointCount() - 1));
        Polygon bufferedpoly = (Polygon) OperatorBuffer.local().execute(poly, sr, sr.getTolerance() * 2, null);
        boolean t = OperatorContains.local().execute(bufferedpoly, geometry, sr, null);
        assertTrue(t);
    }

    @Test
    public void testPolygonWithHole() {
        String wktpolygon = "POLYGON((0 0, 0 10, 10 10, 10 0),(3 3, 7 3, 7 7, 3 7))";
        Geometry geometry = GeometryEngine.geometryFromWkt(wktpolygon, 0, Geometry.Type.Unknown);
        MultiPoint multiPoint = (MultiPoint)OperatorRandomPoints.local().execute(geometry, .0013, 1977, SpatialReference.create(4326), null);

        String wktPolygonNoRing = "POLYGON((0 0, 0 10, 10 10, 10 0))";
        Geometry geometryNoRing = GeometryEngine.geometryFromWkt(wktPolygonNoRing, 0, Geometry.Type.Unknown);
        MultiPoint multiPointNoRing = (MultiPoint)OperatorRandomPoints.local().execute(geometryNoRing, 0.0013, 1977, SpatialReference.create(4326), null);
        Geometry geom = GeometryEngine.intersect(geometry, multiPointNoRing, SpatialReference.create(4326));

        assertEquals(multiPoint.getPointCount(), ((MultiPoint)geom).getPointCount());
    }

    @Test
    public void testMultiPartPolygonCreate() {
        String wktpolygon2 = "MULTIPOLYGON (((0 0, 0 10, 10 10, 10 0)), ((20 0, 20 10, 30 10, 30 0)))";
        Geometry geometry2 = GeometryEngine.geometryFromWkt(wktpolygon2, 0, Geometry.Type.Unknown);
        MultiPoint multiPoint2 = (MultiPoint)OperatorRandomPoints.local().execute(geometry2, .0013, 1977, SpatialReference.create(4326), null);

        String wktPolygon = "POLYGON((0 0, 0 10, 10 10, 10 0))";
        Geometry geometry = GeometryEngine.geometryFromWkt(wktPolygon, 0, Geometry.Type.Unknown);
        MultiPoint multiPoint = (MultiPoint)OperatorRandomPoints.local().execute(geometry, 0.0013, 1977, SpatialReference.create(4326), null);

        assertTrue(multiPoint.getPointCount() * 2 > 3179429);
        assertTrue(multiPoint2.getPointCount() * 2 > 3179429);
    }
}
