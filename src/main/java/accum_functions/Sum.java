package accum_functions;

public class Sum implements IAccumFunc{
	
	private int id;
	private String name = "Sum";
	private String bdFunction = "sum";
	private String descp = "Aggregates by adding the effects.";

	public Sum(int id){
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
	

}
