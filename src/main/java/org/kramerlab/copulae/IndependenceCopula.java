package org.kramerlab.copulae;

/**
 * This is the class to represent Independence copula family for RVines.
 * <br>
 * The idea to use a separate class for Independence was given by the
 * R-Implementation of regular vines.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class IndependenceCopula extends AbstractCopula{
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 * 
	 * Note: The Independence copula does not need any parameters.
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
