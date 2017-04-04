package weka.estimators.vines.copulas;

public class Clayton270RotatedCopula extends ClaytonCopula{

	public Clayton270RotatedCopula(double[] params) {
		super(params);
		lb = -20;
		ub = -tol;
		start = -2;
	}

	@Override
	public double C(double x, double y) {
		double out = 0;
		d = -d;
		out = x - super.C(x, 1-y);
		d = -d;
		return out;
	}
	
	@Override
	public double density(double x, double y) {
		double out = 0;
		d = -d;
		out = super.density(x, 1-y);
		d = -d;
		return out;
	}
	
	@Override
	public double h1Function(double x, double y) {		
		double out = 0;
		d = -d;
		out = hFunction(1-y, x);
		d = -d;
		return 1-out;
	}
	
	@Override
	public double h2Function(double x, double y) {		
		double out = 0;
		d = -d;
		out = hFunction(x, 1-y);
		d = -d;
		return out;
		
	}
	
	@Override
	public double tau() {
		return d/(-d+2);
	}
	
	@Override
	public String name() {
		return "C270";
	}

}
