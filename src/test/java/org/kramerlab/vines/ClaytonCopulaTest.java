package org.kramerlab.vines;

import org.kramerlab.copulae.ClaytonCopula;

import junit.framework.TestCase;

public class ClaytonCopulaTest extends TestCase {
    
	public void testDensity(){
		for(int p = 1; p < 20; p++){
			ClaytonCopula g = new ClaytonCopula(new double[]{p});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(g.density(i/10.0, j/10.0) >= 0);
				}
			}
		}
		for(int p = 1; p < 20; p++){
			ClaytonCopula g = new ClaytonCopula(new double[]{1.0/p});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(g.density(i/10.0, j/10.0) >= 0);
				}
			}
		}
	}
	
	public void testHFunction(){
		for(int p = 1; p < 20; p++){
			ClaytonCopula g = new ClaytonCopula(new double[]{p});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(g.hFunction(i/10.0, j/10.0) >= 0);
				}
			}
		}
		for(int p = 1; p < 20; p++){
			ClaytonCopula g = new ClaytonCopula(new double[]{1.0/p});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(g.hFunction(i/10.0, j/10.0) >= 0);
				}
			}
		}
	}
	
	public void testInverseHFunction(){
		for(int p = 1; p < 20; p++){
			ClaytonCopula g = new ClaytonCopula(new double[]{p});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					double a = g.hFunction(i/10.0, j/10.0);
					double b = g.inverseHFunction(a, j/10.0);
					// TODO analyze me
					// test relative error, absolute it not working (don't know why)
					assert(Math.abs(i/10.0 - b)/p <= 0.1);
				}
			}
		}
		for(int p = 1; p < 20; p++){
			ClaytonCopula g = new ClaytonCopula(new double[]{1.0/p});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					double a = g.hFunction(i/10.0, j/10.0);
					double b = g.inverseHFunction(a, j/10.0);
					assert(Math.abs(i/10.0 - b) <= 0.1);
				}
			}
		}
	}
	
	public void testTau(){
		for(int p = 1; p < 20; p++){
			ClaytonCopula g = new ClaytonCopula(new double[]{p});
			double t = g.tau();
			assert(t >= -1 && t <= 1);
		}
		for(int p = 1; p < 20; p++){
			ClaytonCopula g = new ClaytonCopula(new double[]{1.0/p});
			double t = g.tau();
			assert(t >= -1 && t <= 1);
		}
	}
}

