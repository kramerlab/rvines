package org.kramerlab.copulae;

import org.kramerlab.functions.CopulaMLE;
import org.kramerlab.functions.H1;
import org.kramerlab.functions.H2;
import org.kramerlab.vines.Utils;

/**
 * This is the abstract class to represent copula families for RVines.
 * It implements the parameter handling for copulae.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public abstract class AbstractCopula implements Copula{
	protected double[] params;
	public final static double tol = Math.pow(10, -6);
	public final static boolean WEKA_OPTIMIZATION = true;
	public double lb = -1;
	public double ub = 1;
	public double start = 0;
	
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
	
	public double h1inverse(double x, double y) {
		H1 h = new H1(this, x);
		return Utils.bisectionInvert(h, y, 0, 1);
	}

	public double h2inverse(double x, double y) {
		H2 h = new H2(this, y);
		return Utils.bisectionInvert(h, x, 0, 1);
	}
	
	public double mle(double[] a, double[] b){
		CopulaMLE cmle = new CopulaMLE(this, a, b);
		
		if(WEKA_OPTIMIZATION){
		double[] initX = new double[]{start};
		double[][] constr = new double[][]{{lb}, {ub}};
		try {
			double[] x = cmle.findArgmin(initX, constr); 
			 while(x == null){  // 200 iterations are not enough
			    x = cmle.getVarbValues();  // Try another 200 iterations
			    x = cmle.findArgmin(x, constr);
			 }
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return -cmle.getMinFunction();
		} else{
		try {
			setParams(new double[]{cmle.optimize(lb, ub, start)});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Utils.logLikelihood(this, a, b);
		}
	}
}
