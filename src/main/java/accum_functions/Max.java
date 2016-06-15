package accum_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public class Max implements IAccumFunc{
	
	private int id;
	private String name = "Max";
	private String bdFunction = "max";
	private String descp = "Uses the max value.";
	private ArrayList<FuncParam> params;

	public Max(int id){
		this.id = id;
	}
	
	public double apply(double oldVal, double newValues) {
		//double newSum = Arrays.stream(newValues).sum();
		return Math.max(oldVal,newValues);
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
