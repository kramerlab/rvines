package org.kramerlab.copulae;

import org.kramerlab.vines.Utils;

/**
 * This is a placeholder for the Farlie-Gumbel-Morgenstern (FGM) copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class FGMCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public FGMCopula(double[] params) {
		super(params);
		d = params[0];
	}

	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = 1+d*(1-2*x)*(1-2*y);
		
		return out;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = x*(1+d*(1-x)*(1-2*y));
		
		return out;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		// TODO Use Newton to solve h(x,y)-z = 0
		return 0;
	}
	
	@Override
	public double tau() {
		return 2/9*d;
	}

	@Override
	public String name() {
		return "FGM";
	}
}
