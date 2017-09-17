package weka.estimators.vines.copulas;

import weka.estimators.vines.VineUtils;
import weka.estimators.vines.functions.CopulaMLE;
import weka.estimators.vines.functions.H1;
import weka.estimators.vines.functions.H2;
import weka.estimators.vines.functions.Tau;

/**
 * This is the abstract class to represent copula families for RVines.
 * It implements the parameter handling for copulae.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public abstract class AbstractCopula implements Copula{
	private static final long serialVersionUID = -8767088913269377895L;
	public final static double tol = Math.pow(10, -4);
	protected boolean rotations = false;
	protected double[] params;
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
	
	public boolean rotations(){
		return rotations;
	}
	
	public double h1inverse(double x, double y) {
		H1 h = new H1(this, x);
		return VineUtils.bisectionInvert(h, y, 0, 1);
	}

	public double h2inverse(double x, double y) {
		H2 h = new H2(this, y);
		return VineUtils.bisectionInvert(h, x, 0, 1);
	}
	
	public void tauInverse(double tau) {
		Tau t = new Tau(this);
		VineUtils.bisectionInvert(t, tau, lb[0], ub[0]);
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
			// e.printStackTrace();
		}
		return -cmle.getMinFunction();
	}
	
	public double[][] simulate(int n){
		if(n <= 0 ) return null;
		
		double[][] data = new double[n][2];
		
		for(int i=0; i<n; i++){
			data[i][0] = Math.random();
			data[i][1] = h1inverse(data[i][0], Math.random());
		}
		
		return data;
	}
}
