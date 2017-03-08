package org.kramerlab.copulae;

import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.analysis.integration.UnivariateIntegrator;
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
		return Utils.simpsonIntegrate(new GalambosTauf(d), 1000, 0, 1);
	}

	@Override
	public String name() {
		return "Galambos";
	}
}
