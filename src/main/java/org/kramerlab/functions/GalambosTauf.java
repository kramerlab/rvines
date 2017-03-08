package org.kramerlab.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.kramerlab.vines.Utils;

public class GalambosTauf implements UnivariateFunction {
	double d;
	
	public GalambosTauf(double d){
		this.d = d;
	}
	
	public double A(double x){
		x = Utils.laplaceCorrection(x);
		double y = 1-Math.pow(Math.pow(x, -d)+Math.pow(1-x, -d), -1/d);
		return y;
	}
    
    private double ddA(double x) {
		x = Utils.laplaceCorrection(x);
		double y = (1+d)*Math.pow(x*(1-x), d-2);
		y = y*Math.pow(Math.pow(x, -d)+Math.pow(1-x, -d), -1/d);
		y = y/Math.pow(Math.pow(x, d)+Math.pow(1-x, d), 2);
		return y;
	}

	public double value(double x) {
        return x*(1-x)*ddA(x)/A(x);
    }
}
