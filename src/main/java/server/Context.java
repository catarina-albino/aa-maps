package server;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aa_maps.AAMaps;

import com.vividsolutions.jts.geom.Polygon;

import core.load_data.Loader;

public class Context implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final String GRAIN = "year";
	private static final boolean AAMAPSOFF = false;
	
	private int gridSize, attenFunction;
	private String tableToRead, spatialDataTable, timeDataTable;
	private Loader loader;
	private boolean isRestricted;
	private String timeGranularity;
	private Polygon geometryRestriction;
	private boolean aamaps;
	private Map<String, Object> regionsComputed;
	private HashMap<Integer, double[]> gridsMetaInfo;
	private AAMaps map;
	

	public Context(String grain, int gridSize, String tableToRead, boolean aaMapsMode) {
		this.timeGranularity = grain;
		this.gridSize = gridSize;
		this.tableToRead = tableToRead;
		this.loader = new Loader();	
		this.isRestricted = false;
		this.aamaps = aaMapsMode;
		this.gridsMetaInfo = new HashMap<Integer, double[]>();
	}
	
	public Context() {
		this.timeGranularity = GRAIN;
		this.aamaps = AAMAPSOFF;
		this.map = null;
		this.loader = new Loader();
		this.isRestricted = false;
		this.gridsMetaInfo = new HashMap<Integer, double[]>();
	}
	
	
	public void setAAMapsMetaInfo(AAMaps map) {
		this.map = map;
	}
	
	public AAMaps getAAMapsMetaInfo() {
		return this.map;
	}
	
	public void initMap(String dateInit, String dateEnd, ArrayList<String> dates, int gridSize, int attenFunction) {
		this.attenFunction = attenFunction;
		this.map.initMap(dateInit, dateEnd,dates,gridSize, attenFunction);
	}
	
	public void setGridInfo(int gridSize, double leftx, double lefty, double width) {
		if (!gridsMetaInfo.containsKey(gridSize)){
			gridsMetaInfo.put(gridSize, new double[]{leftx,lefty,width});
		}
	}
	
	public double[] getGridInfo(int gridSize){
		if (gridsMetaInfo.containsKey(gridSize)) return gridsMetaInfo.get(gridSize);
		else return null;
	}
	
	
	public String getTimeGranularity() {
		return timeGranularity;
	}
	
	
	public void setAttenFunction(int attenFunction) {
		this.attenFunction = attenFunction;
	}
	
	public int getAttenFunction() {
		return attenFunction;
	}
	
	
	public boolean isRestricted() {
		return isRestricted;
	}
	
	public boolean isAAMapsMode() {
		return aamaps;
	}
	
	public void setAAMapsMode(boolean mode) {
		this.aamaps = mode;
	}
	

	public void setRestricted(boolean isRestricted) {
		this.isRestricted = isRestricted;
	}
	
	public void setSpatialDataTable(String table){
		this.spatialDataTable = table;
	}
	
	public void setTimeDataTable(String table){
		this.timeDataTable = table;
	}

	public int getGridSize() {
		return gridSize;
	}

	public String getSpatialDataTable() {
		return spatialDataTable;
	}
	
	public String getTimeDataTable() {
		return timeDataTable;
	}
	
	public String getFromTable() {
		return tableToRead;
	}

	public Loader getLoader() {
		return loader;
	}	

	public void setTableToRead(String tableToRead) {
		this.tableToRead = tableToRead;
	}

	
	public void setTimeGranularity(String timeGranularity) {
		this.timeGranularity = timeGranularity;
	}
	
	public void setGridSize(int size) {
		this.gridSize = size;
	}


	public void setLoader(Loader loader) {
		this.loader = loader;
	}
	
	public Polygon getGeometryRestriction() {
		return geometryRestriction;
	}

	public void setGeometryRestriction(Polygon geometryRestriction) {
		this.geometryRestriction = geometryRestriction;
	}
	
	public boolean isRegionComputed(String tableName) {
		return regionsComputed.containsKey(tableName);
	}

	public void setRegionComputed(String tableName) {
		regionsComputed.put(tableName, "0");
	}
	
	
	public void clearRegionsComputed() {
		regionsComputed.clear();
	}

}
