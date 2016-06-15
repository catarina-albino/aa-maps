package accum_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public class Sum implements IAccumFunc{
	
	private int id;
	private String name = "Sum";
	private String bdFunction = "sum";
	private String descp = "Aggregates by adding the effects.";
	private ArrayList<FuncParam> params;

	public Sum(int id){
		this.id = id;
	}
	
	public double apply(double oldVal, double newValues) {
		return oldVal + newValues;
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
