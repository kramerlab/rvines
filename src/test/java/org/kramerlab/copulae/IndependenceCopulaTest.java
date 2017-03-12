package org.kramerlab.copulae;

import junit.framework.TestCase;

public class IndependenceCopulaTest extends TestCase {
    
	public void testDensity(){
		IndependenceCopula g = new IndependenceCopula();
		for(int i=1; i<10; i++){
			for(int j=1; j<10; j++){
				assert(g.density(i/10.0, j/10.0) >= 0);
			}
		}
	}
	
	public void testHFunction(){
		IndependenceCopula g = new IndependenceCopula();
		for(int i=1; i<10; i++){
			for(int j=1; j<10; j++){
				assert(g.hFunction(i/10.0, j/10.0) >= 0);
			}
		}
	}
	
	public void testInverseHFunction(){
		IndependenceCopula g = new IndependenceCopula();
		for(int i=1; i<10; i++){
			for(int j=1; j<10; j++){
				double a = g.hFunction(i/10.0, j/10.0);
				double b = g.inverseHFunction(a, j/10.0);
				assert(Math.abs(i/10.0 - b) <= 0.0001);
			}
		}
	}
	
	public void testTau(){
		IndependenceCopula g = new IndependenceCopula();
		double t = g.tau();
		assert(t >= -1 && t <= 1);
	}
}
