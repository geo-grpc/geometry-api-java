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

    /**
     * @since 1.7
     */
    @Ignore("This test requires data too large to keep in repo")
    @Test
    public static void testRicksSA() {
        try {
            OperatorImportFromGeoJson op = (OperatorImportFromGeoJson) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromGeoJson);

            byte[] encoded = Files.readAllBytes(Paths.get("/Users/davidraleigh/data/descartes/crops/shapes_v1c.json"));
            String testData = new String(encoded, Charset.defaultCharset());
            JSONObject geoJsonObject = new JSONObject(testData);
            Iterator<String> iter = geoJsonObject.keys();
            List<Geometry> geometryList = new ArrayList<Geometry>();
            OperatorSimplify operatorSimplify = (OperatorSimplify.local());
            SpatialReference sr = SpatialReference.create(4326);
            while (iter.hasNext())
            {
                JSONObject jsonObject = geoJsonObject.getJSONObject(iter.next());
                MapGeometry mg = op.execute(0, Geometry.Type.Unknown, jsonObject.toString(), null);
                Geometry mgSimple = operatorSimplify.execute(mg.getGeometry(), sr, true, null);
                geometryList.add(mgSimple);
            }
            SimpleGeometryCursor sgc = new SimpleGeometryCursor(geometryList);
            OperatorUnion union = (OperatorUnion) OperatorFactoryLocal
                    .getInstance().getOperator(Operator.Type.Union);



            GeometryCursor outputCursor = union.execute(sgc, sr, null);
            Geometry result = outputCursor.next();
            OperatorExportToGeoJson operatorExportToGeoJson = OperatorExportToGeoJson.local();

            Geometry resSimple = operatorSimplify.execute(result, sr, true, null);

            String s = operatorExportToGeoJson.execute(resSimple);
            int a = 0;
        } catch (Exception e) {
            assertNull(e);
        }

	}
}
