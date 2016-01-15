package core.load_data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;

public class Functions {
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

	// returns the computed gridSize based on
	// - precision per number of max coordinates increments (X or Y)
	// - gridSize is next power of two
	public static long computeGridSize(final Coordinate minCoords,
			final Coordinate maxCoords, final Double precisionPerCell) {

		double incX = Math.abs(minCoords.x - maxCoords.x);
		double incY = Math.abs(minCoords.y - maxCoords.y);
		double inc = Math.max(incX, incY);

		double tmpNumOfCells = inc / precisionPerCell;

		long ret = computeNextPowerOfTwo(tmpNumOfCells);

		return ret;
	}

	public static long computeNextPowerOfTwo(final double size) {
		int countPow = 0;
		BigInteger me = BigDecimal.valueOf(size).toBigInteger();
		BigInteger tmp = me;

		while (tmp.doubleValue() > 0) {
			countPow++;
			tmp = me.shiftRight(countPow);
		}

		BigInteger tmpRet = BigInteger.valueOf(1L).shiftLeft(countPow);
		// CUIDADO QUE O LONG É APENAS 2^63 - 1 .... logo ajustamos para algo
		// possível
		long ret = tmpRet.doubleValue() >= Long.MAX_VALUE ? BigInteger
				.valueOf(1L).shiftLeft(62).longValue() : tmpRet.longValue();

		return ret;
	}

	public static Coordinate[] convertCoordinates(
			final Coordinate[] coordinates, final Coordinate minCoordinate,
			final Double resolution) {

		BigDecimal bigResolution = BigDecimal.valueOf(resolution);

		BigDecimal minX = BigDecimal.valueOf(minCoordinate.x);
		minX = minX.divide(bigResolution);
		minX = minX.setScale(0, RoundingMode.FLOOR);
		minX = minX.multiply(bigResolution);

		BigDecimal minY = BigDecimal.valueOf(minCoordinate.y);
		minY = minY.divide(bigResolution);
		minY = minY.setScale(0, RoundingMode.FLOOR);
		minY = minY.multiply(bigResolution);

		Coordinate[] result = new Coordinate[coordinates.length];

		BigDecimal resDiv2 = bigResolution.divide(BigDecimal.valueOf(2));

		for (int i = 0; i < coordinates.length; i++) {

			BigDecimal x = BigDecimal.valueOf(coordinates[i].x);
			x = x.subtract(minX);
			x = x.divide(bigResolution);
			x = x.setScale(0, RoundingMode.FLOOR);
			x = x.multiply(bigResolution);
			x = x.add(minX);
			x = x.add(resDiv2);

			BigDecimal y = BigDecimal.valueOf(coordinates[i].y);
			y = y.subtract(minY);
			y = y.divide(bigResolution);
			y = y.setScale(0, RoundingMode.FLOOR);
			y = y.multiply(bigResolution);
			y = y.add(minY);
			y = y.add(resDiv2);

			// BigDecimal tempX = x.add(minX).add(resDiv2);
			// BigDecimal tempY = y.add(minY).add(resDiv2);

			result[i] = new Coordinate(x.doubleValue(), y.doubleValue());
		}
		return result;
	}

	public static Geometry convertToUp(final Geometry geometry,
			final Coordinate minCoordinate, final Double precision,
			final Long currentGridSize, final Long nextGridSize) {

		BigDecimal resolution = BigDecimal.valueOf(precision);
		resolution = resolution.multiply(BigDecimal.valueOf(currentGridSize));
		resolution = resolution.divide(BigDecimal.valueOf(nextGridSize));
		
		Coordinate[] result = convertCoordinates(geometry.getCoordinates(),
				minCoordinate, resolution.doubleValue());

		// TODO Only works with points.
		Geometry ret = new GeometryFactory().createPoint(result[0]);
		return ret;
	}

	public static double findBestPrecision(final double currentPrecision,
			final Geometry geom) {
		Double x = geom.getCoordinate().x;
		Double y = geom.getCoordinate().y;

		String tmpX = x.toString().split("\\.")[1];
		String tmpY = y.toString().split("\\.")[1];

		String tmp = tmpX.length() > tmpY.length() ? tmpX : tmpY;
		double prec = tmp.equals("0") ? 1 : Math.pow(10, -tmp.length());

		return currentPrecision < prec ? currentPrecision : prec;
	}
	
	
	public static boolean areEqual(int[] time1, int[] time2) {
		for (int i=0; i<time1.length; i++) {
			int o1 = time1[i];
			int o2 = time2[i];

			if (o1 != o2)
				return false;
		}
		return true;
	}
	
	
	public static boolean isPrevDate(String date1, String date2){
		Date d1 = createNewDate(date1);
		Date d2 = createNewDate(date2);
		if (d1.compareTo(d2) <= 0) return true;
		return false;
	}
	
	
	
	public static Date createNewDate(String date) {
		Date d = null;
		try {
			d = format.parse(date);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	public static int getDaysBetweenDates(Date d1, Date d2){
		long diff = d2.getTime() - d1.getTime();
		return (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}
	
	
	public static String getNextDate(String curDate) {
		Date date = null;
		try {
			date = format.parse(curDate);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		return format.format(calendar.getTime()); 
	}
	
	
	public static String[] getAllDates (String dateInit, String dateEnd, String grain) {
		Date d1 = createNewDate(dateInit);
		Date d2 = createNewDate(dateEnd);

		int nDays = getDaysBetweenDates(d1, d2);
		String [] dates = new String [nDays+1];
		dates[0] = dateInit;
		dates[nDays] = dateEnd;
		
		for (int i = 1; i < nDays ; i++){
			Date d = null;
			try {
				d = format.parse(dates[i-1]);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(d);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			dates[i]=format.format(calendar.getTime());
			
		}
		return dates;
	}
	
	
	
	private static void printAllDates(String[] dates){
		for (int i = 0; i < dates.length; i++){
			System.out.println(dates[i]);
		}
	}

	public static double[] calcPointInGrid(double refx, double refy, long row, long column, double width){
		double cx = (refx + width*row) + width/2;
		double cy = (refy + width*column) + width/2;
		return new double[]{cx, cy};
	}
	
	
	public static void main(final String[] args) throws ParseException {
		Date d1 = createNewDate("2007/01/01");
		Date d2 = createNewDate("2007/01/15");
		
		Date d3 = createNewDate("2007/12/29");
		Date d4 = createNewDate("2008/01/01");
		System.out.println(getDaysBetweenDates(d1,d2));
		System.out.println(getDaysBetweenDates(d3,d4)+"\n");
		
		System.out.println(getNextDate("2008/01/01"));
		System.out.println(getNextDate("2008/02/29")+"\n");
		
		String[] dates = getAllDates("2007/12/29", "2008/01/05","day");
		printAllDates(dates);
		
		System.out.println(dates.length);
		
		System.out.println(calcPointInGrid(-9.494435,37.01123,286,726,0.0050108828125)[0]);
		System.out.println(calcPointInGrid(-9.494435,37.01123,286,726,0.0050108828125)[1]);
		
		
		// BigDecimal res = BigDecimal.valueOf(0.0016);
		//
		// BigDecimal minY = BigDecimal.valueOf(18.5568999999999);
		// System.out.println(minY);
		// minY = minY.divide(res);
		// System.out.println(minY);
		// minY = minY.setScale(0, RoundingMode.FLOOR);
		// System.out.println(minY);
		// minY = minY.multiply(res);
		// System.out.println(minY);
		//
		// BigDecimal y = BigDecimal.valueOf(19.4848);
		//
		// BigDecimal resDiv2 = res.divide(BigDecimal.valueOf(2));
		//
		// BigDecimal part1 = y.subtract(minY).divide(res);
		// System.out.println(part1);
		// BigDecimal part2 = part1.setScale(0, RoundingMode.FLOOR);
		// System.out.println(part2);
		//
		// part2 = part2.multiply(res);
		// System.out.println(part2);
		//
		// part2 = part2.add(minY);
		// System.out.println(part2);
		//
		// part2 = part2.add(resDiv2);
		// System.out.println(part2);

		// Geometry g = new
		// WKTReader().read("POINT (-73.75367781 41.45330531)");
		// Coordinate min = new Coordinate(-159.3530138, 19.58396668);
		// Geometry g = new WKTReader().read("POINT (36.247751 -115.323697)");
		// Coordinate min = new Coordinate(-180, -90);
		// System.out.println(g.toText());
		// System.out.println(convertToUp(g, min, 1e-6,
		// Zoom.Level_18.getGridSize(), Zoom.Level_10.getGridSize())
		// .toText());

		// System.out.println(convertToUp(g, min, 0.000001,
		// (long) Math.pow(2, 26), (long) Math.pow(2, 18)).toText());
		// Geometry temp = g;
		// double precision = 0.000001;
		// for (int i = 18; i > 0; i--) {
		//
		// Geometry r = convertToUp(temp, min, precision,
		// Zoom.values()[i].getGridSize(),
		// Zoom.values()[i - 1].getGridSize());
		// System.out.println("-----------");
		// System.out.println(Zoom.values()[i].name() + " " + temp.toText());
		// System.out.println(Zoom.values()[i - 1].name() + " " + r.toText());
		// temp = r;
		// precision = precision * Zoom.values()[i].getGridSize()
		// / Zoom.values()[i - 1].getGridSize();
		// }
	}

}
