package weka.estimators.vines;

import java.util.ArrayList;

import weka.estimators.vines.copulas.*;
import weka.estimators.vines.functions.CopulaRotation;

public class CopulaHandler {
	
	private static Copula[] copulas(){
		Copula[] out = new Copula[14];
		
		out[0] = new IndependenceCopula();
		out[1] = new GaussCopula();
		out[2] = new TCopula();
		out[3] = new GumbelCopula();
		out[4] = new CopulaRotation(new GumbelCopula(), CopulaRotation.Mode.ROT90);
		out[5] = new CopulaRotation(new GumbelCopula(), CopulaRotation.Mode.ROT180);
		out[6] = new CopulaRotation(new GumbelCopula(), CopulaRotation.Mode.ROT270);
		out[7] = new ClaytonCopula();
		out[8] = new CopulaRotation(new ClaytonCopula(), CopulaRotation.Mode.ROT90);
		out[9] = new CopulaRotation(new ClaytonCopula(), CopulaRotation.Mode.ROT180);
		out[10] = new CopulaRotation(new ClaytonCopula(), CopulaRotation.Mode.ROT270);
		out[11] = new FrankCopula();
		out[12] = new GalambosCopula();
		out[13] = new FGMCopula();
		
		return out;
	}
	
	public static Copula[] select(boolean[] c){
		Copula[] copulas = copulas();
		ArrayList<Copula> out = new ArrayList<Copula>();
		
		for(int i=0; i<c.length; i++){
			if(c[i]){
				out.add(copulas[i]);
			}
		}
		
		return out.toArray(new Copula[out.size()]);
	}
	
	public String[] loadedCopulas(){
		return null;
	}
}
