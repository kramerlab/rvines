package org.kramerlab.vines;

import org.kramerlab.copulae.FrankCopula;

import junit.framework.TestCase;

public class FrankCopulaTest extends TestCase {
    
	public void testFrankDensity(){
		FrankCopula g = new FrankCopula(new double[]{10});
		for(int i=1; i<10; i++){
			System.out.println(g.density(i/10.0, 1-i/10.0));
		}
	}
}