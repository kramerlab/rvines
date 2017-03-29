package org.kramerlab.functions;

import org.kramerlab.copulae.Copula;
import org.kramerlab.vines.Utils;

import weka.core.Optimization;

public class CopulaMLE extends Optimization {
	public final static double MP = 2.220446e-16;
	private double h = Math.pow(10, -7);
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
		double out = -Utils.logLikelihood(c, a, b);
		
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
	
	private double f(double x){
		c.setParams(new double[]{x});
		return -Utils.logLikelihood(c, a, b);
	}
	
	public double optimize(double lb, double ub, double x) throws Exception{
		double c, d, e, m, p, q, r, tol, t2, u, v, w, fu, fv, fw, fx;
		double eps = Math.sqrt(MP);
		double t = Math.pow(MP, 0.25);
		
		c = 0.5*(3 - Math.sqrt(5));
		v = w = x = lb + c*(ub-lb);
		d = e = 0;
		fv = fw = fx = f(x);
		
		m = 0.5*(lb+ub);
		tol = eps*Math.abs(x)+t/3;
		t2 = 2*tol;
		while(Math.abs(x-m) > t2-0.5*(ub-lb)){
			p = q = r = 0;
			if(Math.abs(e) > tol){
				// Fit parabola
				r = (x-w)*(fx-fv);
				q = (x-v)*(fx-fw);
				p = (x-v)*q-(x-w)*r;
				q = 2*(q-r);
				if(q > 0)
					p = -p;
				else
					q = -q;
				r = e;
				e = d;
			}
			if(Math.abs(e) >= tol && Math.abs(p) < Math.abs(0.5*q*r)
					&& p > q*(lb-x) && p < q*(ub-x)){
				// A parabolic interpolation step
				d = p/q;
				u = x+d;
				// f must not be evaluated too close to a or b
				if(u-lb < t2 || ub-u < t2)
					d = x < m ? tol : -tol;
			}else{
				// A golden section step
				e = x < m ? ub-x : lb-x;
				d = c*e;
			}
			// f must not be evaluated too close to a or b
			u = Math.abs(d) >= tol ? x + d : d > 0 ? x+tol : x-tol;
			fu = f(u);
			
			// Update a,b,v,w and x.
			if(fu <= fx){
				if(u < x)
					ub = x;
				else
					lb = x;
				v = w;
				fv = fw;
				w = x;
				fw = fx;
				x = u;
				fx = fu;
			}else{
				if(u < x)
					lb = u;
				else
					ub = u;
				if(fu <= fw || w == x){
					v = w;
					fv = fw;
					w = u;
					fw = fu;
				}else if(fu <= fv || v == x || v == w){
					v = u;
					fv = fu;
				}
			}
			
			m = 0.5*(lb+ub);
			tol = eps*Math.abs(x)+t/3;
			t2 = 2*tol;
		}
		
		return x;
	}
	
	@Override
	public String getRevision() {
		return "";
	}
}
