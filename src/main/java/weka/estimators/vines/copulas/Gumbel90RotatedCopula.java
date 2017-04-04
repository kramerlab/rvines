package weka.estimators.vines.copulas;

public class Gumbel90RotatedCopula extends GumbelCopula{

	public Gumbel90RotatedCopula(double[] params) {
		super(params);
		lb = -20;
		ub = -1-tol;
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
		return -(1+1/d);
	}
	
	@Override
	public String name() {
		return "Gu90";
	}
}
