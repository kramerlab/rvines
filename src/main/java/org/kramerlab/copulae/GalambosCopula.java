package org.kramerlab.copulae;

import org.kramerlab.functions.GalambosTauf;
import org.kramerlab.functions.H1;
import org.kramerlab.functions.H2;
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
	public double C(double x, double y) {
		// TODO Auto-generated method stub
		return 0;
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
	public double h1Function(double x, double y) {
		return hFunction(y, x);
	}

	@Override
	public double h2Function(double x, double y) {
		return hFunction(x, y);
	}
	
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = x*Math.exp(Math.pow(Math.pow(-Math.log(x), -d)
				+Math.pow(-Math.log(y), -d), -1/d));
		
		out = out*(1-Math.pow(1+
				Math.pow(Math.log(y)/Math.log(x), d), -1-1/d));
		
		return out;
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
