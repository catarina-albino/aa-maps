package accum_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public interface IAccumFunc {
	public double apply(double oldVal, double newVal);
	public String getDescription();
	public String getName();
	public String getBDFunction();
	public int getID();	
	public ArrayList<FuncParam> getParams();	
}
