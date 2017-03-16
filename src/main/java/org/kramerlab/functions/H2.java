package org.kramerlab.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.kramerlab.copulae.Copula;

public class H2 implements UnivariateFunction {
	Copula c;
	double y;
	
	public H2(Copula c, double y){
		this.c = c;
		this.y = y;
	}

	public double value(double x) {
        return c.h2Function(x, y);
    }
	
}
