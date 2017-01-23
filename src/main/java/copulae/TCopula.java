package copulae;

/**
 * This is a placeholder for the T copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class TCopula extends AbstractCopula{

	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public TCopula(double[] params) {
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
		return "T";
	}	
}
