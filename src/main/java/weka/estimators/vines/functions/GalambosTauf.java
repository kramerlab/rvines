package weka.estimators.vines.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

import weka.estimators.vines.Utils;

/**
 * This class represents the inner function for Kendall's
 * tau computation for the Galambos Copula.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GalambosTauf implements UnivariateFunction {
	double d;
	
	/**
	 * Constructor
	 * 
	 * @param d Galambos dependence parameter.
	 */
	public GalambosTauf(double d){
		this.d = d;
	}
	
	/**
	 * A function for Galambos Copula.
	 * 
	 * @param x input value.
	 * @return A(x).
	 */
	public double A(double x){
		x = Utils.laplaceCorrection(x);
		double y = 1-Math.pow(Math.pow(x, -d)+Math.pow(1-x, -d), -1/d);
		return y;
	}
    
	/**
	 * A'' function for Galambos Copula.
	 * 
	 * @param x input value.
	 * @return A''(x).
	 */
    private double ddA(double x) {
		x = Utils.laplaceCorrection(x);
		
		double edx = Math.pow(x, d);
		double edx2 = Math.pow(1-x, d);
		
		double y = (1+d)*Math.pow(x*(1-x), d-2);
		y = y*Math.pow(1/edx+1/edx2, -1/d);
		y = y/((edx+edx2)*(edx+edx2));
		
		return y;
	}

	public double value(double x) {
        return x*(1-x)*ddA(x)/A(x);
    }
}
