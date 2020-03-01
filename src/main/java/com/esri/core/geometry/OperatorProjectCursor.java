package com.esri.core.geometry;

/**
 * Created by davidraleigh on 5/12/17.
 */
public class OperatorProjectCursor extends GeometryCursor {
	ProjectionTransformation m_projectionTransformation;
	ProgressTracker m_progressTracker;

	OperatorProjectCursor(
			GeometryCursor inputGeoms,
			ProjectionTransformation projectionTransformation,
			ProgressTracker progressTracker) {
		m_inputGeoms = inputGeoms;
		m_projectionTransformation = projectionTransformation;
		m_progressTracker = progressTracker;
	}

	@Override
	public Geometry next() {
		if (m_inputGeoms.hasNext()) {
			Geometry geometry = m_inputGeoms.next();
			return Projecter.project(geometry, m_projectionTransformation, m_progressTracker);
		}
		return null;
	}
}
