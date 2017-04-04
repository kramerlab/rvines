package weka.estimators.vines;

import java.util.ArrayList;

import weka.estimators.vines.copulas.*;

public class CopulaHandler {
	
	private static Copula[] copulas(){
		Copula[] out = new Copula[14];
		
		out[0] = new IndependenceCopula();
		out[1] = new GaussCopula(new double[]{0.5});
		out[2] = new TCopula(new double[]{0.5, 1});
		out[3] = new GumbelCopula(new double[]{3});
		out[4] = new Gumbel90RotatedCopula(new double[]{-3});
		out[5] = new Gumbel180RotatedCopula(new double[]{3});
		out[6] = new Gumbel270RotatedCopula(new double[]{-3});
		out[7] = new ClaytonCopula(new double[]{2});
		out[8] = new Clayton90RotatedCopula(new double[]{-2});
		out[9] = new Clayton180RotatedCopula(new double[]{2});
		out[10] = new Clayton270RotatedCopula(new double[]{-2});
		out[11] = new FrankCopula(new double[]{0.5});
		out[12] = new GalambosCopula(new double[]{0});
		out[13] = new FGMCopula(new double[]{0});
		
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
