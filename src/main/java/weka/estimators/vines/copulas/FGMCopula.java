package weka.estimators.vines.copulas;

import weka.estimators.vines.Utils;

/**
 * This is the class to represent Farlie-Gumbel-Morgenstern (FGM) copula family for RVines.
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
	double d;
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * d : -1 &lt; d &lt; 1
	 */
	public FGMCopula(double[] params) {
		super(params);
		d = params[0];
		lb = -1+tol;
		ub = 1-tol;
		start = 0;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double C(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		return x*y+d*x*y*(1-x)*(1-y);
	}
	
	@Override
	public double density(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
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
	
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		return x*(1+d*(1-x)*(1-2*y));
	}
	
	@Override
	public double tau() {
		return 2/9.0*d;
	}

	@Override
	public String name() {
		return "FGM";
	}
	
	@Override
	public double[] getParBounds() {
		return new double[]{lb, ub};
	}
}
