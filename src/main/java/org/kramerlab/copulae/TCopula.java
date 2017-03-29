package org.kramerlab.copulae;

import org.kramerlab.functions.CopulaMLE;
import org.kramerlab.vines.Utils;
import umontreal.ssj.probdist.StudentDist;
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
		lb = -1;
		ub = 1;
		start = 0;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		p = params[0];
		if(params.length > 1)
			v = (int) params[1];
	}
	
	@Override
	public double C(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = StudentDist.inverseF(v, x);
		double b = StudentDist.inverseF(v, y);
		
		return BiStudentDist.cdf(v, a, b, p);
	}
	
	@Override
	public double density(double x, double y) {		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = StudentDist.inverseF(v, x);
		double b = StudentDist.inverseF(v, y);
		
		double pp = p*p;
		
		double ad = StudentDist.density(v, a);
		double bd = StudentDist.density(v, b);
		
		double out = Math.pow(1 + (a*a + b*b - 2*p*a*b)/(v*(1-pp)), -(v+2)/2.0)
				/(2*Math.PI*ad*bd*Math.sqrt(1-pp));
		
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
		
		double a = StudentDist.inverseF(v, x);
		double b = StudentDist.inverseF(v, y);
		
		double out = StudentDist.cdf(v+1, ((a-p*b)/Math.sqrt(((v+b*b)*(1-p*p))/(v+1))));
		return out;
	}
	
	@Override
	public double mle(double[] a, double[] b){
		CopulaMLE cmle = new CopulaMLE(this, a, b);
		double[] initX = new double[]{0, 8};
		double[][] constr= new double[][]{{lb+tol, 2+tol}, {ub-tol, 30}};
		// cmle.setDebug(true);
		
		try {
			double[] x = cmle.findArgmin(initX, constr); 
			 while(x == null){  // 200 iterations are not enough
			    x = cmle.getVarbValues();  // Try another 200 iterations
			    x = cmle.findArgmin(x, constr);
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -cmle.getMinFunction();
	}
	
	@Override
	public double tau() {
		return 2/Math.PI*Math.asin(p);
	}

	@Override
	public String name() {
		return "T";
	}
	
	@Override
	public double[] getParBounds() {
		return new double[]{lb, ub, 1, 30};
	}
}
