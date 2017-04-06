package weka.estimators.vines.copulas;

import weka.estimators.vines.Utils;
import weka.estimators.vines.functions.CopulaMLE;
import weka.estimators.vines.functions.H1;
import weka.estimators.vines.functions.H2;

/**
 * This is the abstract class to represent copula families for RVines.
 * It implements the parameter handling for copulae.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public abstract class AbstractCopula implements Copula{
	protected double[] params;
	public final static double tol = Math.pow(10, -4);
	public double[] lb = new double[0];
	public double[] ub = new double[0];
	public double[] start = new double[0];
	
	public void setParams(double[] params) {
		this.params = params;
	}

	public double[][] getParBounds() {
		return new double[][]{lb, ub};
	}
	
	public double[] getParams() {
		return params;
	}
	
	public double[] getMLEStart() {
		return start;
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
		
		double[] initX = getMLEStart();
		double[][] constr = getParBounds();
		try {
			double[] x = cmle.findArgmin(initX, constr); 
			 while(x == null){  // 200 iterations are not enough
			    x = cmle.getVarbValues();  // Try another 200 iterations
			    x = cmle.findArgmin(x, constr);
			 }
		} catch (Exception e) {
			//System.err.println("Tried to fit "+name());
			//System.out.println();
			//System.out.println("With pars : "+constr[0][0]+" "+constr[1][0]+" "+initX[0]);
			// e.printStackTrace();
		}
		return -cmle.getMinFunction();
	}
}
