package com.esri.core.geometry;


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
        treap.disableBalancing();
        GeneralizeComparator areaComparator = new GeneralizeComparator(editShape, m_generalizeType);
        treap.setComparator(areaComparator);

        for (int iGeometry = editShape.getFirstGeometry(); iGeometry != -1; iGeometry = editShape.getNextGeometry(iGeometry)) {
            int ptCountOriginal = editShape.getPointCount(iGeometry);
            treap.setCapacity(ptCountOriginal);
            int ptCountToRemove = (int)Math.floor(ptCountOriginal * m_percentReduction / 100.0);

            for (int iPath = editShape.getFirstPath(iGeometry); iPath != -1; iPath = editShape.getNextPath(iPath)) {
                for (int iVertex = editShape.getFirstVertex(iPath), i = 0, n = editShape.getPathSize(iPath); i < n; iVertex = editShape.getNextVertex(iVertex), i++) {
                    treap.addElement(iVertex, -1);
                }

                while (0 < ptCountToRemove-- && treap.size(-1) > 0) {

                    int nodeIndex = treap.getFirst(-1);
                    int element = treap.getElement(nodeIndex);


                    GeneralizeComparator.EditShapeTriangle triangle = areaComparator.tryGetCachedTriangle_(element);
                    if (triangle == null) {
                        triangle = areaComparator.tryCreateCachedTriangle_(element);
                        if (triangle == null) {
                            triangle = areaComparator.createTriangle(element);
                        }
                    }

                    int prevElement = triangle.m_prevVertexIndex;
                    int nextElement = triangle.m_nextVertexIndex;
                    int prevNodeIndex = treap.search(prevElement, -1);
                    int nextNodeIndex = treap.search(nextElement, -1);

                    treap.deleteNode(prevNodeIndex, -1);
                    treap.deleteNode(nextNodeIndex, -1);

                    treap.deleteNode(nodeIndex, -1);
                    editShape.removeVertex(element, false);

                    treap.addElement(prevElement, -1);
                    treap.addElement(nextElement, -1);
                }
            }
        }
    }
}
