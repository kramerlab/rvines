package org.kramerlab.vines;

/**
 * This is a placeholder for the Frank copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class FrankCopula extends AbstractCopula{

	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public FrankCopula(double[] params) {
		super(params);
	}

	@Override
	public double cumulativeProbability(double x, double y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double density(double x, double y) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public double hFunction(double x, double y) {
		return 0;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public double tau() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String name() {
		return "Frank";
	}
}
