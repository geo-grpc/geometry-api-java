package com.esri.core.geometry;

import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * Created by davidraleigh on 4/17/16.
 */
public class OperatorGeneralizeAreaCursor extends GeometryCursor {
    ProgressTracker m_progressTracker;
    GeometryCursor m_geoms;
    double m_maxDeviation;
    boolean m_bRemoveDegenerateParts;
    GeneralizeAreaType m_generalizeAreaType;

    public OperatorGeneralizeAreaCursor(GeometryCursor geoms,
                                        double maxDeviation,
                                        boolean bRemoveDegenerateParts,
                                        GeneralizeAreaType generalizeAreaType,
                                        ProgressTracker progressTracker) {
        m_geoms = geoms;
        m_maxDeviation = maxDeviation;
        m_progressTracker = progressTracker;
        m_bRemoveDegenerateParts = bRemoveDegenerateParts;
        m_generalizeAreaType = generalizeAreaType;
    }

    @Override
    public Geometry next() {
        Geometry geom = m_geoms.next();
        if (geom == null)
            return null;
        return GeneralizeArea(geom);
    }

    @Override
    public int getGeometryID() {
        return m_geoms.getGeometryID();
    }

    private Geometry GeneralizeArea(Geometry geom) {
        Geometry.Type gt = geom.getType();

        if (Geometry.isPoint(gt.value()))
            return geom;

        if (gt == Geometry.Type.Envelope) {
            Polygon poly = new Polygon(geom.getDescription());
            poly.addEnvelope((Envelope) geom, false);
            return GeneralizeArea(poly);
        }

        if (geom.isEmpty())
            return geom;

        MultiPath mp = (MultiPath) geom;
        MultiPath dstmp = (MultiPath) geom.createInstance();
        Line line = new Line();

        for (int ipath = 0, npath = mp.getPathCount(); ipath < npath; ipath++) {
            GeneralizeAreaPath((MultiPathImpl) mp._getImpl(),
                                ipath,
                                (MultiPathImpl) dstmp._getImpl(),
                                line);
        }

        return dstmp;
    }

    private class AreaComparator implements Comparator<Point2D[]> {
        public int compare(Point2D[] x, Point2D[] y) {
            if (m_generalizeAreaType == GeneralizeAreaType.Neither) {

            } else if (m_generalizeAreaType == GeneralizeAreaType.ResultContainsOriginal) {

            } else if (m_generalizeAreaType == GeneralizeAreaType.ResultWithinOriginal) {

            }
        }
    }

    private void GeneralizeAreaPath(MultiPathImpl mpsrc,
                                    int ipath,
                                    MultiPathImpl mpdst,
                                    Line lineHelper) {
        PriorityQueue<Point2D[]> priorityQueue = new PriorityQueue<Point2D[]>(mpsrc.getPathSize(ipath), new AreaComparator());

//        var heap = minHeap(),
//                maxArea = 0,
//                triangle;
//
//        feature.coordinates = feature.coordinates.map(function(polygon) {
//            return polygon.map(function(lineString) {
//                var points = lineString.map(projection),
//                triangles = [];
//
//                for (var i = 1, n = lineString.length - 1; i < n; ++i) {
//                    triangle = points.slice(i - 1, i + 2);
//                    if (triangle[1][2] = area(triangle)) {
//                        triangles.push(triangle);
//                        heap.push(triangle);
//                    }
//                }
//
//                for (var i = 0, n = triangles.length; i < n; ++i) {
//                    triangle = triangles[i];
//                    triangle.previous = triangles[i - 1];
//                    triangle.next = triangles[i + 1];
//                }
//
//                return points;
//            });
//        });
//
//        while (triangle = heap.pop()) {
//
//            // If the area of the current point is less than that of the previous point
//            // to be eliminated, use the latter’s area instead. This ensures that the
//            // current point cannot be eliminated without eliminating previously-
//            // eliminated points.
//            if (triangle[1][2] < maxArea) triangle[1][2] = maxArea;
//            else maxArea = triangle[1][2];
//
//            if (triangle.previous) {
//                triangle.previous.next = triangle.next;
//                triangle.previous[2] = triangle[2];
//                update(triangle.previous);
//            } else {
//                triangle[0][2] = triangle[1][2];
//            }
//
//            if (triangle.next) {
//                triangle.next.previous = triangle.previous;
//                triangle.next[0] = triangle[0];
//                update(triangle.next);
//            } else {
//                triangle[2][2] = triangle[1][2];
//            }
//        }
//
//        function update(triangle) {
//                heap.remove(triangle);
//        triangle[1][2] = area(triangle);
//        heap.push(triangle);
//        }
//
//        return feature;
//        }
//
//        simplify.projection = function(_) {
//            if (!arguments.length) return projection;
//            projection = _;
//            return simplify;
//        };
//
//        return simplify;
//    };
//
//    function compare(a, b) {
//        return a[1][2] - b[1][2];
//    }
//
//    function area(t) {
//        return Math.abs((t[0][0] - t[2][0]) * (t[1][1] - t[0][1]) - (t[0][0] - t[1][0]) * (t[2][1] - t[0][1]));
//    }
//
//    function minHeap() {
//        var heap = {},
//        array = [];
//
//        heap.push = function() {
//            for (var i = 0, n = arguments.length; i < n; ++i) {
//                var object = arguments[i];
//                up(object.index = array.push(object) - 1);
//            }
//            return array.length;
//        };
//
//        heap.pop = function() {
//            var removed = array[0],
//                    object = array.pop();
//            if (array.length) {
//                array[object.index = 0] = object;
//                down(0);
//            }
//            return removed;
//        };
//
//        heap.remove = function(removed) {
//            var i = removed.index,
//                    object = array.pop();
//            if (i !== array.length) {
//                array[object.index = i] = object;
//                (compare(object, removed) < 0 ? up : down)(i);
//            }
//            return i;
//        };
//
//        function up(i) {
//                var object = array[i];
//        while (i > 0) {
//            var up = ((i + 1) >> 1) - 1,
//                    parent = array[up];
//            if (compare(object, parent) >= 0) break;
//            array[parent.index = i] = parent;
//            array[object.index = i = up] = object;
//        }
//        }
//
//        function down(i) {
//                var object = array[i];
//        while (true) {
//            var right = (i + 1) << 1,
//                    left = right - 1,
//                    down = i,
//                    child = array[down];
//            if (left < array.length && compare(array[left], child) < 0) child = array[down = left];
//            if (right < array.length && compare(array[right], child) < 0) child = array[down = right];
//            if (down === i) break;
//            array[child.index = i] = child;
//            array[object.index = i = down] = object;
//        }
//        }
//
//        return heap;
//    }

    }
}
