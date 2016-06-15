package atten_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public class ExponentialEaseIn implements IAttenFunc{
	
	private int id;
	private double c;
	
	private String name = "Exponential \n EaseIn";
	private String descp = "Attenuates values by decreasing them exponencially";
	private ArrayList<FuncParam> params;

	public ExponentialEaseIn(int id){
		this.id = id;
		this.c = -0.5;
		//this.params.add(c);
	}
	
	public double apply(int x, int y, double value) {
		return Math.pow(value, c);
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

	public ArrayList<FuncParam> getParams() {
		return params;
	}

	public boolean isOn() {
		return true;
	}
}
