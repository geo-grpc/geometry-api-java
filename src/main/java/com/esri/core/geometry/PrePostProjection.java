package com.esri.core.geometry;

public class PrePostProjection {
    private SpatialReference inputSR = null;
    private SpatialReference operateSR = null;
    private SpatialReference resultSR = null;
    private SpatialReference finalSR = null;


    public void setInputSR(SpatialReference inputSR) {
        if (inputSR == null)
            throw new GeometryException("cannot set input sr to null");

        this.inputSR = inputSR;
        if (operateSR == null && resultSR == null)
            finalSR = inputSR;
    }

    public void setOperateSR(SpatialReference operateSR) {
        if (operateSR == null)
            throw new GeometryException("cannot set input sr to null");

        this.operateSR = operateSR;
        if (resultSR == null)
            finalSR = operateSR;
    }

    public void setResultSR(SpatialReference resultSR) {
        if (resultSR == null)
            throw new GeometryException("cannot set input sr to null");

        this.resultSR = resultSR;
        finalSR = resultSR;
    }

    public SpatialReference getSR() {
        return finalSR;
    }

    protected Geometry preProject(GeometryCursor geometryCursor) {
        // get the previous cursors final sr as the input
        if (geometryCursor.getSR() != null && this.inputSR != null)
            throw new GeometryException("cannot re-define an input sr for a cursor");

        if (inputSR == null) {
            inputSR = geometryCursor.getSR();
        }

        finalSR = inputSR;
        if (operateSR == null || operateSR.equals(inputSR)) {
            return geometryCursor.next();
        }

        finalSR = operateSR;
        // TODO need a projection transformation cache
        return OperatorProject.local().execute(geometryCursor.next(), new ProjectionTransformation(inputSR, operateSR), null);
    }

    Geometry postProject(Geometry geometry) {
        // either the operatorSR, the inputSR or null
        SpatialReference fromSR = operateSR != null ? operateSR : inputSR;
        if (resultSR == null || resultSR.equals(fromSR)) {
            finalSR = fromSR;
            return geometry;
        }

        finalSR = resultSR;
        // TODO need a projection transformation cache
        return OperatorProject.local().execute(geometry, new ProjectionTransformation(fromSR, resultSR), null);
    }
}
