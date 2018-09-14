package com.esri.core.geometry;

import org.proj4.PJException;

import java.util.Random;

/**
 * Created by davidraleigh on 5/10/17.
 */
public class OperatorRandomPointsCursor extends GeometryCursor {
    private double[] m_pointsPerSquareKm;
    private SpatialReferenceImpl m_spatialReference;
    private ProgressTracker m_progressTracker;
    private Random m_numberGenerator;
    private long m_seed;

    private int m_PPSKmindex;

    public OperatorRandomPointsCursor(GeometryCursor inputGeoms,
                               double[] pointsPerSquareKm,
                               long seed,
                               SpatialReference sr,
                               ProgressTracker pr) {
        m_inputGeoms = inputGeoms;
        m_spatialReference = (SpatialReferenceImpl) sr;
        m_pointsPerSquareKm = pointsPerSquareKm;
        // TODO, for distributed case geometries will be done in different order each time,
        // that is why the random number generator is started over with the same seed
        // for each object in the cursor
        m_seed = seed;
        m_numberGenerator = new Random(seed);
    }

    @Override
    public Geometry next() {
        if (hasNext()) {
            if (m_PPSKmindex + 1 < m_pointsPerSquareKm.length)
                m_PPSKmindex++;

            m_numberGenerator.setSeed(m_seed);
            try {
                return RandomPointMaker.generate(
                        m_inputGeoms.next(),
                        m_pointsPerSquareKm[m_PPSKmindex],
                        m_numberGenerator,
                        m_spatialReference,
                        m_progressTracker);
            } catch (PJException e) {
                throw new GeometryException(e.getMessage());
            }
        }
        return null;
    }
}
