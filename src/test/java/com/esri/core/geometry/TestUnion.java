package com.esri.core.geometry;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

public class TestUnion extends TestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public static void testUnion() {
		Point pt = new Point(10, 20);

		Point pt2 = new Point();
		pt2.setXY(10, 10);

		Envelope env1 = new Envelope(10, 10, 30, 50);
		Envelope env2 = new Envelope(30, 10, 60, 50);
		Geometry[] geomArray = new Geometry[] { env1, env2 };
		SimpleGeometryCursor inputGeometries = new SimpleGeometryCursor(
				geomArray);
		OperatorUnion union = (OperatorUnion) OperatorFactoryLocal
				.getInstance().getOperator(Operator.Type.Union);

		SpatialReference sr = SpatialReference.create(4326);

		GeometryCursor outputCursor = union.execute(inputGeometries, sr, null);
		Geometry result = outputCursor.next();
	}

	static double randomWithRange(double min, double max)
	{
		double range = Math.abs(max - min);
		return (Math.random() * range) + (min <= max ? min : max);
	}

	@Test
	public static void testBufferUnionEnvelope() {
		int size = 1000;
		List<Geometry> envList = new ArrayList<>(size);
		Envelope2D envelope2D = new Envelope2D();
		for (int i = 0; i < size; i++){
			double x = randomWithRange(-20, 20);
			double y = randomWithRange(-20, 20);
			Point point = new Point(x, y);
			point.queryEnvelope2D(envelope2D);
			Envelope2D envelope2D1 = envelope2D.getInflated(5, 5);
			Envelope envelope = new Envelope(envelope2D1);
			envList.add(envelope);
		}
		SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(envList);
		double[] d = {2.5};
		OperatorUnionCursor operatorUnionCursor = new OperatorUnionCursor(simpleGeometryCursor, null, null);
		Geometry result = operatorUnionCursor.next();
		assertTrue(result.calculateArea2D() > 40 * 40);
	}

	@Test
	public static void testBufferUnionPoint() {
		int size = 1000;
		List<String> points = new ArrayList<>(size);
		List<Geometry> pointList = new ArrayList<>(size);
		for (int i = 0; i < size; i++){
			double x = randomWithRange(-20, 20);
			double y = randomWithRange(-20, 20);
			points.add(String.format("Point(%f %f)", x, y));
			pointList.add(new Point(x, y));
		}
		SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(pointList);
		double[] d = {2.5};
		OperatorBufferCursor operatorBufferCursor = new OperatorBufferCursor(simpleGeometryCursor, null, d, true, null);
		// Tests union on buffer at next call
		Geometry result = operatorBufferCursor.next();
		assertTrue(result.calculateArea2D() > 40 * 40);
	}

    @Test
    public static void testGeodesicBufferUnionPoint() {
        int size = 2;
        List<String> points = new ArrayList<>(size);
        List<Geometry> pointList = new ArrayList<>(size);
        for (int i = 0; i < size; i++){
            double x = randomWithRange(-20, 20);
            double y = randomWithRange(-20, 20);
            points.add(String.format("Point(%f %f)", x, y));
            pointList.add(new Point(x, y));
        }
        SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(pointList);
        double[] d = {2500};
        OperatorGeodesicBufferCursor operatorGeodesicBufferCursor = new OperatorGeodesicBufferCursor(simpleGeometryCursor, SpatialReference.create(4326), d, 1.0, false, true, null);
        // Tests union on buffer at next call
        Geometry result = operatorGeodesicBufferCursor.next();
        assertTrue(result.calculateArea2D() > 0);
    }
//    /**
//     * @since 1.7
//     */
//    @Ignore("This test requires data too large to keep in repo")
//    @Test
//    public static void testRicksSA() {
//        try {
//            OperatorImportFromGeoJson op = (OperatorImportFromGeoJson) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromGeoJson);
//
//            byte[] encoded = Files.readAllBytes(Paths.get("/Users/davidraleigh/data/descartes/crops/shapes_v1c.json"));
//            String testData = new String(encoded, Charset.defaultCharset());
//            JSONObject geoJsonObject = new JSONObject(testData);
//            Iterator<String> iter = geoJsonObject.keys();
//            List<Geometry> geometryList = new ArrayList<Geometry>();
//            OperatorSimplify operatorSimplify = (OperatorSimplify.local());
//            SpatialReference sr = SpatialReference.create(4326);
//            while (iter.hasNext())
//            {
//                JSONObject jsonObject = geoJsonObject.getJSONObject(iter.next());
//                MapGeometry mg = op.execute(0, Geometry.Type.Unknown, jsonObject.toString(), null);
//                Geometry mgSimple = operatorSimplify.execute(mg.getGeometry(), sr, true, null);
//                geometryList.add(mgSimple);
//            }
//            SimpleGeometryCursor sgc = new SimpleGeometryCursor(geometryList);
//            OperatorUnion union = (OperatorUnion) OperatorFactoryLocal
//                    .getInstance().getOperator(Operator.Type.Union);
//
//
//
//            GeometryCursor outputCursor = union.execute(sgc, sr, null);
//            Geometry result = outputCursor.next();
//            OperatorExportToGeoJson operatorExportToGeoJson = OperatorExportToGeoJson.local();
//
//            Geometry resSimple = operatorSimplify.execute(result, sr, true, null);
//
//            String s = operatorExportToGeoJson.execute(resSimple);
//            int a = 0;
//        } catch (Exception e) {
//            assertNull(e);
//        }
//
//	}
}
