package org.kramerlab.functions;

import org.kramerlab.copulae.Copula;
import org.kramerlab.vines.Utils;

import weka.core.Optimization;

public class CopulaMLE extends Optimization {
	public final static double h = 0.00001;
	private Copula c;
	private double[] a;
	private double[] b;
	
	public CopulaMLE(Copula c, double[] a, double[] b){
		this.c = c;
		this.a = a;
		this.b = b;
	}

	@Override
	protected double objectiveFunction(double[] params) throws Exception {
		c.setParams(params);
		
		// Using negative value because it's a minimizing function
		return -Utils.logLikelihood(c, a, b);
	}
	
	@Override
	protected double[] evaluateGradient(double[] params) throws Exception {
		double[] out = new double[params.length];
		double f = objectiveFunction(params);
		
		for(int i=0; i<params.length; i++){
			params[i] += h;
			out[i] = (objectiveFunction(params)-f)/h;
			params[i] -= h;
		}
		
		return out;
	}
	
	@Override
	public String getRevision() {
		return "";
	}
}
