package org.kramerlab.copulae;

/**
 * This is a placeholder for the Independence copula.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class IndependenceCopula extends AbstractCopula{
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public IndependenceCopula(double[] params) {
		super(params);
	}

	@Override
	public double density(double x, double y) {
		return 1;
	}
	
	@Override
	public double hFunction(double x, double y) {
		return x;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		return x;
	}
	
	@Override
	public double tau() {
		return 0;
	}

	@Override
	public String name() {
		return "Independence";
	}
}
