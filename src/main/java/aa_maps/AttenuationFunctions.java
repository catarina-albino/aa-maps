package aa_maps;

import cern.colt.function.tdouble.IntIntDoubleFunction;

public class AttenuationFunctions extends Object {
	
	/** Little trick to allow for "aliasing", that is, renaming this class.
		Using the aliasing one can instead write:
	 	AttenuationFunctions At = AttenuationFunctions.functions; 
	*/
	public static final AttenuationFunctions functions = new AttenuationFunctions();
	
	
	/*****************************
   			Identity function
	 ****************************/
	 
	 /** Function that returns v (no decay applied) */
	 public static final IntIntDoubleFunction noDecay(){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				return v;
			}
		 };
	 }
	 
	 
	 
	 /*****************************
		   Constant functions
	  ****************************/
	 
	 /** Function that returns v - (constant * step)*/
	 public static final IntIntDoubleFunction linearStepDecay(final double c, final double step){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				return Math.max(0, v - (c*step));
			}
		 };
	 }
	 
	 
	 /** Function that returns v - constant*/
	 public static final IntIntDoubleFunction constantDecay(final double c){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				return Math.max(0, v - c);
			}
		 };
	 }
		 
	 
	 /** Function that returns v - constant*/
	 public static final IntIntDoubleFunction constantNegDecay(final double c){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				return v - c;
			}
		 };
	 }
		
	 
	 
	 /** Function that returns v + constant*/
	 public static final IntIntDoubleFunction constantGrowth(final double c){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				return  Math.max(0,v + c);
			}
		 };
	 }

	 
	 /*****************************
	   	  Exponential functions
	  ****************************/
	 
	 /** Function that returns x(t) = x0 * e^(-r * t) */
	 public static final IntIntDoubleFunction exponentialDecay(final double x0, final double r, final int t){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				return x0 * Math.exp((-r)*t);
			}
		 };
	 }
	 
	 /** Function that returns x(t) = x0 * (1+r)^t */
	 public static final IntIntDoubleFunction exponentialGrowth(final double x0, final double r, final int t){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				return Math.max(0, x0 * Math.pow((1+r), t));
			}
		 };
	 }
	  
	 
	 /*****************************
	   	 	Linear functions
	  ****************************/
	 
	 /** Function that returns x(t) = x0 - r*t */
	 public static final IntIntDoubleFunction linearDecay(final double x0, final double r, final int t){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				 return Math.max(0, x0 - r*t);
			}
		 };
	 }
 
	 
	 /** Function that returns x(t) = x0 + r*t */
	 public static final IntIntDoubleFunction linearGrowth(final double x0, final double r, final int t){
		 return new IntIntDoubleFunction() {
			public double apply(int arg0, int arg1, double v) {
				 return Math.max(0, x0 + r*t);
			}
		 };
	 }
	 
	 
	 /** Demonstrates usage of this class*/
	 @SuppressWarnings("static-access")
	public static void main(String[] args) {
		 AttenuationFunctions At = AttenuationFunctions.functions;
		 double a = 10, b = 50;
		 double c = 5.0, r = 1.5;
		 int t = 5;
		 IntIntDoubleFunction cDecay = At.constantDecay(c);
		 System.out.println(cDecay.apply(1,1,a));
		 System.out.println(cDecay.apply(1,1,b));
		 
		 IntIntDoubleFunction linDecay = At.linearDecay(b,r,t);
		 System.out.println(linDecay.apply(1, 1, b));
		 
		 IntIntDoubleFunction expDecay1 = At.exponentialDecay(b,r,t);
		 System.out.println(expDecay1.apply(1, 1, b));
		 IntIntDoubleFunction expDecay2 = At.exponentialDecay(b,r,2);
		 System.out.println(expDecay2.apply(1, 1, b));
	 }
}
