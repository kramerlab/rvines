package org.kramerlab.copulae;

import org.kramerlab.functions.debyeSub;
import org.kramerlab.vines.Utils;

/**
 * This is a placeholder for the Frank copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class FrankCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public FrankCopula(double[] params) {
		super(params);
		d = params[0];
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	private double expD(double x){
		return Math.exp(d*x);
	}
	
	@Override
	public double density(double x, double y) {
		if(d == 0) return 1;
		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double z = d*expD(1+x+y)*(expD(1)-1);
		z = z/(expD(1)*(1-expD(x)+expD(x+y-1)-expD(y)));
		z = z/(expD(1)*(1-expD(x)+expD(x+y-1)-expD(y)));
		return z;
	}
	
	@Override
	public double hFunction(double x, double y) {
		if(d == 0) return x;
		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-d*y)/(Math.exp(-d*y)-1+
						(1-Math.exp(-d))/(1-Math.exp(-d*x)));
		
		return out;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		if(d == 0) return x;
		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = -Math.log(1-(1-Math.exp(-d))
				/(1+Math.exp(-d*y)*(1/x-1)))/d;
		
		return out;
	}
	
	@Override
	public double tau() {
		if(d == 0) return 0;
		return 1 - 4 / d * (1 - debye1(d));
	}

	private double debye1(double x){
		if(x == 0) return 1;
		
		double y = 1;
		y = Utils.simpsonIntegrate(new debyeSub(), 1000, 0, x);
		
		return y/x;
	}
	
	public static FrankCopula mle(double[] a, double[] b){
		double p = 0;
		
		FrankCopula c = new FrankCopula(new double[]{p});
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
		return "Frank";
	}
}
