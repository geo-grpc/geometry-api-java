package com.esri.core.geometry;

import java.awt.geom.Area;
import java.util.ArrayList;
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
    double m_minArea;

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
        m_minArea = 10;
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
        geom.copyTo(dstmp);
        EditShape editShape = new EditShape();
        editShape.addGeometry(dstmp);

        GeneralizeAreaPath(editShape);
//        for (int ipath = 0, npath = mp.getPathCount(); ipath < npath; ipath++) {
// GeneralizeAreaPath(dstmp);
//        }

        return dstmp;
    }


    class TriangleNode {
        public int m_prevVertexIndex;
        public int m_nextVertexIndex;
        private Point2D m_point;
        private Point2D m_prevPoint;
        private Point2D m_nextPoint;
        public int m_vertexIndex;
        private double m_area;
        private int m_orientation;

        public TriangleNode() {

        }

        public TriangleNode(EditShape editShape,
                            int iVertex) {
            setTriangle(editShape, iVertex);
        }


        void setTriangle(EditShape editShape, int iVertex) {
            int prevVertex = editShape.getPrevVertex(iVertex);
            int nextVertex = editShape.getNextVertex(iVertex);
            Point2D prevPoint = editShape.getXY(prevVertex);
            Point2D currentPoint = editShape.getXY(iVertex);
            Point2D nextPoint = editShape.getXY(nextVertex);

            m_point = currentPoint;
            m_prevPoint = prevPoint;
            m_nextPoint = nextPoint;
            m_vertexIndex = iVertex;
            m_prevVertexIndex = prevVertex;
            m_nextVertexIndex = nextVertex;
            updateArea();
            updateOrientation();
        }

        public int getIndex() {
            return m_vertexIndex;
        }

        public double queryArea() {
            return m_area;
        }

        public int queryOrientation() {
            return m_orientation;
        }

        public void updateArea() {
            m_area = m_prevPoint.calculateTriangleArea2D(m_point, m_nextPoint);
        }

        public void updateOrientation() {
            m_orientation = Point2D.orientationRobust(m_prevPoint, m_point, m_nextPoint);
        }
    }

    class AreaComparator extends Treap.Comparator {

        private EditShape m_editShape;
        private int m_currentNode;
        int m_vertex_1 = -1;
        int m_vertex_2 = -1;
        TriangleNode m_temp_triangle_1 = null;
        TriangleNode m_temp_triangle_2 = null;

        ArrayList<TriangleNode> m_triangle_nodes_buffer;
        ArrayList<TriangleNode> m_triangle_nodes_recycle;
        ArrayList<TriangleNode> m_triangle_nodes_cache;

        AreaComparator(EditShape editShape) {
            super(true);
            m_editShape = editShape;

            m_triangle_nodes_buffer = new ArrayList<TriangleNode>();
            m_triangle_nodes_recycle = new ArrayList<TriangleNode>();
            m_triangle_nodes_cache = new ArrayList<TriangleNode>();

            m_temp_triangle_1 = new TriangleNode();
            m_temp_triangle_2 = new TriangleNode();

            int s = Math.min(editShape.getTotalPointCount() * 3 / 2, (int) (67 /* SIMPLEDGE_CACHESIZE */));
            int cache_size = Math.min((int) 7, s);

            // TODO is this necessary or would a reserve call work?
            for (int i = 0; i < cache_size; i++) {
                m_triangle_nodes_cache.add(null);
            }
        }

        // Returns a cached edge for the given value. May return NULL.
        public TriangleNode tryGetCachedTriangle_(int value) {
            TriangleNode tn = m_triangle_nodes_cache.get((value & NumberUtils.intMax()) % m_triangle_nodes_cache.size());
            if (tn != null) {
                if (tn.m_vertexIndex == value)
                    return tn;
                else {
                    // int i = 0;
                    // cache collision
                }
            }
            return null;
        }

        // Removes cached edge from the cache for the given value.
        public void tryDeleteCachedTriangle_(int value) {
            int ind = (value & NumberUtils.intMax()) % m_triangle_nodes_cache.size();
            TriangleNode se = m_triangle_nodes_cache.get(ind);
            if (se != null && se.m_vertexIndex == value) {// this value is cached
                m_triangle_nodes_recycle.add(se);
                m_triangle_nodes_cache.set(ind, null);
            } else {
                // The value has not been cached
            }
        }

        public TriangleNode tryCreateCachedTriangle_(int value) {
            int ind = (value & NumberUtils.intMax()) % m_triangle_nodes_cache.size();
            TriangleNode tn = m_triangle_nodes_cache.get(ind);
            if (tn == null) {
                if (m_triangle_nodes_recycle.isEmpty()) {
                    m_triangle_nodes_buffer.add(new TriangleNode(m_editShape, value));
                    tn = m_triangle_nodes_buffer.get(m_triangle_nodes_buffer.size() - 1);
                } else {
                    tn = m_triangle_nodes_recycle.get(m_triangle_nodes_recycle.size() - 1);
                    m_triangle_nodes_recycle.remove(m_triangle_nodes_recycle.size() - 1);
                    tn.setTriangle(m_editShape, value);
                }
                m_triangle_nodes_cache.set(ind, tn);
                return tn;
            } else {
                assert(tn.getIndex() != value);
            }

            return null;
        }

        @Override
        public int compare(Treap treap, int left, int node) {
            int right = treap.getElement(node);
            m_currentNode = node;

            return compareTriangles(left, left, right, right);
        }

        int compareTriangles(int leftElm, int left_vertex, int right_elm, int right_vertex) {
            TriangleNode triangleLeft = tryGetCachedTriangle_(leftElm);
            if (triangleLeft == null) {
                if (m_vertex_1 == left_vertex) {
                    triangleLeft = m_temp_triangle_1;
                } else {
                    m_vertex_1 = left_vertex;
                    triangleLeft = tryCreateCachedTriangle_(leftElm);
                    if (triangleLeft == null) {
                        triangleLeft = m_temp_triangle_1;
                        m_temp_triangle_1.setTriangle(m_editShape, leftElm);
                    }

                }
            } else {
                m_vertex_1 = left_vertex;
            }

            TriangleNode triangleRight = tryGetCachedTriangle_(right_elm);
            if (triangleRight == null) {
                if (m_vertex_2 == right_vertex) {
                    triangleRight = m_temp_triangle_2;
                } else {
                    m_vertex_2 = right_vertex;
                    triangleRight = tryCreateCachedTriangle_(right_elm);
                    if (triangleRight == null) {
                        triangleRight = m_temp_triangle_2;
                        m_temp_triangle_2.setTriangle(m_editShape, right_elm);
                    }
                }
            } else {
                m_vertex_2 = right_vertex;
            }

            return compare(triangleLeft, triangleRight);
        }

        @Override
        void onDelete(int elm) {
//            TriangleNode tn = tryGetCachedTriangle_(elm);
//            if (tn == null) {
//                tn = tryCreateCachedTriangle_(elm);
//            }
//
//            int prevVertexIndex = tn.m_prevVertexIndex;
//            int nextVertexIndex = tn.m_nextVertexIndex;

            tryDeleteCachedTriangle_(elm);

//            TriangleNode tnPrev = tryGetCachedTriangle_(prevVertexIndex);
//            if (tnPrev == null) {
//                tnPrev = tryCreateCachedTriangle_(prevVertexIndex);
//            }
//            TriangleNode tnNext = tryGetCachedTriangle_(nextVertexIndex);
//            if (tnNext == null) {
//                tnNext = tryCreateCachedTriangle_(nextVertexIndex);
//            }
        }

        @Override
        void onSet(int oldelm) {
            tryDeleteCachedTriangle_(oldelm);
        }

        @Override
        void onEndSearch(int elm) {
            tryDeleteCachedTriangle_(elm);
        }

        @Override
        void onAddUniqueElementFailed(int elm) {
            tryDeleteCachedTriangle_(elm);
        }

        public int compare(TriangleNode tri1, TriangleNode tri2) {
            int orientation1 = tri1.queryOrientation();
            int orientation2 = tri2.queryOrientation();

            if (m_generalizeAreaType == GeneralizeAreaType.ResultContainsOriginal) {
                if (orientation1 > 0 && orientation2 > 0) {
                    // calculate size only if both orientations are correct
                } else if (orientation1 < 0 && orientation2 < 0) {
                    return 0;
                } else if (orientation1 < 0) {
                    return -1;
                } else if (orientation2 < 0) {
                    return 1;
                }
            } else if (m_generalizeAreaType == GeneralizeAreaType.ResultWithinOriginal) {
                if (orientation1 < 0 && orientation2 < 0) {
                    // calculate size only if both orientations are correct
                } else if (orientation1 > 0 && orientation2 > 0) {
                    return 0;
                } else if (orientation1 > 0) {
                    return -1;
                } else if (orientation2 > 0) {
                    return 1;
                }
            }

            double area1 = tri1.queryArea();
            double area2 = tri2.queryArea();

            if (area1 < area2) {
                return -1;
            } else if (area2 < area1) {
                return 1;
            }
            return 0;
        }
    }



    private void GeneralizeAreaPath(EditShape editShape) {

        Treap treap = new Treap();
        treap.disableBalancing();
        AreaComparator areaComparator = new AreaComparator(editShape);
        treap.setComparator(areaComparator);
        

        int[] arr = new int[8];
        for (int iGeometry = editShape.getFirstGeometry(); iGeometry != -1; iGeometry = editShape.getNextGeometry(iGeometry)) {
            treap.setCapacity(editShape.getPointCount(iGeometry));
            for (int iPath = editShape.getFirstPath(iGeometry); iPath != -1; iPath = editShape.getNextPath(iPath)) {
                for (int iVertex = editShape.getFirstVertex(iPath), i = 0, n = editShape.getPathSize(iPath); i < n; iVertex = editShape.getNextVertex(iVertex), i++) {
                    treap.addElement(iVertex, -1);
                    arr[i]  = iVertex;
                }

                double testArea = 0.0;
                while (testArea < m_minArea) {
                    int nodeIndex = treap.getFirst(-1);
                    int element = treap.getElement(nodeIndex);
                    // TODO no idea on this boolean

                    TriangleNode tn = areaComparator.tryGetCachedTriangle_(element);
                    if (tn == null) {
                        tn = areaComparator.tryCreateCachedTriangle_(element);
                    }

                    double area = tn.queryArea();
                    if (area > m_minArea)
                        break;

                    editShape.removeVertex(element, false);
                    treap.deleteNode(nodeIndex, -1);

                    int prevElement = tn.m_prevVertexIndex;
                    int nextElement = tn.m_nextVertexIndex;

                    int prevNodeIndex = treap.search(prevElement, -1);
                    int nextNodeIndex = treap.search(nextElement, -1);
                    prevElement = treap.getElement(prevNodeIndex);
                    nextElement = treap.getElement(nextNodeIndex);

                    treap.deleteNode(prevNodeIndex, -1);
                    treap.deleteNode(nextNodeIndex, -1);

                    treap.addElement(prevElement, -1);
                    treap.addElement(nextElement, -1);
                }
            }
        }




//        while (true) {
//            mpsrc.queryCoordinates(triangle, 3, startIndex, startIndex + 3);
//            priorityQueue.add(triangle);
//        }

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
