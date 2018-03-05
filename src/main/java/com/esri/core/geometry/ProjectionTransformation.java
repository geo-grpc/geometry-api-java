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
        return new PJ(m_fromSpatialReference.getProj4());
    }

    SpatialReference getFrom() { return m_fromSpatialReference; }
    SpatialReference getTo() { return m_toSpatialReference; }

    PJ getToProj() {
        return new PJ(m_toSpatialReference.getProj4());
    }

    public static ProjectionTransformation getEqualArea(Geometry geometry, SpatialReference spatialReference) {
        Envelope2D inputEnvelope2D = new Envelope2D();
        geometry.queryEnvelope2D(inputEnvelope2D);

        // From GCS Grab point
        // TODO change to work with other GCS
        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984

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
