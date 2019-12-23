package com.esri.core.geometry;

public class MGRS {

    /**
     * The 6° wide UTM zones, numbered 1–60, are intersected by latitude bands that are normally 8° high,
     * lettered C–X (omitting I and O). South of 80°S, UPS South (Universal Polar Stereographic) is used instead
     * of a UTM projection. North of 84°N, UPS North is used.
     */
    final static String[] LAT_BANDS_S_TO_N = new String[] {"C","D","E","F","G","H","J","K","L","M","N","P","Q","R","S","T","U","V","W","X"};

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
        return String.format("%d%s", SpatialReference.getUTMZone(lon), LAT_BANDS_S_TO_N[lat_position]);
    }

    /**
     * The second part of an MGRS coordinate is the 100,000-meter square identification. Each UTM zone is divided into
     * 100,000 meter squares, so that their corners have UTM-coordinates that are multiples of 100,000 meters. The
     * identification consists of a column letter (A–Z, omitting I and O) followed by a row letter (A–V, omitting I
     * and O).
     */
    final static String[] COL_100KM = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    final static String[] ROW_100KM_N = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V"};
    final static String[] ROW_100KM_S = new String[] {"V", "U", "T", "S", "R", "Q", "P", "N", "M", "L", "K", "J", "H", "G", "F", "E", "D", "C", "B", "A"};
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
        int rowIndex = ((int)Math.floor(pt.getY() / GRID_SIZE) + rowOffset) % ROW_100KM_N.length;
        String rowLetter;
        if (lat < 0) {
            rowIndex = (int)(Math.floor((SOUTH_START - pt.getY()) / GRID_SIZE) + (ROW_100KM_S.length - rowOffset)) % ROW_100KM_S.length;
            rowLetter = ROW_100KM_S[rowIndex];
        } else {
            rowLetter = ROW_100KM_N[rowIndex];
        }

        return COL_100KM[colIndex] + rowLetter;
    }
}
