package weka.estimators.vines.copulas;

import weka.estimators.vines.Utils;

/**
 * This is the class to represent Gumbel copula family for RVines.
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
public class GumbelCopula extends AbstractCopula{
	private static final long serialVersionUID = 7409582996477732641L;
	protected double d;
	
	/**
	 * Constructor
	 */
	public GumbelCopula() {
		rotations = true;
		d = 2;
		lb = new double[]{1+tol};
		ub = new double[]{20};
		start = new double[]{2};
	}
	
	/**
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * for normal and 180 deg rotated : d : 1 &lt; d &lt; infinity
	 * for 90 and 270 deg rotated : d : -infinity &lt; d &lt; -1
	 */
	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double C(double x, double y){
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double xt = Math.pow(-Math.log(x), d);
		double yt = Math.pow(-Math.log(y), d);
		
		return Math.exp(-Math.pow(xt+yt, 1/d));
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double lx = Math.log(x);
		double ly = Math.log(y);
		
		double xt = Math.pow(-lx, d);
		double yt = Math.pow(-ly, d);
		
		double xtytd = Math.pow(xt+yt, 1/d);
		
		double out = Math.exp(-xtytd)/(x*y)
				*Math.pow(xt+yt, 2/d-2)*
				Math.pow(lx*ly, d-1)*
				(1+(d-1)/xtytd);
		
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
	 * H function for Gumbel Copula.
	 * @param x input parameter, 0 &lt; x &lt; 1.
	 * @param y input parameter, 0 &lt; y &lt; 1.
	 * @return returns the conditioned value x|y.
	 */
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double lx = Math.log(x);
		double ly = Math.log(y);
		
		double xt = Math.pow(-lx, d);
		double yt = Math.pow(-ly, d);
		
		double xtytd = Math.pow(xt+yt, 1/d);
		
		double out = Math.exp(-xtytd)/y*yt
				/(-ly)*xtytd/(xt+yt);
				
		return out;
	}
	
	@Override
	public double tau() {
		return 1-1/d;
	}
	
	@Override
	public String name() {
		return "Gumbel";
	}
	
	@Override
	public String token() {
		return "Gu";
	}
}