package atten_functions;

import java.util.ArrayList;

import aa_maps.FuncParam;

public class AboveValFunction implements IAttenFunc{
	
	private double t;
	
	public AboveValFunction(double t){
		this.t = t;
	}
	
	public double apply(int x, int y, double value) {
		if (value >= t) return value;
		else return 0;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ArrayList<FuncParam> getParams() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isOn() {
		// TODO Auto-generated method stub
		return false;
	}
}
