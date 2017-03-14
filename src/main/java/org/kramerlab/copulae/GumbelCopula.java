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
	int mode=0;
	String[] modes = new String[]{"", "90", "180", "270"};
	
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
		
		if(mode == 1){
			double out = 0;
			d = -d;
			out = density0(1-x, y);
			d = -d;
			return out;
		}
		
		if(mode == 2)
			return density0(1-x, 1-y);
		
		if(mode == 3){
			double out = 0;
			d = -d;
			out = density0(x, 1-y);
			d = -d;
			return out;
		}
		
		return density0(x, y);
	}
	
	public double density0(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-Math.pow(npl(x)+npl(y), 1/d));
		
		out = out/(x*y)*Math.pow(npl(x)+npl(y), 2/d-2)*
				Math.pow(Math.log(x)*Math.log(y), d-1)*
				(1+(d-1)*Math.pow(npl(x)+npl(y), -1/d));
		
		return out;
	}
	
	@Override
	public double h1Function(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		if(mode == 1){
			double out = 0;
			d = -d;
			out = hFunction(y, 1-x);
			d = -d;
			return out;
		}
		
		if(mode == 2)
			return 1-hFunction(1-y, 1-x);
		
		if(mode == 3){
			double out = 0;
			d = -d;
			out = hFunction(1-y, x);
			d = -d;
			return 1-out;
		}
		
		return hFunction(y, x);
	}
	
	@Override
	public double h2Function(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		if(mode == 1){
			double out = 0;
			d = -d;
			out = hFunction(1-x, y);
			d = -d;
			return 1-out;
		}
		
		if(mode == 2)
			return 1-hFunction(1-x, 1-y);
		
		if(mode == 3){
			double out = 0;
			d = -d;
			out = hFunction(x, 1-y);
			d = -d;
			return out;
		}
		
		return hFunction(x, y);
	}
	
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
	
	public static Copula mle(double[] a, double[] b, double tau){

		GumbelCopula c0 = new GumbelCopula(new double[]{2});
		c0.mode=0;
		c0 = mle_sub(c0, a, b);
		double llc0 = Utils.logLikelihood(c0, a, b);
		
		GumbelCopula c2 = new GumbelCopula(new double[]{2});
		c2.mode=2;
		c2 = mle_sub(c2, a, b);
		double llc2 = Utils.logLikelihood(c2, a, b);
		
		GumbelCopula c1 = new GumbelCopula(new double[]{-2});
		c1.mode=1;
		c1 = mle_sub(c1, a, b);
		double llc1 = Utils.logLikelihood(c1, a, b);
		
		GumbelCopula c3 = new GumbelCopula(new double[]{-2});
		c3.mode=3;
		c3 = mle_sub(c3, a, b);
		double llc3 = Utils.logLikelihood(c3, a, b);
		
		Copula out = c0;
		double llout = llc0;
		
		if(llout < llc1){
			out = c1;
			llout = llc1;
		}
		if(llout < llc2){
			out = c2;
			llout = llc2;
		}
		if(llout < llc3){
			out = c3;
			llout = llc3;
		}
		
		return out;
	}
	
	public static GumbelCopula mle_sub(GumbelCopula c, double[] a, double[] b){
		double p = c.getParams()[0];
		
		double actualLogLik = Utils.logLikelihood(c, a, b);
		double nextLogLik = Double.NEGATIVE_INFINITY;
		double delta = 1.0;		//step length
		
		while((Math.abs(actualLogLik-nextLogLik) > Math.pow(10, -6)
				|| actualLogLik == Double.NEGATIVE_INFINITY)
				&& delta > Math.pow(10, -10)){
			
			//if(c.mode==1) System.out.println(p+" - "+actualLogLik);
			
			actualLogLik = Math.max(actualLogLik, nextLogLik);
			nextLogLik = Double.NEGATIVE_INFINITY;
			
			//watch the parameters, that are in delta range
			double p1 = p-delta;
			double p2 = p+delta;
			
			double logLik1 = Double.NEGATIVE_INFINITY;
			double logLik2 = Double.NEGATIVE_INFINITY;
			
			//calculate the new parameters log-likelihood
			if(c.mode==1 || c.mode==3 || p1 > 1){
				c.setParams(new double[]{p1});
				logLik1 = Utils.logLikelihood(c, a, b);
			}
			if(c.mode==0 || c.mode==2 || p2 < -1){
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
		//if(c.mode==1) System.out.println(p+" - "+actualLogLik+" - "+Utils.logLikelihood(c, a, b));
		return c;
	}
	
	@Override
	public String name() {
		return "Gu"+modes[mode];
	}
}
