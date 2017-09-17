package weka.estimators.vines.statistics;

import java.io.BufferedWriter;
import java.io.FileWriter;

import weka.core.Instances;
import weka.estimators.DensityEstimator;
import weka.estimators.meta.Bagging;
import weka.estimators.vines.RegularVine;
import weka.estimators.vines.VineUtils;

/**
 * This is a class for the Bias-Variance Analysis. <br>
 * It uses the AverageModel and the DataGenerator to estimate the error, bias
 * and variance.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class BiasVarianceAnalysis {
	String savepath = "./stats/";
	String path = "./src/main/data/noisedata.arff";
	int trainSize = 10000;
	int testSize = 1000;
	int iterations = 10;
	DataGenerator datGen;
	DensityEstimator[] models;
	BufferedWriter[] bf;
	AverageModel[] avgs;
	double[] error;
	double[] bias;
	double[] variance;
	double[] buildTimes;
	RegularVine trueModel;

	public static void main(String[] args) {
		BiasVarianceAnalysis vs = new BiasVarianceAnalysis();

		try {
			vs.statistics();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor <br>
	 * Initializes the DataGenerator and the to be tested RVine models.
	 */
	public BiasVarianceAnalysis() {
		System.out.println("Generating Data");

		datGen = new DataGenerator(path);
		trueModel = datGen.getRVine();

		models = new DensityEstimator[18];
		RegularVine rv1 = new RegularVine();
		RegularVine rv2 = new RegularVine();
		RegularVine rv3 = new RegularVine();
		RegularVine rv4 = new RegularVine();
		RegularVine rv5 = new RegularVine();
		RegularVine rv6 = new RegularVine();
		RegularVine rv7 = new RegularVine();
		RegularVine rv8 = new RegularVine();
		RegularVine rv9 = new RegularVine();

		rv1.setTrainMethod(RegularVine.TrainMethod.KENDALL);
		rv1.setBuildMethod(RegularVine.BuildMethod.REGULAR);
		rv2.setTrainMethod(RegularVine.TrainMethod.KENDALL);
		rv2.setBuildMethod(RegularVine.BuildMethod.SCATTERED_INDEP);
		rv3.setTrainMethod(RegularVine.TrainMethod.KENDALL);
		rv3.setBuildMethod(RegularVine.BuildMethod.THRESHOLD);
		rv4.setTrainMethod(RegularVine.TrainMethod.CV);
		rv4.setBuildMethod(RegularVine.BuildMethod.REGULAR);
		rv5.setTrainMethod(RegularVine.TrainMethod.CV);
		rv5.setBuildMethod(RegularVine.BuildMethod.SCATTERED_INDEP);
		rv6.setTrainMethod(RegularVine.TrainMethod.CV);
		rv6.setBuildMethod(RegularVine.BuildMethod.THRESHOLD);
		rv7.setTrainMethod(RegularVine.TrainMethod.MIXED);
		rv7.setBuildMethod(RegularVine.BuildMethod.REGULAR);
		rv8.setTrainMethod(RegularVine.TrainMethod.MIXED);
		rv8.setBuildMethod(RegularVine.BuildMethod.SCATTERED_INDEP);
		rv9.setTrainMethod(RegularVine.TrainMethod.MIXED);
		rv9.setBuildMethod(RegularVine.BuildMethod.THRESHOLD);

		rv1.setCopulaSelection("4");
		rv2.setCopulaSelection("4");
		rv3.setCopulaSelection("4");
		rv4.setCopulaSelection("4");
		rv5.setCopulaSelection("4");
		rv6.setCopulaSelection("4");
		rv7.setCopulaSelection("4");
		rv8.setCopulaSelection("4");
		rv9.setCopulaSelection("4");

		models[0] = rv1;
		models[1] = rv2;
		models[2] = rv3;
		models[3] = rv4;
		models[4] = rv5;
		models[5] = rv6;
		models[6] = rv7;
		models[7] = rv8;
		models[8] = rv9;
		models[9] = new Bagging(rv1);
		models[10] = new Bagging(rv2);
		models[11] = new Bagging(rv3);
		models[12] = new Bagging(rv4);
		models[13] = new Bagging(rv5);
		models[14] = new Bagging(rv6);
		models[15] = new Bagging(rv7);
		models[16] = new Bagging(rv8);
		models[17] = new Bagging(rv9);

		bf = new BufferedWriter[models.length];

		avgs = new AverageModel[models.length];
		error = new double[models.length];
		bias = new double[models.length];
		variance = new double[models.length];
		buildTimes = new double[models.length];
	}

	/**
	 * Method to compute the error, bias and variance of the given models. The
	 * results are stored in the stats folder.
	 */
	public void statistics() throws Exception {
		bf[0] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_Kendall_Regular.txt"));
		bf[1] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_Kendall_Scattered.txt"));
		bf[2] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_Kendall_Threshold.txt"));
		bf[3] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_CV_Regular.txt"));
		bf[4] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_CV_Scattered.txt"));
		bf[5] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_CV_Threshold.txt"));
		bf[6] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_Mixed_Regular.txt"));
		bf[7] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_Mixed_Scattered.txt"));
		bf[8] = new BufferedWriter(new FileWriter(savepath
				+ "RVine_Mixed_Threshold.txt"));
		bf[9] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_Kendall_Regular.txt"));
		bf[10] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_Kendall_Scattered.txt"));
		bf[11] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_Kendall_Threshold.txt"));
		bf[12] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_CV_Regular.txt"));
		bf[13] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_CV_Scattered.txt"));
		bf[14] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_CV_Threshold.txt"));
		bf[15] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_Mixed_Regular.txt"));
		bf[16] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_Mixed_Scattered.txt"));
		bf[17] = new BufferedWriter(new FileWriter(savepath
				+ "Bagged_Mixed_Threshold.txt"));

		Instances test = datGen.randomGenerate(testSize);

		for (int k = 0; k < models.length; k++) {
			// building all models

			avgs[k] = new AverageModel();

			for (int i = 0; i < iterations; i++) {
				Instances train = datGen.randomGenerate(trainSize);
				DensityEstimator m = models[k].getClass().newInstance();

				// parse options
				m.setOptions(models[k].getOptions());

				System.out.println("Building " + models[k].toString() + " : "
						+ i);

				double begin = System.currentTimeMillis();
				m.buildEstimator(train);
				double time = System.currentTimeMillis() - begin;

				System.out.println("done ~" + time);

				buildTimes[k] += time;

				avgs[k].addModel(m, m.logDensity(train));
			}

			System.out.println("Evaluating...");
			// pHat is estimated densities

			for (int i = 0; i < iterations; i++) {
				double err = VineUtils.KullbackLeiblerDivergenceLog(trueModel,
						avgs[k].getModel(i), test);
				error[k] += err;
				variance[k] += VineUtils.KullbackLeiblerDivergenceLog(avgs[k],
						avgs[k].getModel(i), test);
				// Write Output
				bf[k].write("Density: " + avgs[k].getModel(i).logDensity(test)
						+ ", KLD: " + err + ", True: "
						+ trueModel.logDensity(test) + "\n");
			}
			error[k] /= iterations;
			variance[k] /= iterations;
			buildTimes[k] /= iterations;

			bias[k] = VineUtils.KullbackLeiblerDivergenceLog(trueModel,
					avgs[k], test);
			bf[k].write("Error: " + error[k] + ", Bias: " + bias[k]
					+ ", Variance: " + variance[k] + ", Variance2: "
					+ (error[k] - bias[k]) + ", Avg.Time: " + buildTimes[k]
					+ "\n");

			// Output
			System.out.println("Model: " + models[k].toString());
			System.out.println("Error: " + error[k]);
			System.out.println("Bias: " + bias[k]);
			System.out.println("Variance: " + variance[k]);
			System.out.println("Variance2: " + (error[k] - bias[k]));

			avgs[k] = null;
			bf[k].close();
		}
	}
}
