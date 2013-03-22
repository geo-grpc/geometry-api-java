/*
 Copyright 1995-2013 Esri

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

interface ShapeExportFlags {

	public static final int ShapeExportDefaults = 0;
	public static final int ShapeExportNoSwap = 1;
	public static final int ShapeExportAngularDensify = 2;
	public static final int ShapeExportDistanceDensify = 4;
	public static final int ShapeExportTrueNaNs = 8;
	public static final int ShapeExportStripZs = 16;
	public static final int ShapeExportStripMs = 32;
	public static final int ShapeExportStripIDs = 64;
	public static final int ShapeExportStripTextures = 128;
	public static final int ShapeExportStripNormals = 256;
	public static final int ShapeExportStripMaterials = 512;
	public static final int ShapeExportNewArcFormat = 1024;
	public static final int ShapeExportNoCompress = 2048;
}
