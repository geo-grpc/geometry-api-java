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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A simple GeometryCursor implementation that wraps a single Geometry or
 * an array of Geometry classes
 */
public class SimpleGeometryCursor extends GeometryCursor {

    Iterator<Geometry> m_geometryIterator = null;
	int m_index;
	MapGeometryCursor m_mapGeometryCursor = null;
	MapGeometry mapGeometry = null;

	public SimpleGeometryCursor(Geometry geom) {
		this(Arrays.asList(geom));
	}

	public SimpleGeometryCursor(Geometry[] geoms) {
		this(Arrays.asList(geoms));
	}

	public SimpleGeometryCursor(List<Geometry> geoms) {
		m_index = -1;
		m_geometryIterator = geoms.iterator();
	}

	public SimpleGeometryCursor(MapGeometryCursor mapGeometryCursor){
		m_mapGeometryCursor = mapGeometryCursor;
	}

	@Override
	public int getGeometryID() {
		if (m_mapGeometryCursor != null)
			return m_mapGeometryCursor.getGeometryID();

		return m_index;
	}

	@Override
	public Geometry next() {
		if (m_geometryIterator.hasNext()) {
			m_index++;
			return m_geometryIterator.next();
		}
		if (m_mapGeometryCursor != null && (mapGeometry = m_mapGeometryCursor.next()) != null) {
			return mapGeometry.m_geometry;
		}

		return null;
	}
}
