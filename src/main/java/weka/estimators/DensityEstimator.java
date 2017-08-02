package weka.estimators;

import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

/**
 * This is an interface for density estimators.
 *
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public interface DensityEstimator {
	/**
	   * Generates an estimator. Must initialize all fields of the estimator
	   * that are not being set via options (ie. multiple calls of buildEstimator
	   * must always lead to the same result). Must not change the dataset
	   * in any way.
	   *
	   * @param data set of instances serving as training data
	   * @exception Exception if the classifier has not been
	   * generated successfully
	   */
	  public abstract void buildEstimator(Instances data) throws Exception;

	  /**
	   * Estimates the given test instance. The instance has to belong to a
	   * dataset when it's being estimated.
	   *
	   * @param instance the instance to be estimated
	   * @return the estimated density for the given instance
	   * @exception Exception if an error occurred during the prediction
	   */
	  public double logDensity(Instance instance) throws Exception;

	  /**
	   * Estimates the given test instances. The instances has to belong to a
	   * dataset when it's being estimated.
	   *
	   * @param instances the instances to be estimated
	   * @return the sum of the estimated densities for the given instances
	   * @exception Exception if an error occurred during the prediction
	   */
	  public double logDensity(Instances instances) throws Exception;

	  /**
	   * Returns the Capabilities of this classifier. Maximally permissive
	   * capabilities are allowed by default. Derived classifiers should
	   * override this method and first disable all capabilities and then
	   * enable just those capabilities that make sense for the scheme.
	   *
	   * @return            the capabilities of this object
	   * @see               Capabilities
	   */
	  public Capabilities getCapabilities();
}
