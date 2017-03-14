package org.kramerlab.copulae;

import org.apache.commons.math3.distribution.TDistribution;
import org.kramerlab.vines.Utils;

/**
 * This is the class to represent Student T copula family for RVines.
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
public class TCopula extends AbstractCopula{
	private static TDistribution t;
	private double p;
	private double v;
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {p, v}
	 * <br>
	 * p : probability | -1 &lt; p &lt; 1
	 * v : degree of freedom | natural number > 0
	 */
	public TCopula(double[] params) {
		super(params);
		p = params[0];
		v = params[1];
		t = new TDistribution(v);
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		p = params[0];
		v = params[1];
	}
	
	@Override
	public double density(double x, double y) {
		// Aas K. et al, Pair-copula constructions of multiple dependence
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = t.inverseCumulativeProbability(x);
		double b = t.inverseCumulativeProbability(y);
		
		double out = 1/(2*Math.PI*t.density(a)*t.density(b)*Math.sqrt(1-p*p));
		out = out * Math.pow(1 + (a*a + b*b - 2*p*a*b)/(v*(1-p*p)), -(v+2)/2);
		
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
		
		double a = t.inverseCumulativeProbability(x);
		double b = t.inverseCumulativeProbability(y);
		
		TDistribution t2 = new TDistribution(v+1);
		
		double out = t2.cumulativeProbability((a-p*b)/Math.sqrt(((v+b*b)*(1-p*p))/(v+1)));
		return out;
	}

	@Override
	public double inverseHFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		TDistribution t2 = new TDistribution(v+1);
		
		double a = t2.inverseCumulativeProbability(x);
		double b = t.inverseCumulativeProbability(y);
		
		double out = t.cumulativeProbability(a * Math.sqrt(((v+b*b)*(1-p*p))/(v+1)) + p*b);
		
		return out;
	}
	
	@Override
	public double tau() {
		return 2/Math.PI*Math.asin(p);
	}

	@Override
	public String name() {
		return "T";
	}	
}
