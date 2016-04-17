package com.esri.core.geometry;

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

    private void GeneralizeAreaPath(MultiPathImpl mpsrc,
                                    int ipath,
                                    MultiPathImpl mpdst,
                                    Line lineHelper) {

    }
}
