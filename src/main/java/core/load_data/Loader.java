package core.load_data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import aa_maps.AAMaps;
import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import core.Config;
import server.Context;

public class Loader {

	public static final int FETCH_SIZE = Config.getConfigInt("fetch_size");
	public static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");
	public Connection connection = DataStoreInfo.getMetaStore();
	private AAMaps map;

	public Loader() {}


	//Method to be called by server
	public String getInstSpatialEvents(Context context, String date, String metric) {
		//Init json constrution
		StringBuilder strBuilder = new StringBuilder();
		String header= "{\"type\":\"FeatureCollection\",\"features\":[";
		String featureTemplate = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[%s, %s]},\"effect\": %s}";
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildInstSpatialQuery(date, context.getFromTable(),
					context.getAAMapsMetaInfo().getAccumulationFunction().getBDFunction(), metric);
			System.out.println("SQL INSTANT SPATIAL EVENTS: " + sql);

			double [] gridInfo = context.getGridInfo(context.getGridSize());
			double longitud, latitude;
			ResultSet resultSet = st.executeQuery(sql);

			while(resultSet.next()) {
				//Calculate the center of cell
				latitude = Double.parseDouble(resultSet.getString(1)) + gridInfo[2]/2;
				longitud = Double.parseDouble(resultSet.getString(2)) + gridInfo[2]/2;

				if (resultSet.isFirst()) strBuilder.append(header);
				strBuilder.append(String.format(featureTemplate, latitude, longitud,resultSet.getString(3)));
				if(!resultSet.isLast()) strBuilder.append(",");
			}
			strBuilder.append("]}");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return strBuilder.toString();
	}


	//Method to be called by server
	public String getRangeSpatialEvents(Context context, String dateInit, String dateEnd, String grain, String metric)  {
		StringBuilder strBuilder = new StringBuilder();
		String header= "{\"type\":\"FeatureCollection\",\"features\":[";
		String featureTemplate = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[%s, %s]},\"effect\": %s}";

		String table = context.getFromTable();
		Statement st;

		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql;

			if (grain.equals("polygon")) sql = buildPolygonRangeSpatialQuery(dateInit, dateEnd, table, metric);
			else sql = buildRangeSpatialQuery(dateInit, dateEnd, table, 
					context.getAAMapsMetaInfo().getAccumulationFunction().getBDFunction(), metric);
			System.out.println("SQL RANGE SPATIAL EVENTS: " + sql);
			double [] gridInfo = context.getGridInfo(context.getGridSize());
			double longitud, latitude;

			ResultSet resultSet = st.executeQuery(sql);
			while(resultSet.next()) {
				//Calculate center of cell
				latitude = Double.parseDouble(resultSet.getString(1)) + gridInfo[2]/2;
				longitud = Double.parseDouble(resultSet.getString(2)) + gridInfo[2]/2;

				if (resultSet.isFirst()) strBuilder.append(header);
				
				strBuilder.append(String.format(featureTemplate, latitude, longitud,resultSet.getString(3)));
				if(!resultSet.isLast()) strBuilder.append(",");
			}
			strBuilder.append("]}");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database connection failed.");
		}
		return strBuilder.toString();
	}
	
	
	
	//Method to be called by server
		public String getRangeCoordSpatialEvents(Context context, String dateInit, String dateEnd)  {
			StringBuilder strBuilder = new StringBuilder();
			String header= "{\"type\":\"FeatureCollection\",\"features\":[";
			String featureTemplate = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[%s, %s]},\"effect\": %s}";
			String table = context.getDefaultTable();
			Statement st;

			try {
				st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				st.setFetchSize(FETCH_SIZE);
				String sql = buildCoordRangeSpatialQuery(dateInit, dateEnd, table);
				System.out.println("SQL COORD RANGE SPATIAL EVENTS: " + sql);
				double [] gridInfo = context.getGridInfo(context.getGridSize());
				double longitud, latitude;

				ResultSet resultSet = st.executeQuery(sql);
				while(resultSet.next()) {
					//Calculate center of cell
					latitude = Double.parseDouble(resultSet.getString(1)) + gridInfo[2]/2;
					longitud = Double.parseDouble(resultSet.getString(2)) + gridInfo[2]/2;

					if (resultSet.isFirst()) strBuilder.append(header);
					strBuilder.append(String.format(featureTemplate, latitude, longitud, resultSet.getString(3)));
					if(!resultSet.isLast()) strBuilder.append(",");
				}
				strBuilder.append("]}");
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Database connection failed.");
			}
			return strBuilder.toString();
		}



	public String getTimeRange(Context context, String timeGranularity) throws SQLException  {
		//Init json constrution
		StringBuilder strBuilder = new StringBuilder();
		String table = context.getTimeDataTable();
		Statement st;
		String max="", min="", header= "{\"range\": [\"%s\", \"%s\"]}";

		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildMinDateQuery(table,timeGranularity);
			//System.out.println("SQL RANGE QUERY: " + sql);
			ResultSet resultSet = st.executeQuery(sql);
			if(resultSet.next()) {
				min = resultSet.getString(1);
				max = resultSet.getString(2);
			}
			strBuilder.append(String.format(header, min, max));

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database connection failed.");
		}
		return strBuilder.toString();
	}



	public ArrayList<String> getDaysInRange(String date1, String date2, String timeGrain) {
		Statement st;
		ArrayList<String> dates = new ArrayList<String>();
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildTimeStepsInRangeQuery(date1, date2, timeGrain);
			//System.out.println("SQL RANGE DAYS QUERY: " + sql);
			ResultSet resultSet = st.executeQuery(sql);
			while(resultSet.next()) dates.add(resultSet.getString(1));

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database connection failed.");
		}
		return dates;
	}


	public String getSpatialGranularities(Context context) {
		String table = context.getSpatialDataTable();
		Statement st;
		StringBuilder strBuilder = new StringBuilder();
		String header= "{\"grains\": [";
		String featureTemplate = "%s";

		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildSpatialGrainsQuery(table);
			//System.out.println("SQL RANGE QUERY: " + sql);
			ResultSet resultSet = st.executeQuery(sql);
			while (resultSet.next()) {
				if (resultSet.isFirst()) strBuilder.append(header);
				strBuilder.append(String.format(featureTemplate, resultSet.getString(1)));
				if(!resultSet.isLast()) strBuilder.append(",");
			}
			strBuilder.append("]}");

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database connection failed.");
		}
		return strBuilder.toString();
	}


	public ArrayList<Integer> getSpatialGranularitiesArray(Context context)  {
		String table = context.getSpatialDataTable();
		Statement st;
		ArrayList<Integer> sgrains = new ArrayList<Integer>();
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildSpatialGrainsQuery(table);
			ResultSet resultSet = st.executeQuery(sql);
			while (resultSet.next()) sgrains.add(resultSet.getInt(1));
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database connection failed.");
		}
		return sgrains;
	}



	/*******************************************************************
	/************************ AA-MAPS METHODS **************************/
	/*******************************************************************/

	public String getFinalAAMapEvents(Context context, String dateInit, String dateEnd) throws SQLException  {
		int gridSize = context.getGridSize();

		//Init json constrution
		StringBuilder strBuilder = new StringBuilder();
		String header= "{\"type\":\"FeatureCollection\",\"maxEffect\": %s,\"minEffect\": %s,\"features\":[";
		String featureTemplate = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[%s, %s]},\"effect\": %s}";

		SparseDoubleMatrix2D finalMatrix = map.getCurMatrix();
		finalMatrix.trimToSize();
		map.saveComputedMatrix();

		IntArrayList rowList = new IntArrayList(), columnList = new IntArrayList();
		DoubleArrayList valueList = new DoubleArrayList();
		finalMatrix.getNonZeros(rowList,columnList,valueList);
		int nElements = valueList.size();
		double maxEffect = map.getMaxEffect(), minEffect = map.getMinEffect();
		strBuilder.append(String.format(header, maxEffect, minEffect));
		double [] gridInfo = context.getGridInfo(gridSize);

		for (int i = 0; i < nElements; i++){
			double curValue = valueList.get(i);
			double[] coords = Functions.calcPointInGrid(gridInfo[0], gridInfo[1], rowList.get(i), columnList.get(i), gridInfo[2]);
			strBuilder.append(String.format(featureTemplate, coords[1], coords[0],curValue));
			if (i < nElements-1) strBuilder.append(",");
		}	
		strBuilder.append("]}");
		return strBuilder.toString();
	}



	public SparseDoubleMatrix2D getCountAccidents(Context context, int year) {
		Statement st;
		SparseDoubleMatrix2D newMatrix = map.createMatrix();
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildCountAccPerYearQuery(context.getFromTable(), year);
			ResultSet resultSet = st.executeQuery(sql);

			while(resultSet.next()) {
				newMatrix.setQuick(resultSet.getInt(1)-1,  resultSet.getInt(2)-1, resultSet.getDouble(3));	
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newMatrix;
	}



	public void getInstAAMapEffects(Context context, String date, int isNextMatrix) {
		Statement st;
		double effect;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			
			String sql = buildAAMatrixCellQuery(date,context.getFromTable(), 
					context.getAAMapsMetaInfo().getAccumulationFunction().getBDFunction(), context.getMetric());
			//System.out.println("SQL INSTANT AAMAPS EFFECTS: " + sql);
			ResultSet resultSet = st.executeQuery(sql);

			SparseDoubleMatrix2D newMatrix = map.createMatrix();
			while(resultSet.next()) {

				effect = resultSet.getDouble(3);
				if (resultSet.isFirst()) map.setNewMin(effect);
				else if (resultSet.isLast()) map.setNewMax(effect);
				newMatrix.setQuick(resultSet.getInt(1)-1,  resultSet.getInt(2)-1, effect);	
			}
			if (isNextMatrix==1) map.setNextMatrix(newMatrix);
			else map.setCurMatrix(newMatrix);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	// Build the effect sparse matrix for one time grain
	public void createInstMatrix(Context context, ArrayList<String> dates, int n) {
		boolean hasPrevMatrix = map.hasPrevMatrix();

		String curDate = dates.get(n);
		if (dates.size() == 1) {
			if (!hasPrevMatrix) getInstAAMapEffects(context, curDate, 0);
		}
		// First matrix in range
		else if (n==0){ 
			getInstAAMapEffects(context, dates.get(n+1), 1);
			if (!hasPrevMatrix) getInstAAMapEffects(context, curDate, 0);
		}
		// Nth matrix in range
		else {
			map.applyAAFunctions();
			if (n!=dates.size()-1) getInstAAMapEffects(context, dates.get(n+1), 1);
		}
	}


	// Build all the effect Matrices in the timeRange
	public void createAllMatrices(Context context, String dateInit, String dateEnd) {
		map = context.getAAMapsMetaInfo();
		ArrayList<String> dates = null;
		String prevDataset = map.getDataset();
		//New dataset select: prevMatrix does not apply
		if (prevDataset==null || !context.getFromTable().equals(prevDataset)){
			map.clearPrevMatrix();
			map.updateDataset(context.getFromTable());
		}

		map.checkChanges();
		String lastDate = map.getLastMatrixDate();
		if (!lastDate.equals(dateInit)) map.clearPrevMatrix();
		dates = map.getDates();

		for (int i = 0; i < dates.size(); i++ ){
			createInstMatrix(context, dates, i);
		}
	}


	// Get grid meta info from DB
	public double[] getGridMetaInfo(Context context, String table, int gridSize) {
		Statement st;
		double leftx = 0, lefty=0, width=0;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildGridMetaInfoQuery(table,gridSize);
			ResultSet resultSet = st.executeQuery(sql);

			if (resultSet.next()){
				leftx = Double.parseDouble(resultSet.getString(1)); //Longitude
				lefty = Double.parseDouble(resultSet.getString(2)); //Latitude
				width = Double.parseDouble(resultSet.getString(5));	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new double[]{leftx, lefty, width};
	}




	/**********************************************************************/
	/*****************************QUERY METHODS****************************/
	/**********************************************************************/

	private String buildInstSpatialQuery(String date, String tableName, String function, String metric) {
		String sql = "select latitude, longitud, "+function+"("+metric+"), time from " + tableName + " where date" + "='" + date + "'" ;
		sql += " group by latitude, longitud, time";
		return sql;
	}

	private String buildRangeSpatialQuery(String dateInit, String dateEnd, String tableName, String function, String metric) {
		String sql = "select latitude, longitud, "+function+"("+metric+"), time from " + tableName + " where ";
		sql += "date >= '" + dateInit + "' and date <='" + dateEnd + "'";
		sql += " group by latitude, longitud, time";
		return sql;
	}
	
	
	private String buildCoordRangeSpatialQuery(String dateInit, String dateEnd, String tableName) {
		String sql = "select latitude, longitud, sum(aa_total) from " + tableName + " where ";
		sql += "date >= '" + dateInit + "' and date <='" + dateEnd + "'";
		sql += " group by latitude, longitud";
		return sql;
	}


	private String buildAAMatrixCellQuery(String date, String tableName, String function, String metric) {
		String sql = "select x, y, "+function+"("+metric+"), time from " + tableName + " where ";
		sql += "date = '" + date +"' ";
		sql += "group by x, y, time ";
		sql += "order by "+function+"("+metric+") ASC";
		return sql;
	}


	private String buildGridMetaInfoQuery(String tableName, int size) {
		String sql = "select (ST_MetaData(rast)).* ";
		sql += "from " + tableName + " where ";
		sql += " rid = " + size;
		return sql;
	}


	private String buildMinDateQuery(String tableName, String spatialGrain) {
		String sql = "select time_start, time_end ";
		sql += "from " + tableName + " where name = '" + spatialGrain + "'";
		return sql;
	}


	private String buildSpatialGrainsQuery(String tableName) {
		String sql = "select rid from " + tableName + " where rid >= 512";
		return sql;
	}


	private String buildTimeStepsInRangeQuery(String date1, String date2, String timeGrain) {
		String sql = "SELECT date_trunc('"+ timeGrain +"', dd)::date ";
		sql += "FROM generate_series( '" + date1 + "'::timestamp, '" + date2 + "'::timestamp, ";
		sql += "'1 " + timeGrain +"'::interval) dd";
		return sql;
	}


	private String buildCountAccPerYearQuery(String tableName, int year) {
		String sql = "SELECT x, y, count(*) from " + tableName + " where date like '" + year + "%' ";
		sql += "group by x, y order by x, y";
		return sql;
	}

	/*private String buildPolygonInstSpatialQuery(String date, String tableName, String metric) {
		String sql = "select geometry, sum+"("+metric+"), time from " + tableName + " where date" + "='" + date + "'" ;
		sql += " group by geometry, time";
		return sql;
	}*/
	
	private String buildPolygonRangeSpatialQuery(String dateInit, String dateEnd, String tableName, String metric) {
		String sql = "select geometry, sum(ig), time from " + tableName + " where ";
		sql += "date >= '" + dateInit + "' and date <='" + dateEnd + "'";
		sql += " group by geometry, time";
		return sql;
	}

	public static void main(String[] args) {}
}
