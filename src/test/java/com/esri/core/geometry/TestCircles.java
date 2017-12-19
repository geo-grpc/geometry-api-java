package com.esri.core.geometry;

import com.sun.org.apache.xpath.internal.operations.Bool;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        List<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < run_count; i++) {
            OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(multiPoint), SpatialReference.create(4326), null);
            Geometry geometry = operatorEnclosingCircleCursor.next();
            geometries.add(geometry);
        }


        Geometry firstGeometry = geometries.get(0);
        OperatorEquals operatorEquals = (OperatorEquals) (OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Equals));
        HashMap<Integer, Boolean> results = operatorEquals.execute(firstGeometry, new SimpleGeometryCursor(geometries), SpatialReference.create(4326), null);
        for (Integer key : results.keySet()) {
            assertTrue(results.get(key));
        }
    }
}
