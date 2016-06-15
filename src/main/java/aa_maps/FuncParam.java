package aa_maps;

public class FuncParam {
	
	private String name, description;
	private double value;
	
	public FuncParam(String name, double value){
		this.name = name;
		this.value = value;
		this.description = "";
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}
	
	public void setValue(double v) {
		this.value = v;
	}
}
