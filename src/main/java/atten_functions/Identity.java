package atten_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public class Identity implements IAttenFunc{
	
	private int id;
	private String name = "Identity";
	private String descp = "None attenuation aplied";
	private ArrayList<FuncParam> params;

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

	public ArrayList<FuncParam> getParams() {
		return params;
	}

	public boolean isOn() {
		return false;
	}

}
