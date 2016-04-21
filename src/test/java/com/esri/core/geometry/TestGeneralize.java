package com.esri.core.geometry;

import junit.framework.TestCase;

import org.junit.Test;

public class TestGeneralize extends TestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public static void test1() {
		OperatorFactoryLocal engine = OperatorFactoryLocal.getInstance();
		OperatorGeneralize op = (OperatorGeneralize) engine
				.getOperator(Operator.Type.Generalize);

		Polygon poly = new Polygon();
		poly.startPath(0, 0);
		poly.lineTo(1, 1);
		poly.lineTo(2, 0);
		poly.lineTo(3, 2);
		poly.lineTo(4, 1);
		poly.lineTo(5, 0);
		poly.lineTo(5, 10);
		poly.lineTo(0, 10);
		Geometry geom = op.execute(poly, 2, true, null);
		Polygon p = (Polygon) geom;
		Point2D[] points = p.getCoordinates2D();
		assertTrue(points.length == 4);
		assertTrue(points[0].x == 0 && points[0].y == 0);
		assertTrue(points[1].x == 5 && points[1].y == 0);
		assertTrue(points[2].x == 5 && points[2].y == 10);
		assertTrue(points[3].x == 0 && points[3].y == 10);

		Geometry geom1 = op.execute(geom, 5, false, null);
		p = (Polygon) geom1;
		points = p.getCoordinates2D();
		assertTrue(points.length == 3);
		assertTrue(points[0].x == 0 && points[0].y == 0);
		assertTrue(points[1].x == 5 && points[1].y == 10);
		assertTrue(points[2].x == 5 && points[2].y == 10);

		geom1 = op.execute(geom, 5, true, null);
		p = (Polygon) geom1;
		points = p.getCoordinates2D();
		assertTrue(points.length == 0);
	}

	@Test
	public static void test2() {
		OperatorFactoryLocal engine = OperatorFactoryLocal.getInstance();
		OperatorGeneralize op = (OperatorGeneralize) engine
				.getOperator(Operator.Type.Generalize);

		Polyline polyline = new Polyline();
		polyline.startPath(0, 0);
		polyline.lineTo(1, 1);
		polyline.lineTo(2, 0);
		polyline.lineTo(3, 2);
		polyline.lineTo(4, 1);
		polyline.lineTo(5, 0);
		polyline.lineTo(5, 10);
		polyline.lineTo(0, 10);
		Geometry geom = op.execute(polyline, 2, true, null);
		Polyline p = (Polyline) geom;
		Point2D[] points = p.getCoordinates2D();
		assertTrue(points.length == 4);
		assertTrue(points[0].x == 0 && points[0].y == 0);
		assertTrue(points[1].x == 5 && points[1].y == 0);
		assertTrue(points[2].x == 5 && points[2].y == 10);
		assertTrue(points[3].x == 0 && points[3].y == 10);

		Geometry geom1 = op.execute(geom, 5, false, null);
		p = (Polyline) geom1;
		points = p.getCoordinates2D();
		assertTrue(points.length == 2);
		assertTrue(points[0].x == 0 && points[0].y == 0);
		assertTrue(points[1].x == 0 && points[1].y == 10);

		geom1 = op.execute(geom, 5, true, null);
		p = (Polyline) geom1;
		points = p.getCoordinates2D();
		assertTrue(points.length == 2);
		assertTrue(points[0].x == 0 && points[0].y == 0);
		assertTrue(points[1].x == 0 && points[1].y == 10);
	}

	@Test
	public static void testLargeDeviation() {
		{
			Polygon input_polygon = new Polygon();
			input_polygon
					.addEnvelope(Envelope2D.construct(0, 0, 20, 10), false);
			Geometry densified_geom = OperatorDensifyByLength.local().execute(
					input_polygon, 1, null);
			Geometry geom = OperatorGeneralize.local().execute(densified_geom,
					1, true, null);
			int pc = ((MultiPath) geom).getPointCount();
			assertTrue(pc == 4);

			Geometry large_dev1 = OperatorGeneralize.local().execute(
					densified_geom, 40, true, null);
			int pc1 = ((MultiPath) large_dev1).getPointCount();
			assertTrue(pc1 == 0);
			
			Geometry large_dev2 = OperatorGeneralize.local().execute(
					densified_geom, 40, false, null);
			int pc2 = ((MultiPath) large_dev2).getPointCount();
			assertTrue(pc2 == 3);
		}
	}

	@Test
	public static void test1Area() {
		OperatorFactoryLocal engine = OperatorFactoryLocal.getInstance();
		OperatorGeneralizeByArea op = (OperatorGeneralizeByArea) engine.getOperator(Operator.Type.GeneralizeArea);

		assertNotNull(op);

		Polygon poly = new Polygon();
		poly.startPath(0, 0);
		poly.lineTo(1, 1);
		poly.lineTo(2, 0);
		poly.lineTo(3, 2);
		poly.lineTo(4, 1);
		poly.lineTo(5, 0);
		poly.lineTo(5, 10);
		poly.lineTo(0, 10);
		String words2 = GeometryEngine.geometryToWkt(poly, 0);
		Geometry geom = op.execute(poly, 5, true, GeneralizeType.Neither, null);
		String words = GeometryEngine.geometryToWkt(geom, 0);
		assertNotNull(geom);
		Polygon p = (Polygon) geom;
		Point2D[] points = p.getCoordinates2D();
		assertTrue(points.length == 4);
		assertTrue(points[0].x == 0 && points[0].y == 0);
		assertTrue(points[1].x == 5 && points[1].y == 0);
		assertTrue(points[2].x == 5 && points[2].y == 10);
		assertTrue(points[3].x == 0 && points[3].y == 10);

		OperatorContains operatorContains = (OperatorContains)OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Contains);

		Geometry geomContainer = op.execute(poly, 10, true, GeneralizeType.ResultContainsOriginal, null);
		assertTrue(operatorContains.execute(geomContainer, poly, null, null));

		Geometry geomContained = op.execute(poly, 5, true, GeneralizeType.ResultWithinOriginal, null);
		assertTrue(operatorContains.execute(poly, geomContained, null, null));
	}

	@Test
	public static void testCorruptedTreapSearch() {
		String wktInput = "MULTIPOLYGON (((5 2, 5.1308062584602858 2.0042821535227944, 5.2610523844401031 2.0171102772523808, 5.3901806440322559 2.0384294391935405, 5.5176380902050415 2.0681483474218645, 5.6428789306063223 2.1061397410097897, 5.7653668647301792 2.1522409349774274, 5.8845773804380022 2.2062545169346244, 5.9999999999999991 2.2679491924311233, 6.1111404660392035 2.3370607753949102, 6.2175228580174409 2.4132933194175301, 6.3186916302001368 2.4963203850420457, 6.414213562373094 2.5857864376269051, 6.5036796149579548 2.6813083697998623, 6.5867066805824699 2.7824771419825591, 6.6629392246050898 2.8888595339607956, 6.7320508075688767 3, 6.7937454830653765 3.1154226195619974, 6.847759065022573 3.2346331352698208, 6.8938602589902107 3.3571210693936768, 6.9318516525781364 3.4823619097949585, 6.9615705608064609 3.6098193559677436, 6.982889722747621 3.7389476155598969, 6.9957178464772074 3.8691937415397137, 7 4, 6.9992288413054364 4.0555341344807063, 6.6947736951561883 15.015919395853626, 11.905851560026523 31.393592685446105, 11.931851652578136 31.48236190979496, 11.961570560806461 31.609819355967744, 11.982889722747622 31.738947615559898, 11.995717846477207 31.869193741539714, 12 32, 11.995717846477206 32.130806258460282, 11.982889722747618 32.261052384440106, 11.961570560806459 32.390180644032256, 11.931851652578136 32.51763809020504, 11.893860258990211 32.642878930606322, 11.847759065022572 32.765366864730183, 11.793745483065376 32.884577380438003, 11.732050807568877 33, 11.66293922460509 33.111140466039203, 11.6 33.200000000000003, 5.5999999999999996 41.200000000000003, 5.5867066805824699 41.217522858017439, 5.5036796149579548 41.318691630200135, 5.4142135623730949 41.414213562373092, 5.3186916302001377 41.503679614957953, 5.2175228580174409 41.586706680582466, 5.1111404660392044 41.66293922460509, 5 41.732050807568875, 4.8845773804380022 41.793745483065379, 4.7653668647301792 41.847759065022572, 4.6428789306063232 41.893860258990209, 4.5176380902050415 41.931851652578139, 4.3901806440322568 41.961570560806464, 4.2610523844401031 41.982889722747622, 4.1308062584602858 41.995717846477206, 4 42, 3.8691937415397142 41.995717846477206, 3.7389476155598973 41.982889722747622, 3.6098193559677441 41.961570560806457, 3.482361909794959 41.931851652578132, 3.3571210693936773 41.893860258990209, 3.2346331352698212 41.847759065022572, 3.1154226195619983 41.793745483065379, 3.0000000000000009 41.732050807568875, 2.8888595339607965 41.66293922460509, 2.7824771419825591 41.586706680582466, 2.6813083697998632 41.503679614957953, 2.5857864376269055 41.414213562373092, 2.4963203850420457 41.318691630200135, 2.4132933194175301 41.217522858017439, 2.3370607753949102 41.111140466039203, 2.2679491924311233 41, 2.2062545169346235 40.884577380438003, 2.152240934977427 40.765366864730183, 2.1061397410097888 40.642878930606322, 2.0681483474218636 40.51763809020504, 2.0384294391935391 40.390180644032256, 2.017110277252379 40.261052384440106, 2.004282153522793 40.130806258460289, 2 40, 2.0007711586945636 39.944465865519291, 2.6774890785664391 15.582620750131777, 1.094148439973476 10.606407314553895, 1.0681483474218636 10.517638090205041, 1.0384294391935391 10.390180644032256, 1.017110277252379 10.261052384440102, 1.004282153522793 10.130806258460286, 1 10, 1.0042821535227944 9.8691937415397142, 1.0171102772523808 9.7389476155598977, 1.0384294391935405 9.6098193559677441, 1.0681483474218645 9.4823619097949585, 1.1061397410097897 9.3571210693936777, 1.1522409349774274 9.2346331352698208, 1.2062545169346244 9.115422619561997, 1.2679491924311233 9, 1.3370607753949102 8.8888595339607974, 1.4132933194175301 8.7824771419825591, 1.4963203850420457 8.6813083697998632, 1.5857864376269051 8.585786437626906, 1.6813083697998623 8.4963203850420452, 1.7824771419825591 8.4132933194175301, 1.8888595339607956 8.3370607753949102, 2 8.2679491924311233, 2.1154226195619974 8.2062545169346244, 2.2346331352698208 8.1522409349774279, 2.3571210693936768 8.1061397410097893, 2.4823619097949585 8.0681483474218645, 2.6098193559677436 8.0384294391935391, 2.7389476155598969 8.0171102772523781, 2.8691937415397137 8.0042821535227926, 2.888015599689953 8.0036659896852811, 3.0007711586945636 3.9444658655192932, 3.0042821535227944 3.8691937415397142, 3.0171102772523808 3.7389476155598973, 3.0384294391935405 3.6098193559677441, 3.0681483474218645 3.482361909794959, 3.1061397410097897 3.3571210693936773, 3.1522409349774274 3.2346331352698212, 3.2062545169346244 3.1154226195619983, 3.2679491924311233 3.0000000000000009, 3.3370607753949102 2.8888595339607965, 3.4132933194175301 2.7824771419825591, 3.4963203850420457 2.6813083697998632, 3.5857864376269051 2.5857864376269055, 3.6813083697998623 2.4963203850420457, 3.7824771419825591 2.4132933194175301, 3.8888595339607956 2.3370607753949102, 4 2.2679491924311233, 4.1154226195619978 2.2062545169346235, 4.2346331352698208 2.152240934977427, 4.3571210693936768 2.1061397410097888, 4.4823619097949585 2.0681483474218636, 4.6098193559677432 2.0384294391935391, 4.7389476155598969 2.017110277252379, 4.8691937415397142 2.004282153522793, 5 2), (6.3577402241893219 27.149124350660838, 6.1738780506195123 33.768162599173976, 7.7816940790703999 31.624407894572798, 6.3577402241893219 27.149124350660838)))\n" +
				"MULTIPOLYGON (((5 -2, 5.1308062584602858 -1.9957178464772056, 5.2610523844401031 -1.9828897227476183, 5.3901806440322559 -1.9615705608064609, 5.5176380902050415 -1.9318516525781355, 5.6428789306063223 -1.8938602589902089, 5.7427813527082074 -1.8569533817705199, 30.742781352708207 8.1430466182294818, 30.765366864730179 8.1522409349774279, 30.884577380438003 8.2062545169346244, 31 8.2679491924311233, 31.111140466039203 8.3370607753949102, 31.217522858017439 8.4132933194175301, 31.318691630200139 8.4963203850420452, 31.414213562373096 8.585786437626906, 31.503679614957953 8.6813083697998632, 31.58670668058247 8.7824771419825591, 31.66293922460509 8.8888595339607956, 31.732050807568879 9, 31.788854381999833 9.1055728090000834, 36.788854381999833 19.105572809000083, 36.793745483065379 19.115422619561997, 36.847759065022572 19.234633135269821, 36.893860258990209 19.357121069393678, 36.931851652578139 19.48236190979496, 36.961570560806464 19.609819355967744, 36.982889722747622 19.738947615559898, 36.995717846477206 19.869193741539714, 37 20, 36.995717846477206 20.130806258460286, 36.982889722747622 20.261052384440102, 36.961570560806457 20.390180644032256, 36.931851652578132 20.51763809020504, 36.893860258990209 20.642878930606322, 36.847759065022572 20.765366864730179, 36.793745483065379 20.884577380438003, 36.788854381999833 20.894427190999917, 31.788854381999833 30.894427190999917, 31.732050807568875 31, 31.66293922460509 31.111140466039203, 31.58670668058247 31.217522858017439, 31.503679614957953 31.318691630200135, 31.414213562373096 31.414213562373092, 31.318691630200139 31.503679614957953, 31.217522858017443 31.58670668058247, 31.111140466039203 31.66293922460509, 31 31.732050807568875, 30.884577380438003 31.793745483065376, 30.765366864730179 31.847759065022572, 30.642878930606322 31.893860258990209, 30.568176659382747 31.917596225416773, 3.5681766593827478 39.917596225416773, 3.5176380902050415 39.931851652578132, 3.3901806440322564 39.961570560806464, 3.2610523844401031 39.982889722747622, 3.1308062584602863 39.995717846477206, 3 40, 2.8691937415397142 39.995717846477206, 2.7389476155598973 39.982889722747615, 2.6098193559677441 39.961570560806464, 2.482361909794959 39.931851652578132, 2.3571210693936773 39.893860258990209, 2.2346331352698212 39.847759065022572, 2.1154226195619983 39.793745483065379, 2.0000000000000009 39.732050807568875, 1.8888595339607963 39.66293922460509, 1.7824771419825594 39.586706680582466, 1.681308369799863 39.503679614957953, 1.5857864376269055 39.414213562373092, 1.4963203850420457 39.318691630200135, 1.4132933194175301 39.217522858017446, 1.33706077539491 39.111140466039203, 1.267949192431123 39, 1.2062545169346237 38.884577380438003, 1.1522409349774267 38.765366864730183, 1.1061397410097888 38.642878930606322, 1.0681483474218634 38.51763809020504, 1.0384294391935391 38.390180644032256, 1.0171102772523792 38.261052384440106, 1.004282153522793 38.130806258460282, 1 38, 1.0042821535227944 37.869193741539718, 1.0171102772523806 37.738947615559894, 1.0384294391935405 37.609819355967744, 1.0681483474218647 37.48236190979496, 1.1061397410097897 37.357121069393678, 1.1522409349774274 37.234633135269817, 1.2062545169346244 37.115422619561997, 1.2679491924311235 37, 1.3370607753949102 36.888859533960797, 1.4132933194175301 36.782477141982561, 1.4963203850420457 36.681308369799865, 1.5857864376269053 36.585786437626908, 1.6813083697998625 36.496320385042047, 1.7824771419825589 36.413293319417534, 1.8888595339607956 36.33706077539491, 2 36.267949192431118, 2.1154226195619974 36.206254516934621, 2.2346331352698208 36.152240934977428, 2.3571210693936768 36.106139741009784, 2.4318233406172522 36.082403774583227, 28.599409577746219 28.329044889507976, 32.763932022500207 20, 28.551206229908889 11.574548414817356, 6.3845784837230717 2.7078973163430327, 6.3268887828044376 2.9386561200175683, 19.26118525018893 13.447771999767468, 19.318691630200139 13.496320385042045, 19.414213562373096 13.585786437626906, 19.503679614957953 13.681308369799863, 19.58670668058247 13.782477141982559, 19.66293922460509 13.888859533960796, 19.732050807568879 14, 19.793745483065376 14.115422619561997, 19.847759065022572 14.234633135269821, 19.893860258990212 14.357121069393678, 19.931851652578136 14.482361909794959, 19.961570560806461 14.609819355967744, 19.982889722747622 14.738947615559898, 19.995717846477206 14.869193741539714, 20 15, 19.995717846477206 15.130806258460286, 19.982889722747618 15.261052384440102, 19.961570560806461 15.390180644032256, 19.931851652578136 15.517638090205041, 19.893860258990209 15.642878930606322, 19.847759065022572 15.765366864730179, 19.793745483065376 15.884577380438001, 19.732050807568875 16, 19.66293922460509 16.111140466039203, 19.58670668058247 16.217522858017439, 19.503679614957953 16.318691630200135, 19.414213562373096 16.414213562373092, 19.318691630200139 16.503679614957953, 19.217522858017443 16.58670668058247, 19.111140466039203 16.66293922460509, 19 16.732050807568875, 18.884577380438003 16.793745483065376, 18.765366864730179 16.847759065022572, 18.642878930606322 16.893860258990209, 18.51763809020504 16.931851652578139, 18.390180644032256 16.961570560806461, 18.261052384440102 16.982889722747622, 18.130806258460286 16.995717846477206, 18 17, 17.869193741539714 16.995717846477206, 17.738947615559898 16.982889722747622, 17.609819355967744 16.961570560806457, 17.48236190979496 16.931851652578136, 17.357121069393678 16.893860258990209, 17.234633135269821 16.847759065022572, 17.115422619561997 16.793745483065376, 17 16.732050807568875, 16.888859533960797 16.66293922460509, 16.782477141982561 16.58670668058247, 16.73881474981107 16.552228000232532, 5.2559522566699819 7.2224022245553954, 1.9402850002906638 20.485071250072664, 1.9318516525781353 20.51763809020504, 1.8938602589902103 20.642878930606322, 1.8477590650225726 20.765366864730179, 1.7937454830653756 20.884577380438003, 1.7320508075688765 21, 1.6629392246050898 21.111140466039203, 1.5867066805824699 21.217522858017439, 1.5036796149579543 21.318691630200139, 1.4142135623730947 21.414213562373096, 1.3186916302001375 21.503679614957953, 1.2175228580174411 21.58670668058247, 1.1111404660392044 21.66293922460509, 0.99999999999999989 21.732050807568879, 0.88457738043800249 21.793745483065376, 0.76536686473017945 21.847759065022572, 0.64287893060632306 21.893860258990212, 0.51763809020504148 21.931851652578136, 0.3901806440322565 21.961570560806461, 0.26105238444010315 21.982889722747622, 0.13080625846028612 21.995717846477206, 0 22, -0.13080625846028585 21.995717846477206, -0.26105238444010281 21.982889722747618, -0.39018064403225611 21.961570560806461, -0.51763809020504103 21.931851652578136, -0.64287893060632262 21.893860258990209, -0.7653668647301789 21.847759065022572, -0.88457738043800183 21.793745483065376, -0.99999999999999922 21.732050807568875, -1.1111404660392037 21.66293922460509, -1.2175228580174406 21.58670668058247, -1.318691630200137 21.503679614957953, -1.4142135623730945 21.414213562373096, -1.5036796149579543 21.318691630200139, -1.5867066805824699 21.217522858017443, -1.66293922460509 21.111140466039203, -1.732050807568877 21, -1.7937454830653763 20.884577380438003, -1.8477590650225733 20.765366864730179, -1.8938602589902112 20.642878930606322, -1.9318516525781366 20.51763809020504, -1.9615705608064609 20.390180644032256, -1.9828897227476208 20.261052384440102, -1.995717846477207 20.130806258460286, -2 20, -1.9957178464772056 19.869193741539714, -1.9828897227476194 19.738947615559898, -1.9615705608064595 19.609819355967744, -1.9402850002906638 19.514928749927336, 1.4516369470040571 5.947240960748454, 1.3901806440322564 5.9615705608064609, 1.2610523844401031 5.982889722747621, 1.130806258460286 5.9957178464772074, 1 6, 0.86919374153971418 5.9957178464772056, 0.73894761555989719 5.9828897227476192, 0.60981935596774384 5.9615705608064591, 0.48236190979495897 5.9318516525781355, 0.35712106939367738 5.8938602589902107, 0.2346331352698211 5.8477590650225721, 0.11542261956199817 5.7937454830653756, 7.7715611723760958E-16 5.7320508075688767, -0.11114046603920369 5.6629392246050898, -0.21752285801744065 5.5867066805824699, -0.31869163020013702 5.5036796149579548, -0.41421356237309448 5.4142135623730949, -0.50367961495795432 5.3186916302001377, -0.58670668058246989 5.2175228580174409, -0.66293922460509003 5.1111404660392044, -0.73205080756887697 5, -0.79374548306537629 4.8845773804380022, -0.84775906502257325 4.7653668647301792, -0.89386025899021115 4.6428789306063232, -0.93185165257813662 4.5176380902050415, -0.96157056080646086 4.3901806440322568, -0.98288972274762076 4.2610523844401031, -0.99571784647720696 4.1308062584602858, -1 4, -0.99571784647720563 3.8691937415397142, -0.98288972274761943 3.7389476155598973, -0.96157056080645953 3.6098193559677441, -0.93185165257813529 3.482361909794959, -0.89386025899021027 3.3571210693936773, -0.84775906502257259 3.2346331352698212, -0.79374548306537562 3.1154226195619983, -0.78885438199983171 3.1055728090000843, 0.21114561800016829 1.1055728090000843, 0.26794919243112347 1.0000000000000009, 0.33706077539491019 0.88885953396079653, 0.41329331941753011 0.78247714198255913, 0.49632038504204568 0.6813083697998632, 0.5857864376269053 0.58578643762690552, 0.68130836979986253 0.49632038504204568, 0.78247714198255891 0.41329331941753011, 0.88885953396079564 0.33706077539491019, 1 0.26794919243112325, 1.1154226195619974 0.20625451693462349, 1.2346331352698205 0.15224093497742697, 1.3571210693936768 0.10613974100978885, 1.4823619097949585 0.068148347421863598, 1.6098193559677436 0.038429439193539139, 1.7389476155598969 0.017110277252379014, 1.8691937415397137 0.0042821535227930418, 2 0, 2.1308062584602858 0.0042821535227943741, 2.2610523844401027 0.01711027725238079, 2.3901806440322559 0.038429439193540471, 2.517638090205041 0.068148347421864486, 2.6428789306063227 0.10613974100978973, 2.7653668647301788 0.15224093497742741, 2.8845773804380017 0.20625451693462438, 2.8866117144161265 0.20734189110017509, 3.0597149997093362 -0.48507125007266438, 3.0681483474218645 -0.5176380902050397, 3.1061397410097897 -0.64287893060632229, 3.1522409349774274 -0.76536686473017923, 3.2062545169346244 -0.88457738043800305, 3.2679491924311233 -1, 3.3370607753949102 -1.1111404660392026, 3.4132933194175301 -1.2175228580174391, 3.4963203850420457 -1.3186916302001386, 3.5857864376269051 -1.4142135623730958, 3.6813083697998623 -1.503679614957953, 3.7824771419825591 -1.5867066805824699, 3.8888595339607956 -1.6629392246050898, 4 -1.7320508075688785, 4.1154226195619978 -1.7937454830653756, 4.2346331352698208 -1.8477590650225721, 4.3571210693936768 -1.8938602589902125, 4.4823619097949585 -1.9318516525781355, 4.6098193559677432 -1.9615705608064609, 4.7389476155598969 -1.9828897227476219, 4.8691937415397142 -1.9957178464772056, 5 -2)))";
		Geometry poly = GeometryEngine.geometryFromWkt(wktInput, 0, Geometry.Type.Unknown);
		OperatorFactoryLocal engine = OperatorFactoryLocal.getInstance();
		OperatorGeneralizeByArea op = (OperatorGeneralizeByArea) engine.getOperator(Operator.Type.GeneralizeArea);
		Geometry geom = op.execute(poly, 5, true, GeneralizeType.Neither, null);
	}
}
