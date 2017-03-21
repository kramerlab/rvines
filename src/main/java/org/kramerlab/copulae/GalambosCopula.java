package org.kramerlab.copulae;

import org.kramerlab.functions.GalambosTauf;
import org.kramerlab.functions.H1;
import org.kramerlab.functions.H2;
import org.kramerlab.vines.Utils;

/**
 * This is the class to represent Galambos copula family for RVines.
 * <br>
 * The cumulative distribution function, the density function and the 
 * h-function were presented by D. Schirmacher and E. Schirmacher (2008):
 * Multivariate dependence modeling using pair-copulas.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GalambosCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public GalambosCopula(double[] params) {
		super(params);
		d = params[0];
		lb = 0;
		ub = Double.POSITIVE_INFINITY;
		indep = 0;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double C(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double xl = -Math.log(x);
		double yl = -Math.log(y);
		
		double xt = Math.pow(xl, -d);
		double yt = Math.pow(yl, -d);
		
		return x*y*Math.exp(Math.pow(xt+yt, -1/d));
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double xl = -Math.log(x);
		double yl = -Math.log(y);
		
		double xt = Math.pow(xl, -d);
		double yt = Math.pow(yl, -d);
		
		double xtyt = xt+yt;
		double xtytd = Math.pow(xt+yt, -1/d);
		
		double out = Math.exp(xtytd)*(1 - xtytd/xtyt * (xt/xl + yt/yl)
				+ xtytd/(xtyt*xtyt)*Math.pow(xl*yl, -d-1)*(1+d+xtytd));
		
		return out;
	}
	
	@Override
	public double h1Function(double x, double y) {
		return hFunction(y, x);
	}

	@Override
	public double h2Function(double x, double y) {
		return hFunction(x, y);
	}
	
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double xl = -Math.log(x);
		double yl = -Math.log(y);
		
		double xt = Math.pow(xl, -d);
		double yt = Math.pow(yl, -d);
		
		return x*Math.exp(Math.pow(xt+yt, -1/d))*
				(1 - Math.pow(1 + xt/yt, -1-1/d));
		
	}
	
	@Override
	public double tau() {
		return Utils.simpsonIntegrate(new GalambosTauf(d), 1000, 0, 1);
	}

	@Override
	public String name() {
		return "Ga";
	}
}
