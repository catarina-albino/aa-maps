package atten_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public class HalfLifeDecay implements IAttenFunc{
	
	private static double DEFAULT_C = 5.0;
	private int id;
	private String name = "HalfLife \n Decay";
	private String descp = "Attenuates values by decreasing them with Nt = N0*e^(-c*t)";
	private ArrayList<FuncParam> params;

	public HalfLifeDecay(int id){
		this.id = id;
		FuncParam c = new FuncParam("c", DEFAULT_C);
		this.params = new ArrayList<FuncParam>();
		this.params.add(c);
	}
	
	public double apply(int x, int y, double value) {
		double c = params.get(0).getValue();
		double halfVal = Math.round(value*Math.exp((-1)/c) * 100.0) / 100.0;
		if (halfVal >= 1) return halfVal;
		else return 0;
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
		return params.get(0).getValue();
	}
	
	public void setConstant(int c) {
		this.params.get(0).setValue(c);
	}

	public ArrayList<FuncParam> getParams() {
		return params;
	}

	public boolean isOn() {
		return true;
	}
}
