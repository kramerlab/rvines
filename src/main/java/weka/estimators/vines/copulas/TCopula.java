package weka.estimators.vines.copulas;

import umontreal.ssj.probdist.StudentDist;
import umontreal.ssj.probdistmulti.BiStudentDist;
import weka.estimators.vines.VineUtils;

/**
 * This is the class to represent Student T copula family for RVines.
 * <br>
 * The Kendall's tau calculation was presented by H. B. Fang, K. T. Fang and
 * S. Kotz (2002):
 * The meta-elliptical distributions with given marginals.
 * <br>
 * The cumulative distribution function was presented in P.X.-K. Song (2000):
 * Multivariate dispersion models generated from gaussian copula.
 * <br>
 * <br>
 * The cumulative distribution function, the density function, the h-function
 * and its inverse were presented by K. Aas et al. (2009): Pair-copula
 * constructions of multiple dependence.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class TCopula extends AbstractCopula{
	private static final long serialVersionUID = 2868000059648884715L;
	private double p;
	private int v;
	
	/**
	 * Constructor
	 */
	public TCopula() {
		p = 0.5;
		v = 8;
		lb = new double[]{-1, 2};
		ub = new double[]{1, 30};
		start = new double[]{0.5, 8};
	}

	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {p, v}
	 * <br>
	 * p : probability | -1 &lt; p &lt; 1
	 * v : degree of freedom | natural number &gt; 0
	 */
	@Override
	public void setParams(double[] params){
		super.setParams(params);
		p = params[0];
		if(params.length > 1)
			v = (int) params[1];
	}
	
	@Override
	public double C(double x, double y) {
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		double a = StudentDist.inverseF(v, x);
		double b = StudentDist.inverseF(v, y);
		
		return BiStudentDist.cdf(v, a, b, p);
	}
	
	@Override
	public double density(double x, double y) {		
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
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
	 * @param x input parameter, 0 &lt; x &lt; 1.
	 * @param y input parameter, 0 &lt; y &lt; 1.
	 * @return returns the conditioned value x|y.
	 */
	public double hFunction(double x, double y) {
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		double a = StudentDist.inverseF(v, x);
		double b = StudentDist.inverseF(v, y);
		
		double out = StudentDist.cdf(v+1, ((a-p*b)/Math.sqrt(((v+b*b)*(1-p*p))
				/(v+1))));
		return out;
	}
	
	@Override
	public double tau() {
		return 2/Math.PI*Math.asin(p);
	}

	@Override
	public String name() {
		return "Student T";
	}
	
	@Override
	public String token() {
		return "T";
	}
}