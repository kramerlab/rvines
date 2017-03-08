package org.kramerlab.vines;

import org.kramerlab.copulae.GalambosCopula;

import junit.framework.*;

public class GalambosCopulaTest extends TestCase {
    
	public void testGalambosTau(){
		for(int i=1; i<100; i++){
			GalambosCopula g = new GalambosCopula(new double[]{i/10.0});
			double tau = g.tau();
			assert(tau <= 1);
			assert(tau >= 0);
		}
	}
}
