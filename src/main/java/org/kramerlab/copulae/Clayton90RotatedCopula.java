package org.kramerlab.copulae;

public class Clayton90RotatedCopula extends ClaytonCopula{

	public Clayton90RotatedCopula(double[] params) {
		super(params);
		lb = -20;
		ub = -tol;
		start = -2;
	}

	@Override
	public double C(double x, double y) {
		double out = 0;
		d = -d;
		out = y - super.C(1-x, y);
		d = -d;
		return out;
	}
	
	@Override
	public double density(double x, double y) {		
		double out = 0;
		d = -d;
		out = super.density(1-x, y);
		d = -d;
		return out;
	}
	
	@Override
	public double h1Function(double x, double y) {		
		double out = 0;
		d = -d;
		out = hFunction(y, 1-x);
		d = -d;
		return out;
	}
	
	@Override
	public double h2Function(double x, double y) {		
		double out = 0;
		d = -d;
		out = hFunction(1-x, y);
		d = -d;
		return 1-out;
	}
	
	@Override
	public double tau() {
		return d/(-d+2);
	}
	
	@Override
	public String name() {
		return "C90";
	}
}
