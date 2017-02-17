package org.kramerlab.copulae;

import org.kramerlab.vines.Utils;

/**
 * This is the class to represent Clayton copula family for RVines.
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
public class ClaytonCopula extends AbstractCopula{
	private double d;
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * d : 0 &lt; d &lt; infinity
	 */
	public ClaytonCopula(double[] params) {
		super(params);
		d = params[0];
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = (1+d)*Math.pow(x*y, -1-d)
				*Math.pow(Math.pow(x, -d)+Math.pow(y, d)-1, -1/d-2);
		return out;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.pow(y, -1-d)
				*Math.pow(Math.pow(x, -d)+Math.pow(y, d)-1, -1-1/d);
		
		return out;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.pow(Math.pow(x*Math.pow(y, d+1), 1+1/d)
				+1-Math.pow(y, -d), -1/d);
		
		return out;
	}
	
	@Override
	public double tau() {
		return d/(d+2);
	}

	@Override
	public String name() {
		return "Clayton";
	}
}
