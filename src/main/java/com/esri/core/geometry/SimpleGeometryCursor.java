/*
 Copyright 1995-2015 Esri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */
package com.esri.core.geometry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple GeometryCursor implementation that wraps a single Geometry or
 * an array of Geometry classes
 */
public class SimpleGeometryCursor extends GeometryCursor {

    int m_index = -1;
    MapGeometryCursor m_mapGeometryCursor = null;
    ArrayDeque<Geometry> m_geometryDeque = null;

    public SimpleGeometryCursor(Geometry geom) {
        m_geometryDeque = new ArrayDeque<>(1);
        m_geometryDeque.add(geom);
    }

    public SimpleGeometryCursor(Geometry[] geoms) {
        m_geometryDeque = Arrays.stream(geoms).collect(Collectors.toCollection(ArrayDeque::new));
    }

    @Deprecated
    public SimpleGeometryCursor(List<Geometry> geoms) {
        m_geometryDeque = new ArrayDeque<>(geoms);
    }

    public SimpleGeometryCursor(ArrayDeque<Geometry> geoms) {
        m_geometryDeque = geoms;
    }

    public SimpleGeometryCursor(MapGeometryCursor mapGeometryCursor) {
        m_mapGeometryCursor = mapGeometryCursor;
    }

    @Override
    public boolean hasNext() {
        return (m_geometryDeque != null && m_geometryDeque.size() > 0) || (m_mapGeometryCursor != null && m_mapGeometryCursor.hasNext());
    }

    @Override
    public int getGeometryID() {
        if (m_mapGeometryCursor != null)
            return m_mapGeometryCursor.getGeometryID();

        return m_index;
    }

    @Override
    public Geometry next() {
        if (m_geometryDeque != null && !m_geometryDeque.isEmpty()) {
            m_index++;
            return m_geometryDeque.pop();
        } else if (m_mapGeometryCursor != null && m_mapGeometryCursor.hasNext()) {
            m_index++;
            return m_mapGeometryCursor.next().m_geometry;
        }

        return null;
    }
}
