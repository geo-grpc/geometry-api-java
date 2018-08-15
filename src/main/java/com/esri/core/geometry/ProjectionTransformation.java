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

import org.proj4.PJ;

//This is a stub
public class ProjectionTransformation {
    SpatialReference m_fromSpatialReference;
    SpatialReference m_toSpatialReference;
    // TODO maybe cache the PJ objects?

    public ProjectionTransformation(SpatialReference fromSpatialReference, SpatialReference toSpatialReference) {
        m_fromSpatialReference = fromSpatialReference;
        m_toSpatialReference = toSpatialReference;
    }

    public ProjectionTransformation getReverse() {
        return new ProjectionTransformation(m_toSpatialReference, m_fromSpatialReference);
    }

    PJ getFromProj() {
        return ((SpatialReferenceImpl)m_fromSpatialReference).getPJ();
    }

    public SpatialReference getFrom() { return m_fromSpatialReference; }
    public SpatialReference getTo() { return m_toSpatialReference; }

    PJ getToProj() {
        return ((SpatialReferenceImpl)m_toSpatialReference).getPJ();
    }

    public static ProjectionTransformation getEqualArea(Geometry geometry, SpatialReference spatialReference) {
        // TODO implement projection
        if (spatialReference.getCoordinateSystemType() != SpatialReference.CoordinateSystemType.GEOGRAPHIC)
            throw new GeometryException("Not implemented for Projected geometries");

        // TODO change proj4 string below to include other GEOGRAPHIC
        if (spatialReference.getID() != 4326) {
            throw new GeometryException("Not implemented for any GEOGRAPHIC except WGS84");
        }

        Envelope2D inputEnvelope2D = new Envelope2D();
        geometry.queryEnvelope2D(inputEnvelope2D);

        // From GEOGRAPHIC Grab point
        double a = spatialReference.getMajorAxis();
        double e2 = spatialReference.getEccentricitySquared();

        Point2D ptCenter = new Point2D();
        GeoDist.getEnvCenter(a, e2, inputEnvelope2D, ptCenter);
        double longitude = ptCenter.x;
        double latitude = ptCenter.y;

        // create projection transformation that goes from input to input's equal area azimuthal projection
        // +proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs
        String proj4 = String.format(
                "+proj=laea +lat_0=%f +lon_0=%f +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
                latitude,
                longitude);
        SpatialReference spatialReferenceAzi = SpatialReference.createFromProj4(proj4);
        return new ProjectionTransformation(spatialReference, spatialReferenceAzi);
    }
}
