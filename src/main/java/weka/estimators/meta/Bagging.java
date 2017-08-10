package weka.estimators.meta;

import java.util.Enumeration;
import java.util.Random;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.estimators.DensityEstimator;

public class Bagging implements DensityEstimator {
	static final long serialVersionUID = -115879962237199703L;

	/** The size of each bag sample, as a percentage of the training size */
	protected int m_BagSizePercent = 100;

	/** The size of each bag sample, as a percentage of the training size */
	protected DensityEstimator[] m_Estimators = new DensityEstimator[10];

	/** Random number generator */
	protected DensityEstimator m_Estimator;

	public int getBagSizePercent() {
		return m_BagSizePercent;
	}

	public void setBagSizePercent(int m_BagSizePercent) {
		this.m_BagSizePercent = m_BagSizePercent;
	}

	public void setEstimator(DensityEstimator m_Estimator) {
		this.m_Estimator = m_Estimator;
	}

	/** Random number generator */
	protected Random m_random;

	/** Random seed */
	protected int m_Seed;

	/** Reference to the training data */
	protected Instances m_data;

	/** Debug mode */
	protected boolean m_Debug = false;

	/**
	 * Constructor.
	 */
	public Bagging() {
		m_Estimator = new weka.estimators.vines.RegularVine();
	}

	/**
	 * Returns a training set for a particular iteration.
	 * 
	 * @param iteration
	 *            the number of the iteration for the requested training set.
	 * @return the training set for the supplied iteration number.
	 * @throws Exception
	 *             if something goes wrong when generating a training set.
	 */
	protected synchronized Instances getTrainingSet(int iteration)
			throws Exception {
		int bagSize = (int) (m_data.numInstances() * (m_BagSizePercent / 100.0));
		Instances bagData = null;
		Random r = new Random(m_Seed + iteration);

		// create the in-bag dataset
		if (bagSize < m_data.numInstances()) {
			bagData = m_data.resampleWithWeights(r);
			bagData.randomize(r);
			Instances newBagData = new Instances(bagData, 0, bagSize);
			bagData = newBagData;
		} else {
			bagData = m_data.resampleWithWeights(r);
		}
		return bagData;
	}

	@Override
	public void buildEstimator(Instances data) throws Exception {
		// get fresh Instances object
		m_data = new Instances(data);
		m_random = new Random(m_Seed);

		for (int j = 0; j < m_Estimators.length; j++) {
			m_Estimators[j] = m_Estimator.getClass().newInstance();
			// Copy Options of base estimator
			m_Estimators[j].setOptions(m_Estimator.getOptions());
		}

		buildEstimators();
	}

	/**
	 * Does the actual construction of the ensemble
	 *
	 * @throws Exception
	 *             if something goes wrong during the training process
	 */
	protected void buildEstimators() throws Exception {
		for (int i = 0; i < m_Estimators.length; i++) {
			m_Estimators[i].buildEstimator(getTrainingSet(i));
		}
	}

	@Override
	public double logDensity(Instance instance) throws Exception {
		// Average over Ensemble
		double sum = 0;
		for (int i = 0; i < m_Estimators.length; i++) {
			sum += m_Estimators[i].logDensity(instance);
		}
		return sum/m_Estimators.length;
	}

	@Override
	public double logDensity(Instances instances) throws Exception {
		double loglik = 0;
		
		for(int j=0; j<instances.size(); j++){
			Instance x = instances.get(j);
			loglik += logDensity(x);
		}
		return loglik;
	}
	
	// Option Handler
	
	@Override
	public String[] getOptions() {
		return Option.getOptions(this, this.getClass());
	}

	@Override
	public Enumeration<Option> listOptions() {
		return Option.listOptionsForClass(this.getClass()).elements();
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		Option.setOptions(options, this, this.getClass());
	}

}
