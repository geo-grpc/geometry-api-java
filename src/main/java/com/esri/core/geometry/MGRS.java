package com.esri.core.geometry;

import java.util.*;
public class MGRS {

    public enum Zoom {
        Level1M(1), Level10M(10), Level100M(100), Level1K(1000), Level10K(10000), Level100K(100000), LevelGridZone(6);

        int m_level;
        Zoom(int level) {
            m_level = level;
        }
    }

    /**
     * The 6° wide UTM zones, numbered 1–60, are intersected by latitude bands that are normally 8° high,
     * lettered C–X (omitting I and O). South of 80°S, UPS South (Universal Polar Stereographic) is used instead
     * of a UTM projection. North of 84°N, UPS North is used.
     */
    final static List<String> LAT_BANDS_S_TO_N = List.of("C","D","E","F","G","H","J","K","L","M","N","P","Q","R","S","T","U","V","W","X");

    /**
     * The northmost latitude band, X, is 12° high. The intersection of a UTM zone and a latitude band is (normally) a
     * 6° × 8° polygon called a grid zone, whose designation in MGRS is formed by the zone number (one or two
     * digits – the number for zones 1 to 9 is just a single digit, according to the example in DMA TM 8358.1,
     * Section 3-2,[1] Figure 7), followed by the latitude band letter (uppercase).
     * @param lon
     * @param lat
     * @return
     */
    public static String gridZoneCode(double lon, double lat) {
        if (lon < -180 || lon > 180 || lat < -80 || lat > 84) {
            throw new IndexOutOfBoundsException("lat's must be from -80 to 84 and lons from 180 to -180");
        }

        int lat_position = lat < 80 ? (int)Math.floor((lat + 80) / 8) : 19;
        return String.format("%d%s", SpatialReference.getUTMZone(lon), LAT_BANDS_S_TO_N.get(lat_position));
    }

    /**
     * The second part of an MGRS coordinate is the 100,000-meter square identification. Each UTM zone is divided into
     * 100,000 meter squares, so that their corners have UTM-coordinates that are multiples of 100,000 meters. The
     * identification consists of a column letter (A–Z, omitting I and O) followed by a row letter (A–V, omitting I
     * and O).
     */
    // 8 Id column intervals in each zone, with x ranges as follows:
    // 100k-200k, 200k-300k, 300k-400k, 400k-500k, 500k-600k, 600k-700k, 700k-800k, 800k-900k
    final static List<String> COL_100KM = List.of("A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
    final static List<String> ROW_100KM_N = List.of("A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V");
    final static List<String> ROW_100KM_N_SHIFTED;
    final static List<String> ROW_100KM_S;
    final static List<String> ROW_100KM_S_SHIFTED;
    static {
        List<String> list = new ArrayList<>(ROW_100KM_N);
        Collections.reverse(list);
        ROW_100KM_S = list;

        List<String> listNShifted = new ArrayList<>(ROW_100KM_N);
        listNShifted = listNShifted.subList(5, listNShifted.size());
        listNShifted.addAll(ROW_100KM_N.subList(0, 5));
        ROW_100KM_N_SHIFTED = listNShifted;

        List<String> listSShifted = new ArrayList<>(ROW_100KM_N_SHIFTED);
        Collections.reverse(listSShifted);
        ROW_100KM_S_SHIFTED = listSShifted;
    }

    final static double GRID_SIZE = 100000;
    final static double SOUTH_START = 10000000;

    public static String gridSquareId(double lon, double lat) {
        SpatialReference spatialReference = SpatialReference.createUTM(lon, lat);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), spatialReference);
        Point pt = (Point)OperatorProject.local().execute(new Point(lon, lat), projectionTransformation, null);
        int colIndex = (int)Math.floor(pt.getX() / GRID_SIZE) - 1;
        int utmZone = SpatialReference.getUTMZone(lon);

        // Near the equator, the columns of UTM zone 1 have the letters A–H, the columns of UTM zone 2 have the
        // letters J–R (omitting O), and the columns of UTM zone 3 have the letters S–Z. At zone 4, the column letters
        // start over from A, and so on around the world.
        colIndex += utmZone % 3 == 0 ? 16 : (utmZone % 3 == 1 ? 0 : 8);

        // In the AA scheme,[2] also known as MGRS-New,[3] which is used for WGS84 and some other modern geodetic
        // datums, the letter for the first row – just north of the equator – is A in odd-numbered zones, and F in
        // even-numbered zones, as shown in figure 1. Note that the westmost square in this row, in zone 1,
        // has identification AA.
        int rowOffset = utmZone % 2 == 0 ? 5 : 0;
        int rowIndex = ((int)Math.floor(pt.getY() / GRID_SIZE) + rowOffset) % ROW_100KM_N.size();
        String rowLetter;
        if (lat < 0) {
            rowIndex = (int)(Math.floor((SOUTH_START - pt.getY()) / GRID_SIZE) + (ROW_100KM_S.size() - rowOffset)) % ROW_100KM_S.size();
            rowLetter = ROW_100KM_S.get(rowIndex);
        } else {
            rowLetter = ROW_100KM_N.get(rowIndex);
        }

        return COL_100KM.get(colIndex) + rowLetter;
    }

    public static String gridSquare(Point utmPoint, Zoom zoomLevelMeters) {
        String easting = String.format("%06d", (int)Math.floor(utmPoint.getX()));
        String northing = String.format("%06d", (int)Math.floor(utmPoint.getY()));

        // Level1M
        int index = 0;
        switch (zoomLevelMeters) {
            case Level10M:
                index = 1;
                break;
            case Level100M:
                index = 2;
                break;
            case Level1K:
                index = 3;
                break;
            case Level10K:
                index = 4;
                break;
        }

        easting = easting.substring(easting.length() - 5, easting.length() - index);
        northing = northing.substring(northing.length() - 5, northing.length() - index);
        return easting + " " + northing;
    }

    public static String gridSquare(double lon, double lat, Zoom zoomLevelMeters) {
        // project coordinate to UTM
        SpatialReference spatialReference = SpatialReference.createUTM(lon, lat);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReference.create(4326), spatialReference);
        Point utmPoint = (Point)OperatorProject.local().execute(new Point(lon, lat), projectionTransformation, null);

        return gridSquare(utmPoint, zoomLevelMeters);
    }

    public static String getMGRSPosition(double lon, double lat, Zoom zoomLevelMeters) {
        switch (zoomLevelMeters) {
            case LevelGridZone:
                return gridZoneCode(lon, lat);
            case Level100K:
                return gridZoneCode(lon, lat) + " " + gridSquareId(lon, lat);
            default:
                return gridZoneCode(lon, lat) + " " + gridSquareId(lon, lat) + " " + gridSquare(lon, lat, zoomLevelMeters);
        }
    }

    public static int getUTMZone(String mgrsZone) {
        return Integer.parseInt(mgrsZone.substring(0, mgrsZone.length() - 1));
    }

    public static Envelope getZoneEnvelope(String mgrsZone) {
        String latBandCode = mgrsZone.substring(mgrsZone.length() - 1);
        int latIndex = LAT_BANDS_S_TO_N.indexOf(latBandCode);
        int utmZone = getUTMZone(mgrsZone);
        double xmax = -180 + utmZone * 6;
        double xmin = xmax - 6;

        double ymin = -80 + latIndex * 8;
        double ymax = ymin + 8;
        if (ymin == 72)
            ymax += 4;

        return new Envelope(xmin, ymin, xmax, ymax);
    }

    static double getGridYSouth(String rowId, boolean bUtmZoneEven, double utmZoneYMax) {
        // get the y value for the top grid square id
        double yMax = Math.ceil(utmZoneYMax / GRID_SIZE) * GRID_SIZE;

        int rowIndexShift = (int)((SOUTH_START - yMax) / GRID_SIZE) % ROW_100KM_S.size();

        int rowIndex = ROW_100KM_S.indexOf(rowId);
        if (bUtmZoneEven)
            rowIndex = ROW_100KM_S_SHIFTED.indexOf(rowId);

        if (rowIndex >= rowIndexShift)
            return yMax - (rowIndex - rowIndexShift + 1) * GRID_SIZE;
        return yMax - (rowIndex + (ROW_100KM_N.size() - rowIndexShift) + 1) * GRID_SIZE;
    }

    static double getGridYNorth(String rowId, boolean bUtmZoneEven, double utmZoneYmin) {
        double yMin = Math.floor(utmZoneYmin / GRID_SIZE) * GRID_SIZE;

        int rowIndexShift = (int)((yMin / GRID_SIZE) % ROW_100KM_N.size());

        int rowIndex = ROW_100KM_N.indexOf(rowId);
        if (bUtmZoneEven)
            rowIndex = ROW_100KM_N_SHIFTED.indexOf(rowId);

        if (rowIndex >= rowIndexShift)
            return yMin + (rowIndex - rowIndexShift) * GRID_SIZE;
        return yMin + (rowIndex + (ROW_100KM_N.size() - rowIndexShift)) * GRID_SIZE;
    }

    static double getGridYmin(String mgrsSquareId, int utmZone, boolean bSouth, Envelope utmZoneEnvelope) {
        /*
         * The second part of an MGRS coordinate is the 100,000-meter square identification. Each UTM zone is divided into
         * 100,000 meter squares, so that their corners have UTM-coordinates that are multiples of 100,000 meters. The
         * identification consists of a column letter (A–Z, omitting I and O) followed by a row letter (A–V, omitting I
         * and O).
         */
        // In the AA scheme,[2] also known as MGRS-New,[3] which is used for WGS84 and some other modern geodetic
        // datums, the letter for the first row – just north of the equator – is A in odd-numbered zones, and F in
        // even-numbered zones, as shown in figure 1. Note that the westmost square in this row, in zone 1,
        // has identification AA.
        boolean bUtmZoneEven = utmZone % 2 == 0;
        String rowId = mgrsSquareId.substring(1, 2);
        if (bSouth)
            return getGridYSouth(rowId, bUtmZoneEven, utmZoneEnvelope.getYMax());
        else
            return getGridYNorth(rowId, bUtmZoneEven, utmZoneEnvelope.getYMin());
    }

    public static Envelope getSquareIdEnvelope(String mgrsSquareId, int utmZone, Envelope zoneEnvelope, ProjectionTransformation transformationTo4326) {
        boolean bSouth = zoneEnvelope.getCenter().getY() < 0;

        Envelope queryEnvelope = new Envelope();
        OperatorProject.local().execute(zoneEnvelope, transformationTo4326.getReverse(), null).queryEnvelope(queryEnvelope);

        double yMin = getGridYmin(mgrsSquareId, utmZone, bSouth, queryEnvelope);

        /*
         * The identification consists of a column letter (A–Z, omitting I and O)
         */
        String colId = mgrsSquareId.substring(0, 1);

        int colIndex = COL_100KM.indexOf(colId) % 8 - 4;
        double xMin = 5 * GRID_SIZE + GRID_SIZE * colIndex;

        return new Envelope(xMin, yMin, xMin + GRID_SIZE, yMin + GRID_SIZE);
    }

    public static Envelope getGridSquare(Envelope gridEnvelopeUTM, String xLocation, String yLocation, ProjectionTransformation transformationTo4326) {
        double xShift = Math.pow(10, 5 - xLocation.length());
        double yShift = Math.pow(10, 5 - yLocation.length());
        double numericalLocationX = Double.parseDouble(xLocation) * xShift;
        double numericalLocationY = Double.parseDouble(yLocation) * yShift;

        Envelope utmEnvelope = new Envelope();
        gridEnvelopeUTM.queryEnvelope(utmEnvelope);

        utmEnvelope.setXMin(gridEnvelopeUTM.getXMin() + numericalLocationX);
        utmEnvelope.setYMin(gridEnvelopeUTM.getYMin() + numericalLocationY);

        utmEnvelope.setXMax(gridEnvelopeUTM.getXMin() + xShift + numericalLocationX);
        utmEnvelope.setYMax(gridEnvelopeUTM.getYMin() + yShift + numericalLocationY);

        return utmEnvelope;
    }

    public static Polygon getMGRSPolygon(String mgrsCode, boolean bWGS84Result) {
        Polygon result = new Polygon();
        String[] mgrsParts = mgrsCode.split("[ ]+");
        if (mgrsParts.length == 0)
            return result;

        Envelope zoneEnvelope = getZoneEnvelope(mgrsParts[0]);
        int utmZone = getUTMZone(mgrsParts[0]);
        SpatialReference utmSpatialReference = SpatialReference.createUTM(utmZone, zoneEnvelope.getCenter().getY() < 0);
        ProjectionTransformation transformationTo4326 = new ProjectionTransformation(utmSpatialReference, SpatialReference.create(4326));

        if (mgrsParts.length == 1 && bWGS84Result) {
            result.addEnvelope(zoneEnvelope, false);
            return result;
        } else if (mgrsParts.length == 1){
            return (Polygon)OperatorProject.local().execute(zoneEnvelope, transformationTo4326.getReverse(), null);
        }

        Envelope utmGridEnvelope = getSquareIdEnvelope(mgrsParts[1], utmZone, zoneEnvelope, transformationTo4326);
        if (mgrsParts.length == 2 && !bWGS84Result) {
            result.addEnvelope(utmGridEnvelope, false);
            return result;
        } else if (mgrsParts.length == 2) {
            return (Polygon)OperatorProject.local().execute(utmGridEnvelope, transformationTo4326, null);
        }

        Envelope utmGridSquare = getGridSquare(utmGridEnvelope, mgrsParts[2], mgrsParts[3], transformationTo4326);
        if (!bWGS84Result) {
            result.addEnvelope(utmGridSquare, false);
            return result;
        }
        return (Polygon)OperatorProject.local().execute(utmGridSquare, transformationTo4326, null);

    }

    public static SpatialReference getMGRSSpatialReference(String mgrsCode) {
        String[] mgrsParts = mgrsCode.split("[ ]+");
        Envelope zoneEnvelope = getZoneEnvelope(mgrsParts[0]);
        int utmZone = getUTMZone(mgrsParts[0]);
        return SpatialReference.createUTM(utmZone, zoneEnvelope.getCenter().getY() < 0);
    }
}
