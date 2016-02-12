package accum_functions;

public class Min implements IAccumFunc{
	
	private String name = "Min";
	private String bdFunction = "min";
	private int id;
	private String descp = "Uses the min value.";

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

}
