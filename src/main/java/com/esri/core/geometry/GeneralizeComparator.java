package com.esri.core.geometry;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by davidraleigh on 4/19/16.
 */
public class GeneralizeComparator implements Comparator<Integer> {//extends Treap.Comparator {
    class EditShapeTriangle {
        int m_prevVertexIndex;
        int m_nextVertexIndex;
        private Point2D m_point;
        private Point2D m_prevPoint;
        private Point2D m_nextPoint;
        int m_vertexIndex;
        private double m_area;
        private int m_orientation;

        EditShapeTriangle() {

        }

        EditShapeTriangle(EditShape editShape,
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

        int getIndex() {
            return m_vertexIndex;
        }

        double queryArea() {
            return m_area;
        }

        int queryOrientation() {
            return m_orientation;
        }

        void updateArea() {
            m_area = m_prevPoint.calculateTriangleArea2D(m_point, m_nextPoint);
        }

        void updateOrientation() {
            m_orientation = Point2D.orientationRobust(m_prevPoint, m_point, m_nextPoint);
        }
    }

    private EditShape m_editShape;
    private int m_currentNode;
    int m_vertex_1 = -1;
    int m_vertex_2 = -1;

    private GeneralizeAreaType m_generalizeAreaType;

    EditShapeTriangle m_temp_triangle_1 = null;
    EditShapeTriangle m_temp_triangle_2 = null;

    ArrayList<EditShapeTriangle> m_triangle_nodes_buffer;
    ArrayList<EditShapeTriangle> m_triangle_nodes_recycle;
    ArrayList<EditShapeTriangle> m_triangle_nodes_cache;

    GeneralizeComparator(EditShape editShape, GeneralizeAreaType generalizeAreaType) {
        // TODO
        //super(true);
        m_editShape = editShape;

        m_generalizeAreaType = generalizeAreaType;

        m_triangle_nodes_buffer = new ArrayList<EditShapeTriangle>();
        m_triangle_nodes_recycle = new ArrayList<EditShapeTriangle>();
        m_triangle_nodes_cache = new ArrayList<EditShapeTriangle>();

        m_temp_triangle_1 = new EditShapeTriangle();
        m_temp_triangle_2 = new EditShapeTriangle();

        int s = Math.min(editShape.getTotalPointCount() * 3 / 2, (int) (67 /* SIMPLEDGE_CACHESIZE */));
        int cache_size = Math.min((int) 7, s);

        // TODO is this necessary or would a reserve call work?
        for (int i = 0; i < cache_size; i++) {
            m_triangle_nodes_cache.add(null);
        }
    }

    EditShapeTriangle createTriangle(int value) {
        EditShapeTriangle triangle = new EditShapeTriangle(m_editShape, value);
        return triangle;
    }

    // Returns a cached edge for the given value. May return NULL.
    EditShapeTriangle tryGetCachedTriangle_(int value) {
        EditShapeTriangle triangle = m_triangle_nodes_cache.get((value & NumberUtils.intMax()) % m_triangle_nodes_cache.size());
        if (triangle != null) {
            if (triangle.m_vertexIndex == value)
                return triangle;
            else {
                // int i = 0;
                // cache collision
            }
        }
        return null;
    }

    // Removes cached edge from the cache for the given value.
    void tryDeleteCachedTriangle_(int value) {
        int ind = (value & NumberUtils.intMax()) % m_triangle_nodes_cache.size();
        EditShapeTriangle se = m_triangle_nodes_cache.get(ind);
        if (se != null && se.m_vertexIndex == value) {// this value is cached
            m_triangle_nodes_recycle.add(se);
            m_triangle_nodes_cache.set(ind, null);
        } else {
            // The value has not been cached
        }
    }

    EditShapeTriangle tryCreateCachedTriangle_(int value) {
        int ind = (value & NumberUtils.intMax()) % m_triangle_nodes_cache.size();
        EditShapeTriangle triangle = m_triangle_nodes_cache.get(ind);
        if (triangle == null) {
            if (m_triangle_nodes_recycle.isEmpty()) {
                m_triangle_nodes_buffer.add(new EditShapeTriangle(m_editShape, value));
                triangle = m_triangle_nodes_buffer.get(m_triangle_nodes_buffer.size() - 1);
            } else {
                triangle = m_triangle_nodes_recycle.get(m_triangle_nodes_recycle.size() - 1);
                m_triangle_nodes_recycle.remove(m_triangle_nodes_recycle.size() - 1);
                triangle.setTriangle(m_editShape, value);
            }

            m_triangle_nodes_cache.set(ind, triangle);
            return triangle;
        } else {
            assert(triangle.getIndex() != value);
        }

        return null;
    }

    @Override
    public int compare(Integer left, Integer right) {//(Treap treap, int left, int node) {
//        int right = treap.getElement(node);
//        m_currentNode = node;

        return compareTriangles(left, left, right, right);
    }

    int compareTriangles(int leftElm, int left_vertex, int right_elm, int right_vertex) {
        EditShapeTriangle triangleLeft = tryGetCachedTriangle_(leftElm);
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

        EditShapeTriangle triangleRight = tryGetCachedTriangle_(right_elm);
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

//        @Override
//        void onDelete(int elm) {
////            EditShapeTriangle triangle = tryGetCachedTriangle_(elm);
////            if (triangle == null) {
////                triangle = tryCreateCachedTriangle_(elm);
////            }
////
////            int prevVertexIndex = triangle.m_prevVertexIndex;
////            int nextVertexIndex = triangle.m_nextVertexIndex;
//
//            tryDeleteCachedTriangle_(elm);
//
////            EditShapeTriangle trianglePrev = tryGetCachedTriangle_(prevVertexIndex);
////            if (trianglePrev == null) {
////                trianglePrev = tryCreateCachedTriangle_(prevVertexIndex);
////            }
////            EditShapeTriangle triangleNext = tryGetCachedTriangle_(nextVertexIndex);
////            if (triangleNext == null) {
////                triangleNext = tryCreateCachedTriangle_(nextVertexIndex);
////            }
//        }
//
//        @Override
//        void onSet(int oldelm) {
//            tryDeleteCachedTriangle_(oldelm);
//        }
//
//        @Override
//        void onEndSearch(int elm) {
//            tryDeleteCachedTriangle_(elm);
//        }
//
//        @Override
//        void onAddUniqueElementFailed(int elm) {
//            tryDeleteCachedTriangle_(elm);
//        }

    int compare(EditShapeTriangle tri1, EditShapeTriangle tri2) {

        if (m_generalizeAreaType != GeneralizeAreaType.Neither) {
            // 1 for obtuse angle counter-clockwise,
            // -1 for obtuse angle clockwise
            // 0 for collinear
            int orientation1 = tri1.queryOrientation();
            int orientation2 = tri2.queryOrientation();

            if (m_generalizeAreaType == GeneralizeAreaType.ResultContainsOriginal) {
                // if the result contains the original no vertices with a
                // counter clockwise obtuse angle rotation (1) can be removed
                if (orientation1 > 0 && orientation2 > 0) {
                    return 0;
                } else if (orientation1 < 0 && orientation2 > 0) {
                    return -1;
                } else if (orientation2 < 0 && orientation1 > 1) {
                    return 1;
                }
            } else if (m_generalizeAreaType == GeneralizeAreaType.ResultWithinOriginal) {
                if (orientation1 < 0 && orientation2 < 0) {
                    return 0;
                } else if (orientation1 < 0 && orientation2 > 0) {
                    return 1;
                } else if (orientation2 < 0 && orientation1 > 1) {
                    return -1;
                }
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