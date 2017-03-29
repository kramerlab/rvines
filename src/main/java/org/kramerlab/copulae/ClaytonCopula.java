package org.kramerlab.copulae;

import org.kramerlab.vines.Utils;

/**
 * This is the class to represent Clayton copula family for RVines.
 * <br>
 * The Kendall's tau calculation was presented by M. Mahfoud and M. Michael (2012):
 * Bivariate archimedean copulas: an application to two stock market indices.
 * <br>
 * The cumulative distribution function, the density function, the h-function
 * and its inverse werepresented by K. Aas et al. (2009):
 * Pair-copula constructions of multiple dependence.
 * <br>
 * The rotations are presented by Brechmann, E. C. & Schepsmeier, U. (2013):
 * Modeling dependence with C-and D-vine copulas: The R-package CDVine.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class ClaytonCopula extends AbstractCopula{
	protected double d;
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * for mode 0 and 2 : d : 0 &lt; d &lt; infinity
	 * for mode 1 and 3 : d : -infinity &lt; d &lt; 0
	 */
	public ClaytonCopula(double[] params) {
		super(params);
		d = params[0];
		lb = 0+tol;
		ub = 20;
		start = 2;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double C(double x, double y){
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		return Math.pow(Math.pow(x, -d)+Math.pow(y, -d)-1, -1/d);
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
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
	 * @param x, y input parameters.
	 * @return returns the conditioned value x|y.
	 */
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
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
		return "C";
	}

	@Override
	public double[] getParBounds() {
		return new double[]{lb, ub};
	}
}