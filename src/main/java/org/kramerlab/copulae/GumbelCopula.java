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
 * <br>
 * The rotations are presented by Brechmann, E. C. & Schepsmeier, U. (2013):
 * Modeling dependence with C-and D-vine copulas: The R-package CDVine.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GumbelCopula extends AbstractCopula{
	double d;
	int mode=0;
	String[] modes = new String[]{"", "90", "180", "270"};
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * <br>
	 * for mode 0 and 2 : d : 1 &lt; d &lt; infinity
	 * for mode 1 and 3 : d : -infinity &lt; d &lt; -1
	 */
	public GumbelCopula(double[] params) {
		super(params);
		d = params[0];
		lb = 1;
		ub = Double.POSITIVE_INFINITY;
		indep = 1;
	}
	
	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	/**
	 * Mode change function to use rotated Gumbel.
	 * @param mode Rotation mode:
	 * <br>
	 * 0 -> no rotation
	 * 1 -> rotation by 90 degrees
	 * 2 -> rotation by 180 degrees
	 * 3 -> rotation by 270 degrees
	 * <br>
	 * for mode 0 and 2 : d : 1 &lt; d &lt; infinity
	 * for mode 1 and 3 : d : -infinity &lt; d &lt; -1
	 */
	public void changeMode(int mode){
		if(mode == 0 || mode == 2){
			this.mode = mode;
			lb = 1;
			ub = Double.POSITIVE_INFINITY;
			indep = 1;
		}else if(mode == 1 || mode ==3){
			this.mode = mode;
			lb = Double.NEGATIVE_INFINITY;
			ub = -1;
			indep = -1;
		}else{
			System.err.println("Illegal mode!");
		}
	}
	
	@Override
	public double C(double x, double y) {
		if(mode == 1){
			double out = 0;
			d = -d;
			out = y - C0(1-x, y);
			d = -d;
			return out;
		}
		
		if(mode == 2)
			return x + y - 1 + C0(1-x, 1-y);
		
		if(mode == 3){
			double out = 0;
			d = -d;
			out = x - C0(x, 1-y);
			d = -d;
			return out;
		}
		
		return C0(x, y);
	}
	
	/**
	 * C function for non-rotated Gumbel Copula.
	 * @param x, y input parameters.
	 * @return returns the Copula CDF value on point x, y. 
	 */
	private double C0(double x, double y){
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
	
	/**
	 * Density function for non-rotated Gumbel Copula.
	 * @param x, y input parameters.
	 * @return returns the Copula PDF value on point x, y. 
	 */
	public double density0(double x, double y) {
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
	
	/**
	 * H function for non-rotated Gumbel Copula.
	 * @param x, y input parameters.
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
		if(mode==0 || mode == 2)
			return 1-1/d;
		return -(1-1/(-d));
	}
	
	@Override
	public String name() {
		return "Gu"+modes[mode];
	}
}