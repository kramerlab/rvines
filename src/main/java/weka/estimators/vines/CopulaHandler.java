package weka.estimators.vines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import weka.core.ClassDiscovery;
import weka.core.WekaPackageClassLoaderManager;
import weka.estimators.vines.copulas.*;
import weka.estimators.vines.functions.CopulaRotation;

/**
 * This is an adapter class for copulas.
 * <br>
 * It uses Weka's ClassDiscovery and WekaPackageClassLoaderManager
 * to load copula classes from the weka.estimators.vines.copulas
 * package automatically.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class CopulaHandler implements Serializable{
	private static final long serialVersionUID = 7964742147646150808L;
	Copula[] copulas;
	
	/**
	 * Constructor
	 * <br>
	 * Loads copulas from the package when initialized.
	 */
	public CopulaHandler(){
		Vector<String> copulaclasses = ClassDiscovery.find(
				Copula.class,
				"weka.estimators.vines.copulas");
		copulas = new Copula[copulaclasses.size()];
		
		for(int i=0; i<copulaclasses.size(); i++){
			try {
				copulas[i] = (Copula) WekaPackageClassLoaderManager.forName(
						copulaclasses.get(i)).newInstance();
			} catch (ClassNotFoundException e) {
				System.err.println("Cannot find "+copulaclasses.get(i));
				// e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Failed to load "+copulaclasses.get(i));
				// e.printStackTrace();
			}
		}
	}
	
	/**
	 * Select a subset of copulas.
	 * <br>
	 * Use loadedCopulas function to get the copula indices.
	 * The selection array is mapped to the copula array.
	 * @param c Selection array.
	 * @return Selected subset as copula array.
	 */
	public Copula[] select(boolean[] c){
		if(c.length != copulas.length){
			System.out.println("Selection array needs to fit "
					+ "the copula array size!");
			return null;
		}
		
		ArrayList<Copula> out = new ArrayList<Copula>();
		
		for(int i=0; i<copulas.length; i++){
			if(c[i]){
				try{
					out.add(copulas[i].getClass().newInstance());
					if(copulas[i].rotations()){
						out.add(new CopulaRotation(
								copulas[i].getClass().newInstance(),
								CopulaRotation.Mode.ROT90));
						out.add(new CopulaRotation(
								copulas[i].getClass().newInstance(),
								CopulaRotation.Mode.ROT180));
						out.add(new CopulaRotation(
								copulas[i].getClass().newInstance(),
								CopulaRotation.Mode.ROT270));
					}
				} catch (Exception e){
					System.out.println("Failed to load "+copulas[i].name());
				}
			}
		}
		
		return out.toArray(new Copula[out.size()]);
	}
	
	/**
	 * Get the loaded copula descriptions.
	 * @return An array containing the indices and
	 * 			names of the loaded copula classes.
	 */
	public Copula[] loadedCopulas(){
		return copulas;
	}
}
