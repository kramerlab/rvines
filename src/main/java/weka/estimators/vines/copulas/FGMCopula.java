package weka.estimators.vines.copulas;

import weka.estimators.vines.VineUtils;

/**
 * This is the class to represent Farlie-Gumbel-Morgenstern (FGM) copula family
 * for RVines.
 * <br>
 * The density function is provided by Bekrizadeh, Parham and Zadkarmi:
 * The New Generalization of Farlie-Gumbel-Morgenstern Copulas.
 * (Applied Mathematical Sciences, Vol. 6, 2012, no. 71, 3527 - 3533)
 * <br>
 * The the h-function is presented in the R-Project:
 * https://cran.r-project.org/web/packages/vines/vines.pdf
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class FGMCopula extends AbstractCopula{
	private static final long serialVersionUID = -4023999984385944320L;
	double d;
	
	/**
	 * Constructor
	 */
	public FGMCopula() {
		d = 0.5;
		params = new double[]{d};
		lb = new double[]{-1+tol};
		ub = new double[]{1-tol};
		start = new double[]{0.5};
	}

	/**
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * d : -1 &lt; d &lt; 1
	 */
	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double C(double x, double y) {
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		return x*y+d*x*y*(1-x)*(1-y);
	}
	
	@Override
	public double density(double x, double y) {
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		return 1+d*(1-2*x)*(1-2*y);
	}
	
	@Override
	public double h1Function(double x, double y) {
		return hFunction(y, x);
	}

	@Override
	public double h2Function(double x, double y) {
		return hFunction(x, y);
	}
	
	/**
	 * H function for FGM Copula.
	 * @param x input parameter, 0 &lt; x &lt; 1.
	 * @param y input parameter, 0 &lt; y &lt; 1.
	 * @return returns the conditioned value x|y.
	 */
	public double hFunction(double x, double y) {
		x = VineUtils.laplaceCorrection(x);
		y = VineUtils.laplaceCorrection(y);
		
		return x*(1+d*(1-x)*(1-2*y));
	}
	
	@Override
	public double tau() {
		return 2/9.0*d;
	}

	@Override
	public String name() {
		return "Farlie-Gumbel-Morgenstern";
	}
	
	@Override
	public String token() {
		return "FGM";
	}
	
}
