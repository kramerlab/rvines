package org.kramerlab.copulae;

import org.kramerlab.vines.Utils;

/**
 * This is the class to represent Gumbel copula family for RVines.
 * <br>
 * The Kendall's tau calculation is presented in J.F. Di&szlig;mann's diploma thesis (2010):
 * Statistical inference for regular vines and application.
 * <br>
 * The density function, the h-function and its inverse were
 * presented by K. Aas et al. (2009): Pair-copula constructions of
 * multiple dependence.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GumbelCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public GumbelCopula(double[] params) {
		super(params);
		d = params[0];
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-Math.pow(Math.pow(-Math.log(x), d)
				+Math.pow(-Math.log(y), d), 1/d));
		
		out = out*1/(x*y)*Math.pow(Math.pow(-Math.log(x), d)
				+Math.pow(-Math.log(y), d), 2/d-2)*
				Math.pow(Math.log(x)*Math.log(y), d-1)*
				(1+(d-1)*Math.pow(Math.pow(-Math.log(x), d)
						+Math.pow(-Math.log(y), d), -1/d));
		
		return out;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-Math.pow(Math.pow(-Math.log(x), d)
				+Math.pow(-Math.log(y), d), 1/d));
		
		out = out/y*Math.pow(-Math.log(y), d-1)*
				Math.pow(Math.pow(-Math.log(x), d)
				+Math.pow(-Math.log(y), d), 1/d-1);
				
		return out;
	}

	@Override
	public double inverseHFunction(double x, double y) {
		//Use Newton's method to solve h(x,y)-z = 0 with fixed y and z.
		double z = x;
		double x1 = 0.5;
		double e = 1;
		
		while(e > Math.pow(10, -10)){
			double x2 = x1-(hFunction(x1, y)-z)/density(x1, y);
			e = Math.abs(x2 - x1);
			x1 = x2;
		}
		
		return x1;
	}
	
	@Override
	public double tau() {
		return 1-1/d;
	}

	@Override
	public String name() {
		return "Gumbel";
	}	
}
