package org.kramerlab.functions;

import org.apache.commons.math3.analysis.*;

public class GalambosTauf implements UnivariateFunction {
	double d;
	
	public GalambosTauf(double d){
		this.d = d;
	}
	
	private double A(double x){
		return 1-Math.pow(Math.pow(x, -d)+Math.pow(1-x, -d), -1/d);
	}
	
	private double ddA(double x){
		double y = (1+d)*Math.pow(x*(1-x), d-2);
		y = y*Math.pow(Math.pow(x, -d)+Math.pow(1-x, -d), -1/d);
		y = y/Math.pow(Math.pow(x, d)+Math.pow(1-x, d), 2);
		return y;
	}
	
    public double value2(double x) {
        return x*(1-x)*ddA(x)/A(x);
    }
    
    public double value(double x) {
        return x*(1-x)*ddA(x)/A(x);
    }
}
