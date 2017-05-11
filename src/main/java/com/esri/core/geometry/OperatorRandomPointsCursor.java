package com.esri.core.geometry;

import java.util.Random;

/**
 * Created by davidraleigh on 5/10/17.
 */
class OperatorRandomPointsCursor extends GeometryCursor {

    private GeometryCursor m_inputGeoms;
    private double[] m_pointsPerSquareKm;
    private SpatialReferenceImpl m_spatialReference;
    private ProgressTracker m_progressTracker;
    private Random m_numberGenerator;
    private long m_seed;

    private int m_index;
    private int m_PPSKmindex;

    OperatorRandomPointsCursor(GeometryCursor inputGeoms,
                               double[] pointsPerSquareKm,
                               long seed,
                               SpatialReference sr,
                               ProgressTracker pr) {
        m_index = -1;
        m_inputGeoms = inputGeoms;
        m_spatialReference = (SpatialReferenceImpl)sr;
        m_pointsPerSquareKm = pointsPerSquareKm;
        // TODO, for distributed case geometries will be done in different order each time,
        // that is why the random number generator is started over with the same seed
        // for each object in the cursor
        m_seed = seed;
        m_numberGenerator = new Random(seed);
    }

    @Override
    public Geometry next() {
        Geometry geom;
        while ((geom = m_inputGeoms.next()) != null) {
            m_index = m_inputGeoms.getGeometryID();
            if (m_PPSKmindex + 1 < m_pointsPerSquareKm.length)
                m_PPSKmindex++;

            m_numberGenerator.setSeed(m_seed);
            return RandomPointMaker.generate(geom, m_pointsPerSquareKm[m_PPSKmindex], m_numberGenerator, m_spatialReference, m_progressTracker);
        }
        return null;
    }

    @Override
    public int getGeometryID() {
        return 0;
    }

}
