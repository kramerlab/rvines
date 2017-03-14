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
	
	public static GaussCopula mle(double[] a, double[] b){
		double p = Math.random()*2-1;
		GaussCopula c = new GaussCopula(new double[]{p});
		double actualLogLik = Utils.logLikelihood(c,a,b);
		double nextLogLik = Double.NEGATIVE_INFINITY;
		double delta = 1.0;		//step length
		
		while((Math.abs(actualLogLik-nextLogLik) > Math.pow(10, -10)
				|| actualLogLik == Double.NEGATIVE_INFINITY)
				&& delta > Math.pow(10, -20)){
			actualLogLik = Math.max(actualLogLik, nextLogLik);
			nextLogLik = Double.NEGATIVE_INFINITY;
			
			//watch the parameters, that are in delta range
			double p1 = p-delta;
			double p2 = p+delta;
			
			double logLik1 = Double.NEGATIVE_INFINITY;
			double logLik2 = Double.NEGATIVE_INFINITY;
			
			//calculate the new parameters log-likelihood
			if(-1 < p1 && p1 < 1){
				c.setParams(new double[]{p1});
				logLik1 = Utils.logLikelihood(c, a, b);
			}
			if(-1 < p2 && p2 < 1){
				c.setParams(new double[]{p2});
				logLik2 = Utils.logLikelihood(c, a, b);
			}
			
			//if there is no improvement
			if(Math.max(logLik1, logLik2) <= actualLogLik){
				delta = delta * 0.1; //reduce step length
			}else{
				//else set the better improvement
				nextLogLik = Math.max(logLik1, logLik2);
				if(nextLogLik == logLik1){
					p = p1;
				}else{
					p = p2;
				}
			}
		}
		
		c.setParams(new double[]{p});
		return c;
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
