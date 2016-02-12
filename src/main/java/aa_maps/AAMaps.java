package aa_maps;

import java.util.ArrayList;
import java.util.Hashtable;
import accum_functions.*;
import atten_functions.*;
import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tlong.LongArrayList;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class AAMaps {

	public static int grid_dim;
	private String lastMatrixDate, endDate;
	private String dataset;
	private ArrayList<String> dates;
	private IAccumFunc AccumFunction;
	private IAttenFunc AttenFunction;
	private ArrayList<IAccumFunc> accumFunctions;
	private ArrayList<IAttenFunc> attenFunctions;
	
	@SuppressWarnings("unused")
	private int curAttenID, curAccumID, nsteps, curStep;
	@SuppressWarnings("unused")
	private Hashtable<String, SparseDoubleMatrix2D> allMatrices;
	private boolean newContext;
	private double maxEffect, minEffect;
	private static SparseDoubleMatrix2D curMatrix, nextMatrix;
	//private static final double SAVEPERCENT = 0.1;
	//private long stepToSave;

	
	public AAMaps(){
		addAccumFunctions();
		addAttenFunctions();
		this.lastMatrixDate = "";
		this.dataset = null;
		this.newContext = true;
		this.allMatrices = new Hashtable<String,SparseDoubleMatrix2D>(); 
		curMatrix = createMatrix();
	}
	
	private void addAccumFunctions() {
		int index = 0;
		accumFunctions = new ArrayList<IAccumFunc>();
		accumFunctions.add(new Sum(index++));
		accumFunctions.add(new Max(index++));
		accumFunctions.add(new Count(index++));
		accumFunctions.add(new Min(index++));
	}
	
	
	private void addAttenFunctions() {
		int index = 0;
		attenFunctions = new ArrayList<IAttenFunc>();
		attenFunctions.add(new LinearDecay(index++));
		attenFunctions.add(new ConstantGrowth(index++));
		attenFunctions.add(new Identity(index++));
		attenFunctions.add(new ExponentialEaseIn(index++));
	}
	
	
	public ArrayList<IAccumFunc> getAllAccumFunctions(){
		return accumFunctions;
	}
	
	public ArrayList<IAttenFunc> getAllAttenFunctions(){
		return attenFunctions;
	}
	
	public IAccumFunc getCurAccumFunction(){
		return AccumFunction;
	}
	

	private void initMinMax(){
		this.maxEffect = Integer.MIN_VALUE;
		this.minEffect = Integer.MAX_VALUE;
	}
	
	/*private long calcStepToSave(){
		return nsteps / (Math.round(SAVEPERCENT * nsteps));
	}*/
	

	public void initMap(String dateInit, String dateEnd, ArrayList<String> dates, int gridSize, 
					int attenFunction, int accumFunction){
		grid_dim = gridSize;
		nextMatrix = createMatrix();
		this.endDate = dateEnd;
		this.dates = dates;
		this.curStep = 0;
		this.nsteps = dates.size();
		setAttenuationFunction(attenFunction);
		setAccumulationFunction(accumFunction);
	}
	
	public ArrayList<String> getDates(){
		return dates;
	}

	public void updateDataset(String newDataset){
		this.dataset = newDataset;
	}

	public String getDataset(){
		return this.dataset;
	}

	public void checkChanges(){
		System.out.println("\n\n"+newContext);
		if (newContext){
			clearPrevMatrix();
			newContext = false;
		}
	}

	
	public void setAttenuationFunction(int id){
		this.AttenFunction = attenFunctions.get(id);
		if (curAttenID != id) newContext = true;
		else newContext = false;
		curAttenID = id;
	}

	
	public void setAccumulationFunction(int id){
		this.AccumFunction = accumFunctions.get(id);
		if (curAccumID != id) newContext = true;
		else newContext = false;
		curAccumID = id;		
	}
	
	public IAccumFunc getAccumulationFunction(){
		return this.AccumFunction;
	}
	

	public boolean hasPrevMatrix(){
		return (!lastMatrixDate.equals(""));
	}

	public void clearPrevMatrix(){
		lastMatrixDate = "";
		initMinMax();
		newContext = false;
	}

	public SparseDoubleMatrix2D createMatrix(){
		return new SparseDoubleMatrix2D(grid_dim, grid_dim);
	}
	
	
	public static SparseDoubleMatrix2D createDimMatrix(int dim){
		return new SparseDoubleMatrix2D(dim, dim);
	}



	public void setNewMax(double newMax){
		if (newMax > maxEffect) maxEffect = newMax;
	}

	public void setNewMin(double newMin){
		if (newMin < minEffect) minEffect = newMin;
	}


	public double getMinEffect(){
		return minEffect;
	}

	public double getMaxEffect(){
		return maxEffect;
	}


	public SparseDoubleMatrix2D getCurMatrix(){
		return curMatrix;
	}

	public SparseDoubleMatrix2D getNextMatrix(){
		return nextMatrix;
	}


	public void setCurMatrix(SparseDoubleMatrix2D newMatrix){
		curMatrix = newMatrix;
	}

	public void setNextMatrix(SparseDoubleMatrix2D newMatrix){
		nextMatrix = newMatrix;
	}


	public void saveComputedMatrix(){
		lastMatrixDate = endDate;
	}

	public String getLastMatrixDate(){
		return lastMatrixDate;
	}


	public void attenuateValues(){
		curMatrix.forEachNonZero(AttenFunction);
	}

	public void accumulateValues(){
		LongArrayList nextKeys = nextMatrix.elements().keys();
		DoubleArrayList nextValues = nextMatrix.elements().values();
		long[] indexes = null;
		for (int i=0; i < nextValues.size(); i++){
			indexes = AAMaps.get2DIndex(nextKeys.get(i));
			int row = (int)indexes[0];
			int column = (int)indexes[1];
			curMatrix.setQuick(row, column, AccumFunction.apply(curMatrix.getQuick(row, column), nextValues.get(i)));
		}
		saveCurMatrix();
	}
	

	public void saveCurMatrix(){
		/*if (curStep % stepToSave == 0) {
			allMatrices.put(dates.get(curStep), curMatrix);
			//System.out.println(allMatrices.keySet());
		}
		curStep++;*/
	}

	public static long[] get2DIndex(long index){
		return new long[] {index / grid_dim, index % grid_dim};
	}

	public void applyAAFunctions(){
		/** 1 - Attenuate the previous matrix **/
		attenuateValues();
		/** 2 - Aggregate the 2 matrix to form the final one **/
		accumulateValues();
	}
}
