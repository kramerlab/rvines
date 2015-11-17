package org.kramerlab.vines;

/**
 * This is a placeholder for the Gumpel copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GumbelCopula extends AbstractCopula{

	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public GumbelCopula(double[] params) {
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
		// TODO Auto-generated method stub
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
		return "Gumbel";
	}	
}
