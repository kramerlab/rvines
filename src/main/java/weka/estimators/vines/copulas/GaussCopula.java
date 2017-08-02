package weka.estimators.vines.copulas;

import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.probdistmulti.BiNormalDist;
import weka.estimators.vines.VineUtils;

/**
 * This is the class to represent Gauss copula family for RVines.
 * <br>
 * The Kendall's tau calculation was presented by H. B. Fang, K. T. Fang and S. Kotz (2002):
 * The meta-elliptical distributions with given marginals.
 * <br>
 * The cumulative distribution function was presented in P.X.-K. Song (2000):
 * Multivariate dispersion models generated from gaussian copula.
 * <br>
 * The density function, the h-function and its inverse were
 * presented by K. Aas et al. (2009): Pair-copula constructions of
 * multiple dependence.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GaussCopula extends AbstractCopula{
	private static final long serialVersionUID = 7450364339088849215L;
	private double p;
	
	/**
	 * Constructor
	 */
	public GaussCopula() {
		p = 0;
		lb = new double[]{-1+tol};
		ub = new double[]{1-tol};
		start = new double[]{0};
	}

	/**
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {p}
	 * <br>
	 * p : probability | -1 &lt; p &lt; 1
	 */
	@Override
	public void setParams(double[] params){
		super.setParams(params);
		p = params[0];
	}

	public double C(double x, double y) {
		if(p==0) return x*y;
		
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		double a = NormalDist.inverseF01(x);
		double b = NormalDist.inverseF01(y);
		
		return BiNormalDist.cdf(a, b, p);
	}
	
	public double density(double x, double y) {
		if(p==0) return 1;
		
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		double a = NormalDist.inverseF01(x);
		double b = NormalDist.inverseF01(y);
		
		double pp = p*p;
		
		double out = Math.exp(-(pp*(a*a+b*b)-2*p*a*b) / (2*(1-pp)))
						/Math.sqrt(1-pp);
		
		return out;
	}

	public double h1Function(double x, double y) {
		if(p==0) return y;
		
		return hFunction(y, x);
	}

	public double h2Function(double x, double y) {
		if(p==0) return x;
		
		return hFunction(x, y);
	}
	
	/**
	 * H function for Gauss Copula.
	 * @param x input parameter, 0 &lt; x &lt; 1.
	 * @param y input parameter, 0 &lt; y &lt; 1.
	 * @return returns the conditioned value x|y.
	 */
	public double hFunction(double x, double y) {
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		double a = NormalDist.inverseF01(x);
		double b = NormalDist.inverseF01(y);
		
		double out = NormalDist.cdf01(
				( a-p*b ) / Math.sqrt(1-p*p) );
		
		return out;
	}
	
	public double tau(){
		return 2/Math.PI*Math.asin(p);
	}
	
	public String name() {
		return "Gauss";
	}
	
	public String token() {
		return "G";
	}
}
