package org.kramerlab.copulae;

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
		
		GumbelCopula g = new GumbelCopula(new double[]{d});
		
		return g.density(1-x, 1-y);
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = x*y*Math.exp(Math.pow(Math.pow(-Math.log(x), -d)
				+Math.pow(-Math.log(y), -d), -1/d));
		
		out = out/y*(1-Math.pow(1+
				Math.pow(Math.log(x)/Math.log(y), d), -1-1/d));
		
		return out;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		// TODO Use Newton to solve h(x,y)-z = 0
		return 0;
	}
	
	@Override
	public double tau() {
		// TODO Look for suitable computation
		return 0;
	}

	@Override
	public String name() {
		return "Galambos";
	}
}
