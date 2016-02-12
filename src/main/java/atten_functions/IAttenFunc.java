package atten_functions;

import cern.colt.function.tdouble.IntIntDoubleFunction;

public interface IAttenFunc extends IntIntDoubleFunction{

	public double apply(int x, int y, double value);
	public String getDescription();
	public String getName();
	public int getID();	
}
