package com.esri.core.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EnclosingCircler {
    class Circle {
        final Point2D m_center;
        final double m_radius;
        Circle(Point2D center, double radius) {
            m_center = center;
            m_radius = radius;
        }

        public boolean contains(Point2D point2D) {
            return Point2D.distance(point2D, m_center) <= m_radius * (1 + m_tolerance / 2.0);
        }
    };

    Random m_random;

    // P is the set of all points to be processed.
    List<Integer> m_indices;

    // S is the set of processed points. that will be kept track of using an index into the indices stream
    int m_processedIndex = 0;

    // R is the set boundary points for the circle that encompasses all points in set R
    List<Point2D> m_boundaryPoints = new ArrayList<>();

//    Point2D m_circleCenter = new Point2D();
//    double m_radius = Double.MIN_VALUE;
    Circle m_circle = null;
    // Geometry to search
    MultiVertexGeometryImpl m_multiVertexGeometry;

    int m_circleCount = 96;
    SpatialReference m_spatialReference;
    ProgressTracker m_progressTracker;
    double m_tolerance = 1e-10;

    EnclosingCircler(Geometry geometry, SpatialReference spatialReference, ProgressTracker progressTracker) {
        m_multiVertexGeometry = (MultiVertexGeometryImpl) geometry._getImpl();
        m_spatialReference = spatialReference;
        m_progressTracker = progressTracker;
        if (m_spatialReference != null) {
            m_tolerance = m_spatialReference.getTolerance();
        }
        m_random = new Random(1977);
    }

    Geometry search() {

//        AttributeStreamOfDbl xyPositions = (AttributeStreamOfDbl) multiVertexGeometry.getAttributeStreamRef(0);

        m_indices = IntStream.range(0, m_multiVertexGeometry.getPointCount()).boxed().collect(Collectors.toList());
        Collections.shuffle(m_indices, m_random);

        // Place first two points in boundary list
        Point2D pt1 = __getShuffledPoint(m_processedIndex++);
        Point2D pt2 = __getShuffledPoint(m_processedIndex++);
        Point2D testCenter = new Point2D();
        testCenter.interpolate(pt1, pt2, .5);
        double radius = Point2D.distance(pt1, pt2) / 2.0;
        m_boundaryPoints.add(pt1);
        m_boundaryPoints.add(pt2);

        m_circle = new Circle(testCenter, radius);

        __updateCirlce();

        return __constructCircle();
    }

    Geometry __constructCircle() {
        Point point = new Point(m_circle.m_center);
        return OperatorBuffer.local().execute(point, m_spatialReference, m_circle.m_radius, m_progressTracker);
    }

    Point2D __getShuffledPoint(int index) {
        return m_multiVertexGeometry.getXY(m_indices.get(index));
    }

    void __updateCirlce() {
        // loop through all points in geometry
        Circle circle = new Circle(m_circle.m_center, m_circle.m_radius);
        while (m_processedIndex < m_multiVertexGeometry.getPointCount()) {
            Point2D testPoint = __getShuffledPoint(m_processedIndex++);
            if (!circle.contains(testPoint)) {
                // if the point is outside the current circle
                circle = __updateCircle(testPoint);
            }
        }

        m_circle = circle;
    }

    Circle __updateCircle(Point2D newBoundaryPoint) {
        // two point option
        Point2D testCenter = new Point2D();
        testCenter.interpolate(newBoundaryPoint, __getShuffledPoint(0), .5);
        Circle circle = new Circle(testCenter, Point2D.distance(testCenter, newBoundaryPoint));
        for (int i = 1; i < m_processedIndex; i++) {
            Point2D testPoint = __getShuffledPoint(i);
            if (!circle.contains(testPoint)) {
                circle = __updateCircle(newBoundaryPoint, testPoint);
            }
        }

        return circle;
    }

    Circle __updateCircle(Point2D newBoundaryPoint, Point2D testPoint) {
        Point2D testCenter = new Point2D();
        testCenter.interpolate(newBoundaryPoint, testPoint, .5);
        Circle circle = new Circle(testCenter, Point2D.distance(testCenter, newBoundaryPoint));

        // TODO, this probably only needs to be the set of bounday points from the previous search
        for (int i = 0; i < m_processedIndex; i++) {
            if (circle.contains(__getShuffledPoint(i))) {
                continue;
            }

            // create circle from three points
            testCenter = Point2D.calculateCircleCenterFromThreePoints(newBoundaryPoint, testPoint, __getShuffledPoint(i));
            circle = new Circle(testCenter, Point2D.distance(testCenter, newBoundaryPoint));
        }

        return circle;
    }




//    void __assembleBoundary() {
//        while (++m_processedIndex < m_multiVertexGeometry.getPointCount()) {
//            // get the next potential boundary point
//            Point2D testPoint = m_multiVertexGeometry.getXY(m_indices.get(m_processedIndex));
//            // calculate the distance from the current circle center to the above test point
//            double testRadius = Point2D.distance(testPoint, m_circleCenter);
//
////            // if current radius if only slightly larger than the above testRadius, add the point as a potential
////            // boundary point
////            if (m_radius - testRadius <= m_tolerance) {
////                m_boundaryPoints.add(testPoint);
//            if (testRadius < m_radius * (1 - m_tolerance / 2.0))
//                // point is inside circle
//                continue;
//            // if the testRadius is larger than the current radius, then the testPoint is outside the current circle
//            else if (testRadius > m_radius * (1 + m_tolerance / 2.0)) {
//                // search for the point on the current boundary that is the furthest from the testPoint
//                // current test distance
//                double testDistanceMax = 0;
//                // point furthest from the testPoint
//                Point2D maxOther = null;
//                // list of points that area on the boundary that are all closer to the testPoint than the maxOther point
////                List<Point2D> notMax = new ArrayList<>();
//                for (Integer testIndex : m_indices.subList(0, m_processedIndex - 1))
//                {
//                    Point2D processedPoint = m_multiVertexGeometry.getXY(testIndex);
//                    double processedDistance = Point2D.distance(m_circleCenter, processedPoint)
//
//                }
//                for (Point2D boundaryPt : m_boundaryPoints) {
//                    double testDistance = Point2D.distance(testPoint, boundaryPt);
//                    // TODO tolerance?
//                    // if the testDistance is greater than the current testDistanceMax, update the testDistanceMax and maxOther
//                    if (testDistance > testDistanceMax) {
//                        // if there has already been a maxOther defined, remove it by placing it in the notMax list
//                        if (maxOther != null)
//                            notMax.add(maxOther);
//
//                        // update the maxOther and distance
//                        maxOther = boundaryPt;
//                        testDistanceMax = testDistance;
//
//                    } else {
//                        // if the point is not the furthest from this newly discovered outside point, testPoint,
//                        // then place it in the list of points to be removed from the boundary
//                        notMax.add(boundaryPt);
//                    }
//                }
//                assert(maxOther != null);
//
//                // Now that we have a point that is furthest from the new boundary defining point, testPoint,
//                // we define the center between those two points
//                Point2D tempCenter = new Point2D();
//                tempCenter.interpolate(testPoint, maxOther, .5);
//
//
//                // check that not Max are within circle
//                final double tempRadius = testDistanceMax / 2.0;
//                // TODO there sould be some tolerance squigle in here.
//                List<Point2D> notInside = notMax
//                        .stream()
//                        .filter(pt -> Point2D.distance(tempCenter, pt) > tempRadius)
//                        .collect(Collectors.toList());
//
//                // remove all the old boundary points (some may get added back in if they're in notInside
//                //m_boundaryPoints.removeAll(notMax);
//                m_boundaryPoints.add(testPoint);
//                //assert(m_boundaryPoints.size() == 2);
////                m_circleCenter.interpolate(maxOther, testPoint, .5);
////                m_radius = Point2D.distance(m_circleCenter, testPoint);
//
//                // now we're looking for the larges triangle area
//                double maxTriangleArea = 0;
//                Point2D maxTriangleAreaPt = null;
//                for (Point2D trianglePt : notInside) {
//                    double tempArea = trianglePt.calculateTriangleArea2D(testPoint, maxOther);
//                    if (tempArea > maxTriangleArea) {
//                        maxTriangleAreaPt = trianglePt;
//                        maxTriangleArea = tempArea;
//                    }
//                }
//
//
//                double maxRadius = 0;
//                Point2D maxRadiusPt = null;
//                Point2D maxRadiusCenterPt = null;
//                for (Point2D ptNotInside : notInside) {
//                    Point2D tempCenterRadius = Point2D.calculateCircleCenterFromThreePoints(ptNotInside, testPoint, maxOther);
//                    double tempMaxRadius = Point2D.distance(ptNotInside, tempCenterRadius);
//                    if (tempMaxRadius > maxRadius) {
//                        maxRadius = tempMaxRadius;
//                        maxRadiusPt = ptNotInside;
//                        maxRadiusCenterPt = tempCenterRadius;
//                    }
//                }
//
////                assert(maxTriangleAreaPt.equals(maxRadiusPt));
//
//                if (maxRadiusPt != null && maxRadiusCenterPt != null && maxRadius > m_radius) {
//                    m_boundaryPoints.add(maxRadiusPt);
//                    m_circleCenter = maxRadiusCenterPt;
//                    m_radius = maxRadius;
//                }
//            }
//        }
//    }
}
