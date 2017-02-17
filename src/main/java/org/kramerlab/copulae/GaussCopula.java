package org.kramerlab.copulae;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.kramerlab.vines.Utils;

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
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		p = params[0];
	}

	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		double out = 1/Math.sqrt(1-p*p) *
				Math.exp(- (p*p*(a*a+b*b)-2*p*a*b) / (2*(1-p*p)) );
		
		return out;
	}

	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		
		double out = standardNormal.cumulativeProbability(
				( a-p*b ) / Math.sqrt(1-p*p) );
		
		return out;
	}

	@Override
	public double inverseHFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		
		return standardNormal.cumulativeProbability( a*Math.sqrt(1-p*p)+p*b );
	}
	
	/**
	 * The static copula density for a bivariate observation x, y.
	 * <br>
	 * The static function is used for RVine-Matrix calculations.
	 *
	 * @param	x an observation from a random variable.
	 * @param	y an observation from another random variable.
	 * @param	p the Gauss copula parameter.
	 * @return	returns the copula density.
	 */
	public static double density(double x, double y, double p) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		double out = 1/Math.sqrt(1-p*p) *
				Math.exp(- (p*p*(a*a+b*b)-2*p*a*b) / (2*(1-p*p)) );
		
		return out;
	}
	
	/**
	 * The static h-function for the copula.
	 * It is used to create pseudo observations.
	 * <br>
	 * The static function is used for RVine-Matrix calculations.
	 *
	 * @param	x	to be conditioned parameter
	 * @param	y	to be conditioning parameter
	 * @param	p the Gauss copula parameter.
	 * @return returns the constrained value x|y.
	 */
	public static double hFunction(double x, double y, double p) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		
		double out = standardNormal.cumulativeProbability(
				( a-p*b ) / Math.sqrt(1-p*p) );
		
		return out;
	}
	
	/**
	 * The static inverse h-function.
	 * It is used to de-transform the values for sampling.
	 * <br>
	 * The static function is used for RVine-Matrix calculations.
	 *
	 * @param	x	to be unconditioned parameter
	 * @param	y	the conditioning parameter
	 * @param	p the Gauss copula parameter.
	 * @return returns the unconditioned value.<br>
	 * If x is z|y, the method returns z.
	 */
	public static double inverseHFunction(double x, double y, double p) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = standardNormal.inverseCumulativeProbability(x);
		double b = standardNormal.inverseCumulativeProbability(y);
		
		return standardNormal.cumulativeProbability( a*Math.sqrt(1-p*p)+p*b );
	}
	
	@Override
	public double tau(){
		return 2/Math.PI*Math.asin(p);
	}
	
	@Override
	public String name() {
		return "Gauss";
	}	
}
