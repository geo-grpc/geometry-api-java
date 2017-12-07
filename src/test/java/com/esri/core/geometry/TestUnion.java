/*
 Copyright 1995-2017 Esri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */

package com.esri.core.geometry;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class TestUnion extends TestCase {
    public ArrayDeque<Geometry> pointList = null;
    public ArrayDeque<Geometry> bufferedPointList = null;

    @Override
    protected void setUp() throws Exception {
        Random random = new Random(1977);
        int max_size = 10000;
        pointList = new ArrayDeque<>(max_size);
        bufferedPointList = new ArrayDeque<>(max_size);
        for (int i = 0; i < max_size; i++) {
            double x = randomWithRange(-20, 20, random);
            double y = randomWithRange(-20, 20, random);
            Geometry point = new Point(x, y);
            pointList.add(point);
            bufferedPointList.add(OperatorBufferLocal.local().execute(point, null, 2.5, null));
        }

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testUnion() {
        Point pt = new Point(10, 20);

        Point pt2 = new Point();
        pt2.setXY(10, 10);

        Envelope env1 = new Envelope(10, 10, 30, 50);
        Envelope env2 = new Envelope(30, 10, 60, 50);
        Geometry[] geomArray = new Geometry[]{env1, env2};
        SimpleGeometryCursor inputGeometries = new SimpleGeometryCursor(
                geomArray);
        OperatorUnion union = (OperatorUnion) OperatorFactoryLocal
                .getInstance().getOperator(Operator.Type.Union);

        SpatialReference sr = SpatialReference.create(4326);

        GeometryCursor outputCursor = union.execute(inputGeometries, sr, null);
        Geometry result = outputCursor.next();
    }

    static double randomWithRange(double min, double max, Random random) {
        double range = Math.abs(max - min);
        return (random.nextDouble() * range) + (min <= max ? min : max);
    }


//	@Test
//    @Ignore
//	public void testBufferUnionEnvelope() {
//		int size = 1000;
//        Random random = new Random(1977);
//		List<Geometry> envList = new ArrayList<>(size);
//		Envelope2D envelope2D = new Envelope2D();
//		for (int i = 0; i < size; i++){
//		    pointList.pop().queryEnvelope2D(envelope2D);
//			Envelope2D envelope2D1 = envelope2D.getInflated(5, 5);
//			Envelope envelope = new Envelope(envelope2D1);
//			envList.add(envelope);
//		}
//		SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(envList);
//		double[] d = {2.5};
//		OperatorUnionCursor operatorUnionCursor = new OperatorUnionCursor(simpleGeometryCursor, null, null);
//		Geometry result = operatorUnionCursor.next();
//		assertTrue(result.calculateArea2D() > 40 * 40);
//	}

//    @Ignore
//    @Test
//    public void testBufferUnionPoint() {
//        int size = 10000;
//        SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(bufferedPointList);
//
//        double[] d = {2.5};
//        OperatorUnionCursor operatorUnionCursor = new OperatorUnionCursor(simpleGeometryCursor, null, null);
//
//        // Tests union on buffer at next call
//        long startTime = System.nanoTime();
//        Geometry result = operatorUnionCursor.next();
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime) / 1000000;
//        System.out.println(result.calculateArea2D());
//        System.out.println(duration);
//        assertTrue(result.calculateArea2D() > 40 * 40);
//    }

    @Test
    public void testQuadTreeIterator() {
        int size = 1000;
        SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(bufferedPointList.stream().collect(Collectors.toList()).subList(0, size));

        HashMap<Integer, Geometry> m_quadTreeMap = new HashMap<>();
        Envelope2D quad_envelope2D = new Envelope2D();
        Geometry geometry = null;
        Envelope2D geometry_env = new Envelope2D();
        int count_index = 0;
        while ((geometry = simpleGeometryCursor.next()) != null) {
            geometry.queryEnvelope2D(geometry_env);
            quad_envelope2D.merge(geometry_env);
            m_quadTreeMap.put(count_index++, geometry);
        }
        QuadTree quadTree = new QuadTree(quad_envelope2D, 16);
        for (Integer element_id : m_quadTreeMap.keySet()) {
            m_quadTreeMap.get(element_id).queryEnvelope2D(geometry_env);
            quadTree.insert(element_id, geometry_env);
        }

        QuadTree.QuadTreeIterator quadTreeIterator = quadTree.getIterator(true);
        quadTreeIterator.resetIterator(quad_envelope2D, 0.0);
        int element_handle = -1;
        List<Geometry> geometryList = new ArrayList<>();
        assertFalse(geometryList.containsAll(m_quadTreeMap.values()));

        int max_height = 0;
        while ((element_handle = quadTreeIterator.next()) != -1) {
            int element_id = quadTree.getElement(element_handle);
            int quad_handle = quadTree.getQuad(element_handle);

            int sub_count = quadTree.getContainedSubTreeElementCount(quad_handle);
            int sub_count_2 = quadTree.getSubTreeElementCount(quad_handle);

            int quad_height = quadTree.getHeight(quad_handle);
            max_height = quad_height > max_height ? quad_height : max_height;
            Envelope2D envelope2D = quadTree.getExtent(quad_handle);
            assertTrue(quadTree.hasData(envelope2D, 0.0));
            geometryList.add(m_quadTreeMap.get(element_id));
        }
        assertTrue(max_height == 3);
        assertEquals(16, quadTree.getMaxHeight());

        assertTrue(geometryList.containsAll(m_quadTreeMap.values()));
    }

//    @Test
//    @Ignore
//    public void testGeodesicBufferUnionPoint() {
//        int size = 2;
//        SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(pointList);
//        double[] d = {2500};
//        OperatorGeodesicBufferCursor operatorGeodesicBufferCursor = new OperatorGeodesicBufferCursor(simpleGeometryCursor, SpatialReference.create(4326), d, 1.0, false, true, null);
//        // Tests union on buffer at next call
//        Geometry result = operatorGeodesicBufferCursor.next();
//        assertTrue(result.calculateArea2D() > 0);
//    }
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
