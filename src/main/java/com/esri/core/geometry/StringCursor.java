package com.esri.core.geometry;

import java.util.Iterator;

public abstract class StringCursor implements Iterator<String> {
    private PrePostProjection m_projectionHelper = new PrePostProjection();

    public void setInputSR(SpatialReference inputSR) { this.m_projectionHelper.setInputSR(inputSR); }

    public void setOperateSR(SpatialReference operateSR) { this.m_projectionHelper.setOperateSR(operateSR); }

    public void setResultSR(SpatialReference resultSR) { this.m_projectionHelper.setResultSR(resultSR); }

    public Geometry preProject(GeometryCursor geometryCursor) { return this.m_projectionHelper.preProject(geometryCursor); }

    public Geometry postProject(Geometry geometry) { return this.m_projectionHelper.postProject(geometry); }

    public abstract String next();

    public abstract long getID();

    public abstract SimpleStateEnum getSimpleState();

    public abstract String getFeatureID();

    public abstract boolean hasNext();
}
