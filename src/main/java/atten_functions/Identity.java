package atten_functions;

public class Identity implements IAttenFunc{
	
	private int id;
	private String name = "Identity";
	private String descp = "None attenuation aplied";

	public Identity(int id){
		this.id = id;
	}
	
	public double apply(int x, int y, double value) {
		return value;
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

}
