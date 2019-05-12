/*
 Copyright 1995-2018 Esri

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

import com.fasterxml.jackson.core.JsonParser;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.Math;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.SpatialReferenceSerializer;
import com.esri.core.geometry.VertexDescription;
import com.fasterxml.jackson.core.JsonParser;
import org.proj4.PJ;

/**
 * A class that represents the spatial reference for the geometry.
 * This class provide tolerance value for the topological and relational operations.
 */
public abstract class SpatialReference implements Serializable {
    // Note: We use writeReplace with SpatialReferenceSerializer. This field is
    // irrelevant. Needs to be removed after final.
    private static final long serialVersionUID = 2L;

    enum CoordinateSystemType {
        Uknown, Local, GEOGRAPHIC, PROJECTED
    }

    /**
     * Creates an instance of the spatial reference based on the provided well
     * known ID for the horizontal coordinate system.
     *
     * @param wkid The well-known ID.
     * @return SpatialReference The spatial reference.
     * @throws IllegalArgumentException if wkid is not supported or does not exist.
     */
    public static SpatialReference create(int wkid) {
        SpatialReferenceImpl spatRef = SpatialReferenceImpl.createImpl(wkid);
        return spatRef;
    }

    /**
     * From a lat long wgs84 geometry, get the utm zone for the geometries center
     * @param geometry
     * @return
     */
    public static SpatialReference createUTM(Geometry geometry) {
        // TODO this will fail for multipart geometries on either side of the dateline
        Envelope envelope = new Envelope();
        geometry.queryEnvelope(envelope);

        // if the geometry is outside of the -180 to 180 range shift it back into range
        Point point = (Point)OperatorProjectLocal.foldInto360Range(envelope.getCenter(), SpatialReference.create(4326));
        return createUTM(point.getX(), point.getY());
    }

    /**
     * choose the UTM zone that contains the longitude and latitude
     * @param longitude
     * @param latitude
     * @return
     */
    public static SpatialReference createUTM(double longitude, double latitude) {
        // epsg code for N1 32601
        int epsg_code = 32601;
        if (latitude < 0) {
            // epsg code for S1 32701
            epsg_code += 100;
        }

        double diff = longitude + 180.0;

        // TODO ugly
        if (diff == 0.0) {
            return SpatialReference.create(epsg_code);
        }

        // 6 degrees of separation between zones, started with zone one, so subtract 1
        Double interval_count = Math.ceil(diff / 6);
        int bump = interval_count.intValue() - 1;

        return SpatialReference.create(epsg_code + bump);
    }


    public static SpatialReference createEqualArea(Geometry geometry, SpatialReference spatialReference) {
        Envelope2D inputEnvelope2D = new Envelope2D();

        if (spatialReference == null) {
            throw new GeometryException("Requires spatial reference");
        }

        if (spatialReference.getID() != 4326) {
            OperatorProject operatorProject = (OperatorProject) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Project);
            // TODO this should be grabbing the GCS wkid from the input spatialreference instead of assuming 4326
            ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReference, SpatialReference.create(4326));
            Geometry projectedGeom = operatorProject.execute(geometry, projectionTransformation, null);
            projectedGeom.queryEnvelope2D(inputEnvelope2D);
        } else {
            geometry.queryEnvelope2D(inputEnvelope2D);
        }

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
        return SpatialReference.createFromProj4(proj4);
    }

    /**
     * Creates an instance of the spatial reference based on the provided well
     * known text representation for the horizontal coordinate system.
     *
     * @param wktext The well-known text string representation of spatial
     *               reference.
     * @return SpatialReference The spatial reference.
     */
    public static SpatialReference create(String wktext) {
        return SpatialReferenceImpl.createImpl(wktext);
    }



    public static SpatialReference createFromProj4(String proj4test) {
        return SpatialReferenceImpl.createFromProj4Impl(proj4test);
    }

    /**
     * @return boolean Is spatial reference local?
     */
    boolean isLocal() {
        return false;
    }

    /**
     * Returns spatial reference from the JsonParser.
     *
     * @param parser The JSON parser.
     * @return The spatial reference or null if there is no spatial reference
     * information, or the parser does not point to an object start.
     * @throws Exception if parsing has failed
     */
    public static SpatialReference fromJson(JsonParser parser) throws Exception {
        return fromJson(new JsonParserReader(parser));
    }

    public static SpatialReference fromJson(String string) throws Exception {
        return fromJson(JsonParserReader.createFromString(string));
    }

    public static SpatialReference fromJson(JsonReader parser) throws Exception {
        // Note this class is processed specially: it is expected that the
        // iterator points to the first element of the SR object.
        boolean bFoundWkid = false;
        boolean bFoundLatestWkid = false;
        boolean bFoundVcsWkid = false;
        boolean bFoundLatestVcsWkid = false;
        boolean bFoundWkt = false;

        int wkid = -1;
        int latestWkid = -1;
        int vcs_wkid = -1;
        int latestVcsWkid = -1;
        String wkt = null;
        while (parser.nextToken() != JsonReader.Token.END_OBJECT) {
            String name = parser.currentString();
            parser.nextToken();

            if (!bFoundWkid && name.equals("wkid")) {
                bFoundWkid = true;

                if (parser.currentToken() == JsonReader.Token.VALUE_NUMBER_INT)
                    wkid = parser.currentIntValue();
            } else if (!bFoundLatestWkid && name.equals("latestWkid")) {
                bFoundLatestWkid = true;

                if (parser.currentToken() == JsonReader.Token.VALUE_NUMBER_INT)
                    latestWkid = parser.currentIntValue();
            } else if (!bFoundWkt && name.equals("wkt")) {
                bFoundWkt = true;

                if (parser.currentToken() == JsonReader.Token.VALUE_STRING)
                    wkt = parser.currentString();
            } else if (!bFoundVcsWkid && name.equals("vcsWkid")) {
                bFoundVcsWkid = true;

                if (parser.currentToken() == JsonReader.Token.VALUE_NUMBER_INT)
                    vcs_wkid = parser.currentIntValue();
            } else if (!bFoundLatestVcsWkid && name.equals("latestVcsWkid")) {
                bFoundLatestVcsWkid = true;

                if (parser.currentToken() == JsonReader.Token.VALUE_NUMBER_INT)
                    latestVcsWkid = parser.currentIntValue();
            }
        }

        if (latestVcsWkid <= 0 && vcs_wkid > 0) {
            latestVcsWkid = vcs_wkid;
        }

        // iter.step_out_after(); do not step out for the spatial reference,
        // because this method is used standalone

        SpatialReference spatial_reference = null;

        if (wkt != null && wkt.length() != 0) {
            try {
                spatial_reference = SpatialReference.create(wkt);
            } catch (Exception e) {
            }
        }

        if (spatial_reference == null && latestWkid > 0) {
            try {
                spatial_reference = SpatialReference.create(latestWkid);
            } catch (Exception e) {
            }
        }

        if (spatial_reference == null && wkid > 0) {
            try {
                spatial_reference = SpatialReference.create(wkid);
            } catch (Exception e) {
            }
        }

        return spatial_reference;
    }

    /**
     * Returns the well known ID for the horizontal coordinate system of the
     * spatial reference.
     *
     * @return wkid The well known ID.
     */
    public abstract int getID();

    public abstract String getText();

    public abstract String getProj4();


    public abstract double getMajorAxis();

    public abstract double getEccentricitySquared();

    /**
     * Returns the oldest value of the well known ID for the horizontal
     * coordinate system of the spatial reference. This ID is used for JSON
     * serialization. Not public.
     */
    abstract int getOldID();

    /**
     * Returns the latest value of the well known ID for the horizontal
     * coordinate system of the spatial reference. This ID is used for JSON
     * serialization. Not public.
     */
    abstract int getLatestID();

    /**
     * Returns the XY tolerance of the spatial reference.
     * <p>
     * The tolerance value defines the precision of topological operations, and
     * "thickness" of boundaries of geometries for relational operations.
     * <p>
     * When two points have xy coordinates closer than the tolerance value, they
     * are considered equal. As well as when a point is within tolerance from
     * the line, the point is assumed to be on the line.
     * <p>
     * During topological operations the tolerance is increased by a factor of
     * about 1.41 and any two points within that distance are snapped
     * together.
     *
     * @return The XY tolerance of the spatial reference.
     */
    public double getTolerance() {
        return getTolerance(VertexDescription.Semantics.POSITION);
    }

    /**
     * Get the XY tolerance of the spatial reference
     *
     * @return The XY tolerance of the spatial reference as double.
     */
    abstract double getTolerance(int semantics);

    Object writeReplace() throws ObjectStreamException {
        SpatialReferenceSerializer srSerializer = new SpatialReferenceSerializer();
        srSerializer.setSpatialReferenceByValue(this);
        return srSerializer;
    }

    abstract CoordinateSystemType getCoordinateSystemType();

    /**
     * Returns string representation of the class for debugging purposes. The
     * format and content of the returned string is not part of the contract of
     * the method and is subject to change in any future release or patch
     * without further notice.
     */
    public String toString() {
        return "[ tol: " + getTolerance() + "; wkid: " + getID() + "; wkt: "
                + getText() + "]";
    }
}
