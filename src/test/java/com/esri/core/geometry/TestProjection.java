package com.esri.core.geometry;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.proj4.PJ;

import java.util.Arrays;

/**
 * Created by davidraleigh on 11/3/16.
 */
public class TestProjection extends TestCase {

    static {
        System.loadLibrary("proj");
    }

    SpatialReference spatialReferenceWGS = SpatialReference.create(4326);
    SpatialReference spatialReferenceMerc = SpatialReference.create(3857);
    ProjectionTransformation projectionTransformationToMerc = new ProjectionTransformation(spatialReferenceWGS, spatialReferenceMerc);
    ProjectionTransformation projectionTransformationToWGS = new ProjectionTransformation(spatialReferenceMerc, spatialReferenceWGS);

    @Before
    public void setUp() {

    }

    @Test
    public void testProj4() throws Exception {
        PJ sourcePJ = new PJ("+init=epsg:32632");                   // (x,y) axis order
        PJ targetPJ = new PJ("+proj=latlong +datum=WGS84");         // (λ,φ) axis order
        PJ sourcePJ_utm = new PJ("+proj=utm +zone=32 +datum=WGS84 +units=m +no_defs ");
        PJ targetPJ_4326 = new PJ("+init=epsg:4326");

        double[] coordinates_32632 = {
                500000, 0,   // First coordinate
                400000, 100000,   // Second coordinate
                600000, -100000    // Third coordinate
        };

        double[] coordinates_WGS84 = Arrays.copyOf(coordinates_32632, coordinates_32632.length);
        double[] coordinates_4326 = Arrays.copyOf(coordinates_32632, coordinates_32632.length);

        sourcePJ.transform(targetPJ, 2, coordinates_WGS84, 0, 3);
        sourcePJ.transform(targetPJ_4326, 2, coordinates_4326, 0, 3);

        for (int i = 0; i < coordinates_WGS84.length; i++) {
            if (i == 1)
                continue;

            assertTrue((Math.abs(coordinates_WGS84[i] - coordinates_32632[i])) > 1);
            assertTrue((Math.abs(coordinates_4326[i] - coordinates_32632[i])) > 1);
            assertEquals(coordinates_4326[i], coordinates_WGS84[i]);
        }
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
        Point point = new Point(500000, 0);
        Point pointOut = (Point) OperatorProject.local().execute(point, projectionTransformation, null);
        assertNotNull(pointOut);
        assertTrue(Math.abs(point.getX() - pointOut.getY()) > 1);
        projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.create(32632));
        Point originalPoint = (Point) OperatorProject.local().execute(pointOut, projectionTransformation, null);
        assertEquals(originalPoint.getX(), point.getX());
        assertEquals(originalPoint.getY(), point.getY());
    }

    @Test
    public void testProjectMultiPoint() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(32632), SpatialReference.create(4326));
        MultiPoint multiPoint = new MultiPoint();
        multiPoint.add(500000, 0);
        multiPoint.add(400000, 100000);
        multiPoint.add(600000, -100000);
        MultiPoint multiPointOut = (MultiPoint) OperatorProject.local().execute(multiPoint, projectionTransformation, null);
        assertNotNull(multiPointOut);
        assertFalse(multiPointOut.equals(multiPoint));
        assertEquals(multiPoint.getPointCount(), multiPointOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.create(32632));
        MultiPoint originalMultiPoint = (MultiPoint) OperatorProject.local().execute(multiPointOut, projectionTransformation, null);

        for (int i = 0; i < multiPoint.getPointCount(); i++) {
            assertEquals(multiPoint.getPoint(i).getX(), originalMultiPoint.getPoint(i).getX(), 1e-10);
            assertEquals(multiPoint.getPoint(i).getY(), originalMultiPoint.getPoint(i).getY(), 1e-10);
        }
    }

    @Test
    public void testProjectPolyline() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(32632), SpatialReference.create(4326));
        Polyline polyline = new Polyline();
        polyline.startPath(500000, 0);
        polyline.lineTo(400000, 100000);
        polyline.lineTo(600000, -100000);
        Polyline polylineOut = (Polyline) OperatorProject.local().execute(polyline, projectionTransformation, null);
        assertNotNull(polylineOut);
        assertFalse(polylineOut.equals(polyline));

        MultiPathImpl polyline_impl = (MultiPathImpl) polylineOut._getImpl();
        int point_count = polyline_impl.getPointCount();
        int path_count = polyline_impl.getPathCount();
        assertEquals(point_count, 3);
        assertEquals(path_count, 1);

        assertEquals(polyline.getPointCount(), polylineOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.create(32632));
        Polyline originalPolyline = (Polyline) OperatorProject.local().execute(polylineOut, projectionTransformation, null);

        for (int i = 0; i < polyline.getPointCount(); i++) {
            assertEquals(polyline.getPoint(i).getX(), originalPolyline.getPoint(i).getX(), 1e-10);
            assertEquals(polyline.getPoint(i).getY(), originalPolyline.getPoint(i).getY(), 1e-10);
        }
    }

    @Test
    public void testProjectPolygon() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(32632), SpatialReference.create(4326));
        Polygon polygon = new Polygon();
        polygon.startPath(500000, 0);
        polygon.lineTo(400000, 100000);
        polygon.lineTo(600000, -100000);
        polygon.closeAllPaths();
        Polygon polygonOut = (Polygon) OperatorProject.local().execute(polygon, projectionTransformation, null);
        assertNotNull(polygonOut);
        assertFalse(polygonOut.equals(polygon));
        assertEquals(polygon.getPointCount(), polygonOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), SpatialReference.create(32632));
        Polygon originalPolygon = (Polygon) OperatorProject.local().execute(polygonOut, projectionTransformation, null);

        for (int i = 0; i < polygon.getPointCount(); i++) {
            assertEquals(polygon.getPoint(i).getX(), originalPolygon.getPoint(i).getX(), 1e-10);
            assertEquals(polygon.getPoint(i).getY(), originalPolygon.getPoint(i).getY(), 1e-10);
        }
    }

    @Test
    public void testProjectEnvelope() {
        Envelope2D envelope2D = new Envelope2D(-10000, -10000, 10000, 10000);
        String proj4 = String.format(
                "+proj=laea +lat_0=%f +lon_0=%f +x_0=0.0 +y_0=0.0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
                0f, 0f);

        SpatialReference spatialReference = SpatialReference.createFromProj4(proj4);
        SpatialReference spatialReferenceWgs84 = SpatialReference.create(4326);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReference, spatialReferenceWgs84);
        Polygon polygon = (Polygon) OperatorProject.local().execute(new Envelope(envelope2D), projectionTransformation, null);
        assertNotNull(polygon);
        Point2D point2D = new Point2D();
        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984
        Envelope2D gcsEnvelope = new Envelope2D();
        polygon.queryEnvelope2D(gcsEnvelope);
        GeoDist.getEnvCenter(a, e2, gcsEnvelope, point2D);
        assertEquals(point2D.x, 0, 1e-14);
        assertEquals(point2D.y, 0, 1e-6);

        // TODO
    }

    @Test
    public void testEPSGCodes() {
        String wktGeom = "MULTIPOLYGON (((6311583.246999994 1871386.1630000025, 6311570 1871325, 6311749.093999997 1871285.9699999988, 6311768.118000001 1871345.9619999975, 6311583.246999994 1871386.1630000025)))";
        SpatialReference spatialReference = SpatialReference.create(102646);
        SpatialReference spatialReferenceWgs84 = SpatialReference.create(4326);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReference, spatialReferenceWgs84);
        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, projectionTransformation, null);
        Geometry geometry = projectCursor.next();

        assertNotNull(geometry);
    }

    @Test
    public void testFoldInto360() {
        String wktGeom = "POLYGON((120 48.2246726495652,140 48.2246726495652,140 25.799891182088334,120 25.799891182088334,120 48.2246726495652))";
        SimpleStringCursor result = new SimpleStringCursor(wktGeom);

        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, result);
        Geometry expectedGeometry = wktCursor.next();

        String wktGeom360 = "POLYGON((480 48.2246726495652,500 48.2246726495652,500 25.799891182088334,480 25.799891182088334,480 48.2246726495652))";
        SimpleStringCursor test = new SimpleStringCursor(wktGeom360);
        wktCursor = new OperatorImportFromWktCursor(0, test);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, this.projectionTransformationToMerc, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, this.projectionTransformationToWGS, null);

        Polygon actualGeometry = (Polygon) reProjectCursor.next();

        assertTrue(GeometryEngine.equals(actualGeometry, expectedGeometry, spatialReferenceWGS));
    }


    @Test
    public void testWrapTriangle() {
        String wktGeom = "POLYGON((167 30, 201 49, 199 18, 167 30))";
        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, this.projectionTransformationToMerc, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, this.projectionTransformationToWGS, null);

        Polygon result = (Polygon) reProjectCursor.next();
        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Simplify);
        boolean isSimple = simplify.isSimpleAsFeature(result, spatialReferenceWGS, true, nonSimpleResult, null);

        simpleStringCursor = new SimpleStringCursor(wktGeom);
        wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        Polygon expected = (Polygon) wktCursor.next();
        assertTrue(GeometryEngine.isSimple(expected, spatialReferenceWGS));

        assertEquals(expected.calculateArea2D(), result.calculateArea2D(), 1e-10);
    }

    @Test
    public void testWrapTriangleOtherSide() {
        String wktGeom = "POLYGON((-193 -30, -160 -29, -158 -40, -193 -30))";
        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, this.projectionTransformationToMerc, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, this.projectionTransformationToWGS, null);

        Polygon result = (Polygon) reProjectCursor.next();
        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Simplify);
        boolean isSimple = simplify.isSimpleAsFeature(result, spatialReferenceWGS, true, nonSimpleResult, null);

        simpleStringCursor = new SimpleStringCursor(wktGeom);
        wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        Polygon expected = (Polygon) wktCursor.next();
        assertTrue(GeometryEngine.isSimple(expected, spatialReferenceWGS));

        assertEquals(expected.calculateArea2D(), result.calculateArea2D(), .00000000001);
    }

    @Test
    public void testWrap() {
        String wktGeom = "POLYGON((167.87109375 30.751277776257812," +
                                 "201.43359375 49.38237278700955," +
                                 "232.49609375 -5.266007882805485," +
                                 "116.19500625 -17.308687886770024," +
                                 "199.54296875 18.979025953255267," +
                                 "126.03515625 12.897489183755892," +
                                 "167.87109375 30.751277776257812))";
        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, this.projectionTransformationToMerc, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, this.projectionTransformationToWGS, null);

        Polygon result = (Polygon) reProjectCursor.next();
        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Simplify);
        boolean isSimple = simplify.isSimpleAsFeature(result, spatialReferenceWGS, true, nonSimpleResult, null);

        simpleStringCursor = new SimpleStringCursor(wktGeom);
        wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        Polygon expected = (Polygon) wktCursor.next();
        assertTrue(GeometryEngine.isSimple(expected, spatialReferenceWGS));

        assertEquals(expected.calculateArea2D(), result.calculateArea2D(), 1e-10);
    }

    @Test
    public void testAlbers() {
        String wktGeom = "MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)), ((20 35, 45 20, 30 5, 10 10, 10 30, 20 35), (30 20, 20 25, 20 15, 30 20)))";
        int wkid = 102003;

        ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReferenceWGS, SpatialReference.create(wkid));

        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, projectionTransformation, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, projectionTransformation.getReverse(), null);

        Polygon result = (Polygon) reProjectCursor.next();
        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Simplify);
        boolean isSimple = simplify.isSimpleAsFeature(result, spatialReferenceWGS, true, nonSimpleResult, null);
        assertTrue(isSimple);

        simpleStringCursor = new SimpleStringCursor(wktGeom);
        wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        Polygon expected = (Polygon) wktCursor.next();
        assertTrue(GeometryEngine.isSimple(expected, spatialReferenceWGS));

        assertEquals(expected.calculateArea2D(), result.calculateArea2D(), 1e-10);
    }

    @Test
    public void testProjectionTransformation() {
        int count = 400;
        Envelope e = new Envelope(0,0,40, 40);
        RandomCoordinateGenerator randomCoordinateGenerator = new RandomCoordinateGenerator(count, e, SpatialReference.create(4326).getTolerance());
        MultiPoint multiPoint = new MultiPoint();
        for (int i = 0; i < count; i++) {
            multiPoint.add(randomCoordinateGenerator._GenerateNewPoint());
        }

        ProjectionTransformation projectionTransformation = ProjectionTransformation.getEqualArea(multiPoint, spatialReferenceWGS);
        Geometry projected = OperatorProject.local().execute(multiPoint, projectionTransformation, null);
        Geometry reprojected = OperatorProject.local().execute(projected, projectionTransformation.getReverse(), null);

        assertTrue(OperatorEquals.local().execute(reprojected, multiPoint, SpatialReference.create(104919), null));

        Geometry reProjectedConvexhull = OperatorProject.local().execute(OperatorConvexHull.local().execute(projected, null), projectionTransformation.getReverse(), null);
        Geometry convexHull = OperatorConvexHull.local().execute(multiPoint, null);

        assertEquals(convexHull.calculateArea2D(), reProjectedConvexhull.calculateArea2D(), 1);
    }

    @Test
    public void testGeometryEnvelope() {
        MultiPoint multiPoint = new MultiPoint();
        multiPoint.add(0,0);
        multiPoint.add(0,20);
        multiPoint.add(40,40);

        ProjectionTransformation projectionTransformation = ProjectionTransformation.getEqualArea(multiPoint, spatialReferenceWGS);
        Geometry projected = OperatorProject.local().execute(multiPoint, projectionTransformation, null);

        Envelope2D envelope2D = new Envelope2D();
        projected.queryEnvelope2D(envelope2D);

        assertTrue(envelope2D.xmax != 40);

    }

    @Test
    public void testDateline() {
        String wktGeom = "POLYGON((-185 0, -185 10, -170 10, -170 0),(-182 3, -172 3, -172 7, -182 7))";
        Geometry original = OperatorImportFromWkt.local().execute(
                0,
                Geometry.Type.Unknown,
                wktGeom,
                null);
        Geometry projected = OperatorProject.local().execute(
                original,
                projectionTransformationToMerc, null);

        assertNotNull(projected);

        Geometry reProjected = OperatorProject.local().execute(projected, projectionTransformationToWGS, null);
        assertNotNull(reProjected);

        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        assertTrue(OperatorSimplify.local().isSimpleAsFeature(reProjected, spatialReferenceWGS, true, nonSimpleResult, null));

        assertEquals(original.calculateArea2D(), reProjected.calculateArea2D(), 0.00001);
    }

    @Test
    public void testWrapNotWGS84() {
        String wktGeom = "POLYGON((-185 0, -185 10, -170 10, -170 0),(-182 3, -172 3, -172 7, -182 7))";
        Geometry original = OperatorImportFromWkt.local().execute(0,Geometry.Type.Unknown, wktGeom,null);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(4269), spatialReferenceMerc);
        Geometry projected = OperatorProject.local().execute(
                original,
                projectionTransformation, null);

        assertNotNull(projected);

        Geometry reProjected = OperatorProject.local().execute(projected, projectionTransformation.getReverse(), null);
        assertNotNull(reProjected);

        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        assertTrue(OperatorSimplify.local().isSimpleAsFeature(reProjected, SpatialReference.create(4269), true, nonSimpleResult, null));

        assertEquals(original.calculateArea2D(), reProjected.calculateArea2D(), 0.00001);
    }
}