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
	
	public Copula[] select(boolean[] c){
		if(c.length != copulas.length){
			System.out.println("Selection array needs to fit the copula array size!");
			return null;
		}
		
		ArrayList<Copula> out = new ArrayList<Copula>();
		
		for(int i=0; i<copulas.length; i++){
			if(c[i]){
				try{
					out.add(copulas[i].getClass().newInstance());
					if(copulas[i].rotations()){
						out.add(new CopulaRotation(copulas[i].getClass().newInstance(), CopulaRotation.Mode.ROT90));
						out.add(new CopulaRotation(copulas[i].getClass().newInstance(), CopulaRotation.Mode.ROT180));
						out.add(new CopulaRotation(copulas[i].getClass().newInstance(), CopulaRotation.Mode.ROT270));
					}
				} catch (Exception e){
					System.out.println("Failed to load "+copulas[i].name());
				}
			}
		}
		
		return out.toArray(new Copula[out.size()]);
	}
	
	public String[] loadedCopulas(){
		String[] out = new String[copulas.length];
		for(int i=0; i<out.length; i++){
			out[i] = i+" - "+copulas[i].name();
		}
		return out;
	}
}
