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
        poly.startPath(20, 13);
        poly.lineTo(150, 120);
        poly.lineTo(300, 414);
        poly.lineTo(610, 14);
        poly.lineTo(6210, 140);
        poly.closePathWithLine();
        MultiPoint geometry = (MultiPoint) operatorRandomPoints.execute(poly, 100, 1977, null, null);
        assertNotNull(geometry);
        assertEquals(geometry.getPointCount(), 13414);
        assertNotNull(geometry.getXY(0));
        assertNotNull(geometry.getXY(geometry.getPointCount() - 1));
        boolean t = OperatorContains.local().execute(poly, geometry, null, null);
        assertTrue(t);
    }
}
