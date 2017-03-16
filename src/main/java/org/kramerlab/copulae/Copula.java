package org.kramerlab.copulae;

/**
 * This is an interface for bivariate copula families.
 * The bivariate copulae are used to model the edges of the RVine.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public interface Copula {
	
	/**
	 * Set the parameters for the copula.
	 *
	 * @param	params copula parameters as double array.
	 */
	public void setParams(double[] params);
	
	/**
	 * Get the parameters for the copula.
	 *
	 * @return	returns the copula parameters as double array.
	 */
	public double[] getParams();
	
	/**
	 * The copula probability function for a bivariate observation x, y.
	 *
	 * @param	x an observation from a random variable.
	 * @param	y an observation from another random variable.
	 * @return	returns the copula probability value.
	 */
	public double C(double x, double y);
	
	/**
	 * The copula density for a bivariate observation x, y.
	 *
	 * @param	x an observation from a random variable.
	 * @param	y an observation from another random variable.
	 * @return	returns the copula density.
	 */
	public double density(double x, double y);
	
	/**
	 * The h-function for the copula.
	 * It is used to create pseudo observations.
	 * We need two different functions because some
	 * of them are not symmetric.
	 *
	 * @param	x	to be conditioning parameter
	 * @param	y	to be conditioned parameter
	 * @return returns the constrained value y|x.
	 */
	public double h1Function(double x, double y);
	
	/**
	 * The h-function for the copula.
	 * It is used to create pseudo observations.
	 * We need two different functions because some
	 * of them are not symmetric.
	 *
	 * @param	x	to be conditioned parameter
	 * @param	y	to be conditioning parameter
	 * @return returns the constrained value x|y.
	 */
	public double h2Function(double x, double y);
	
	/**
	 * The inverse h1-function for the copula.
	 * It is used to de-transform the values for sampling.
	 *
	 * @param	x	to be conditioning parameter
	 * @param	y	the unconditioned parameter
	 * @return returns the unconditioned value.<br>
	 * If y is z|x, the method returns z.
	 */
	public double h1inverse(double x, double y);
	
	/**
	 * The inverse h2-function for the copula.
	 * It is used to de-transform the values for sampling.
	 *
	 * @param	x	to be unconditioned parameter
	 * @param	y	the conditioning parameter
	 * @return returns the unconditioned value.<br>
	 * If x is z|y, the method returns z.
	 */
	public double h2inverse(double x, double y);
	
	/**
	 * MLE to estimate the copula parameters.
	 * 
	 * @return returns the maximum likelihood.
	 */
	public double mle(double[] a, double[] b);
	
	/**
	 * Copula based Kendall's tau calculation.
	 * <br>
	 * This is a function to get the tau value, based on a
	 * Kendall's tau calculation function for the copula family.
	 *
	 * @return returns the tau value of the copula.
	 */
	public double tau();
	
	/**
	 * Get the copula name.
	 * It is used to differentiate the copulae for visualization.
	 *
	 * @return	returns the copula name.
	 */
	public String name();
}
