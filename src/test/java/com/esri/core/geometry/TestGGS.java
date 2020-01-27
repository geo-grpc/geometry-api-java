package com.esri.core.geometry;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestGGS extends TestCase {
    @Test
    public void testMGRSGridZone() {
        assertEquals(1, SpatialReference.getUTMZone(-180));
        assertEquals(2, SpatialReference.getUTMZone(-174));
        assertEquals(1, SpatialReference.getUTMZone(180));
        assertEquals(60, SpatialReference.getUTMZone(179));
        assertEquals("HA", MGRS.getSquareIdCode(-174.000001, 0.00001));
        assertEquals("JF", MGRS.getSquareIdCode(-173.999999, 0.00001));
        assertEquals("1C", MGRS.getZoneCode(-180, -80));
        assertEquals("1C", MGRS.getZoneCode(-174.01, -76.01));
        assertEquals("2D", MGRS.getZoneCode(-174, -72));
        assertEquals("2D", MGRS.getZoneCode(-168.000001, -64.00001));
        assertEquals("60X", MGRS.getZoneCode(179.9999, 83.999));
        assertEquals("1X", MGRS.getZoneCode(180, 83.999));
        assertEquals("AA", MGRS.getSquareIdCode(-179.99999, 0.00001));
        assertEquals("AV", MGRS.getSquareIdCode(-179.99999, -0.00001));
        assertEquals("HV", MGRS.getSquareIdCode(-174.000001, -0.00001));
        assertEquals("JE", MGRS.getSquareIdCode(-173.999999, -0.00001));
        assertEquals("UA", MGRS.getSquareIdCode(-112.61440, 50.00820));
        assertEquals("12U", MGRS.getZoneCode(-112.61440, 50.00820));
    }

    @Test
    public void testSquares() {
        //12U UA 84323 40791
        assertEquals("84323 40791", MGRS.getSquareZoomCode(-112.61440, 50.00820, MGRS.Zoom.Level1M));
        assertEquals("8432 4079", MGRS.getSquareZoomCode(-112.61440, 50.00820, MGRS.Zoom.Level10M));
        assertEquals("843 407", MGRS.getSquareZoomCode(-112.61440, 50.00820, MGRS.Zoom.Level100M));
        assertEquals("84 40", MGRS.getSquareZoomCode(-112.61440, 50.00820, MGRS.Zoom.Level1K));
        assertEquals("8 4", MGRS.getSquareZoomCode(-112.61440, 50.00820, MGRS.Zoom.Level10K));
    }

    @Test
    public void testMGRSposition() {
        //12U UA 84323 40791
        assertEquals("12U UA 84323 40791", MGRS.getMGRSCode(-112.61440, 50.00820, MGRS.Zoom.Level1M));
        assertEquals("12U UA 8432 4079", MGRS.getMGRSCode(-112.61440, 50.00820, MGRS.Zoom.Level10M));
        assertEquals("12U UA 843 407", MGRS.getMGRSCode(-112.61440, 50.00820, MGRS.Zoom.Level100M));
        assertEquals("12U UA 84 40", MGRS.getMGRSCode(-112.61440, 50.00820, MGRS.Zoom.Level1K));
        assertEquals("12U UA 8 4", MGRS.getMGRSCode(-112.61440, 50.00820, MGRS.Zoom.Level10K));
        assertEquals("12U UA", MGRS.getMGRSCode(-112.61440, 50.00820, MGRS.Zoom.Level100K));
        assertEquals("12U", MGRS.getMGRSCode(-112.61440, 50.00820, MGRS.Zoom.LevelGridZone));
    }

    @Test
    public void testMGRSCode() {
        Envelope envelope = new Envelope(-114, 48, -108, 56);
        Polygon result = MGRS.parseMGRS("12U", true);
        Envelope resultEnv = new Envelope();
        result.queryEnvelope(resultEnv);
        assertEquals(envelope.getXMax(), resultEnv.getXMax());
        assertEquals(envelope.getXMin(), resultEnv.getXMin());
        assertEquals(envelope.getYMax(), resultEnv.getYMax());
        assertEquals(envelope.getYMin(), resultEnv.getYMin());
        
        envelope = new Envelope(-162, 16, -156, 24);
        result = MGRS.parseMGRS("4Q", true);
        result.queryEnvelope(resultEnv);
        assertEquals(envelope.getXMax(), resultEnv.getXMax());
        assertEquals(envelope.getXMin(), resultEnv.getXMin());
        assertEquals(envelope.getYMax(), resultEnv.getYMax());
        assertEquals(envelope.getYMin(), resultEnv.getYMin());


        result = MGRS.parseMGRS("4Q FJ", false);
        result.queryEnvelope(resultEnv);
        assertEquals(resultEnv.getYMin(), 2300000.);
        assertEquals(resultEnv.getYMax(), 2400000.);

        result = MGRS.parseMGRS("4Q FJ", true);
        result.queryEnvelope(resultEnv);
        assertTrue(OperatorContains.local().execute(envelope, resultEnv, null, null));



        result = MGRS.parseMGRS("3Q XD", true);
        Envelope resultEnv2 = new Envelope();
        result.queryEnvelope(resultEnv2);
        assertEquals(resultEnv.getYMin(), resultEnv2.getYMin());
        assertEquals(resultEnv.getYMax(), resultEnv2.getYMax());

        result = MGRS.parseMGRS("12U", true);
        assertTrue(OperatorContains.local().execute(result, new Point(-112.61440, 50.00820), null, null));

        result = MGRS.parseMGRS("12U UA", true);
        assertTrue(OperatorContains.local().execute(result, new Point(-112.61440, 50.00820), null, null));

        result = MGRS.parseMGRS("12U UA 8 4", true);
        assertTrue(OperatorContains.local().execute(result, new Point(-112.61440, 50.00820), null, null));
        result = MGRS.parseMGRS("12U UA 84323 40791", true);
        assertTrue(OperatorContains.local().execute(result, new Point(-112.61440, 50.00820), null, null));
        result = MGRS.parseMGRS("12U UA 8432 4079", true);
        assertTrue(OperatorContains.local().execute(result, new Point(-112.61440, 50.00820), null, null));
        result = MGRS.parseMGRS("12U UA 843 407", true);
        assertTrue(OperatorContains.local().execute(result, new Point(-112.61440, 50.00820), null, null));
        result = MGRS.parseMGRS("12U UA 84 40", true);
        assertTrue(OperatorContains.local().execute(result, new Point(-112.61440, 50.00820), null, null));
    }

    @Test
    public void testRoundTrip() {
        double xMax = 180;
        double xmin = -180;
        double yMax = 84;
        double yMin = -80;
        RandomCoordinateGenerator random = new RandomCoordinateGenerator(400, new Envelope(xmin, yMin, xMax, yMax), 0.0);
        List<MGRS.Zoom> somethingList = Arrays.asList(MGRS.Zoom.values());
        int randCount = 0;
        Envelope envelope = new Envelope();
        while (randCount++ < 1200) {
            Point point = random.GetRandomCoord();
            // TODO handle this
            if (Math.abs(point.getY()) >= 80 || Math.abs(point.getX()) >= 180) {
                continue;
            }
            Point utmPoint = (Point)OperatorProject.local().execute(point, new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.createUTM(point)), null);

            for (MGRS.Zoom zoom : somethingList) {
                String code = MGRS.getMGRSCode(point.getX(), point.getY(), zoom);
                if (zoom == MGRS.Zoom.LevelGridZone) {
                    Polygon result = MGRS.parseMGRS(code, true);
                    result.queryEnvelope(envelope);
                    assertTrue(String.format(
                            "\ntest number: %d\npt: %s\ncode: %s\nenvelope: %s\n",
                            randCount,
                            point.toString(),
                            code,
                            envelope), OperatorIntersects.local().execute(result, point, SpatialReference.create(4326), null));
                } else {
                    Polygon result = MGRS.parseMGRS(code, false);
                    result.queryEnvelope(envelope);
                    assertTrue(String.format(
                            "\ntest number: %d\npt: %s\ncode: %s\nenvelope: %s\n",
                            randCount,
                            point.toString(),
                            code,
                            envelope), OperatorIntersects.local().execute(result, utmPoint, SpatialReference.createUTM(point), null));
                }


//                String code2 = MGRS.getMGRSCode(envelope.getXMin(), envelope.getYMin(), zoom);
//                assertEquals(String.format("\n%d\npt: %s\nenv: %s\n", randCount, point, envelope), code, code2);
            }
        }
    }

    public void testScenario1() {
        /*
    pt: 14.971362, -44.800527
    code: 33G VL 97735 39207
    envelope: 14.970905, -45.700687, 16.226223, -45.341274
         */
        Point pt = new Point(14.971362470219722, -44.80052691636635);
        Polygon result = MGRS.parseMGRS("33G", true);
        assertTrue(OperatorContains.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("33G VL", true);
        assertTrue(OperatorContains.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("33G VL 9 3", true);
        assertTrue(OperatorContains.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("33G VL 97 39", true);
        assertTrue(OperatorContains.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("33G VL 977 392", true);
        assertTrue(OperatorContains.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("33G VL 9773 3920", true);
        assertTrue(OperatorContains.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("33G VL 97735 39207", true);
        assertTrue(OperatorContains.local().execute(result, pt, null, null));
    }

    public void testScenario2() {
        /*
    pt: -0.663764, -80.000000
    code: 30C WS 45277 17504
    envelope: 8.000436, -88.027989, 19.800078, -87.761320
         */
        Point pt = new Point(-0.663764, -80.000000);
        Polygon result = MGRS.parseMGRS("30C", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("30C WS", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("30C WS 45277 17504", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
    }

    public void testScenario3() {
        /*
    test number: 14
    pt: -18.145760, 56.499253
    code: 27V XC 75682 65297
    envelope: -15.138683, 74.318858, -12.328319, 74.981807
         */
        Point pt = new Point(-18.145760, 56.499253);
        Polygon result = MGRS.parseMGRS("27V", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("27V XC", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("27V XC 75682 65297", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));

    }

    public void testScenario4() {
        /*
test number: 16
pt: -25.118686, 84.000000
code: 26X
envelope: -30.000000, 72.000000, -24.000000, 80.000000
         */
        Point pt = new Point(-25.118686, 84.000000);
        Polygon result = MGRS.parseMGRS("26X", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
//        result = MGRS.parseMGRS("27V XC", true);
//        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
//        result = MGRS.parseMGRS("27V XC 75682 65297", true);
//        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
    }

    public void testScenario5() {
        /*
        test number: 56
        pt: -132.774604, -2.458531
        code: 8M QC 47466 28050
        envelope: -132.630631, -20.530172, -132.171919, -20.270181
         */
        Point pt = new Point(-132.774604, -2.458531);
        Polygon result = MGRS.parseMGRS("8M", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("8M QC", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
        result = MGRS.parseMGRS("8M QC 47466 28050", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
    }

    public void testScenario6() {
        /*
test number: 110
pt: POINT (-125.20231713562477 -77.01914862680061)
code: 10C DV 4 5
envelope: Envelope: [-125.39323359521163, -77.02861215838094, -123.383491378233, -76.48074053627055]
         */
        Envelope env = new Envelope(-126.0, -80.0, -120.0, -72.0);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.createUTM(env));
        Geometry projected = OperatorProject.local().execute(env, projectionTransformation, null);
        Geometry reProjected = OperatorProject.local().execute(projected, projectionTransformation.getReverse(), null);
        Envelope envRe = new Envelope();
        projected.queryEnvelope(envRe);
        reProjected.queryEnvelope(envRe);
        assertTrue(OperatorEquals.local().execute(reProjected, envRe, SpatialReference.create(4326), null));
        Point pt = new Point(-125.20231713562477, -77.01914862680061);
        Point ptt2 = new Point(-125.20231713562477, 0);
        InverseResult inverseResult = OperatorGeodeticInverse.local().execute(pt, ptt2, SpatialReference.create(4326), SpatialReference.create(4326), GeodeticCurveType.Geodesic, null);

        Polygon result = MGRS.parseMGRS("10C DV 4 5", true);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
    }

    public void testScenario7() {
        /*
test number: 271
pt: POINT (-95.08164160685115 -30.390430586679607)
code: 15J TG
envelope: Envelope: [-96.13180313970017, -30.716083310981194, -95.06959008486949, -29.79401200205343]
         */
        Point originalPt = new Point(-95.08164160685115, -30.390430586679607);
        Point pt = (Point)OperatorProject.local().execute(originalPt, new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.createUTM(originalPt)), null);
        Polygon result = MGRS.parseMGRS("15J", false);
        assertTrue(OperatorIntersects.local().execute(result, pt, null, null));
        Envelope envelope = new Envelope();
        result.queryEnvelope(envelope);
        assertTrue(envelope.getXMin() <= pt.getX() && pt.getX() <= envelope.getXMax());
        assertTrue(envelope.getYMin() <= pt.getY() && pt.getY() <= envelope.getYMax());
        assertTrue(OperatorIntersects.local().execute(envelope, pt, null, null));
        assertTrue(OperatorIntersects.local().execute(envelope, result, null, null));
        assertTrue(OperatorIntersects.local().execute(result, pt, SpatialReference.create(4326), null));
    }

    public void testBasics() {
        Envelope envelope = new Envelope();
        Point point = new Point(-174,0);
        String code = MGRS.getMGRSCode(point.getX(), point.getY(), MGRS.Zoom.LevelGridZone);
        Polygon result = MGRS.parseMGRS(code, true);
        assertTrue(OperatorIntersects.local().execute(result, point, null, null));

        result = MGRS.parseMGRS(code, false);
        result.queryEnvelope(envelope);
        assertEquals(500000.0, envelope.getCenterX(), .00001);


        code = MGRS.getMGRSCode(point.getX(), point.getY(), MGRS.Zoom.Level100K);
        result = MGRS.parseMGRS(code, true);
        assertTrue(OperatorIntersects.local().execute(result, point, null, null));
        result = MGRS.parseMGRS(code, false);

        assertEquals(result.calculateRingArea2D(0), Math.pow(100000, 2), 0.0001);
        result.queryEnvelope(envelope);
        assertEquals(150000.0, envelope.getCenterX(), .00001);


        code = MGRS.getMGRSCode(point.getX(), point.getY(), MGRS.Zoom.Level10K);
        result = MGRS.parseMGRS(code, true);
        assertTrue(OperatorIntersects.local().execute(result, point, null, null));

        result = MGRS.parseMGRS(code, false);
        assertEquals(result.calculateRingArea2D(0), Math.pow(10000, 2), 0.0001);
        result.queryEnvelope(envelope);
        assertEquals(165000.0, envelope.getCenterX(), .00001);


        code = MGRS.getMGRSCode(point.getX(), point.getY(), MGRS.Zoom.Level1K);
        result = MGRS.parseMGRS(code, true);
        assertTrue(OperatorIntersects.local().execute(result, point, null, null));

        result = MGRS.parseMGRS(code, false);
        assertEquals(result.calculateRingArea2D(0), Math.pow(1000, 2), 0.0001);
        result.queryEnvelope(envelope);
        assertEquals(166500.0, envelope.getCenterX(), .00001);


        code = MGRS.getMGRSCode(point.getX(), point.getY(), MGRS.Zoom.Level100M);
        result = MGRS.parseMGRS(code, true);
        assertTrue(OperatorIntersects.local().execute(result, point, null, null));

        result = MGRS.parseMGRS(code, false);
        assertEquals(result.calculateRingArea2D(0), Math.pow(100, 2), 0.0001);
        result.queryEnvelope(envelope);
        assertEquals(166050.0, envelope.getCenterX(), .00001);


        code = MGRS.getMGRSCode(point.getX(), point.getY(), MGRS.Zoom.Level10M);
        result = MGRS.parseMGRS(code, true);
        assertTrue(OperatorIntersects.local().execute(result, point, null, null));

        result = MGRS.parseMGRS(code, false);
        assertEquals(result.calculateRingArea2D(0), Math.pow(10, 2), 0.0001);
        result.queryEnvelope(envelope);
        assertEquals(166025.0, envelope.getCenterX(), .00001);


        code = MGRS.getMGRSCode(point.getX(), point.getY(), MGRS.Zoom.Level1M);
        result = MGRS.parseMGRS(code, true);
        assertTrue(OperatorIntersects.local().execute(result, point, null, null));

        result = MGRS.parseMGRS(code, false);
        assertEquals(result.calculateRingArea2D(0), Math.pow(1, 2), 0.0001);
        result.queryEnvelope(envelope);
        assertEquals(166021.5, envelope.getCenterX(), .00001);
    }
}
