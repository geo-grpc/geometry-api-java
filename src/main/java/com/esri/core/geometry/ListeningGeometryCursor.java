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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A GeometryCursor implementation that allows pushing geometries into it.
 * <p>
 * To be used with aggregating operations, OperatorUnion and OperatorConvexHull,
 * when the geometries are not available at the time of the execute method call,
 * but are coming in a stream.
 */
public final class ListeningGeometryCursor extends GeometryCursor {
    // required for use with grpc streaming
    // https://github.com/grpc/grpc-java/blob/06e9b8814787b5cdec10a60e0d9a2228feb8554d/examples/src/main/java/io/grpc/examples/routeguide/RouteGuideServer.java#L246
    private Queue<Geometry> m_geomList = new ConcurrentLinkedQueue<Geometry>();

    private long m_index = -1;

    public ListeningGeometryCursor() {
    }

    @Override
    public boolean hasNext() { return !m_geomList.isEmpty(); }

    @Override
    public long getGeometryID() {
        return m_index;
    }

    @Override
    public Geometry next() {
        if (m_geomList != null && !m_geomList.isEmpty()) {
            m_index++;
            return m_geomList.poll();
        }

        m_geomList = null;//prevent the class from being used again
        return null;
    }

    /**
     * Call this method to add geometry to the cursor. After this method is
     * called, call immediately the tock() method on the GeometryCursor returned
     * by the OperatorUnion (or OperatorConvexHull with b_merge == true). Call
     * next() on the GeometryCursor returned by the OperatorUnion when done
     * listening to incoming geometries to finish the union operation.
     *
     * @param geom The geometry to be pushed into the cursor.
     */
    public void tick(Geometry geom) {
        m_geomList.add(geom);
    }
}
