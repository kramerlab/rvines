package org.kramerlab.copulae;

import junit.framework.TestCase;

public class FGMCopulaTest extends TestCase {
    
	public void testDensity(){
		for(int p = 1; p < 20; p++){
			FGMCopula g = new FGMCopula(new double[]{p/10.0-1});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(g.density(i/10.0, j/10.0) >= 0);
				}
			}
		}
	}
	
	public void testHFunction(){
		for(int p = 1; p < 20; p++){
			FGMCopula g = new FGMCopula(new double[]{p/10.0-1});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(g.hFunction(i/10.0, j/10.0) >= 0);
				}
			}
		}
	}
	
	public void testInverseHFunction(){
		for(int p = 1; p < 20; p++){
			FGMCopula g = new FGMCopula(new double[]{p/10.0-1});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					double a = g.h2Function(i/10.0, j/10.0);
					double b = g.h2inverse(a, j/10.0);
					assert(Math.abs(i/10.0 - b) <= 0.1);
				}
			}
		}
	}
	
	public void testTau(){
		for(int p = 1; p < 20; p++){
			FGMCopula g = new FGMCopula(new double[]{p/10.0-1});
			double t = g.tau();
			assert(t >= -1 && t <= 1);
		}
	}
}

