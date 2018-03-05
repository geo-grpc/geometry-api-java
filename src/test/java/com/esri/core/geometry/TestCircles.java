package com.esri.core.geometry;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.HashMap;

public class TestCircles extends TestCase {
    @Test
    public void testSquare() {
        Polygon polygon = new Polygon();
        polygon.startPath(-2, -2);
        polygon.lineTo(-2, 2);
        polygon.lineTo(2, 2);
        polygon.lineTo(2, -2);
        polygon.closeAllPaths();
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(polygon), null, null);
        Geometry geometry = operatorEnclosingCircleCursor.next();
        double radius = Math.sqrt(2*2 + 2*2);
        double area = Math.PI * radius * radius;
        assertEquals(geometry.calculateArea2D(), area, 1e-1);
    }

    @Test
    public void testBuffered() {
        Point center = new Point(0,0);
        Geometry buffered = GeometryEngine.buffer(center, SpatialReference.create(4326), 20);
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(buffered), null, null);
        Geometry geometry = operatorEnclosingCircleCursor.next();
        assertEquals(geometry.calculateArea2D(), buffered.calculateArea2D(), 1e-10);
        assertTrue(GeometryEngine.equals(geometry, buffered, null));
    }

    @Test
    public void testGeodesicBuffer() {
        Point center = new Point(0,0);
        Geometry buffered = OperatorGeodesicBuffer.local().execute(center,
                SpatialReference.create(4326),
                GeodeticCurveType.Geodesic,
                4000,
                20,
                false,
                null);

        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(
                new SimpleGeometryCursor(buffered),
                SpatialReference.create(4326),
                null);

        Geometry geometry = operatorEnclosingCircleCursor.next();
        // This tests fails without multiVertexGeometry._setDirtyFlag(DirtyFlags.dirtyAll, true); in project
        assertEquals(geometry.calculateArea2D(), buffered.calculateArea2D(), 1e-4);

        Geometry bufferedContainer = OperatorGeodesicBuffer.local().execute(center,
                SpatialReference.create(4326),
                GeodeticCurveType.Geodesic,
                4050,
                20,
                false,
                null);
        assertTrue(GeometryEngine.contains(bufferedContainer, geometry, SpatialReference.create(4326)));
    }

    @Test
    public void testBufferedClipped() {
        Point center = new Point(0,0);
        Geometry buffered = GeometryEngine.buffer(center, SpatialReference.create(4326), 20);
        Geometry clipped = GeometryEngine.clip(buffered, new Envelope(0, -20, 40, 40), null);
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(clipped), null, null);
        Geometry geometry = operatorEnclosingCircleCursor.next();
        assertEquals(geometry.calculateArea2D(), buffered.calculateArea2D(), 1e-10);
        assertTrue(GeometryEngine.equals(geometry, buffered, null));
    }

    @Test
    public void testBufferedClippedUnionedSmall() {
        Point center = new Point(0,0);
        Geometry buffered = GeometryEngine.buffer(center, SpatialReference.create(4326), 20);
        Geometry clipped = GeometryEngine.clip(buffered, new Envelope(0, -20, 40, 40), null);
        Geometry bufferedSmall = GeometryEngine.buffer(center, SpatialReference.create(4326), 10);
        Geometry[] two = new Geometry[] {bufferedSmall, clipped};
        Geometry unionedGeom = GeometryEngine.union(two, null);
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(unionedGeom), null, null);
        Geometry geometry = operatorEnclosingCircleCursor.next();
        assertEquals(geometry.calculateArea2D(), buffered.calculateArea2D(), 1e-10);
        assertTrue(GeometryEngine.equals(geometry, buffered, null));
    }


    @Test
    public void testRandomPoints() {
        int count = 400;
        Envelope e = new Envelope(0,0,40, 40);
        RandomCoordinateGenerator randomCoordinateGenerator = new RandomCoordinateGenerator(count, e, SpatialReference.create(4326).getTolerance());
        MultiPoint multiPoint = new MultiPoint();
        for (int i = 0; i < count; i++) {
            multiPoint.add(randomCoordinateGenerator._GenerateNewPoint());
        }

        int run_count = 10;
        ArrayDeque<Geometry> geometries = new ArrayDeque<>();
        for (int i = 0; i < run_count; i++) {
            OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(multiPoint), SpatialReference.create(4326), null);
            Geometry geometry = operatorEnclosingCircleCursor.next();
            geometries.add(geometry);
        }


//        http://opensourceconnections.com/blog/2014/04/11/indexing-polygons-in-lucene-with-accuracy/
//        http://my-spatial4j-project.blogspot.be/2014/01/minimum-bounding-circle-algorithm-jts.html
        Geometry firstGeometry = geometries.peekFirst();
        OperatorEquals operatorEquals = (OperatorEquals) (OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Equals));
        HashMap<Integer, Boolean> results = operatorEquals.execute(firstGeometry, new SimpleGeometryCursor(geometries), SpatialReference.create(4326), null);
        for (Integer key : results.keySet()) {
            assertTrue(results.get(key));
        }
    }

    @Test
    public void testReproject() {
        String wktGeom = "MULTIPOLYGON (((-70.64802717477117 43.128706076602874, -70.6479465414909 43.128714649179535, -70.64788357509747 43.1287487128581, -70.64783854531692 43.12880812176545, -70.64781164513992 43.12889262152996, -70.64780298999258 43.12900185036985, -70.64781261723796 43.12913534064154, -70.6478404860113 43.12929252084091, -70.64788647738838 43.129472718047886, -70.64795039488814 43.129675160806016, -70.6480319653057 43.129898982422475, -70.64813083987421 43.13014322467642, -70.64824659574889 43.13040684191825, -70.64837873780841 43.13068870554328, -70.64852670076544 43.1309876088204, -70.64868985157774 43.131302272055365, -70.64886749214907 43.13163134806583, -70.64905886230987 43.13197342794668, -70.64926314306291 43.132327047097704, -70.64947946008277 43.13269069149233, -70.64970688745207 43.13306280415637, -70.64994445161977 43.13344179183187, -70.65019113556465 43.133826031796644, -70.65044588314564 43.134213878809994, -70.6507076036211 43.13460367215594, -70.65097517631722 43.134993742752336, -70.6512474554263 43.135382420297674, -70.65152327491325 43.135768040422036, -70.65180145351054 43.13614895181459, -70.65208079977953 43.13652352329523, -70.652360117216 43.13689015080003, -70.65263820937935 43.1372472642522, -70.65291388502193 43.137593334287445, -70.65318596319706 43.13792687880496, -70.65345327832425 43.138246469317686, -70.65371468518873 43.138550737072336, -70.65396906385456 43.138838378914386, -70.65421532446969 43.139108162872276, -70.65445241194281 43.139358933437414, -70.65467931047083 43.13958961651619, -70.6548950478984 43.13979922403416, -70.65509869988989 43.1399868581709, -70.65528939389672 43.14015171520967, -70.65546631290157 43.14029308898237, -70.65562869892466 43.140410373897815, -70.65577585627611 43.140503067538305, -70.65590715454054 43.14057077281427, -70.65602203128121 43.14061319966741, -70.65611999445184 43.14063016631512, -70.65620062450573 43.14062160003068, -70.65626357619321 43.14058753745568, -70.65630858004002 43.14052812444448, -70.65633544349907 43.14044361543887, -70.65634405177256 43.1403343723787, -70.6563343682994 43.14020086315083, -70.6563064349065 43.14004365958389, -70.65626037162373 43.13986343499769, -70.65619637616243 43.13966096131707, -70.65611472306145 43.13943710576406, -70.65601576250236 43.139192827140086, -70.65589991880132 43.13892917171714, -70.65576768858229 43.13864726875311, -70.65561963864108 43.13834832565221, -70.65545640350835 43.13803362279019, -70.65527868272287 43.13770450802718, -70.65508723782693 43.13736239093172, -70.65488288909637 43.137008736740526, -70.65466651201972 43.13664506007974, -70.65443903354196 43.136272918475676, -70.6542014280886 43.13589390568142, -70.65395471338735 43.13550964484906, -70.65369994610545 43.13512178157618, -70.65343821732164 43.13473197685713, -70.65317064785184 43.13434189996841, -70.65289838344866 43.1339532213195, -70.6526225898955 43.133567605299014, -70.65234444801612 43.13318670314755, -70.652065148621 43.13281214588721, -70.65178588741227 43.132445537338526, -70.6515078598685 43.13208844725394, -70.65123225613159 43.13174240459792, -70.65096025591802 43.13140889100291, -70.65069302347425 43.131089334426754, -70.65043170260032 43.13078510304208, -70.65017741176095 43.130497499381256, -70.64993123930526 43.130227754762316, -70.64969423881631 43.12997702402096, -70.64946742460877 43.12974638056925, -70.64925176739531 43.12953681180378, -70.64904819013898 43.12934921488169, -70.64885756410969 43.12918439288311, -70.64868070516164 43.12904305137656, -70.64851837024698 43.128925795401116, -70.64837125418116 43.12883312687935, -70.64823998667316 43.12876544247107, -70.64812512963337 43.128723031877364, -70.64802717477117 43.128706076602874)))";
        Geometry geometry = OperatorImportFromWkt.local().execute(0, Geometry.Type.Unknown, wktGeom, null);
        ProjectionTransformation projectionTransformation = ProjectionTransformation.getEqualArea(geometry, SpatialReference.create(4326));
        Geometry projected = OperatorProject.local().execute(geometry, projectionTransformation, null);
        Envelope envelope = new Envelope();
        projected.queryEnvelope(envelope);
        Point2D centerXY = envelope.getCenter2D();

        double minRadius = Double.MAX_VALUE;
        double maxRadius = Double.MIN_VALUE;
        for (Point2D point2D : ((MultiVertexGeometry)geometry).getCoordinates2D()) {
            double radius = Point2D.distance(point2D, centerXY);
            if (radius > maxRadius) {
                maxRadius = radius;
            }
            if (radius < minRadius) {
                minRadius = radius;
            }
        }
        assertEquals(minRadius, maxRadius, 0.01);
    }
}
