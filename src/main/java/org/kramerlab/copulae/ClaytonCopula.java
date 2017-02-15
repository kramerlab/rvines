package org.kramerlab.copulae;

import org.kramerlab.vines.Utils;

/**
 * This is a placeholder for the Clayton copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class ClaytonCopula extends AbstractCopula{
	private double d;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public ClaytonCopula(double[] params) {
		super(params);
		d = params[0];
	}

	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = (1+d)*Math.pow(x*y, -1-d)
				*Math.pow(Math.pow(x, -d)+Math.pow(y, d)-1, -1/d-2);
		return out;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.pow(y, -1-d)
				*Math.pow(Math.pow(x, -d)+Math.pow(y, d)-1, -1-1/d);
		
		return out;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.pow(Math.pow(x*Math.pow(y, d+1), 1+1/d)
				+1-Math.pow(y, -d), -1/d);
		
		return out;
	}
	
	@Override
	public double tau() {
		return d/(d+1);
	}

	@Override
	public String name() {
		return "Clayton";
	}
}
