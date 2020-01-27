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
        if (resultSR == null ||
                (operateSR == null && inputSR == null) ||
                (resultSR.equals(operateSR)) || (operateSR == null && resultSR.equals(inputSR))) {
            return geometry;
        }

        finalSR = resultSR;
        if (setWithoutProject) {
            return geometry;
        }

        // TODO need a projection transformation cache
        ProjectionTransformation projectionTransformation;
        if (operateSR != null) {
            projectionTransformation = new ProjectionTransformation(operateSR, resultSR);
        } else {
            projectionTransformation = new ProjectionTransformation(inputSR, resultSR);
        }

        return OperatorProject.local().execute(geometry, projectionTransformation, null);
    }
}
