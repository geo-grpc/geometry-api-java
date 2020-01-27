package com.esri.core.geometry;

import java.util.Iterator;

public abstract class StringCursor implements Iterator<String> {
    private PrePostProjection m_projectionHelper = new PrePostProjection();

    public void setInputSR(SpatialReference inputSR) {
        if (inputSR != null)
            this.m_projectionHelper.setInputSR(inputSR);
    }

    public void setResultSR(SpatialReference resultSR, boolean setWithoutProject) {
        this.m_projectionHelper.setResultSR(resultSR, setWithoutProject);
    }

    public void setOperateSR(SpatialReference operateSR) {
        this.m_projectionHelper.setOperateSR(operateSR);
    }

    public SpatialReference getSR() {
        return this.m_projectionHelper.getSR();
    }

    public Geometry preProjectNext(GeometryCursor geometryCursor) {
        return this.m_projectionHelper.preProjectNext(geometryCursor);
    }

    public Geometry postProject(Geometry geometry) {
        return this.m_projectionHelper.postProject(geometry);
    }

    public abstract String next();

    public abstract long getID();

    public abstract SimpleStateEnum getSimpleState();

    public abstract String getFeatureID();

    public abstract boolean hasNext();
}
