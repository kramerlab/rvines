package weka.estimators.vines.statistics;

import java.util.ArrayList;
import java.util.Enumeration;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.estimators.DensityEstimator;

/**
 * This is an average model class. <br>
 * The average model is used for the Bias-Variance Analysis.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class AverageModel implements DensityEstimator {
	ArrayList<DensityEstimator> ensemble = new ArrayList<DensityEstimator>();
	ArrayList<Double> weights = new ArrayList<Double>();
	ArrayList<Double> scaled = new ArrayList<Double>();

	/**
	 * A method to normalize the edge weights.
	 */
	private void rescaleWeights() {
		scaled = new ArrayList<Double>();
		double scale = 0.0;
		for (Double w : weights) {
			scale += w;
		}

		for (Double w : weights) {
			scaled.add(w / scale);
		}
	}

	@Override
	public double logDensity(Instance inst) throws Exception {
		int s = ensemble.size();
		if (s == 0)
			return 0;

		double dens = 0.0;
		for (int i = 0; i < s; i++) {
			dens += scaled.get(i) * ensemble.get(i).logDensity(inst);
		}

		return dens;
	}

	@Override
	public double logDensity(Instances inst) throws Exception {
		double loglik = 0;

		for (Instance x : inst) {
			loglik += logDensity(x);
		}

		return loglik;
	}

	/**
	 * Add a model from the ensemble.
	 * 
	 * @param m
	 *            A density model to be added.
	 * @param weight
	 *            The weight of the density model.
	 */
	public void addModel(DensityEstimator m, double weight) {
		ensemble.add(m);
		weights.add(weight);
		rescaleWeights();
	}

	/**
	 * Remove a model from the ensemble.
	 * 
	 * @param i
	 *            Index of the to be deleted model.
	 */
	public void removeModel(int i) {
		ensemble.remove(i);
		weights.remove(i);
		rescaleWeights();
	}

	/**
	 * Get a model from the ensemble.
	 * 
	 * @param i
	 *            Index of the to be returned model.
	 * @return Model at index i.
	 */
	public DensityEstimator getModel(int i) {
		return ensemble.get(i);
	}

	/**
	 * Get a weight of a model from the ensemble.
	 * 
	 * @param i
	 *            Index of the to be returned weight.
	 * @return Weight from model at index i.
	 */
	public double getWeight(int i) {
		return weights.get(i);
	}

	@Override
	public void buildEstimator(Instances arg0) throws Exception {
		// No own building method
	}

	@Override
	public String[] getOptions() {
		// No own options
		return null;
	}

	@Override
	public Enumeration<Option> listOptions() {
		// No own options
		return null;
	}

	@Override
	public void setOptions(String[] arg0) throws Exception {
		// No own options
	}
}
