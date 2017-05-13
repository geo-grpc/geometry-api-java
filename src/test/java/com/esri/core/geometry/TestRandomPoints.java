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
        Polygon bufferedpoly = (Polygon)OperatorBuffer.local().execute(poly, sr, sr.getTolerance() * 2, null);
        boolean t = OperatorContains.local().execute(bufferedpoly, geometry, sr, null);
        assertTrue(t);
    }

    @Test
    public void testMultiPartPolygonCreate() {

    }
}
