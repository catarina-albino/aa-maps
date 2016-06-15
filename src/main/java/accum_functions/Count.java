package accum_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public class Count implements IAccumFunc{
	
	private String name = "Count";
	private String bdFunction = "count";
	private int id;
	private String descp = "Counts the number of events occured.";
	private ArrayList<FuncParam> params;

	public Count(int id){
		this.id = id;
	}
	
	public double apply(double oldVal, double newValues) {
		//double newSum = Arrays.stream(newValues).sum();
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
