package weka.estimators.vines.functions;

import org.apache.commons.math3.analysis.UnivariateFunction;

/**
 * This class represents the inner function of Debye1 integration.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class debyeSub implements UnivariateFunction {

	public double value(double x) {
		if(x == 0) return 0;
		
        return x / (Math.exp(x)-1);
    }
}
