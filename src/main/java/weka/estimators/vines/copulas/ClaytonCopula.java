package weka.estimators.vines.copulas;

import weka.estimators.vines.VineUtils;

/**
 * This is the class to represent Clayton copula family for RVines.
 * <br>
 * The Kendall's tau calculation was presented by M. Mahfoud and M. Michael (2012):
 * Bivariate archimedean copulas: an application to two stock market indices.
 * <br>
 * The cumulative distribution function, the density function, the h-function
 * and its inverse were presented by K. Aas et al. (2009):
 * Pair-copula constructions of multiple dependence.
 * <br>
 * The rotations are presented by Brechmann, E. C. &amp; Schepsmeier, U. (2013):
 * Modeling dependence with C-and D-vine copulas: The R-package CDVine.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class ClaytonCopula extends AbstractCopula{
	private static final long serialVersionUID = -365618187939149392L;
	protected double d;
	
	/**
	 * Constructor
	 */
	public ClaytonCopula() {
		rotations = true;
		d = 2;
		params = new double[]{d};
		lb = new double[]{0+tol};
		ub = new double[]{20};
		start = new double[]{2};
	}

	/**
	 * Parameter setter
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * for normal and 180 deg rotated : d : 0 &lt; d &lt; infinity
	 * for 90 and 270 deg rotated : d : -infinity &lt; d &lt; 0
	 */
	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double C(double x, double y){
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		return Math.pow(Math.pow(x, -d)+Math.pow(y, -d)-1, -1/d);
	}
	
	@Override
	public double density(double x, double y) {
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		double out = (1+d)*Math.pow(x*y, -1-d)
				*Math.pow(Math.pow(x, -d)+Math.pow(y, -d)-1, -1/d-2);
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
	 * H function for Clayton Copula.
	 * @param x input parameter, 0 &lt; x &lt; 1.
	 * @param y input parameter, 0 &lt; y &lt; 1.
	 * @return returns the conditioned value x|y.
	 */
	public double hFunction(double x, double y) {
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		double xpd = Math.pow(x, -d);
		double ypd = Math.pow(y, -d);
		
		double out = ypd/y*Math.pow(xpd+ypd-1, -(d+1)/d);
		
		return out;
	}
	
	@Override
	public double tau() {
		return d/(d+2);
	}
	
	@Override
	public String name() {
		return "Clayton";
	}
	
	@Override
	public String token() {
		return "C";
	}
}