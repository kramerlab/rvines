package org.kramerlab.copulae;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.kramerlab.vines.Utils;
import umontreal.ssj.probdistmulti.BiNormalDist;

/**
 * This is the class to represent Gauss copula family for RVines.
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
public class GaussCopula extends AbstractCopula{
	private static NormalDistribution standardNormal = new NormalDistribution();
	private double p;
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {p}
	 * <br>
	 * p : probability | -1 &lt; p &lt; 1
	 */
	public GaussCopula(double[] params) {
		super(params);
		p = params[0];
		lb = -1;
		ub = 1;
		indep = 0;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		p = params[0];
	}

	public double C(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		
		return BiNormalDist.cdf(a, b, p);
	}
	
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		
		double pp = p*p;
		
		double out = Math.exp(-(pp*(a*a+b*b)-2*p*a*b) / (2*(1-pp)))
						/Math.sqrt(1-pp);
		
		return out;
	}

	public double h1Function(double x, double y) {
		return hFunction(y, x);
	}

	public double h2Function(double x, double y) {
		return hFunction(x, y);
	}
	
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		
		double out = standardNormal.cumulativeProbability(
				( a-p*b ) / Math.sqrt(1-p*p) );
		
		return out;
	}
	
	public double tau(){
		return 2/Math.PI*Math.asin(p);
	}
	
	public String name() {
		return "Gauss";
	}
}
