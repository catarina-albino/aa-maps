package aamaps_results;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Random;

import aa_maps.AAMaps;
import cern.colt.function.tdouble.DoubleDoubleFunction;
import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tlong.LongArrayList;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class AAMapsTests {

	private static SparseDoubleMatrix2D curMatrix, nextMatrix;
	private static SparseDoubleMatrix2D[] dayMatrices;
	private static final double c = 5.0;
	private static final int DIM = 3, MAXEFFECT = 150;

	public AAMapsTests(){

	}


	/****************************************************************
	 ******************** TOY EXAMPLES AND TESTS *********************
	 ****************************************************************/

	public static long[] get2DDimIndex(long index, int dim){
		return new long[] {index / dim, index % dim};
	}


	@SuppressWarnings("unused")
	private void serializeMatrix(SparseDoubleMatrix2D matrix){
		try {
			Writer output;
			output = new BufferedWriter(new FileWriter("matrizes.txt", true));
			output.append(matrix.toString());
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	private static SparseDoubleMatrix2D matrixGen1(int dim){
		Random r = new Random();
		SparseDoubleMatrix2D matrix = AAMaps.createDimMatrix(dim);
		for(int i=0; i < matrix.rows()-2; i++){
			for(int j=0; j < matrix.columns()-1; j++){
				// nextInt is normally exclusive of the top value,
				// so add 1 to make it inclusive
				//matrix.setQuick(i, j, ThreadLocalRandom.current().nextInt((int)c, MAXEFFECT + 1));
				matrix.setQuick(i, j, r.nextInt(MAXEFFECT));
			}
		}
		return matrix;
	}

	private static SparseDoubleMatrix2D matrixGen2(int dim){
		Random r = new Random();
		SparseDoubleMatrix2D matrix = AAMaps.createDimMatrix(dim);
		for(int i=1; i < matrix.rows()-1; i++){
			for(int j=1; j < matrix.columns()-1; j++){
				matrix.setQuick(i, j, r.nextInt(MAXEFFECT));
				//matrix.setQuick(i, j, ThreadLocalRandom.current().nextInt((int)c, MAXEFFECT + 1));
			}
		}
		return matrix;
	}

	private static SparseDoubleMatrix2D matrixGen3(int dim){
		Random r = new Random();
		SparseDoubleMatrix2D matrix = AAMaps.createDimMatrix(dim);
		for(int i=2; i < matrix.rows(); i++){
			for(int j=2; j < matrix.columns(); j++){
				matrix.setQuick(i, j, r.nextInt(MAXEFFECT));
				//matrix.setQuick(i, j, ThreadLocalRandom.current().nextInt((int)c, MAXEFFECT + 1));
			}
		}
		return matrix;
	}


	private static SparseDoubleMatrix2D matrixGen4(int dim){
		Random r = new Random();
		SparseDoubleMatrix2D matrix = AAMaps.createDimMatrix(dim);
		for(int i=0; i < matrix.rows(); i++){
			for(int j=2; j < matrix.columns(); j++){
				matrix.setQuick(i, j, r.nextInt(MAXEFFECT));
				//matrix.setQuick(i, j, ThreadLocalRandom.current().nextInt((int)c, MAXEFFECT + 1));
			}
		}
		return matrix;
	}


	private static SparseDoubleMatrix2D[] genAllMatrices(int n, int dim){
		SparseDoubleMatrix2D[] matrices = new SparseDoubleMatrix2D[n];
		Random r = new Random();
		int count=0;
		for (int i=0; i < n; i++){
			count = r.nextInt(4);
			if (count == 0) matrices[i] = matrixGen1(dim);
			else if (count == 1) matrices[i] = matrixGen2(dim);
			else if (count == 2) matrices[i] = matrixGen3(dim);
			else matrices[i] = matrixGen4(dim);
		}
		return matrices;
	}


	private static SparseDoubleMatrix2D initAuxMatrix(int initTime, int step){
		SparseDoubleMatrix2D aux = AAMaps.createDimMatrix(DIM);
		for(int i=0; i < aux.rows(); i++){
			for(int j=0; j < aux.columns(); j++){
				//double v = dayMatrices[initTime].getQuick(i, j);
				//if (v < c && v > 0 ) aux.setQuick(i, j, (v - c) - (step * c));
				//if (v < c && v > 0 ) aux.setQuick(i, j, (v - c));
				//else aux.setQuick(i, j, -c);
				aux.setQuick(i, j, -c);
			}
		}
		return aux;
	}


	private static void attenuateAuxMatrix(LongArrayList keys, SparseDoubleMatrix2D aux){
		long[] indexes = null;
		for (int i = 0; i < keys.size(); i++){
			indexes = AAMapsTests.get2DDimIndex(keys.get(i), DIM);
			int row = (int)indexes[0];
			int column = (int)indexes[1];
			aux.setQuick(row, column, (aux.getQuick(row, column)) - c);
		}
	}


	@SuppressWarnings("unused")
	private static Hashtable<Integer, SparseDoubleMatrix2D[]> genIntermedPoints(int step, int time, 
			IntIntDoubleFunction atten1, IntIntDoubleFunction atten2, DoubleDoubleFunction accum){

		int nPoints = Math.round(time/step);
		Hashtable<Integer, SparseDoubleMatrix2D[]> points = new Hashtable<Integer, SparseDoubleMatrix2D[]>();

		SparseDoubleMatrix2D curMatrix, nextMatrix, auxMatrixInit;

		for (int i = 0; i < nPoints; i++){
			int min = i * step, max = min + step;	
			curMatrix = null; nextMatrix = null;
			auxMatrixInit = initAuxMatrix(min, step-1);
			
			SparseDoubleMatrix2D curAuxMatrix = auxMatrixInit;
			IntIntDoubleFunction attenFunction= atten1;
			//if (i==0) attenFunction= atten1;
			for (int j = min ; j < max; j++){
				if (j == min) {
					curMatrix = (SparseDoubleMatrix2D) dayMatrices[j].copy();
					nextMatrix = (SparseDoubleMatrix2D) dayMatrices[j+1].copy();
				}
				else { 
					if (i!=0){
						LongArrayList curKeys = curMatrix.elements().keys();
						LongArrayList auxKeysToAtenuate = curAuxMatrix.elements().keys();
						auxKeysToAtenuate.removeAll(curKeys);
						attenuateAuxMatrix(auxKeysToAtenuate, curAuxMatrix);
					}
					attenuateMatrix(attenFunction, curMatrix);
					curMatrix = accumulateMatrix(accum, curMatrix, nextMatrix);
					if (j<max-1) nextMatrix = (SparseDoubleMatrix2D) dayMatrices[j+1].copy();
				}
			}

			points.put(i, new SparseDoubleMatrix2D[]{curMatrix, curAuxMatrix});
		}
		return points;
	}


	@SuppressWarnings("unused")
	private static SparseDoubleMatrix2D calcAttenAccumMatrix(int startTime, int maxTime, 
			IntIntDoubleFunction atten, DoubleDoubleFunction accum){
		SparseDoubleMatrix2D curMatrix = null, nextMatrix = null;
		for (int i = startTime; i < maxTime; i++){
			if (i == startTime) {
				curMatrix = (SparseDoubleMatrix2D) dayMatrices[i].copy();
				nextMatrix = (SparseDoubleMatrix2D) dayMatrices[i+1].copy();
			}
			else {
				attenuateMatrix(atten, curMatrix);
				accumulateMatrix(accum, curMatrix, nextMatrix);
				if (i<maxTime-1) nextMatrix = (SparseDoubleMatrix2D) dayMatrices[i+1].copy();
			}
			//printMatrix (curMatrix, "AA-Maps Matrix Step " + i +" =");
		}
		return curMatrix;
	}



	@SuppressWarnings("unused")
	private static SparseDoubleMatrix2D optimizeCalcFinalMatrix(int step, int time,
			DoubleDoubleFunction accum, DoubleDoubleFunction positiveAccum, SparseDoubleMatrix2D[] allMatrices,
			Hashtable<Integer, SparseDoubleMatrix2D[]> intermedPoints){

		curMatrix = (SparseDoubleMatrix2D) intermedPoints.get(0)[0].copy();
		nextMatrix = (SparseDoubleMatrix2D) intermedPoints.get(1)[0].copy();
		//printMatrix (curMatrix, "Intermed 1 = ");
		//printMatrix (nextMatrix, "Intermed 2 = ");
		//printMatrix (intermedPoints.get(1)[1], "Intermed Aux = ");
		curMatrix = accumulateMatrix(positiveAccum, curMatrix, intermedPoints.get(1)[1]); 
		curMatrix = accumulateMatrix(positiveAccum, curMatrix, nextMatrix);
		return curMatrix;
	}




	private static void attenuateMatrix(IntIntDoubleFunction attenFunction, SparseDoubleMatrix2D matrix){
		matrix.forEachNonZero(attenFunction);
	}

	private static SparseDoubleMatrix2D accumulateMatrix(DoubleDoubleFunction accFunction, SparseDoubleMatrix2D cur, SparseDoubleMatrix2D next){
		LongArrayList nextKeys = next.elements().keys();
		DoubleArrayList nextValues = next.elements().values();
		long[] indexes = null;
		for (int i=0; i < nextValues.size(); i++){
			indexes = AAMapsTests.get2DDimIndex(nextKeys.get(i),cur.columns());
			int row = (int)indexes[0];
			int column = (int)indexes[1];
			cur.setQuick(row, column, accFunction.apply(cur.getQuick(row, column), nextValues.get(i)));
		}	

		return cur;
	}


	private static void printAllMatrices(SparseDoubleMatrix2D[] matrices){
		for (int i=0; i < matrices.length; i++){
			System.out.println("Matrix Time "+ (i+1)+" = "+matrices[i].elements()+"\n");
		}
	}

	private static void printMatrix(SparseDoubleMatrix2D m, String name){
		System.out.println(name + " = " + m.elements()+"\n");
	}


	@SuppressWarnings({ "unused", "null" })
	public static void main(String[] args) {
		double c = 5.0;
		int maxTime = 5, step = 2, obsTime = 4, tests = 500;
		/*IntIntDoubleFunction linearOneStep = AttenuationFunctions.constantDecay(c);
		IntIntDoubleFunction negDecay = AttenuationFunctions.constantNegDecay(c);
		DoubleDoubleFunction sum = AccumulationFunctions.sum;
		DoubleDoubleFunction positiveSum = AccumulationFunctions.positiveSum;*/
		
		long start1 = 0, start2 = 0, end1 = 0, end2 = 0;

		for (int i = 0; i < tests; i++ ){
			dayMatrices = genAllMatrices(maxTime,DIM);
			//printAllMatrices(dayMatrices);

			start1 += System.nanoTime();
			SparseDoubleMatrix2D finalMatrix = null; //calcAttenAccumMatrix(0, obsTime, linearOneStep, sum);
			end1 += System.nanoTime();
			
			//finalMatrix.trimToSize();
			//printMatrix (finalMatrix, "Final Normal Matrix");
			
			Hashtable<Integer, SparseDoubleMatrix2D[]> intermedPoints = null; //genIntermedPoints(step, maxTime, linearOneStep, negDecay, sum);
			start2 += System.nanoTime();
			SparseDoubleMatrix2D finalOptmizedMatrix = null; 
			//optimizeCalcFinalMatrix(step, obsTime, sum, positiveSum,dayMatrices, intermedPoints);
			end2 += System.nanoTime();
			
			//printMatrix (finalOptmizedMatrix, "Final Optim. Matrix");

			if (!finalMatrix.equals(finalOptmizedMatrix)) {
				System.out.println("\n********* Test " + i + " Failed *******");
				printMatrix (finalMatrix, "Final Normal Matrix");
				printMatrix (finalOptmizedMatrix, "Final Optim. Matrix");
				printMatrix (intermedPoints.get(0)[0], "Intermed 1 = ");
				printMatrix (intermedPoints.get(1)[0], "Intermed 2 = ");
				printMatrix (intermedPoints.get(1)[1], "Intermed Aux = ");
				printAllMatrices(dayMatrices);
				System.out.println("************************************\n");
			}
		}
		System.out.println("AA-Maps time:" + (end1-start1)/tests);
		System.out.println("Intermed. Matrix Version time:" + (end2-start2)/tests);
		System.out.println("Done testing.");
	}
}
