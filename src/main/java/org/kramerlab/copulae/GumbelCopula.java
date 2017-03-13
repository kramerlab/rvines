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
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	private double npl(double x){
		return Math.pow(-Math.log(x), d);
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-Math.pow(npl(x)+npl(y), 1/d));
		
		out = out/(x*y)*Math.pow(npl(x)+npl(y), 2/d-2)*
				Math.pow(Math.log(x)*Math.log(y), d-1)*
				(1+(d-1)*Math.pow(npl(x)+npl(y), -1/d));
		
		return out;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-Math.pow(npl(x)+npl(y), 1/d));
		
		out = out/y*Math.pow(-Math.log(y), d-1)*
				Math.pow(npl(x)+npl(y), 1/d-1);
				
		return out;
	}

	//https://github.com/tnagler/VineCopula/blob/master/src/hfunc.c
	@Override
	public double inverseHFunction(double x, double y) {
		boolean br = false;
	    double ans = 0.0, tol = 0, x0 = 0, x1 = 1, it=0, fl, fh, val;
	    fl = hFunction(x0, y);
	    fl -= x;
	    fh = hFunction(x1, y);
	    fh -= x;
	    
	    if (Math.abs(fl) <= tol) {
	        ans = x0;
	        br = true;
	    }
	    if (Math.abs(fh) <= tol) {
	        ans = x1;
	        br = true;
	    }

	    while (!br){
	        ans = (x0 + x1) / 2.0;
	        val = hFunction(ans, y);
	        val -= x;

	        //stop if values become too close (avoid infinite loop)
	        if (Math.abs(val) <= tol) br = true;
	        if (Math.abs(x0-x1) <= tol) br = true;

	        if (val > 0.0) {
	            x1 = ans;
	            fh = val;
	        } else {
	            x0 = ans;
	            fl = val;
	        }

	        //stop if too many iterations are required (avoid infinite loop)
	        ++it;
	        if (it > 50) br = true;
	    }

	    return ans;
	}
	
	@Override
	public double tau() {
		return 1-1/d;
	}
	
	public static GumbelCopula mle(double[] a, double[] b){
		double p = 5;
		
		GumbelCopula c = new GumbelCopula(new double[]{p});
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
			c.setParams(new double[]{p1});
			logLik1 = Utils.logLikelihood(c, a, b);
			
			c.setParams(new double[]{p2});
			logLik2 = Utils.logLikelihood(c, a, b);
			
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
	public String name() {
		return "Gumbel";
	}
}
