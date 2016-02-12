package atten_functions;

public class LinearDecay implements IAttenFunc{
	
	private int id;
	private double c;
	private String name = "Linear";
	private String descp = "Attenuates values by decreasing a constant percentage.";

	public LinearDecay(int id){
		this.id = id;
		this.c = 5.0;
	}
	
	
	public double apply(int x, int y, double value) {
		return Math.max(0, value - c);
	}

	public String getDescription() {
		return descp;
	}

	public int getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public double getConstant() {
		return c;
	}
	
	public void setConstant(int c) {
		this.c = c;
	}

}
