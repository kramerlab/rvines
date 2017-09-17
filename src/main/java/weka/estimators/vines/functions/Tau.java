package weka.estimators.vines.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

import weka.estimators.vines.copulas.Copula;

/**
 * This class is a univariate wrapper for the tau function inversion.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class Tau implements UnivariateFunction {
	Copula c;
	
	/**
	 * Constructor
	 * 
	 * @param c any Copula
	 */
	public Tau(Copula c){
		this.c = c;
	}
	
	@Override
	public double value(double x) {
		
		double[] pars = c.getParams();
		pars[0] = x;
		c.setParams(pars);
		
		return c.tau();
	}

}
