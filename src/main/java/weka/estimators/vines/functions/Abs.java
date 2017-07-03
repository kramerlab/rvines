package weka.estimators.vines.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class Abs implements UnivariateFunction {
	public double value(double x) {
		return Math.abs(x);
    }
}
