package org.kramerlab.copulae;

import org.kramerlab.vines.Utils;

/**
 * This is the class to represent Farlie-Gumbel-Morgenstern (FGM) copula family for RVines.
 * <br>
 * The density function is provided by Bekrizadeh, Parham and Zadkarmi:
 * The New Generalization of Farlie-Gumbel-Morgenstern Copulas.
 * (Applied Mathematical Sciences, Vol. 6, 2012, no. 71, 3527 - 3533)
 * <br>
 * The the h-function is presented in the R-Project:
 * https://cran.r-project.org/web/packages/vines/vines.pdf
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class FGMCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * d : -1 &lt; d &lt; 1
	 */
	public FGMCopula(double[] params) {
		super(params);
		d = params[0];
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = 1+d*(1-2*x)*(1-2*y);
		
		return out;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = x*(1+d*(1-x)*(1-2*y));
		
		return out;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		//Use Newton's method to solve h(x,y)-z = 0 with fixed y and z.
		double z = x;
		double x1 = 0.5;
		double e = 1;
		
		while(e > Math.pow(10, -10)){
			double x2 = x1-(hFunction(x1, y)-z)/density(x1, y);
			e = Math.abs(x2 - x1);
			x1 = x2;
		}
		
		return x1;
	}
	
	@Override
	public double tau() {
		return 2/9*d;
	}

	@Override
	public String name() {
		return "FGM";
	}
}
