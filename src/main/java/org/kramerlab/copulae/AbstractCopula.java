package org.kramerlab.copulae;

/**
 * This is the abstract class to represent copula families for RVines.
 * It implements the parameter handling for copulae.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public abstract class AbstractCopula implements Copula{
	protected double[] params;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public AbstractCopula(double[] params){
		this.params = params;
	}
	
	public void setParams(double[] params) {
		this.params = params;
	}

	public double[] getParams() {
		return params;
	}
}
