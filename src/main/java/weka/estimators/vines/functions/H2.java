package weka.estimators.vines.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

import weka.estimators.vines.copulas.Copula;

/**
 * This class is a univariate wrapper for the h function inversion.
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class H2 implements UnivariateFunction {
	Copula c;
	double y;
	
	/**
	 * Constructor
	 * 
	 * @param c any Copula
	 * @param y the fixed y value.
	 */
	public H2(Copula c, double y){
		this.c = c;
		this.y = y;
	}

	public double value(double x) {
        return c.h2Function(x, y);
    }
	
}
