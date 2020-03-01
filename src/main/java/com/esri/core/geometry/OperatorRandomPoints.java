/**
 * Created by davidraleigh on 5/10/17.
 */

package com.esri.core.geometry;

public abstract class OperatorRandomPoints extends Operator {
	@Override
	public Operator.Type getType() {
		return Type.RandomPoints;
	}

	public abstract GeometryCursor execute(
			GeometryCursor inputPolygons,
			double[] pointsPerSquareKm,
			long seed,
			SpatialReference sr,
			ProgressTracker progressTracker);

	public abstract Geometry execute(
			Geometry inputPolygon,
			double pointsPerSquareKm,
			long seed,
			SpatialReference sr,
			ProgressTracker progressTracker);

	public static OperatorRandomPoints local() {
		return (OperatorRandomPoints) OperatorFactoryLocal.getInstance().getOperator(Type.RandomPoints);
	}

}
