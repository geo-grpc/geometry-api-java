package com.esri.core.geometry;

import org.proj4.*;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import com.jhlabs.map.*;

import java.awt.geom.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by davidraleigh on 11/3/16.
 */
public class TestProjection extends TestCase {
    private static final double PRECESISSION = 1e-3;

    private final List<Projection> inversableProjections = new ArrayList<Projection>();

    @Before
    public void setUp() {
        for (final String projectionName : ProjectionFactory.getOrderedProjectionNames()) {
            final Projection projection = ProjectionFactory.getNamedProjection(projectionName);
            if (!projection.hasInverse()) {
                System.out.println("INFO: Ignore not inverable projection: " + projection);
                continue;
            }
            inversableProjections.add(projection);
        }
        System.out.println();
        System.out.flush();
    }

    @Test
    public void testProjectionAndInverseAreTheSame() throws Exception {
        for (final Projection projection : inversableProjections) {
            try {
                final java.awt.geom.Point2D.Double sourceLL = new java.awt.geom.Point2D.Double(0, 1);
                final java.awt.geom.Point2D.Double projectedXY = projection.project(sourceLL.getX(), sourceLL.getY(), new Point2D.Double());
                final java.awt.geom.Point2D.Double sourceLL2 = projection.projectInverse(projectedXY.getX(), projectedXY.getX(), new java.awt.geom.Point2D.Double());


                System.out.println("INFO: Test inverable projection: " + projection);
                checkAndPrintErrorForProjection(projection, sourceLL.getX(), sourceLL2.getX());
                checkAndPrintErrorForProjection(projection, sourceLL.getY(), sourceLL2.getY());
            } catch (final Exception e) {
                System.err.println("ERROR: Exception for  " + projection);
                e.printStackTrace(System.err);
            }
        }
    }

    @Test
    public void testProj4() throws Exception {
        PJ sourcePJ = new PJ("+init=epsg:32632");                   // (x,y) axis order
        PJ targetPJ = new PJ("+proj=latlong +datum=WGS84");         // (λ,φ) axis order
        double[] coordinates = {
                500000,       0,   // First coordinate
                400000,  100000,   // Second coordinate
                600000, -100000    // Third coordinate
        };
        sourcePJ.transform(targetPJ, 2, coordinates, 0, 3);
        System.out.println(Arrays.toString(coordinates));
    }


    private void checkAndPrintErrorForProjection(final Projection projection, final double expected, final double actual) {
        if (Math.abs(expected - actual) > PRECESISSION) {
            System.err.println("ERROR: Expected " + expected + ", but was " + actual + " for " + projection);
        }
    }
}