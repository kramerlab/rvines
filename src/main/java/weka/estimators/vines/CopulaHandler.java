package weka.estimators.vines;

import java.util.ArrayList;
import java.util.Vector;

import weka.core.ClassDiscovery;
import weka.core.WekaPackageClassLoaderManager;
import weka.estimators.vines.copulas.*;
import weka.estimators.vines.functions.CopulaRotation;

public class CopulaHandler {
	Copula[] copulas;
	
	public CopulaHandler(){
		Vector<String> copulaclasses = ClassDiscovery.find(Copula.class, "weka.estimators.vines.copulas");
		copulas = new Copula[copulaclasses.size()];
		
		for(int i=0; i<copulaclasses.size(); i++){
			try {
				copulas[i] = (Copula) WekaPackageClassLoaderManager.forName(copulaclasses.get(i)).newInstance();
			} catch (ClassNotFoundException e) {
				System.err.println("Cannot find "+copulaclasses.get(i));
				// e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Failed to load "+copulaclasses.get(i));
				// e.printStackTrace();
			}
		}
	}
	
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
		String[] out = new String[copulas.length];
		for(int i=0; i<out.length; i++){
			out[i] = copulas[i].name();
		}
		return out;
	}
}
