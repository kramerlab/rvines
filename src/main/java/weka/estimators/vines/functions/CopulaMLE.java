package weka.estimators.vines.functions;

import weka.core.Optimization;
import weka.estimators.vines.VineUtils;
import weka.estimators.vines.copulas.Copula;

/**
 * This class is a MLE class using Weka's Optimization class.
 * <br>
 * It uses numerical derivation methods to approximate
 * the gradient and hessian matrix.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class CopulaMLE extends Optimization {
	private double h = Math.pow(10, -7);
	private Copula c;
	private double[] a;
	private double[] b;
	
	/**
	 * Constructor
	 * <br>
	 * Bivariate Copula c will be optimized
	 * by MLE to fit between a and b.
	 * 
	 * @param c Copula to be optimized via MLE.
	 * @param a An observation array.
	 * @param b Another observation array.
	 */
	public CopulaMLE(Copula c, double[] a, double[] b){
		this.c = c;
		this.a = a;
		this.b = b;
	}

	@Override
	protected double objectiveFunction(double[] params) throws Exception {
		c.setParams(params);
		
		// Using negative value because it's a minimizing function
		double out = -VineUtils.logLikelihood(c, a, b);
		
		if(Double.isNaN(out) || Math.abs(out) == Double.POSITIVE_INFINITY){
			return 0;
		}
		
		return out;
	}
	
	@Override
	protected double[] evaluateGradient(double[] params) throws Exception {
		double[] out = new double[params.length];
		
		for(int i=0; i<params.length; i++){
			params[i] += h;
			out[i] = objectiveFunction(params);
			params[i] -= 2*h;
			out[i] -= objectiveFunction(params);
			params[i] += h;
			out[i] /= 2*h;
		}
		
		return out;
	}
	
	@Override
	protected double[] evaluateHessian(double[] params, int index) throws Exception {
		double[] out = new double[params.length];
		
		if(params.length == 1){
			return null;
		}
		
		if(index == 0){
			// double deviation to p ( f(x+h) - 2f(x) + f(x-h) ) / h*h
			params[0] += h;
			out[0] = objectiveFunction(params);
			params[0] -= h;
			out[0] -= 2*objectiveFunction(params);
			params[0] -= h;
			out[0] = objectiveFunction(params);
			params[0] += h;
			out[0] /= h*h;
			
			// deviation p and v
			double[] params1 = new double[]{params[0], params[1]-h};
			double[] params2 = new double[]{params[0], params[1]+h};
			
			double out1 = evaluateGradient(params1)[0];
			double out2 = evaluateGradient(params2)[0];
			
			out[1] = (out2-out1)/2*h;
		}
		if(index == 1){
			// deviation v and p
			double[] params1 = new double[]{params[0]-h, params[1]};
			double[] params2 = new double[]{params[0]+h, params[1]};
			
			double out1 = evaluateGradient(params1)[1];
			double out2 = evaluateGradient(params2)[1];
			
			out[1] = (out2-out1)/2*h;
			
			// double deviation to pv ( f(x+h) - 2f(x) + f(x-h) ) / h*h
			params[1] += h;
			out[1] = objectiveFunction(params);
			params[1] -= h;
			out[1] -= 2*objectiveFunction(params);
			params[1] -= h;
			out[1] = objectiveFunction(params);
			params[1] += h;
			out[1] /= h*h;
		}
		
		return out;
	}
	
	@Override
	public String getRevision() {
		return "";
	}
}
