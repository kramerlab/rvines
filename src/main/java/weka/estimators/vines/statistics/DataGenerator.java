package weka.estimators.vines.statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.estimators.vines.Edge;
import weka.estimators.vines.Graph;
import weka.estimators.vines.RegularVine;
import weka.estimators.vines.copulas.Copula;

/**
 * This is a data generation class for the Bias-Variance Analysis.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class DataGenerator {
	Instances data;
	RegularVine rvine;
	private boolean dep = true;
	
	public DataGenerator(){
		this("./src/daxreturns.arff");
	}
	
	/**
	 * Constructor
	 * <br>
	 * Initializes the true model.
	 * 
	 * @param path The path of the base dataset.
	 */
	public DataGenerator(String path){
		try {
			Instances inst = new Instances(new BufferedReader(
					new FileReader(path)));
			
			rvine = new RegularVine();
			rvine.buildEstimator(inst);
			
			if(dep) modifyDependence();
			
			data = inst;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to modify the true model's dependencies.
	 */
	private void modifyDependence() {
		Graph[] trees = rvine.getRegularVine();
		
		for(int i=0; i<trees.length; i++){
			Graph g = trees[i];
			for(Edge e : g.getUndirectedEdgeList()){
				Copula c = e.getCopula();
				double tau = c.tau();
				tau *= 2;
				c.tauInverse(tau);
			}
		}
		
		rvine.createRVineMatrix();
	}

	/**
	 * Method to generate a new dataset.
	 * <br>
	 * The method uses the sampling method of the initialized true model to
	 * generate a new dataset of the given size.
	 * 
	 * @param dataSize the desired size of the generated dataset.
	 * @return the generated dataset.
	 */
	public Instances randomGenerate(int dataSize){
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		
		for(int i=0; i<data.numAttributes(); i++){
			attributes.add(data.attribute(i));
		}

		Instances out = new Instances("Generated", attributes, 0);
		
		for(int i=0; i<dataSize; i++){
			// generate random Instance
			Instance sample = rvine.createRandomSample();			
			out.add(sample);
		}
		
		return out;
	}
	
	/**
	 * Get the true RVine model.
	 * @return true RVine model.
	 */
	public RegularVine getRVine(){
		return rvine;
	}
}
