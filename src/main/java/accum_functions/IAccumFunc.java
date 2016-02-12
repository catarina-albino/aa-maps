package accum_functions;

public interface IAccumFunc {

	public double apply(double oldVal, double newVal);
	public String getDescription();
	public String getName();
	public String getBDFunction();
	public int getID();	
}
