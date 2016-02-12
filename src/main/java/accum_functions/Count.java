package accum_functions;

public class Count implements IAccumFunc{
	
	private String name = "Count";
	private String bdFunction = "count";
	private int id;
	private String descp = "Counts the number of events occured.";

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

}
