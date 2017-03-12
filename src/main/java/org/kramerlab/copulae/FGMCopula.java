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
	
	//https://github.com/tnagler/VineCopula/blob/master/src/hfunc.c
		@Override
		public double inverseHFunction(double x, double y) {
			boolean br = false;
		    double ans = 0.0, tol = Math.pow(10, -10), x0 = 0, x1 = 1, it=0, fl, fh, val;
		    fl = hFunction(x0, y);
		    fl -= x;
		    fh = hFunction(x1, y);
		    fh -= x;
		    
		    if (Math.abs(fl) <= tol) {
		        ans = x0;
		        br = true;
		    }
		    if (Math.abs(fh) <= tol) {
		        ans = x1;
		        br = true;
		    }

		    while (!br){
		        ans = (x0 + x1) / 2.0;
		        val = hFunction(ans, y);
		        val -= x;

		        //stop if values become too close (avoid infinite loop)
		        if (Math.abs(val) <= tol) br = true;
		        if (Math.abs(x0-x1) <= tol) br = true;

		        if (val > 0.0) {
		            x1 = ans;
		            fh = val;
		        } else {
		            x0 = ans;
		            fl = val;
		        }

		        //stop if too many iterations are required (avoid infinite loop)
		        ++it;
		        if (it > 50) br = true;
		    }

		    return ans;
		}
	
	@Override
	public double tau() {
		return 2/9.0*d;
	}

	@Override
	public String name() {
		return "FGM";
	}
}
