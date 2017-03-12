package org.kramerlab.copulae;

import org.kramerlab.functions.GalambosTauf;
import org.kramerlab.vines.Utils;

/**
 * This is a placeholder for the Galambos copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GalambosCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public GalambosCopula(double[] params) {
		super(params);
		d = params[0];
	}

	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(Math.pow(Math.pow(-Math.log(x), -d)
				+Math.pow(-Math.log(y), -d), -1/d));
		
		out = out*(1-Math.pow(Math.pow(-Math.log(x), -d) + Math.pow(-Math.log(y), -d), -1-1/d)
				*(Math.pow(-Math.log(x), -d-1) + Math.pow(-Math.log(y), -d-1))
				+Math.pow(Math.pow(-Math.log(x), -d) + Math.pow(-Math.log(y), -d), -2-1/d)
				*Math.pow(Math.log(x)* Math.log(y), -d-1)
				*(1+d+Math.pow(Math.pow(-Math.log(x), -d) + Math.pow(-Math.log(y), -d), -1/d)));
		
		if(out < 0 && Math.abs(out) < Math.pow(10, -10))
			return 0;
		return out;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = x*Math.exp(Math.pow(Math.pow(-Math.log(x), -d)
				+Math.pow(-Math.log(y), -d), -1/d));
		
		out = out*(1-Math.pow(1+
				Math.pow(Math.log(y)/Math.log(x), d), -1-1/d));
		
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
	        if (it > 5000) br = true;
	    }

	    return ans;
	}
	
	@Override
	public double tau() {
		return Utils.simpsonIntegrate(new GalambosTauf(d), 1000, 0, 1);
	}

	@Override
	public String name() {
		return "Galambos";
	}
}
