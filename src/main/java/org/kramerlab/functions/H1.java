package org.kramerlab.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.kramerlab.copulae.Copula;

public class H1 implements UnivariateFunction {
	Copula c;
	double x;
	
	public H1(Copula c, double x){
		this.c = c;
		this.x = x;
	}

	public double value(double y) {
        return c.h1Function(x, y);
    }
	
}
