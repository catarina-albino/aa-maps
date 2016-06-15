package aa_maps;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import cern.colt.list.tdouble.DoubleArrayList;
import core.load_data.Functions;
import server.Context;

public class ConfusionMatrix {
	
	private double recall, specificity, ppv, npv;
	private int true_positives, false_positives, false_negatives, true_negatives, n, pnCount;
	private DoubleArrayList TP, FP, FN, TN, PN;
	private ArrayList<Integer> truePositivesList, falseNegativeList;
	private double [] gridInfo;
	
	
	public ConfusionMatrix(Context context, int pnCount){
		initCoordArrays();
		gridInfo = context.getGridInfo(context.getGridSize());
		this.pnCount = pnCount;
		genFNList();
	}
	

	public double getSpecificity() {
		return specificity;
	}
	
	
	public void setAllPNCoords(DoubleArrayList pn){
		this.PN = pn;
	}
	
	
	public int getFalse_positives() {
		return false_positives;
	}


	public int getTrue_positives() {
		return true_positives;
	}


	public int getN() {
		return n;
	}

	
	public double getNPV(){
		return this.npv;
	}
	
	public double getPPV(){
		return this.ppv;
	}
	
	
	public void calcResults(){
		getFNList();
		this.true_positives = truePositivesList.size();
		this.false_negatives = pnCount - true_positives;
		double a = true_positives;
		double b = false_positives;
		double c = false_negatives;
		double d = true_negatives;
		this.n = (int) (a+b+c+d);
		this.recall = TwoCellCalculation(a,c);
		this.specificity = TwoCellCalculation(b,d);
		this.ppv = TwoCellCalculation(a,b);
		this.npv = TwoCellCalculation(d,c);
		/*this.positive_likelihood = this.sensitivity / (1-this.specificity);
		this.negative_likelihood = 1/this.positive_likelihood;
		this.prevalence = (a+c)/(a+b+c+d);
		this.fpr = 1 - this.specificity;
		this.fdr = 1 - this.ppv;
		this.fnr = this.false_negatives / (this.false_negatives+this.true_positives);
		this.mcc = ((this.true_positives*this.true_negatives)-(this.false_positives*this.false_negatives))/ 
					Math.sqrt((this.true_positives+this.false_positives)*(this.true_positives+this.false_negatives)*
							(this.true_negatives+this.false_positives)*(this.true_negatives+this.false_negatives));
		this.acc = (this.true_positives+this.true_negatives)/this.n;
		this.f1 = 2*(this.true_positives)/(2*this.true_positives+this.false_positives+this.false_negatives);
		this.informedness = this.sensitivity+this.specificity-1;
		this.markedness = this.ppv + this.npv - 1;*/
	}
	
	
	private void initCoordArrays(){
		this.truePositivesList = new ArrayList<Integer>();
		this.falseNegativeList = new ArrayList<Integer>();
		this.TP = new DoubleArrayList();
		this.FP = new DoubleArrayList();
		this.FN = new DoubleArrayList();
		this.TN = new DoubleArrayList();
	}
	

	private double TwoCellCalculation(double k, double j) {
		if (k == 0) return 0;
		else return (k / (k+j))*100;
	}
	
	

	
	private double[] getCoord(int x, int y){
		double[] coords = Functions.calcPointInGrid(gridInfo[0], gridInfo[1], x, y, gridInfo[2]);
		return new double[]{coords[1], coords[0]};
	}
	
	
	public void addTPos(int x, int y){
		this.true_positives++;
		double c[] = getCoord(x, y);
		this.TP.add(c[0]);
		this.TP.add(c[1]);
	}
	
	
	public void addTPosID(ArrayList<Integer> newIDs){
		for (Integer id : newIDs){
			if (!(truePositivesList.contains(id))) {
				truePositivesList.add(id);
				falseNegativeList.remove(id);
				int pos = (id - 1)*2;
				this.TP.add(PN.get(pos));
				this.TP.add(PN.get(pos+1));
				falseNegativeList.remove(id);
			}
		}
	}
	
	
	
	private void genFNList(){
		for (int i = 0; i < pnCount; i++){
			falseNegativeList.add(i, i+1);
		}
	}
	
	
	private void getFNList(){
		int pos = 0;
		for (Integer id : falseNegativeList){
			pos = (id - 1)*2;
			this.FN.add(PN.get(pos));
			this.FN.add(PN.get(pos+1));
		}
	}
	
	
	public void addFPos(int x, int y){
		this.false_positives++;
		double c[] = getCoord(x, y);
		this.FP.add(c[0]);
		this.FP.add(c[1]);		
	}
	
	
	public void addTNeg(int x, int y){
		this.true_negatives++;
		double c[] = getCoord(x, y);
		this.TN.add(c[0]);
		this.TN.add(c[1]);
	}
	
	public void addFNeg(int x, int y){
		this.false_negatives++;
		double c[] = getCoord(x, y);
		this.FN.add(c[0]);
		this.FN.add(c[1]);		
	}
	

	public String toString(){
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
	    String str = "\t\t|\t"+ true_positives + "\t|\t" + false_positives + "\t\t|\tPPV = " + df.format(ppv)+ "%\t|\n";
	    str += "\t\t|\t"+ false_negatives + "\t|\t" + true_negatives + "\t\t|\tNPV = " + df.format(npv)+ "%\t|\n";
	    return str;
	}
	
	
	public String getPrecAndRecall(){
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
	    String str = "\t\tPrecision = "+df.format(ppv)+ "%\t Recall = "+df.format(recall)+"\n";
	    return str;
	}
	
	
	public String getCSVMetrics(){
	    return getCSVLine(ppv, recall, true_positives, false_positives, false_negatives, true_negatives, ";");
	}
	
	
	private String getCSVLine(double ppv, double recall, int truePos, 
			int falsePos, int falseNeg, int trueNeg, String separator){
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		return df.format(ppv) + separator +df.format(recall) + separator +  truePos +
				separator + falsePos + separator + falseNeg + separator + trueNeg;
	}

}
