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
	 * The inverse h-function for the copula.
	 * It is used to de-transform the values for sampling.
	 *
	 * @param	x	to be unconditioned parameter
	 * @param	y	the conditioning parameter
	 * @return returns the unconditioned value.<br>
	 * If x is z|y, the method returns z.
	 */
	public double inverseHFunction(double x, double y);
	
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
