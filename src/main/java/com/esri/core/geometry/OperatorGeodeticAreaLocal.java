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

import java.util.ArrayList;

//This is a stub
class OperatorGeodeticAreaLocal extends OperatorGeodeticArea {
    @Override
    public double[] execute(GeometryCursor geoms, SpatialReference sr,
                            int geodeticCurveType, ProgressTracker progressTracker) {
        ArrayList<Double> areas = new ArrayList<>();
        while (geoms.hasNext()) {
            areas.add(execute(geoms.next(), sr, geodeticCurveType, progressTracker));
        }
        return areas.stream().mapToDouble(d -> d).toArray();
    }

    @Override
    public double execute(Geometry geom, SpatialReference sr,
                          int geodeticCurveType, ProgressTracker progressTracker) {
        if (geodeticCurveType != GeodeticCurveType.Geodesic) {
            throw new GeometryException("Only implemented for Geodesic");
        }
        return ((OperatorProject)OperatorFactoryLocal.getInstance().getOperator(Type.Project)).execute(geom, ProjectionTransformation.getEqualArea(geom, sr), null).calculateArea2D();
    }
}
