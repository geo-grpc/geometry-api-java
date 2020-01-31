package com.esri.core.geometry;

public class PrePostProjection {
    private SpatialReference inputSR = null;
    private SpatialReference operateSR = null;
    private SpatialReference resultSR = null;
    private SpatialReference finalSR = null;
    private boolean setWithoutProject = false;


    public void setInputSR(SpatialReference inputSR) {
        if (inputSR != null)
            this.inputSR = inputSR;
    }

    public SpatialReference getInputSR() {
        return this.inputSR;
    }

    public void setResultSR(SpatialReference resultSR, boolean setWithoutProject) {
        this.resultSR = resultSR;
        this.setWithoutProject = setWithoutProject;
    }

    public void setOperateSR(SpatialReference operateSR) {
        this.operateSR = operateSR;
    }

    public SpatialReference getSR() {
        if (finalSR == null) {
            return resultSR != null ? resultSR : (operateSR != null ? operateSR : inputSR);
        }
        return finalSR;
    }

    protected Geometry preProjectNext(GeometryCursor geometryCursor) {
        if (geometryCursor.getSR() != null) {
            inputSR = geometryCursor.getSR();
        }

        finalSR = inputSR;
        if (inputSR == null || operateSR == null || inputSR.equals(operateSR)) {
            return geometryCursor.next();
        }
        finalSR = operateSR;

        // TODO need a projection transformation cache
        return OperatorProject.local().execute(geometryCursor.next(), new ProjectionTransformation(inputSR, operateSR), null);
    }

    Geometry postProject(Geometry geometry) {
        SpatialReference fromSR = operateSR != null ? operateSR : inputSR;
        if (resultSR == null || resultSR.equals(finalSR)) {
            finalSR = fromSR;
            return geometry;
        }

        finalSR = resultSR;
        if (setWithoutProject) {
            return geometry;
        }

        // TODO need a projection transformation cache
        return OperatorProject.local().execute(geometry, new ProjectionTransformation(fromSR, resultSR), null);
    }
}
