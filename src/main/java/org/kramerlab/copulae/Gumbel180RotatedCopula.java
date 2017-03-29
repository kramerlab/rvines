package org.kramerlab.copulae;

public class Gumbel180RotatedCopula extends GumbelCopula{

	public Gumbel180RotatedCopula(double[] params) {
		super(params);
		lb = 1+tol;
		ub = 20;
		start = 2;
	}

	@Override
	public double C(double x, double y) {
		return x + y - 1 + super.C(1-x, 1-y);
	}
	
	@Override
	public double density(double x, double y) {		
		return super.density(1-x, 1-y);
	}
	
	@Override
	public double h1Function(double x, double y) {		
		return 1-hFunction(1-y, 1-x);
	}
	
	@Override
	public double h2Function(double x, double y) {		
		return 1-hFunction(1-x, 1-y);
	}

	@Override
	public String name() {
		return "Gu180";
	}
}
