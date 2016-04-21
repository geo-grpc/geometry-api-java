package com.esri.core.geometry;


import java.util.ArrayList;

/**
 * Created by davidraleigh on 4/17/16.
 */
public class OperatorGeneralizeByAreaCursor extends GeometryCursor {
    ProgressTracker m_progressTracker;
    GeometryCursor m_geoms;
    boolean m_bRemoveDegenerateParts;
    GeneralizeType m_generalizeType;
    double m_percentReduction;
    int m_ptRemovalGoal;

    public OperatorGeneralizeByAreaCursor(GeometryCursor geoms,
                                          double percentReduction,
                                          boolean bRemoveDegenerateParts,
                                          GeneralizeType generalizeType,
                                          ProgressTracker progressTracker) {
        m_geoms = geoms;
        m_progressTracker = progressTracker;
        m_bRemoveDegenerateParts = bRemoveDegenerateParts;
        m_generalizeType = generalizeType;
        m_percentReduction = percentReduction;
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

        Treap treap = new Treap();
        GeneralizeComparator areaComparator = new GeneralizeComparator(editShape, m_generalizeType);
        treap.setComparator(areaComparator);

        // TODO fix this. path removal stuff. It's a messy solution to the whole treap cleanup problem


        for (int iGeometry = editShape.getFirstGeometry(); iGeometry != -1; iGeometry = editShape.getNextGeometry(iGeometry)) {
            for (int iPath = editShape.getFirstPath(iGeometry); iPath != -1; iPath = editShape.getNextPath(iPath)) {
                int n = editShape.getPathSize(iPath);
                treap.setCapacity(n);
                int ptCountToRemove = (int)(n * m_percentReduction / 100.0);
                areaComparator.setPathCount(n);
                // if there are points that will remain after removals, then first create the treap
                for (int iVertex = editShape.getFirstVertex(iPath), i = 0; i < n; iVertex = editShape.getNextVertex(iVertex), i++) {
                    if (iVertex == 335)
                        iVertex +=0;
                    treap.addElement(iVertex, -1);
                }

                while (0 < ptCountToRemove-- && treap.size(-1) > 0) {

                    int vertexNode = treap.getFirst(-1);
                    int vertexElm = treap.getElement(vertexNode);

                    GeneralizeComparator.EditShapeTriangle triangle = areaComparator.tryGetCachedTriangle_(vertexElm);
                    if (triangle == null) {
                        triangle = areaComparator.tryCreateCachedTriangle_(vertexElm);
                        if (triangle == null) {
                            triangle = areaComparator.createTriangle(vertexElm);
                        }
                    }

                    if ((m_generalizeType == GeneralizeType.ResultContainsOriginal && triangle.queryOrientation() < 0) ||
                        (m_generalizeType == GeneralizeType.ResultWithinOriginal && triangle.queryOrientation() > 0))
                        break;

                    if (treap.size(-1) == 1) {
                        treap.deleteNode(vertexNode, -1);
                        editShape.removeVertex(vertexElm, false);
                    } else {
                        int prevElement = triangle.m_prevVertexIndex;
                        int nextElement = triangle.m_nextVertexIndex;

                        int prevNodeIndex = treap.search(prevElement, -1);
                        int nextNodeIndex = treap.search(nextElement, -1);

                        if (prevNodeIndex > -1)
                            treap.deleteNode(prevNodeIndex, -1);
                        if (nextNodeIndex > -1)
                            treap.deleteNode(nextNodeIndex, -1);

                        treap.deleteNode(vertexNode, -1);
                        editShape.removeVertex(vertexElm, false);

                        if (prevNodeIndex > -1)
                            treap.addElement(prevElement, -1);
                        if (nextNodeIndex > -1)
                            treap.addElement(nextElement, -1);
                    }
                }
                treap.clear();
            }
        }
    }
}
