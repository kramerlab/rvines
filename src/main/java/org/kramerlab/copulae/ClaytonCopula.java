package org.kramerlab.copulae;

import org.kramerlab.functions.CopulaMLE;
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
	private double d;
	private int mode=0;
	String[] modes = new String[]{"", "90", "180", "270"};
	
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
		lb = 0;
		ub = Double.POSITIVE_INFINITY;
		indep = 0;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	/**
	 * Mode change function to use rotated Clayton.
	 * @param mode Rotation mode:
	 * <br>
	 * 0 -> no rotation
	 * 1 -> rotation by 90 degrees
	 * 2 -> rotation by 180 degrees
	 * 3 -> rotation by 270 degrees
	 * <br>
	 * for mode 0 and 2 : d : 0 &lt; d &lt; infinity
	 * for mode 1 and 3 : d : -infinity &lt; d &lt; 0
	 */
	public void changeMode(int mode){
		if(mode == 0 || mode == 2){
			this.mode = mode;
			lb = 0;
			ub = Double.POSITIVE_INFINITY;
		}else if(mode == 1 || mode ==3){
			this.mode = mode;
			lb = Double.NEGATIVE_INFINITY;
			ub = 0;
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
	 * C function for non-rotated Clayton Copula.
	 * @param x, y input parameters.
	 * @return returns the Copula CDF value on point x, y. 
	 */
	private double C0(double x, double y){
		return Math.pow(Math.pow(x, -d)+Math.pow(y, -d)-1, -1/d);
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
	 * Density function for non-rotated Clayton Copula.
	 * @param x, y input parameters.
	 * @return returns the Copula PDF value on point x, y. 
	 */
	private double density0(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = (1+d)*Math.pow(x*y, -1-d)
				*Math.pow(Math.pow(x, -d)+Math.pow(y, -d)-1, -1/d-2);
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
	 * H function for non-rotated Clayton Copula.
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
		if(mode==0 || mode == 2)
			return d/(d+2);
		return d/(-d+2);
	}

	private double tauInv(double tau){
		if(mode==0 || mode == 2)
			return 2/(1-tau)-2;
		return 2-2/(1+tau);
	}
	
	@Override
	public double mle(double[] a, double[] b){
		double tau = Utils.kendallsTau(a, b);
		
		// 90 and 270 rotated can't model positive dependency
		if(tau > 0 && (mode==1 || mode==3)){
			return Double.NEGATIVE_INFINITY;
		}
		
		// 0 and 180 rotated can't model negative dependency
		if(tau < 0 && (mode==0 || mode==2)){
			return Double.NEGATIVE_INFINITY;
		}
		
		CopulaMLE cmle = new CopulaMLE(this, a, b);
		double[] initX = new double[]{tauInv(tau)};
		double[][] constr;
		if(mode==0 || mode==2)
			constr = new double[][]{{1.0001}, {Double.POSITIVE_INFINITY}};
		else
			constr = new double[][]{{Double.NEGATIVE_INFINITY}, {-1.0001}};
		
		try {
			cmle.findArgmin(initX, constr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -cmle.getMinFunction();
	}
	
	@Override
	public String name() {
		return "C"+modes[mode];
	}

}