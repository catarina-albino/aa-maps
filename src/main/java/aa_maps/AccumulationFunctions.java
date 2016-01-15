package aa_maps;

import cern.colt.function.tdouble.DoubleDoubleFunction;


public class AccumulationFunctions extends Object {
	
	/** Little trick to allow for "aliasing", that is, renaming this class.
		Using the aliasing one can instead write:
	 	AttenuationFunctions At = AttenuationFunctions.functions; 
	*/
	public static final AccumulationFunctions functions = new AccumulationFunctions();
	 
	 
	 /*********************************************************
		   Aggregation functions between t and t+1 (2 values) 
	  *********************************************************/
	 
	 /** Function that returns v1 + v2 */
	 public static final DoubleDoubleFunction sum = new DoubleDoubleFunction() {
		 public final double apply(double v1, double v2) { return v1 + v2; }
	 };
	 
	 /** Function that returns v1 + v2 */
	 public static final DoubleDoubleFunction positiveSum = new DoubleDoubleFunction() {
		 public final double apply(double v1, double v2) { return Math.max(0, v1 + v2); }
	 };
	 
	 
	 /** Function that returns (v1+v2)/2*/
	 public static final DoubleDoubleFunction avg = new DoubleDoubleFunction() {
		 public final double apply(double v1, double v2) { return (v1 + v2)/2; }
	 };
	 
	 
	 /** Function that returns max(v1,v2)*/
	 public static final DoubleDoubleFunction max = new DoubleDoubleFunction() {
		 public final double apply(double v1, double v2) { return Math.max(v1, v2); }
	 };
	 
	 /** Function that returns min(v1,v2)*/
	 public static final DoubleDoubleFunction min = new DoubleDoubleFunction() {
		 public final double apply(double v1, double v2) { return Math.min(v1, v2); }
	 };
	 

	 
	 /** Demonstrates usage of this class*/
	 @SuppressWarnings("static-access")
	public static void main(String[] args) {
		 AccumulationFunctions Ac = AccumulationFunctions.functions;
		 double a = 10; 
		 double b = 50;
		 DoubleDoubleFunction sum = Ac.sum;
		 DoubleDoubleFunction avg = Ac.avg;
		 DoubleDoubleFunction max = Ac.max;
		 DoubleDoubleFunction min = Ac.min;
		 System.out.println(sum.apply(a, b));
		 System.out.println(avg.apply(a, b));
		 System.out.println(max.apply(a, b));
		 System.out.println(min.apply(a, b));
	 }
}
