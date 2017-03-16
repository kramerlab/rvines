package org.kramerlab.copulae;

import org.apache.commons.math3.distribution.TDistribution;
import org.kramerlab.vines.Utils;

import umontreal.ssj.probdistmulti.BiStudentDist;

/**
 * This is the class to represent Student T copula family for RVines.
 * <br>
 * The Kendall's tau calculation was presented by H. B. Fang, K. T. Fang and S. Kotz (2002):
 * The meta-elliptical distributions with given marginals.
 * <br>
 * The cumulative distribution function was presented in P.X.-K. Song (2000):
 * Multivariate dispersion models generated from gaussian copula.
 * <br>
 * <br>
 * The cumulative distribution function, the density function, the h-function
 * and its inverse were presented by K. Aas et al. (2009): Pair-copula constructions of
 * multiple dependence.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class TCopula extends AbstractCopula{
	private static TDistribution t;
	private double p;
	private int v;
	
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
		v = (int) params[1];
		t = new TDistribution(v);
		lb = -1;
		ub = 1;
		indep = 0;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		p = params[0];
		if(params.length > 1){
			v = (int) params[1];
			t = new TDistribution(v);
		}
	}
	
	@Override
	public double C(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = t.inverseCumulativeProbability(x);
		double b = t.inverseCumulativeProbability(y);
		
		return BiStudentDist.cdf(v, a, b, p);
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = t.inverseCumulativeProbability(x);
		double b = t.inverseCumulativeProbability(y);
		
		double pp = p*p;
		
		double out = Math.pow(1 + (a*a + b*b - 2*p*a*b)/(v*(1-pp)), -(v+2)/2.0)
				/(2*Math.PI*t.density(a)*t.density(b)*Math.sqrt(1-pp));
		
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
	
	/**
	 * H function for T Copula.
	 * Since T Copula is symmetric, we don't need
	 * separate h functions.
	 * @param x, y input parameters.
	 * @return returns the conditioned value x|y.
	 */
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
	public double mle(double[] a, double[] b){
		double ps = p;
		double pl = p;
		int vl = 1;
		
		setParams(new double[]{ps, vl});
		double ll = Utils.mle(this, a, b, lb, ub, indep, tol);
		
		setParams(new double[]{ps, vl+1});
		double lln = Utils.mle(this, a, b, lb, ub, indep, tol);
		
		while(ll < lln && vl <= 10){
			ll = lln;
			pl = p;
			vl = v;
			setParams(new double[]{ps, vl+1});
			lln = Utils.mle(this, a, b, lb, ub, indep, tol);
		}
		
		setParams(new double[]{pl, vl});
		
		return ll;
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
