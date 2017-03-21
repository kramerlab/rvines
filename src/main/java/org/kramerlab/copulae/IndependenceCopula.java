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
	
	public IndependenceCopula() {
		super(new double[0]);
	}

	@Override
	public double C(double x, double y) {
		return x*y;
	}
	
	@Override
	public double density(double x, double y) {
		return 1;
	}
	
	@Override
	public double h1Function(double x, double y) {
		return hFunction(y, x);
	}

	@Override
	public double h2Function(double x, double y) {
		return hFunction(x, y);
	}

	public double hFunction(double x, double y) {
		return x;
	}
	
	@Override
	public double tau() {
		return 0;
	}

	@Override
	public String name() {
		return "In";
	}

	@Override
	public double mle(double[] a, double[] b) {
		// There is no parameter to optimize
		return 0;
	}
}
