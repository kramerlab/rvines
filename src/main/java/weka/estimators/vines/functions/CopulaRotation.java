package weka.estimators.vines.functions;

import weka.estimators.vines.copulas.AbstractCopula;
import weka.estimators.vines.copulas.Copula;

public class CopulaRotation extends AbstractCopula{
	public enum Mode {ROT90, ROT180, ROT270}
	private Copula c;
	private Mode m;
	
	public CopulaRotation(Copula c, Mode m){
		if(c == null || m == null){
			System.err.println("Rotation Error, got any null value!");
		}
		
		this.c = c;
		this.m = m;		
		
		if(m != Mode.ROT180){
			// shift parameter space to negative
			double[][] bounds = c.getParBounds();
			lb = negate(bounds[1]);
			ub = negate(bounds[0]);
			start = negate(c.getMLEStart());
		}else{
			double[][] bounds = c.getParBounds();
			lb = bounds[0];
			ub = bounds[1];
			start = c.getMLEStart();
		}
		
		setParams(start);
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		if(m == Mode.ROT180){
			c.setParams(params);
		}
		else{
			c.setParams(negate(params));
		}
	}
	
	@Override
	public double C(double x, double y) {
		if(m == Mode.ROT90) return y - c.C(1-x, y);
		if(m == Mode.ROT180) return x + y - 1 + c.C(1-x, 1-y);
		if(m == Mode.ROT270) return x - c.C(x, 1-y);
		
		return 0;
	}

	@Override
	public double density(double x, double y) {
		if(m == Mode.ROT90) return c.density(1-x, y);
		if(m == Mode.ROT180) return c.density(1-x, 1-y);
		if(m == Mode.ROT270) return c.density(x, 1-y);
		
		return 1;
	}

	@Override
	public double h1Function(double x, double y) {
		if(m == Mode.ROT90) return c.h1Function(1-x, y);
		if(m == Mode.ROT180) return 1-c.h1Function(1-x, 1-y);
		if(m == Mode.ROT270) return 1-c.h1Function(x, 1-y);
		
		return 0;
	}

	@Override
	public double h2Function(double x, double y) {
		if(m == Mode.ROT90) return 1-c.h2Function(1-x, y);
		if(m == Mode.ROT180) return 1-c.h2Function(1-x, 1-y);
		if(m == Mode.ROT270) return c.h2Function(x, 1-y);
		
		return 0;
	}

	@Override
	public double tau() {
		double t = c.tau();
		if(m != Mode.ROT180) t = -t;
		return t;
	}

	@Override
	public String name() {
		String name = c.name();
		if(m == Mode.ROT90) name = name + " rotated by 90 degree";
		if(m == Mode.ROT180) name = name + " rotated by 180 degree";
		if(m == Mode.ROT270) name = name + " rotated by 270 degree";
		return name;
	}
	
	@Override
	public String token() {
		String name = c.token();
		if(m == Mode.ROT90) name = name + "90";
		if(m == Mode.ROT180) name = name + "180";
		if(m == Mode.ROT270) name = name + "270";
		return name;
	}
	
	private double[] negate(double[] a){
		double[] neg = new double[a.length];
		for(int i=0; i<a.length; i++) neg[i] = - a[i];
		return neg;
	}
}
