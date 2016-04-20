package com.esri.core.geometry;

import java.util.PriorityQueue;


/**
 * Created by davidraleigh on 4/17/16.
 */
public class OperatorGeneralizeAreaCursor extends GeometryCursor {
    ProgressTracker m_progressTracker;
    GeometryCursor m_geoms;
    boolean m_bRemoveDegenerateParts;
    GeneralizeAreaType m_generalizeAreaType;
    double m_minArea;

    public OperatorGeneralizeAreaCursor(GeometryCursor geoms,
                                        double areaThreshold,
                                        boolean bRemoveDegenerateParts,
                                        GeneralizeAreaType generalizeAreaType,
                                        ProgressTracker progressTracker) {
        m_geoms = geoms;
        m_progressTracker = progressTracker;
        m_bRemoveDegenerateParts = bRemoveDegenerateParts;
        m_generalizeAreaType = generalizeAreaType;
        m_minArea = areaThreshold;
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

        EditShape editShape = new EditShape();
        editShape.addGeometry(geom);

        GeneralizeAreaPath(editShape);

        return editShape.getGeometry(editShape.getFirstGeometry());
    }


    private void GeneralizeAreaPath(EditShape editShape) {

//        Treap treap = new Treap();
//        treap.disableBalancing();
        GeneralizeComparator areaComparator = new GeneralizeComparator(editShape, m_generalizeAreaType);
//        treap.setComparator(areaComparator);

        PriorityQueue<Integer> priorityQueue = new PriorityQueue<Integer>(8, areaComparator);
        for (int iGeometry = editShape.getFirstGeometry(); iGeometry != -1; iGeometry = editShape.getNextGeometry(iGeometry)) {
            //treap.setCapacity(editShape.getPointCount(iGeometry));
            for (int iPath = editShape.getFirstPath(iGeometry); iPath != -1; iPath = editShape.getNextPath(iPath)) {
                for (int iVertex = editShape.getFirstVertex(iPath), i = 0, n = editShape.getPathSize(iPath); i < n; iVertex = editShape.getNextVertex(iVertex), i++) {
                    priorityQueue.add(iVertex);
                    //treap.addElement(iVertex, -1);
                }

                double testArea = 0.0;
                while (testArea < m_minArea) {

                    Integer element = priorityQueue.peek();
//                    int nodeIndex = treap.getFirst(-1);
//                    int element = treap.getElement(nodeIndex);


                    GeneralizeComparator.EditShapeTriangle triangle = areaComparator.tryGetCachedTriangle_(element);
                    if (triangle == null) {
                        triangle = areaComparator.tryCreateCachedTriangle_(element);
                        if (triangle == null) {
                            triangle = areaComparator.createTriangle(element);
                        }
                    }

                    double area = triangle.queryArea();
                    // if the area is larger than the threshold exit
                    if (area > m_minArea)
                        break;

                    priorityQueue.poll();

                    editShape.removeVertex(element, false);
                    areaComparator.tryDeleteCachedTriangle_(element);
//                    treap.deleteNode(nodeIndex, -1);

                    int prevElement = triangle.m_prevVertexIndex;
                    int nextElement = triangle.m_nextVertexIndex;
//                    int prevNodeIndex = treap.search(prevElement, -1);
//                    int nextNodeIndex = treap.search(nextElement, -1);
//                    prevElement = treap.getElement(prevNodeIndex);
//                    nextElement = treap.getElement(nextNodeIndex);

                    priorityQueue.remove(prevElement);
                    priorityQueue.remove(nextElement);
                    areaComparator.tryDeleteCachedTriangle_(prevElement);
                    areaComparator.tryDeleteCachedTriangle_(nextElement);
//                    treap.deleteNode(prevNodeIndex, -1);
//                    treap.deleteNode(nextNodeIndex, -1);

                    priorityQueue.add(prevElement);
                    priorityQueue.add(nextElement);
//                    treap.addElement(prevElement, -1);
//                    treap.addElement(nextElement, -1);
                }
            }
        }
    }
}
