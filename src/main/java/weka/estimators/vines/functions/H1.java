package weka.estimators.vines.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

import weka.estimators.vines.copulas.Copula;

/**
 * This class is a univariate wrapper for the h function inversion.
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class H1 implements UnivariateFunction {
	Copula c;
	double x;
	
	/**
	 * Constructor
	 * 
	 * @param c any Copula
	 * @param x the fixed x value.
	 */
	public H1(Copula c, double x){
		this.c = c;
		this.x = x;
	}

	public double value(double y) {
        return c.h1Function(x, y);
    }
	
}
