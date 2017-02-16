package org.kramerlab.copulae;

import org.kramerlab.vines.Utils;

/**
 * This is a placeholder for the Gumpel copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GumbelCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public GumbelCopula(double[] params) {
		super(params);
		d = params[0];
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-Math.pow(Math.pow(-Math.log(x), d)
				+Math.pow(-Math.log(y), d), 1/d));
		
		out = out*1/(x*y)*Math.pow(Math.pow(-Math.log(x), d)
				+Math.pow(-Math.log(y), d), 2/d-2)*
				Math.pow(Math.log(x)*Math.log(y), d-1)*
				(1+(d-1)*Math.pow(Math.pow(-Math.log(x), d)
						+Math.pow(-Math.log(y), d), -1/d));
		
		return out;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-Math.pow(Math.pow(-Math.log(x), d)
				+Math.pow(-Math.log(y), d), 1/d));
		
		out = out/y*Math.pow(-Math.log(y), d-1)*
				Math.pow(Math.pow(-Math.log(x), d)
				+Math.pow(-Math.log(y), d), 1/d-1);
				
		return out;
	}

	@Override
	public double inverseHFunction(double x, double y) {
		// TODO Use Newton to solve h(x,y)-z = 0
		return 0;
	}
	
	@Override
	public double tau() {
		return 1-1/d;
	}

	@Override
	public String name() {
		return "Gumbel";
	}	
}
