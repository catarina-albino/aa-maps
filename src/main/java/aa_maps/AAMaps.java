package aa_maps;

import java.util.ArrayList;
import java.util.Hashtable;

import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tlong.LongArrayList;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class AAMaps {

	private AttenuationFunctions At = AttenuationFunctions.functions;
	private AccumulationFunctions Ac = AccumulationFunctions.functions;
	private IntIntDoubleFunction AttenFunction;
	@SuppressWarnings("unused")
	private DoubleDoubleFunction AccumFunction;
	public static int grid_dim;
	//private long stepToSave;
	private String lastMatrixDate, endDate;
	@SuppressWarnings("unused")
	private int lastAtten, curAtten, nsteps, curStep;
	private double maxEffect, minEffect;
	private static SparseDoubleMatrix2D curMatrix, nextMatrix;
	@SuppressWarnings("unused")
	private Hashtable<String, SparseDoubleMatrix2D> allMatrices;

	private String dataset;
	private ArrayList<String> dates;
	private static final double c = 5.0;
	//private static final double SAVEPERCENT = 0.1;
	

	public AAMaps(){
		this.lastMatrixDate = "";
		this.dataset = null;
		this.lastAtten = -1;
		this.curAtten = -1;
		this.allMatrices = new Hashtable<String,SparseDoubleMatrix2D>(); 
		curMatrix = createMatrix();
	}


	private void initMinMax(){
		this.maxEffect = Integer.MIN_VALUE;
		this.minEffect = Integer.MAX_VALUE;
	}
	
	/*private long calcStepToSave(){
		return nsteps / (Math.round(SAVEPERCENT * nsteps));
	}*/


	public void initMap(String dateInit, String dateEnd, ArrayList<String> dates, int gridSize, int attenFunction){
		grid_dim = gridSize;
		nextMatrix = createMatrix();
		this.lastAtten = this.curAtten;
		this.curAtten = attenFunction;
		this.endDate = dateEnd;
		this.dates = dates;
		this.curStep = 0;
		this.nsteps = dates.size();
		//this.stepToSave = calcStepToSave();
		setAttenuationFunction(attenFunction);
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
		if (lastAtten!=curAtten) {
			clearPrevMatrix();
		}
	}

	@SuppressWarnings("static-access")
	public void setAttenuationFunction(int number){
		switch (number) {
		case 0:  AttenFunction = At.constantDecay(c);
		break;
		case 1:  AttenFunction = At.noDecay();
		break;
		default: AttenFunction = At.constantDecay(c);
		break;
		}
	}


	@SuppressWarnings("static-access")
	public void setAccumulationFunction(int number){
		switch (number) {
		case 0:  AccumFunction = Ac.sum;
		break;
		case 1:  AccumFunction = Ac.max;
		break;
		case 2:  AccumFunction = Ac.min;
		break;
		default: AccumFunction = Ac.sum;
		break;
		}
	}


	public boolean hasPrevMatrix(){
		return (!lastMatrixDate.equals(""));
	}

	public void clearPrevMatrix(){
		lastMatrixDate = "";
		initMinMax();
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


	public void attenuateValues(IntIntDoubleFunction attenFunction){
		curMatrix.forEachNonZero(attenFunction);
	}

	public void accumulateValues(DoubleDoubleFunction accFunction){
		LongArrayList nextKeys = nextMatrix.elements().keys();
		DoubleArrayList nextValues = nextMatrix.elements().values();

		long[] indexes = null;
		for (int i=0; i < nextValues.size(); i++){
			indexes = AAMaps.get2DIndex(nextKeys.get(i));
			int row = (int)indexes[0];
			int column = (int)indexes[1];
			curMatrix.setQuick(row, column, accFunction.apply(curMatrix.getQuick(row, column), nextValues.get(i)));
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


	@SuppressWarnings("static-access")
	public void applyAAFunctions(){
		/** 1 - Attenuate the previous matrix **/
		attenuateValues(AttenFunction);
		
		/** 2 - Aggregate the 2 matrix to form the final one **/
		accumulateValues(Ac.sum);
	}
}
