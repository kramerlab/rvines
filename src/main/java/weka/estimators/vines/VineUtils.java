package weka.estimators.vines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.math3.analysis.UnivariateFunction;

import weka.core.Instances;
import weka.estimators.vines.copulas.*;

/**
 * This class contains utility function.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class VineUtils {
	private static boolean debug = false;

	/**
	 * Get the maximum spanning tree. <br>
	 * This function is specialized for Regular Vines. For that purpose, it uses
	 * function based Edge weights to calculate the maximum spanning tree. <br>
	 * The result is a spanning tree, which represents the most correlating
	 * variable pairs for the RVine. <br>
	 * It uses the idea of Prim's MST algorithm, but searching for the maximum
	 * spanning tree instead of the minimum spanning tree.
	 * 
	 * @param g
	 *            a connected Graph.
	 * @return returns the maximum spanning tree of g.
	 */
	public static Graph maxSpanTree(Graph g) {
		if (g == null) {
			return null;
		}
		if (g.isEmpty()) {
			return null;
		}

		Graph maxST = new Graph();

		ArrayList<Node> nodeList = g.getNodeList();
		for (Node n : nodeList) {
			maxST.addNode(n);
		}

		ArrayList<Node> nodeListMaxST = new ArrayList<Node>();
		nodeListMaxST
				.add(nodeList.get((int) (Math.random() * nodeList.size())));

		while (!(nodeListMaxST.size() == nodeList.size())) {
			ArrayList<Edge> edges = new ArrayList<Edge>();
			for (Node n : nodeListMaxST) {
				edges.addAll(g.getGraph().get(n));
			}

			Edge maxEdge = null;
			for (Edge e : edges) {
				if (!nodeListMaxST.contains(e.getTo())) {
					if (maxEdge == null) {
						maxEdge = e;
					} else {
						if (maxEdge.getWeight()
								< e.getWeight()) {
							maxEdge = e;
						}
					}
				}
			}

			nodeListMaxST.add(maxEdge.getTo());
			maxST.addEdge(maxEdge);
		}
		return maxST;
	}

	/**
	 * Calculates the empirical Kendall's tau. <br>
	 * Both random variables need to be rank normalized to get a reliable
	 * Kendall's tau value.
	 * 
	 * @param a
	 *            a rank normalized random variable.
	 * @param b
	 *            another rank normalized random variable.
	 * @return returns the empirical Kendall's tau for a and b.
	 */
	public static double kendallsTau(double[] a, double[] b) {
		if (!(a.length == b.length)) {
			return Double.NaN;
		}

		// Sort lists with respect to a
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();

		for (int i = 0; i < a.length; i++) {
			double in = a[i];
			int k;
			for (k = 0; k < x.size(); k++) {
				if (in < x.get(k)) {
					break;
				}
			}
			x.add(k, in);
			y.add(k, b[i]);
		}
		if (debug) {
			System.out.println("a = " + a);
			System.out.println("b = " + b);
			System.out.println("x = " + x);
			System.out.println("y = " + y);
		}
		// Begin counting:

		int P = 0; // number of concordant pairs
		int Q = 0; // number of discordant pairs
		int T = 0; // number of ties only in a
		int U = 0; // number of ties only in b
		// if a tie appears in a and b it is not added to either T or U

		if (debug) {
			System.out.println();
		}
		for (int i = 0; i < x.size() - 1; i++) {
			for (int j = i + 1; j < y.size(); j++) {
				if (debug) {
					System.out.println("Comparing " + i + " to " + j);
				}
				if (x.get(i) < x.get(j) && y.get(i) < y.get(j)) {
					P++;
				}
				if (x.get(i) < x.get(j) && y.get(i) > y.get(j)) {
					Q++;
				}
				if (x.get(i).equals(x.get(j)) && !(y.get(i).equals(y.get(j)))) {
					T++;
				}
				if (!(x.get(i).equals(x.get(j))) && y.get(i).equals(y.get(j))) {
					U++;
				}
				if (debug) {
					System.out.println("P: " + P + ", Q: " + Q + ", T: " + T
							+ ", U:" + U);
				}
			}
		}
		if (debug) {
			System.out.println();
			System.out.println("P: " + P + ", Q: " + Q + ", T: " + T + ", U:"
					+ U);
		}
		double n = (P + Q + T);
		double m = (P + Q + U);
		return (P - Q) / Math.sqrt(n * m);
	}

	/**
	 * This is a goodness of fit test for copulas. <br>
	 * It tests the copulae for fitting between a and b. The best copula will be
	 * returned with its parameters.
	 * 
	 * @param copulae
	 *            An array of copula families that participate on the GOF-Test.
	 * @param a
	 *            An observation array.
	 * @param b
	 *            Another observation array.
	 * @return The copula with its parameters that fits best.
	 */
	public static Copula goodnessOfFit(Copula[] copulae, double[] a, double[] b) {

		double[] p = new double[copulae.length];

		for (int i = 0; i < copulae.length; i++) {
			Copula c = copulae[i];
			p[i] = pValue(c, a, b);
		}

		int out = 0;
		for (int i = 1; i < copulae.length; i++) {
			if (p[out] < p[i])
				out = i;
		}

		return copulae[out];
	}

	/**
	 * Bootstrap method that is used for the GOF-Test
	 * 
	 * @param c
	 *            A copula to compute the p value on.
	 * @param a
	 *            An observation array.
	 * @param b
	 *            Another observation array.
	 * @return The statistical p value for c.
	 */
	private static double pValue(Copula c, double[] a, double[] b) {
		int N = 100;
		int n = a.length;

		c.mle(a, b);

		double sn = 0;
		for (int i = 0; i < n; i++) {
			sn += Math.pow(empCop(a, b, a[i], b[i]) - c.C(a[i], b[i]), 2);
		}

		int hitCount = 0;

		for (int k = 0; k < N; k++) {
			// generate random samples
			double[] a2 = new double[n];
			double[] b2 = new double[n];

			for (int i = 0; i < n; i++) {
				a2[i] = Math.random();
				b2[i] = c.h2inverse(a2[i], Math.random());
			}

			double[] u1 = rankNormalization(a2);
			double[] u2 = rankNormalization(b2);

			c.mle(u1, u2);

			double sn2 = 0;
			for (int i = 0; i < n; i++) {
				sn2 += Math.pow(
						empCop(u1, u2, u1[i], u2[i]) - c.C(u1[i], u2[i]), 2);
			}

			if (sn2 > sn)
				hitCount++;

		}

		return hitCount / ((double) N);
	}

	/**
	 * Method to simulate an empirical copula.
	 * 
	 * @param a
	 *            An observation array.
	 * @param b
	 *            Another observation array.
	 * @param x
	 *            X value of request point.
	 * @param y
	 *            Y value of request point.
	 * @return The cdf of the empirical copula for a and b evaluated at x, y.
	 */
	private static double empCop(double[] a, double[] b, double x, double y) {
		int N = a.length;
		int obs = 0;

		for (int i = 0; i < N; i++) {
			if (a[i] <= x && b[i] <= y)
				obs++;
		}

		return 1.0 / N * obs;
	}

	/**
	 * Log-Likelihood calculation for copulae. <br>
	 * It is used to calculate the copula log-likelihood for the MLE.
	 * 
	 * @param c
	 *            a copula, whose log-likelihood is calculated.
	 * @param a
	 *            an observation array.
	 * @param b
	 *            another observation array.
	 * @return returns the log-likelihood.
	 */
	public static double logLikelihood(Copula c, double[] a, double[] b) {
		double logLik = 0;

		for (int i = 0; i < a.length; i++) {
			logLik += Math.log(c.density(a[i], b[i]));
		}
		return logLik;
	}

	/**
	 * Get rank normalized data.
	 * 
	 * @param data
	 *            the data to be rank normalized.
	 * @return the rank normalized data.
	 */
	public static double[] rankNormalization(double[] data) {

		// Get a copy of the list, with values sorted from least to greatest.
		// That is: S[i] <= S[2] <= ... <= S[N]
		// Thus, the index of each list item equates to its ranking position

		ArrayList<Double> S = new ArrayList<Double>();

		for (double e : data) {
			S.add(e);
		}

		Collections.sort(S);

		// Create a lookup table from value to ranking position
		// Correct for ties in the sorted list by average as we go.

		double tieCount = 0;
		double tieTotal = 0;
		double tieValue = Double.NaN;
		HashMap<Double, Double> table = new HashMap<Double, Double>();

		for (int i = 1; i <= S.size(); i++) {
			if (S.get(i - 1) == tieValue) {
				tieTotal += i;
				tieCount++;
			} else {
				table.put(S.get(i - 1), (double) i);

				// If we are coming off a run of ties, find the avg
				// and use that as the ranking position for that value

				if (tieCount > 1) {
					table.put(tieValue, tieTotal / tieCount);
				}

				tieCount = 1;
				tieTotal = i;
				tieValue = S.get(i - 1);
			}
		}

		// Check if S ended in a run of ties and treat as above

		if (tieCount > 1) {
			table.put(tieValue, tieTotal / tieCount);
		}

		// Now, create a new list of ranking positions corresponding
		// to the order of the original list of values.

		double[] out = new double[data.length];
		double maxRank = tieTotal / tieCount;

		for (int i = 0; i < data.length; i++) {
			// normalize with dividing by max rank
			out[i] = table.get(data[i]) / maxRank;
		}

		return out;
	}

	/**
	 * Correction function. <br>
	 * Corrects the value of x. <br>
	 * It is used for infinity value handling.
	 * 
	 * @param x
	 *            the value to be corrected.
	 * @return returns the correction of x.
	 */
	public static double laplaceCorrection(double x) {
		x = Math.min(x, 1 - Math.pow(10, -4));
		x = Math.max(x, Math.pow(10, -4));
		return x;
	}

	/**
	 * Numerical integration function based on Simpson's rule.
	 * 
	 * @param f
	 *            The function to be integrated.
	 * @param N
	 *            The number of sub-intervals.
	 * @param lb
	 *            The lower bound of the integration.
	 * @param ub
	 *            The upper bound of the integration.
	 * @return An approximation to the integration of f between lb and ub.
	 */
	public static double simpsonIntegrate(UnivariateFunction f, int N,
			double lb, double ub) {
		double h = (ub - lb) / N; // step size

		double sum = f.value(lb) + f.value(ub);

		for (int i = 1; i < N; i++) {
			double x = lb + h * i;
			sum += 2 * f.value(x);
		}

		for (int i = 1; i <= N; i++) {
			double x = lb + h * (i - 1);
			double y = lb + h * i;
			sum += 4 * f.value((x + y) / 2);
		}

		return sum * h / 6.0;
	}

	/**
	 * Numerical inversion based on Bisection method. <br>
	 * We assert a function f with a value at a certain point x, such that f(x)
	 * = z and x is inside the [lb, ub] interval. <br>
	 * For the given values f, z, lb and ub it will return an approximation to
	 * x. <br>
	 * Note that lb and ub need to be finite!
	 * 
	 * @param f
	 *            The function to be inverted.
	 * @param z
	 *            The functional value f(x) = z.
	 * @param lb
	 *            The lower bound of the inversion.
	 * @param ub
	 *            The upper bound of the inversion.
	 * @return An approximation to the inversion of f between lb and ub.
	 */
	public static double bisectionInvert(UnivariateFunction f, double z,
			double lb, double ub) {
		boolean br = false;
		double ans = 0.0, tol = 0, x0 = lb, x1 = ub, it = 0, fl, fh, val;

		fl = f.value(x0);
		fl -= z;
		fh = f.value(x1);
		fh -= z;

		if (Math.abs(fl) <= tol) {
			ans = x0;
			br = true;
		}
		if (Math.abs(fh) <= tol) {
			ans = x1;
			br = true;
		}

		while (!br) {
			ans = (x0 + x1) / 2.0;
			val = f.value(ans);
			val -= z;

			// stop if values become too close (avoid infinite loop)
			if (Math.abs(val) <= tol)
				br = true;
			if (Math.abs(x0 - x1) <= tol)
				br = true;

			if (val > 0.0) {
				x1 = ans;
				fh = val;
			} else {
				x0 = ans;
				fl = val;
			}

			// stop if too many iterations are required (avoid infinite loop)
			++it;
			if (it > 50)
				br = true;
		}

		return ans;
	}
	
	/**
	 * Transforms Instances to double array data.
	 * 
	 * @param in Data as Instances.
	 * @return Data as double matrix.
	 */
	public static double[][] transform(Instances in){
		double[][] data;			
		int k = in.numAttributes();
		
		data = new double[k][];
		
		for(int i=0; i<k; i++){
			data[i] = in.attributeToDoubleArray(i);
		}
		
		return data;
	}
	
	/**
	 * Tests if the data input is correct for RVine usage.
	 * 
	 * @param data Data as double matrix.
	 * @return Boolean if data is correct.
	 */
	public static boolean testData(Instances data){
		for(int i=0; i<data.numAttributes(); i++){
			double[] x = data.attributeToDoubleArray(i);
			for(int j=0; j<data.size(); j++){
				if(x[j] < 0 || x[j] > 1) return false; 
			}
		}
		return true;
	}
	
	/**
	 * Method for Kullback Leibler Divergence.
	 * Use this one if your data already is log-data.
	 * 
	 * @param p Data as double array (log).
	 * @param q Data as double array (log).
	 * @return Kullback Leibler Divergence of q,p
	 */
	public static double KullbackLeiblerDivergenceLog(double[] q, double[] p){
		double out = 0.0;
		
		for(int i=0; i<p.length; i++){
			out += Math.exp(q[i])*(q[i]- p[i]);
		}
		
		return out;
	}
}
