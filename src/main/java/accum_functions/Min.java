package accum_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public class Min implements IAccumFunc{
	
	private String name = "Min";
	private String bdFunction = "min";
	private int id;
	private String descp = "Uses the min value.";
	private ArrayList<FuncParam> params;

	public Min(int id){
		this.id = id;
	}
	
	public double apply(double oldVal, double newValues) {
		if (oldVal > 0.0 ) return Math.min(oldVal,newValues);
		else return newValues;
	}

	public String getDescription() {
		return descp;
	}

	public String getBDFunction() {
		return bdFunction;
	}

	
	public int getID() {
		return id;
	}
	
	
	public String getName() {
		return name;
	}

	public ArrayList<FuncParam> getParams() {
		return params;
	}

}
