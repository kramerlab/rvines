package org.kramerlab.vines;

import org.kramerlab.copulae.GalambosCopula;

import junit.framework.*;

public class GalambosTest extends TestCase {
    
	public void testGalambosTau(){
		for(int i=0; i<100; i++){
			GalambosCopula g = new GalambosCopula(new double[]{i});
			double tau = g.tau();
			System.out.println(tau);
			assert(tau <= 1);
			assert(tau >= 0);
		}
	}
}
