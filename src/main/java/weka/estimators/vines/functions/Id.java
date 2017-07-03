package weka.estimators.vines.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class Id implements UnivariateFunction{
	public double value(double x) {
		return x;
    }
}
