package com.esri.core.geometry;

import junit.framework.TestCase;
import org.junit.Test;

public class TestGGS extends TestCase {
    @Test
    public void testMGRSGridZone() {
        assertEquals(1, SpatialReference.getUTMZone(-180));
        assertEquals(2, SpatialReference.getUTMZone(-174));
        assertEquals(1, SpatialReference.getUTMZone(180));
        assertEquals(60, SpatialReference.getUTMZone(179));
        assertEquals("HA", MGRS.gridSquareId(-174.000001, 0.00001));
        assertEquals("JF", MGRS.gridSquareId(-173.999999, 0.00001));
        assertEquals("1C", MGRS.gridZoneCode(-180, -80));
        assertEquals("1C", MGRS.gridZoneCode(-174.01, -76.01));
        assertEquals("2D", MGRS.gridZoneCode(-174, -72));
        assertEquals("2D", MGRS.gridZoneCode(-168.000001, -64.00001));
        assertEquals("60X", MGRS.gridZoneCode(179.9999, 83.999));
        assertEquals("1X", MGRS.gridZoneCode(180, 83.999));
        assertEquals("AA", MGRS.gridSquareId(-179.99999, 0.00001));
        assertEquals("AV", MGRS.gridSquareId(-179.99999, -0.00001));
        assertEquals("HV", MGRS.gridSquareId(-174.000001, -0.00001));
        assertEquals("JE", MGRS.gridSquareId(-173.999999, -0.00001));
        assertEquals("UA", MGRS.gridSquareId(-112.61440, 50.00820));
        assertEquals("12U", MGRS.gridZoneCode(-112.61440, 50.00820));
    }
}
