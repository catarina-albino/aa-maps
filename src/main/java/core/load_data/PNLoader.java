package core.load_data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import aa_maps.ConfusionMatrix;
import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tobject.impl.SparseObjectMatrix2D;
import core.Config;
import server.Context;

public class PNLoader {

	public static final int FETCH_SIZE = Config.getConfigInt("fetch_size");
	public static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");
	public Connection connection = DataStoreInfo.getMetaStore();

	
	public PNLoader() {}


	//Method to be called by server
	public String getInstPN(Context context, String date, String timeGranularity) {
		//Init json constrution
		StringBuilder strBuilder = new StringBuilder();
		String header= "{\"type\":\"FeatureCollection\",\"features\":[";
		String featureTemplate = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[%s, %s]}}";
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildInstPNQuery(date, context.getDefaultPNTable()+timeGranularity);

			double longitud, latitude;
			ResultSet resultSet = st.executeQuery(sql);

			while(resultSet.next()) {
				//Calculate the center of cell
				latitude = Double.parseDouble(resultSet.getString(1));
				longitud = Double.parseDouble(resultSet.getString(2));

				if (resultSet.isFirst()) strBuilder.append(header);
				strBuilder.append(String.format(featureTemplate, latitude, longitud));
				if(!resultSet.isLast()) strBuilder.append(",");
			}
			strBuilder.append("]}");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return strBuilder.toString();
	}

	

	//Method to be called by server
	public String getAllPN(Context context, String timeGranularity)  {
		//Init json constrution
		StringBuilder strBuilder = new StringBuilder();
		
		String header= "{\"type\":\"FeatureCollection\",\"features\":[";
		String featureTemplate = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[%s, %s]}}";

		String table = context.getDefaultPNTable()+timeGranularity;
		Statement st;

		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildAllPNQuery(table);
			System.out.println("Pontos Negros SQL: " + sql);

			ResultSet resultSet = st.executeQuery(sql);
			while(resultSet.next()) {
				//Calculate center of cell
				double latitude = Double.parseDouble(resultSet.getString(1));
				double longitud = Double.parseDouble(resultSet.getString(2));

				if (resultSet.isFirst()) strBuilder.append(header);
				strBuilder.append(String.format(featureTemplate, latitude, longitud));
				if(!resultSet.isLast()) strBuilder.append(",");
			}
			strBuilder.append("]}");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Database connection failed.");
		}
		return strBuilder.toString();
	}
	
	
	
	public SparseDoubleMatrix2D getInstPN(Context context, String date, int grid_dim) {
		Statement st;
		SparseDoubleMatrix2D pnMatrix = null;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			
			String sql = buildPNMatrixCellQuery(date,context.getPNFTable());
			System.out.println("Pontos Negros SQL: " + sql);
			ResultSet resultSet = st.executeQuery(sql);
			pnMatrix = new SparseDoubleMatrix2D(grid_dim,grid_dim);
			while(resultSet.next()) {
				pnMatrix.setQuick(resultSet.getInt(1)-1,  resultSet.getInt(2)-1, 1);	
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pnMatrix;
	}
	
	
	
	@SuppressWarnings({ "unchecked"})
	public SparseObjectMatrix2D getInstPNV2(Context context, String year, int grid_dim) {
		Statement st;
		SparseObjectMatrix2D pnMatrix = null;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			
			String sql = buildPNMatrixCellQueryV2(grid_dim, year);
			//System.out.println("Pontos Negros SQL: " + sql);
			ResultSet resultSet = st.executeQuery(sql);
			pnMatrix = new SparseObjectMatrix2D(grid_dim,grid_dim);
			while(resultSet.next()) {
				int x = resultSet.getInt(1)-1, y = resultSet.getInt(2)-1;
				Object pnList = pnMatrix.getQuick(x,y);
				
				if (pnList == null) pnList = new ArrayList<Integer>();
				((ArrayList<Integer>) pnList).add(resultSet.getInt(3));
				pnMatrix.setQuick(x,y,pnList);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pnMatrix;
	}
	
	
	
	public int getCountPN(Context context, String year) {
		Statement st;
		int count = 0;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildCountPNQuery(year, context.getPNFTable());
			//System.out.println("Pontos Negros Count SQL: " + sql);
			ResultSet resultSet = st.executeQuery(sql);
			if (resultSet.next()) count = resultSet.getInt(2);	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
	
	
	
	public double[] getPNCoord(String table, String id) {
		Statement st;
		double[] coord = new double[2];
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = buildGetPNCoordQuery(table, id);
			System.out.println("Pontos Negros Get Coord SQL: " + sql);
			ResultSet resultSet = st.executeQuery(sql);
			if (resultSet.next()) {
				coord[0]= Double.parseDouble(resultSet.getString(1));
				coord[1]= Double.parseDouble(resultSet.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return coord;
	}
	
	

	public DoubleArrayList getAllPNCoord(String table, String year) {
		Statement st;
		DoubleArrayList allCoords = new DoubleArrayList();
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);
			String sql = builGetAllPNCoord(table, year);
			System.out.println("Pontos Negros Get All Coord SQL: " + sql);
			ResultSet resultSet = st.executeQuery(sql);
			while (resultSet.next()) {
				double lat = Double.parseDouble(resultSet.getString(2));
				double longit = Double.parseDouble(resultSet.getString(3));
				allCoords.add(lat);
				allCoords.add(longit);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allCoords;
	}
	

	
	/**********************************************************************/
	/************************** Confusion Matrix **************************/
	/**********************************************************************/

	public ConfusionMatrix getConfusionMatrix(Context context, int dim, Integer year, 
				int attenFunction, int accumFunction, int version, boolean percentage, int threshold){
	
		SparseDoubleMatrix2D aaMatrices[] = calcAAMapsYearMatrix(context,year, attenFunction, accumFunction, version);
		SparseObjectMatrix2D pnMatrix = calcPNYearMatrixV2(context,dim,year);
		
		/*SparseDoubleMatrix2D pnMatrix = calcPNYearMatrix(context,dim,year);*/
		/*ConfusionMatrix cm = context.getAAMapsMetaInfo().getConfusionMatrix(context, version, percentage, threshold, dim,
				aaMatrices[0], pnMatrix, aaMatrices[1]);*/
		
		int pnCount = getCountPN(context,year.toString());
		DoubleArrayList allPN = getAllPNCoord(context.getPNFTable(), year.toString());
		ConfusionMatrix cm = context.getAAMapsMetaInfo().getConfusionMatrixV2(context, version, percentage, (double) threshold, dim,
				aaMatrices[0], pnMatrix, aaMatrices[1], pnCount, allPN);
		
		return cm;
	}
	
	
	public SparseDoubleMatrix2D calcPNYearMatrix(Context context, int dim, Integer year){
		context.setPNFTable(dim);
		return getInstPN(context, year.toString(), dim);
	}
	
	
	public SparseObjectMatrix2D calcPNYearMatrixV2(Context context, int dim, Integer year){
		context.setPNFTable(dim);
		return getInstPNV2(context, year.toString(), dim);
	}
	
	
	private static SparseDoubleMatrix2D[] calcAAMapsYearMatrix(Context context, int year, int attenFunction, 
			int accumFunction, int version){
		String timeGranularity = context.getTimeGranularity();
		int gridSize = context.getGridSize();
		String table = context.getDefaultAAMapsTable()+gridSize+context.getTimeGranularity()+"s";
		context.setTableToRead(table);
		
		Loader loader = context.getLoader();
		double[] gridInfo = loader.getGridMetaInfo(context,context.getSpatialDataTable(), gridSize);
		context.setGridInfo(gridSize, gridInfo[0], gridInfo[1], gridInfo[2]);

		String dates[] = Functions.getInitAndLastDate(year);
		ArrayList<String> datesBD = loader.getDaysInRange(dates[0], dates[1], timeGranularity);
		context.initMap(dates[1], dates[1], datesBD, gridSize, attenFunction, accumFunction); //AA Functions setting
		loader.createAllMatrices(context, dates[0],dates[1]);
		SparseDoubleMatrix2D aaCountMatrix = null, aaMatrix = context.getAAMapsMetaInfo().getCurMatrix();
		
		if (version != 1) aaCountMatrix = calcAAMapsCountMatrix(context, year);
		return new SparseDoubleMatrix2D[]{aaMatrix, aaCountMatrix};
	}
	
	
	
	
	public static SparseDoubleMatrix2D calcAAMapsCountMatrix(Context context, int year){
		return context.getLoader().getCountAccidents(context, year);
	}
	
	


	/**********************************************************************/
	/*****************************QUERY METHODS****************************/
	/**********************************************************************/

	private String buildInstPNQuery(String date, String tableName) {
		String sql = "select latitude, longitud from " + tableName + " where ";
		sql += "ano = '" + date+ "'";
		return sql;
	}
	
	
	private String buildAllPNQuery(String tableName) {
		String sql = "select latitude, longitud from " + tableName;
		return sql;
	}
	
	
	private static String buildPNMatrixCellQuery(String year, String tableName) {
		String sql = "select x, y from " + tableName + " where ";
		sql += "ano = '" + year +"' ";
		sql += "group by x, y ";
		sql += "order by x, y";
		return sql;
	}
	
	
	private static String buildCountPNQuery(String year, String tablename){
		String sql = "select distinct ano, count(*) from " + tablename + " where ";
		sql += "ano = '" + year +"' ";
		sql += "group by ano ";
		sql += "order by ano";
		return sql;
	}
	
	
	private static String buildGetPNCoordQuery(String tablename, String id){
		String sql = "select latitude, longitud from " + tablename + " where ";
		sql += "id = '" + id +"' ";
		return sql;
	}
	
	
	private static String builGetAllPNCoord(String tablename, String year){
		String sql = "select id_ano, latitude, longitud from " + tablename + " where ";
		sql += "ano = '" + year +"' ";
		sql += "order by id_ano";
		return sql;
	}
	
	
	private static String buildPNMatrixCellQueryV2(int grid, String year) {
		String sql = "select distinct columnx, rowy, id from ( ";
		sql += "select (ST_WorldToRasterCoord((select rast from accidents_portugal_spatialgranularity where rid= "+ grid +"), longitude, latitude)).*, * ";
		sql += "from (select (ST_RasterToWorldCoord( buffer_rast,x, y)).*, * from (select (ST_PixelAsPoints(buffer_rast,1)).*, * ";
		sql += "from (select st_asraster( (ST_Transform(ST_Buffer(ST_Transform(ST_SetSRID(ST_MakePoint(longitud, latitude),4326),26986), 100), 4326)), ";
		sql += "(select rast from accidents_portugal_spatialgranularity where rid= "+grid+"), '8BUI', 1, 0, TRUE) as buffer_rast, pontos_negros_janela_fixa.id_ano as id, ano ";
		sql += "from pontos_negros_janela_fixa where ano = "+year+") a) points) matrixcoords) tmp order by id";
		return sql;	
	}
	
	public static void main(String[] args) {}
}
