package com.esri.core.geometry;

/**
 * Created by davidraleigh on 5/10/17.
 */
public class OperatorRandomPointsLocal extends OperatorRandomPoints {
	@Override
	public GeometryCursor execute(
			GeometryCursor inputPolygons,
			double[] pointsPerSquareKm,
			long seed,
			SpatialReference sr,
			ProgressTracker progressTracker) {
		GeometryCursor randomPointsCursor = new OperatorRandomPointsCursor(inputPolygons, pointsPerSquareKm, seed, sr, progressTracker);
		return randomPointsCursor;
	}

	@Override
	public Geometry execute(
			Geometry inputPolygon,
			double pointsPerSquareKm,
			long seed,
			SpatialReference sr,
			ProgressTracker progressTracker) {
		double[] perSqrKM = {pointsPerSquareKm};
		GeometryCursor res = execute(new SimpleGeometryCursor(inputPolygon), perSqrKM, seed, sr, progressTracker);
		return res.next();
	}
}
