package org.kramerlab.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;
public class debyeSub implements UnivariateFunction {

	public double value(double x) {
		if(x == 0) return 0;
		
        return x / (Math.exp(x)-1);
    }
}
