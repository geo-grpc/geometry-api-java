package com.esri.core.geometry;

import com.google.common.geometry.*;
import junit.framework.TestCase;
import org.junit.Test;


public class TestGGS extends TestCase {
    @Test
    public void testBasic() {
        S2LatLng llRad = S2LatLng.fromRadians(S2.M_PI_4, S2.M_PI_2);
        assertTrue(llRad.lat().radians() == S2.M_PI_4);
        assertTrue(llRad.lng().radians() == S2.M_PI_2);
        assertTrue(llRad.isValid());
        S2LatLng llDeg = S2LatLng.fromDegrees(45, 90);
        assertEquals(llDeg, llRad);
        assertTrue(llDeg.isValid());
        assertTrue(!S2LatLng.fromDegrees(-91, 0).isValid());
        assertTrue(!S2LatLng.fromDegrees(0, 181).isValid());

        S2LatLng bad = S2LatLng.fromDegrees(120, 200);
        assertTrue(!bad.isValid());
        S2LatLng better = bad.normalized();
        assertTrue(better.isValid());
        assertEquals(better.lat(), S1Angle.degrees(90));
        assertEquals(better.lng().radians(), S1Angle.degrees(-160).radians());

        bad = S2LatLng.fromDegrees(-100, -360);
        assertTrue(!bad.isValid());
        better = bad.normalized();
        assertTrue(better.isValid());
        assertEquals(better.lat(), S1Angle.degrees(-90));
        assertEquals(better.lng().radians(), 0, 0.000000001);

        assertTrue((S2LatLng.fromDegrees(10, 20).add(S2LatLng.fromDegrees(20, 30))).approxEquals(
                S2LatLng.fromDegrees(30, 50)));
        assertTrue((S2LatLng.fromDegrees(10, 20).sub(S2LatLng.fromDegrees(20, 30))).approxEquals(
                S2LatLng.fromDegrees(-10, -10)));
        assertTrue((S2LatLng.fromDegrees(10, 20).mul(0.5)).approxEquals(S2LatLng.fromDegrees(5, 10)));
    }

    @Test
    public void testLatLngRect() {
        S2LatLng s2LatLng = S2LatLng.fromDegrees(10, 20);
        S2Cell s2Cell = new S2Cell(s2LatLng);
        long id = s2Cell.id().id();
        Envelope envelope = (Envelope)OperatorImportFromS2.local().execute(id, 0, null);
        assertEquals(envelope.getCenter().getX(), 14.99999999, .00001);
    }
}
