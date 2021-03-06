package weka.estimators.vines;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import weka.core.CommandlineRunnable;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionMetadata;
import weka.estimators.DensityEstimator;
import weka.estimators.vines.copulas.Copula;
import weka.estimators.vines.copulas.IndependenceCopula;
import weka.gui.ProgrammaticProperty;

/**
 * This class represents the Regular Vine. <br>
 * It is the main class of the vines package. <br>
 * It uses the Graph class to store the Graphs for its dimensions. <br>
 * It can be used to perform sampling (or simulation) and (pseudo) log-density
 * computation. <br>
 * The implementation is based on J.F. Di&szlig;mann's diploma thesis (2010):
 * Statistical inference for regular vines and application.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class RegularVine implements DensityEstimator, CommandlineRunnable,
		Serializable {
	private static final long serialVersionUID = -5876664157542627697L;
	protected boolean built, timestamps, help, loaded, rvm, fam, parm, pllm,
			taum, etaum, sum;
	protected String filepath;
	protected CopulaHandler ch = new CopulaHandler();
	protected boolean[] selected;
	protected Graph[] rvine;
	protected int[][] m;
	protected Edge[][] edges;
	protected Instances data;
	protected TrainMethod trainMethod = TrainMethod.KENDALL;
	protected BuildMethod buildMethod = BuildMethod.REGULAR;
	protected double threshold = 0.1;
	protected int cvFolds = 10;

	/**
	 * This is an enum class for possible training methods. KENDALL - Use
	 * Kendall's Tau as dependence measure and MLE as Copula Selection. CV - Use
	 * Cross-Validation as dependence measure and Copula Selection. MIXED - Use
	 * Kendall's Tau as dependence measure and CV as Copula Selection.
	 */
	public enum TrainMethod {
		KENDALL, CV, MIXED
	}

	/**
	 * This is an enum class for possible building methods. REGULAR - No special
	 * building method. SCATTERED_INDEP - Scatter independence copulas dependent
	 * on tree level. THRESHOLD - Use independence copula beyond a given
	 * threshold.
	 */
	public enum BuildMethod {
		REGULAR, SCATTERED_INDEP, THRESHOLD
	}

	/**
	 * Constructor
	 */
	public RegularVine() {
		selected = new boolean[ch.loadedCopulas().length];
		for (int i = 0; i < selected.length; i++)
			selected[i] = true;

		built = false;
		timestamps = false;
		help = false;
		loaded = false;
		rvm = false;
		fam = false;
		parm = false;
		pllm = false;
		taum = false;
		etaum = false;
		sum = false;
		filepath = "";
	}

	/**
	 * Build the Regular Vine density estimator on the given data set. <br>
	 * See the model selection algorithm presented in J.F. Di&szlig;mann's
	 * diploma thesis.
	 * 
	 * @param data
	 *            the training data set to build the estimator on.
	 */
	@Override
	public void buildEstimator(Instances data) {
		rvine = new Graph[data.numAttributes() - 1];
		this.data = data;
		Graph g = new Graph();

		double start = System.currentTimeMillis();
		double stamp = start;

		if (timestamps) {
			System.out.println("Building T1 ...");
			if (trainMethod == TrainMethod.CV) {
				System.out.print("\t Compute Cross-Validated Likelihood... ");
			} else {
				System.out.print("\t Compute Kendall's tau... ");
			}
		}

		// initialize nodes
		for (int i = 1; i <= data.numAttributes(); i++) {
			Node n = new Node(i);
			n.putData(i, data.attributeToDoubleArray(i - 1));
			g.addNode(n);
		}

		// initialize edges
		for (int i = 0; i < g.getNodeList().size(); i++) {
			for (int j = i + 1; j < g.getNodeList().size(); j++) {
				Node a = g.getNodeList().get(i);
				Node b = g.getNodeList().get(j);
				Edge e = new Edge(a, b, 0);
				weightEdge(e);
				g.addEdge(e);
			}
		}

		if (timestamps) {
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ " + (time - stamp) + "ms");
			System.out.print("\t Compute max. spanning tree... ");
			stamp = time;
		}

		// calculate maximal spanning tree of graph
		g = VineUtils.maxSpanTree(g);

		if (timestamps) {
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ " + (time - stamp) + "ms");
			System.out.print("\t Compute fitting Copulae... ");
			stamp = time;
		}

		// fit copulas to the edges
		for (Edge e : g.getUndirectedEdgeList()) {
			selectCopula(e, 0);
		}

		if (timestamps) {
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ " + (time - stamp) + "ms");
			stamp = time;
		}

		// add graph to rvine
		rvine[0] = g;

		// until regular vine is fully specified, do:
		for (int lev = 1; lev < rvine.length; lev++) {
			stamp = System.currentTimeMillis();
			Graph gNext = new Graph();
			g = rvine[lev - 1];

			if (timestamps) {
				double time = System.currentTimeMillis();
				System.out.println("Building T" + (lev + 1) + "... ");
				System.out.print("\t Merge Nodes... ");
				stamp = time;
			}

			// for all edges of MST, do
			for (Edge e : g.getUndirectedEdgeList()) {
				gNext.addNode(mergeNodes(e));
			}

			if (timestamps) {
				double time = System.currentTimeMillis();
				System.out.println("finished! ~ " + (time - stamp) + "ms");
				if (trainMethod == TrainMethod.CV) {
					System.out
							.print("\t Compute Cross-Validated Likelihood... ");
				} else {
					System.out.print("\t Compute Kendall's tau... ");
				}
				stamp = time;
			}

			// calculate kendall's tau and add edges to graph,
			// for all possible edges (proximity condition)
			for (int i = 0; i < gNext.getNodeList().size(); i++) {
				for (int j = i + 1; j < gNext.getNodeList().size(); j++) {
					Node a = gNext.getNodeList().get(i);
					Node b = gNext.getNodeList().get(j);

					if (a.isIntersected(b)) {
						Edge e = new Edge(a, b, 0);
						weightEdge(e);
						gNext.addEdge(e);
					}
				}
			}

			if (timestamps) {
				double time = System.currentTimeMillis();
				System.out.println("finished! ~ " + (time - stamp) + "ms");
				System.out.print("\t Compute max. spanning tree... ");
				stamp = time;
			}

			// calculate maximal spanning tree of graph
			gNext = VineUtils.maxSpanTree(gNext);

			if (timestamps) {
				double time = System.currentTimeMillis();
				System.out.println("finished! ~ " + (time - stamp) + "ms");
				System.out.print("\t Compute fitting Copulae... ");
				stamp = time;
			}

			// fit copulas to the edges
			for (Edge e : gNext.getUndirectedEdgeList()) {
				selectCopula(e, lev);
			}

			if (timestamps) {
				double time = System.currentTimeMillis();
				System.out.println("finished! ~ " + (time - stamp) + "ms");
				stamp = time;
			}

			rvine[lev] = gNext;
		}

		// Use merge only to set last Edge label
		for (Edge e : rvine[rvine.length - 1].getUndirectedEdgeList()) {
			mergeNodes(e);
		}

		if (timestamps) {
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ " + (time - stamp) + "ms");
			System.out.print("Building Matrices... ");
			stamp = time;
		}

		createRVineMatrix();
		built = true;

		if (timestamps) {
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ " + (time - stamp) + "ms");
			System.out.println("Total time: " + (time - start) + "ms");
			System.out.println();
		}
	}

	/**
	 * Weights the edge using the selected method.
	 * 
	 * @param e
	 *            The Edge to be weighted.
	 */
	protected void weightEdge(Edge e) {
		if (trainMethod == TrainMethod.KENDALL
				|| trainMethod == TrainMethod.MIXED) {
			kendallWeight(e);
		}
		if (trainMethod == TrainMethod.CV) {
			cvFitCopula(e, selected);
		}
	}

	/**
	 * Selects a copula for the edge using the selected method.
	 * 
	 * @param e
	 *            The Edge to select a copula for.
	 * @param lev
	 *            Edge level, needed to compute the spread_indep probability.
	 */
	protected void selectCopula(Edge e, int lev) {
		if (buildMethod == BuildMethod.SCATTERED_INDEP) {
			double p = ((double) lev) / (rvine.length);
			if (Math.random() < p) {
				e.setCopula(new IndependenceCopula());
				e.setLogLik(0);
				return;
			}
		}
		if (buildMethod == BuildMethod.THRESHOLD) {
			double weight = e.getWeight();

			if (trainMethod == TrainMethod.KENDALL
					|| trainMethod == TrainMethod.MIXED) {
				weight = Math.abs(weight);
			}

			if (weight < threshold) {
				e.setCopula(new IndependenceCopula());
				e.setLogLik(0);
				return;
			}
		}
		if (trainMethod == TrainMethod.KENDALL) {
			fitCopula(e, selected);
		}
		if (trainMethod == TrainMethod.MIXED) {
			cvFitCopula(e, selected);
		}
		if (trainMethod == TrainMethod.CV) {
			// Already done in weight step
			// cvFitCopula(e, selected);
		}
	}

	/**
	 * Creates a sample instance. <br>
	 * It can be used either for random sampling or for sampling specific
	 * entries only. <br>
	 * x shall be a complete instance, where we can flag in the given array if
	 * we use the value of x for the new instance or if we re-sample the x
	 * value. <br>
	 * Only the flagged as given values from x will affect the sampling. The
	 * other values will be overwritten with sampled values. <br>
	 * The main part of the sampling approach is based on the sampling algorithm
	 * presented in J.F. Di&szlig;mann's diploma thesis.
	 * 
	 * @param x
	 *            an instance
	 * @param given
	 *            a boolean flag for each value of x if it shall be used as
	 *            given value.
	 * @return returns the sampled instance.
	 */
	public Instance createSample(double[] x, boolean[] given) {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return null;
		}
		int n = m.length;
		double[] u = new double[n];
		double[][] v = new double[n][n];
		Copula c;

		// random observations of uniform(0,1) distribution
		for (int i = 0; i < n; i++) {
			u[i] = Math.random();
		}

		if (!given[m[n - 1][n - 1] - 1]) {
			x[m[n - 1][n - 1] - 1] = u[m[n - 1][n - 1] - 1];
		}

		for (int k = n - 2; k >= 0; k--) {
			if (!given[m[k][k] - 1]) {
				for (int i = k + 1; i < n - 1; i++) {
					// run path down to get x_i with inverse h-function
					c = edges[i][k].getCopula();
					if (m[k][k] > m[i][k]) {
						u[m[k][k] - 1] = c
								.h1inverse(v[m[i][k] - 1][m[i + 1][k] - 1],
										u[m[k][k] - 1]);
					} else {
						u[m[k][k] - 1] = c.h2inverse(u[m[k][k] - 1],
								v[m[i][k] - 1][m[i + 1][k] - 1]);
					}
				}
				// level 0, get x value from h-inverse of adjacent x-value (last
				// entry in current column)
				c = edges[n - 1][k].getCopula();
				if (m[k][k] > m[n - 1][k]) {
					x[m[k][k] - 1] = c.h1inverse(x[m[n - 1][k] - 1],
							u[m[k][k] - 1]);
				} else {
					x[m[k][k] - 1] = c.h2inverse(u[m[k][k] - 1],
							x[m[n - 1][k] - 1]);
				}
			}
			// one dimensional transformed values
			c = edges[n - 1][k].getCopula();
			if (m[k][k] > m[n - 1][k]) {
				v[m[k][k] - 1][m[n - 1][k] - 1] = c.h1Function(
						x[m[n - 1][k] - 1], x[m[k][k] - 1]);
				v[m[n - 1][k] - 1][m[k][k] - 1] = c.h2Function(
						x[m[n - 1][k] - 1], x[m[k][k] - 1]);
			} else {
				v[m[k][k] - 1][m[n - 1][k] - 1] = c.h2Function(x[m[k][k] - 1],
						x[m[n - 1][k] - 1]);
				v[m[n - 1][k] - 1][m[k][k] - 1] = c.h1Function(x[m[k][k] - 1],
						x[m[n - 1][k] - 1]);
			}

			for (int i = n - 2; i > k; i--) {
				// run path up to generate transformed values
				c = edges[i][k].getCopula();
				if (m[k][k] > m[i][k]) {
					v[m[k][k] - 1][m[i][k] - 1] = c.h1Function(
							v[m[i][k] - 1][m[i + 1][k] - 1],
							v[m[k][k] - 1][m[i + 1][k] - 1]);
					v[m[i][k] - 1][m[k][k] - 1] = c.h2Function(
							v[m[i][k] - 1][m[i + 1][k] - 1],
							v[m[k][k] - 1][m[i + 1][k] - 1]);
				} else {
					v[m[k][k] - 1][m[i][k] - 1] = c.h2Function(
							v[m[k][k] - 1][m[i + 1][k] - 1],
							v[m[i][k] - 1][m[i + 1][k] - 1]);
					v[m[i][k] - 1][m[k][k] - 1] = c.h1Function(
							v[m[k][k] - 1][m[i + 1][k] - 1],
							v[m[i][k] - 1][m[i + 1][k] - 1]);
				}
			}
		}

		return new DenseInstance(1.0, x);
	}

	/**
	 * Creates a completely random sampled instance. <br>
	 * It used the createSample-function with a default x-array, which are all
	 * flagged as not given.
	 * 
	 * @return returns a random sampled instance.
	 */
	public Instance createRandomSample() {
		int n = rvine.length + 1;
		return createSample(new double[n], new boolean[n]);
	}

	/**
	 * The log-likelihood for a given instance. <br>
	 * The method is based on an algorithm presented in J.F. Di&szlig;mann's
	 * diploma thesis.
	 * 
	 * @param inst
	 *            observation instance.
	 * @return returns the log-likelihood for the instance.
	 */
	public double logDensity(Instance inst) {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return 0;
		}
		double[] x = inst.toDoubleArray();
		double loglik = 0;
		int n = m.length;
		double[][] v = new double[n][n];
		Copula c;

		for (int k = n - 2; k >= 0; k--) {
			// one dimensional transformed values
			c = edges[n - 1][k].getCopula();

			if (m[k][k] > m[n - 1][k]) {
				v[m[k][k] - 1][m[n - 1][k] - 1] = c.h1Function(
						x[m[n - 1][k] - 1], x[m[k][k] - 1]);
				v[m[n - 1][k] - 1][m[k][k] - 1] = c.h2Function(
						x[m[n - 1][k] - 1], x[m[k][k] - 1]);
				loglik += Math.log(c
						.density(x[m[n - 1][k] - 1], x[m[k][k] - 1]));
			} else {
				v[m[k][k] - 1][m[n - 1][k] - 1] = c.h2Function(x[m[k][k] - 1],
						x[m[n - 1][k] - 1]);
				v[m[n - 1][k] - 1][m[k][k] - 1] = c.h1Function(x[m[k][k] - 1],
						x[m[n - 1][k] - 1]);
				loglik += Math.log(c
						.density(x[m[k][k] - 1], x[m[n - 1][k] - 1]));
			}

			for (int i = n - 2; i > k; i--) {
				// run path up to generate transformed values
				c = edges[i][k].getCopula();

				if (m[k][k] > m[i][k]) {
					v[m[k][k] - 1][m[i][k] - 1] = c.h1Function(
							v[m[i][k] - 1][m[i + 1][k] - 1],
							v[m[k][k] - 1][m[i + 1][k] - 1]);
					v[m[i][k] - 1][m[k][k] - 1] = c.h2Function(
							v[m[i][k] - 1][m[i + 1][k] - 1],
							v[m[k][k] - 1][m[i + 1][k] - 1]);
					loglik += Math.log(c.density(
							v[m[i][k] - 1][m[i + 1][k] - 1],
							v[m[k][k] - 1][m[i + 1][k] - 1]));
				} else {
					v[m[k][k] - 1][m[i][k] - 1] = c.h2Function(
							v[m[k][k] - 1][m[i + 1][k] - 1],
							v[m[i][k] - 1][m[i + 1][k] - 1]);
					v[m[i][k] - 1][m[k][k] - 1] = c.h1Function(
							v[m[k][k] - 1][m[i + 1][k] - 1],
							v[m[i][k] - 1][m[i + 1][k] - 1]);
					loglik += Math.log(c.density(
							v[m[k][k] - 1][m[i + 1][k] - 1],
							v[m[i][k] - 1][m[i + 1][k] - 1]));
				}
			}
		}

		return loglik;
	}

	/**
	 * The log-likelihood for a several instances. <br>
	 * It calculates the sum of log-likelihoods of every instance using the
	 * log-likelihood function for single instances.
	 * 
	 * @param data
	 *            matrix of observations.
	 * @return returns the log-likelihood for the instances.
	 */
	public double logDensity(Instances data) {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return 0;
		}
		double loglik = 0;

		for (int j = 0; j < data.size(); j++) {
			Instance x = data.get(j);
			loglik += logDensity(x);
		}
		return loglik;
	}

	/**
	 * Creates the RVine-Matrix stored in a global variable m. <br>
	 * See the RVine-Matrix creation algorithm presented in J.F. Di&szlig;mann's
	 * diploma thesis.
	 */
	public void createRVineMatrix() {
		int n = rvine.length + 1;
		m = new int[n][n];
		edges = new Edge[n][n];

		TreeSet<Integer> B = new TreeSet<Integer>();
		TreeSet<Integer> items = new TreeSet<Integer>();

		// create an all items set
		for (int i = 1; i <= n; i++) {
			items.add(i);
		}

		// Create CV, Set of constraint sets of RVine
		@SuppressWarnings("unchecked")
		ArrayList<Edge>[] CV = new ArrayList[n - 1];

		for (int i = 0; i < n - 1; i++) {
			CV[i] = new ArrayList<Edge>();
			CV[i].addAll(rvine[i].getUndirectedEdgeList());
		}
		// CV creation completed

		// Matrix creation loop
		for (int i = 1; i < n; i++) {
			Edge es = CV[n - 1 - i].get(0);
			ArrayList<Integer> x = createConditionedSet(es.getFrom(),
					es.getTo());
			CV[n - 1 - i].remove(x);

			int xl = x.get(0);
			int xr = x.get(1);

			m[i - 1][i - 1] = xl;
			m[i][i - 1] = xr;
			edges[i][i - 1] = es;

			for (int k = i + 2; k <= n; k++) {
				Iterator<Edge> it = CV[n - k].iterator();
				Edge e = it.next();
				ArrayList<Integer> x2 = createConditionedSet(e.getFrom(),
						e.getTo());
				while (!x2.contains(xl)) {
					e = it.next();
					x2 = createConditionedSet(e.getFrom(), e.getTo());
				}
				CV[n - k].remove(e);

				int xs = x2.get(0) == xl ? x2.get(1) : x2.get(0);

				m[k - 1][i - 1] = xs;
				edges[k - 1][i - 1] = e;
			}
			B.add(xl);
		}
		items.removeAll(B);
		int x = items.first();
		m[n - 1][n - 1] = x;
	}

	/**
	 * Weights the edge by Kendall's tau.
	 * 
	 * @param e
	 *            Edge to be weighted.
	 */
	private void kendallWeight(Edge e) {
		double[] a, b;

		// get the corresponding data from a merged Node
		int val1 = createConditionedSet(e.getFrom(), e.getTo()).get(0);
		a = e.getFrom().getData(val1);
		if (a == null) {
			a = e.getTo().getData(val1);
		}

		// get the corresponding data from the other merged Node
		int val2 = createConditionedSet(e.getFrom(), e.getTo()).get(1);
		b = e.getFrom().getData(val2);
		if (b == null) {
			b = e.getTo().getData(val2);
		}

		// set the edge weight
		e.setWeight(Math.abs(VineUtils.kendallsTau(a, b)));
	}

	/**
	 * Fits Copula to the Edge e, actually using MLE method.
	 * 
	 * @param e
	 *            Edge to be fitted to.
	 * @param selected
	 *            Copula selection array.
	 */
	private void fitCopula(Edge e, boolean[] selected) {
		Copula[] copSet = ch.select(selected);
		double[] lls = new double[copSet.length];
		double[] a, b;

		// get the corresponding data from a merged Node
		int val1 = createConditionedSet(e.getFrom(), e.getTo()).get(0);
		a = e.getFrom().getData(val1);
		if (a == null) {
			a = e.getTo().getData(val1);
		}

		// get the corresponding data from the other merged Node
		int val2 = createConditionedSet(e.getFrom(), e.getTo()).get(1);
		b = e.getFrom().getData(val2);
		if (b == null) {
			b = e.getTo().getData(val2);
		}

		for (int i = 0; i < copSet.length; i++) {
			Copula c = copSet[i];
			lls[i] = c.mle(a, b);
		}

		int out = 0;
		for (int i = 1; i < copSet.length; i++) {
			if (lls[out] < lls[i])
				out = i;
		}

		e.setCopula(copSet[out]);
		e.setLogLik(lls[out]);
	}

	/**
	 * Fits Copula to the Edge e using CV method.
	 * 
	 * @param e
	 *            Edge to be fitted to.
	 * @param selected
	 *            Copula selection array.
	 */
	private void cvFitCopula(Edge e, boolean[] selected) {
		Copula[] copSet = ch.select(selected);
		Copula[] bestCops = ch.select(selected);
		double[] lls = new double[copSet.length];
		double[] a, b;

		// initialize lls
		for (int i = 0; i < lls.length; i++) {
			lls[i] = Double.NEGATIVE_INFINITY;
		}

		// get the corresponding data from a merged Node
		int val1 = createConditionedSet(e.getFrom(), e.getTo()).get(0);
		a = e.getFrom().getData(val1);
		if (a == null) {
			a = e.getTo().getData(val1);
		}

		// get the corresponding data from the other merged Node
		int val2 = createConditionedSet(e.getFrom(), e.getTo()).get(1);
		b = e.getFrom().getData(val2);
		if (b == null) {
			b = e.getTo().getData(val2);
		}

		// cvFold - Cross Validation
		int foldSize = (int) Math.floor(((double) a.length) / cvFolds);
		int[] foldSizes = new int[cvFolds];
		for (int i = 0; i < cvFolds; i++) {
			if (i < a.length % cvFolds) {
				foldSizes[i] = foldSize + 1;
			} else {
				foldSizes[i] = foldSize;
			}
		}

		for (int i = 0; i < cvFolds; i++) {
			int testSize = foldSizes[i];

			// build CV train / validation sets

			double[] newA = new double[a.length - testSize];
			double[] newB = new double[b.length - testSize];

			double[] testA = new double[testSize];
			double[] testB = new double[testSize];

			int k2 = 0;
			int ub = foldSizes[k2];
			int lb = 0;

			for (int en = 0; en < a.length; en++) {
				if (en == ub) {
					k2++;
					ub += foldSizes[k2];
					lb = en;
				}

				if (k2 == i) {
					testA[en - lb] = a[en];
					testB[en - lb] = b[en];
				}
				if (k2 < i) {
					newA[en] = a[en];
					newB[en] = b[en];
				}
				if (k2 > i) {
					newA[en - testSize] = a[en];
					newB[en - testSize] = b[en];
				}
			}

			// train with MLE and validate

			for (int k = 0; k < copSet.length; k++) {
				Copula c = copSet[k];
				c.mle(newA, newB);

				double ll = 0;
				for (int t = 0; t < testSize; t++) {
					ll += Math.log(c.density(testA[t], testB[t]));
				}

				if (ll > lls[k]) {
					bestCops[k].setParams(c.getParams());
					lls[k] = ll;
				}
			}
		}

		int out = 0;
		for (int i = 1; i < copSet.length; i++) {
			if (lls[out] < lls[i])
				out = i;
		}

		e.setCopula(bestCops[out]);
		e.setLogLik(lls[out]);
		if (trainMethod == TrainMethod.CV) {
			e.setWeight(lls[out]);
		}
	}

	/**
	 * Merges two Nodes of a given Edge to create the Node for the next
	 * dimension.
	 * 
	 * @param e
	 *            the Edge from which the new Node is created.
	 * @return the merged Node.
	 */
	private Node mergeNodes(Edge e) {
		// get the Node label
		Node out = createConstraintSet(e);
		double[] a, b;

		// get the corresponding data from a merged Node
		int val1 = out.getCondSet().get(0);
		a = e.getFrom().getData(val1);
		if (a == null) {
			a = e.getTo().getData(val1);
		}

		// get the corresponding data from the other merged Node
		int val2 = out.getCondSet().get(1);
		b = e.getFrom().getData(val2);
		if (b == null) {
			b = e.getTo().getData(val2);
		}

		// transform the data / create pseudo observations
		double[] a_new = transformData2(e.getCopula(), a, b);
		double[] b_new = transformData1(e.getCopula(), a, b);

		out.putData(val1, a_new);
		out.putData(val2, b_new);
		out.setMergedFrom(e);

		return out;
	}

	/**
	 * Creates pseudo observations from two observation arrays using the copula
	 * h-function. <br>
	 * array a and array b need to have the same size.
	 * 
	 * @param c
	 *            the copula from which the h-function is used.
	 * @param a
	 *            an array of observations.
	 * @param b
	 *            another array of observations.
	 * 
	 * @return the pseudo observations, the values are constraint b|a.
	 */
	private double[] transformData1(Copula c, double[] a, double[] b) {
		// assert that a and b have the same size
		if (!(a.length == b.length)) {
			return null;
		}

		double[] out = new double[a.length];

		// use the copula h-function to create pseudo observations
		for (int i = 0; i < a.length; i++) {
			out[i] = c.h1Function(a[i], b[i]);
		}

		return out;
	}

	/**
	 * Creates pseudo observations from two observation arrays using the copula
	 * h-function. <br>
	 * array a and array b need to have the same size.
	 * 
	 * @param c
	 *            the copula from which the h-function is used.
	 * @param a
	 *            an array of observations.
	 * @param b
	 *            another array of observations.
	 * 
	 * @return the pseudo observations, the values are constraint a|b.
	 */
	private double[] transformData2(Copula c, double[] a, double[] b) {
		// assert that a and b have the same size
		if (!(a.length == b.length)) {
			return null;
		}

		double[] out = new double[a.length];

		// use the copula h-function to create pseudo observations
		for (int i = 0; i < a.length; i++) {
			out[i] = c.h2Function(a[i], b[i]);
		}

		return out;
	}

	/**
	 * Creates a Node labeled with the constraint set from an Edge. <br>
	 * See the constraint set definitions presented in J.F. Di&szlig;mann's
	 * diploma thesis.
	 * 
	 * @param e
	 *            the Edge from which the new Node is created.
	 * @return the new Node.
	 */
	private Node createConstraintSet(Edge e) {
		TreeSet<Integer> U_a = new TreeSet<Integer>(e.getFrom().set());
		TreeSet<Integer> U_b = new TreeSet<Integer>(e.getTo().set());

		TreeSet<Integer> D = new TreeSet<Integer>(U_a);
		D.retainAll(U_b);

		TreeSet<Integer> C = new TreeSet<Integer>(U_a);
		C.addAll(U_b);
		C.removeAll(D);

		Node n = new Node(C, D);

		e.setLabel(n.getName());

		return n;
	}

	/**
	 * Creates the conditioned set from two Nodes. <br>
	 * See the constraint set definitions presented in J.F. Di&szlig;mann's
	 * diploma thesis.
	 * 
	 * @param a
	 *            a Node.
	 * @param b
	 *            another Node.
	 * @return conditioned set created by both nodes.
	 */
	private static ArrayList<Integer> createConditionedSet(Node a, Node b) {
		TreeSet<Integer> U_a = new TreeSet<Integer>(a.set());
		TreeSet<Integer> U_b = new TreeSet<Integer>(b.set());
		TreeSet<Integer> D = new TreeSet<Integer>(U_a);
		D.retainAll(U_b);

		TreeSet<Integer> C = new TreeSet<Integer>(U_a);
		C.addAll(U_b);
		C.removeAll(D);
		ArrayList<Integer> Ca = new ArrayList<Integer>(C);
		Collections.sort(Ca);
		return Ca;
	}

	// Printing methods

	/**
	 * Returns the RVine summary as String.
	 * 
	 * @return RVine summary as String
	 */
	public String summary() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return null;
		}
		String out = "";
		out += "Regular Vine Summary :\n";
		// prepare statistics
		HashMap<String, Integer> stats = new HashMap<String, Integer>();

		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				if (edges[i][j] != null) {
					String cop = edges[i][j].getCopula().name();
					if (!stats.containsKey(cop)) {
						stats.put(cop, 1);
					} else {
						stats.put(cop, stats.get(cop) + 1);
					}
				}
			}
		}
		out += "\n";
		out += "Used Copulas : " + "\n";
		for (String cop : stats.keySet())
			out += cop + " : " + stats.get(cop) + "\n";

		out += "\n";
		// print trees
		out += "RVine Trees : \n";
		for (int i = 0; i < rvine.length; i++) {
			out += "Tree " + (i + 1) + " : \n";
			for (Edge e : rvine[i].getUndirectedEdgeList()) {
				Copula c = e.getCopula();
				out += e.getLabel() + " : " + c.name() + "(";

				if (c.getParams().length > 0) {
					out += "pars:{";
					for (int k = 0; k < c.getParams().length - 1; k++) {
						out += round(c.getParams()[k]) + ", ";
					}
					out += round(c.getParams()[c.getParams().length - 1])
							+ "}, ";
				}
				out += "tau:" + round(c.tau()) + ", weight:"
						+ round(e.getWeight()) + ")\n";
			}
			out += "\n";
		}

		return out;
	}

	/**
	 * Prints the RVine summary.
	 */
	public void printSummary() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return;
		}
		System.out.println(summary());
	}

	/**
	 * Returns the pairwise empirical Kendall's tau matrix concerned to the
	 * RVine matrix.
	 * 
	 * @return pairwise emp tau matrix.
	 */
	public String[][] getEmpTauMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return null;
		}

		String[][] out = new String[edges.length][edges.length];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				out[i][j] = " - ";
				if (edges[i][j] != null) {
					double val = round(edges[i][j].getWeight());
					if ((int) val == val) {
						out[i][j] = Integer.toString((int) val);
					} else {
						out[i][j] = String.valueOf(val);
					}
				}
			}
		}
		return out;
	}

	/**
	 * Prints the pairwise empirical Kendall's tau matrix concerned to the RVine
	 * matrix.
	 */
	public void printEmpTauMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return;
		}

		System.out.println("Empirical Tau - Matrix");
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				String out = " - ";
				if (edges[i][j] != null) {
					double val = round(edges[i][j].getWeight());
					if ((int) val == val) {
						out = Integer.toString((int) val);
					} else {
						out = String.valueOf(val);
					}
				}
				if (j < edges.length - 1) {
					System.out.print(out + "\t&\t");
				} else {
					System.out.print(out + "\\\\");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Returns the pairwise Kendall's tau matrix concerned to the RVine matrix.
	 * 
	 * @return pairwise tau matrix.
	 */
	public String[][] getTauMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return null;
		}

		String[][] out = new String[edges.length][edges.length];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				out[i][j] = " - ";
				if (edges[i][j] != null) {
					double val = round(edges[i][j].getCopula().tau());
					if ((int) val == val) {
						out[i][j] = Integer.toString((int) val);
					} else {
						out[i][j] = String.valueOf(val);
					}
				}
			}
		}
		return out;
	}

	/**
	 * Prints the pairwise Kendall's tau matrix concerned to the RVine matrix.
	 */
	public void printTauMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return;
		}

		System.out.println("Tau - Matrix");
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				String out = " - ";
				if (edges[i][j] != null) {
					double val = round(edges[i][j].getCopula().tau());
					if ((int) val == val) {
						out = Integer.toString((int) val);
					} else {
						out = String.valueOf(val);
					}
				}
				if (j < edges.length - 1) {
					System.out.print(out + "\t&\t");
				} else {
					System.out.print(out + "\\\\");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Returns the RVine matrix.
	 * 
	 * @return RVine matrix as string matrix.
	 */
	public String[][] getRVineMatrix2() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return null;
		}

		String[][] out = new String[edges.length][edges.length];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				out[i][j] = " - ";
				if (edges[i][j] != null) {
					out[i][j] = Integer.toString(m[i][j]);
				}
			}
		}
		return out;
	}

	/**
	 * Prints the RVine matrix.
	 */
	public void printRVineMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return;
		}

		int[][] m = getRVineMatrix();

		System.out.println("RVine - Matrix");
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m.length; j++) {
				if (j < m.length - 1) {
					System.out.print(m[i][j] + "\t&\t");
				} else {
					System.out.print(m[i][j] + "\\\\");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Returns the pairwise copula family matrix concerned to the RVine matrix.
	 * 
	 * @return pairwise family matrix.
	 */
	public String[][] getFamilyMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return null;
		}

		String[][] out = new String[edges.length][edges.length];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				out[i][j] = " - ";
				if (edges[i][j] != null) {
					out[i][j] = edges[i][j].getCopula().token();
				}
			}
		}
		return out;
	}

	/**
	 * Prints the pairwise copula family matrix concerned to the RVine matrix.
	 */
	public void printFamilyMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return;
		}

		System.out.println("Family - Matrix");
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				String out = " - ";
				if (edges[i][j] != null)
					out = edges[i][j].getCopula().token();

				if (j < edges.length - 1) {
					System.out.print(out + "\t&\t");
				} else {
					System.out.print(out + "\\\\");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Returns the pairwise copula parameter matrix concerned to the RVine
	 * matrix.
	 * 
	 * @return pairwise parameter matrices.
	 */
	public String[][][] getParMatrices() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return null;
		}

		String[][][] out = new String[2][edges.length][edges.length];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				if (edges[i][j] != null) {
					out[0][i][j] = " - ";
					out[1][i][j] = " - ";

					if (edges[i][j] != null) {
						double[] pars = edges[i][j].getCopula().getParams();
						if (pars != null) {
							double val = round(pars[0]);
							if ((int) val == val) {
								out[0][i][j] = Integer.toString((int) val);
							} else {
								out[0][i][j] = String.valueOf(val);
							}
						}
						if (pars.length == 2) {
							double val = round(pars[1]);
							if ((int) val == val) {
								out[1][i][j] = Integer.toString((int) val);
							} else {
								out[1][i][j] = String.valueOf(val);
							}
						}
					}
				}
			}
		}
		return out;
	}

	/**
	 * Prints the pairwise copula parameter matrix concerned to the RVine
	 * matrix.
	 */
	public void printParameterMatrices() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return;
		}

		System.out.println("Parameter 1 - Matrix");
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				String out = " - ";
				if (edges[i][j] != null) {
					double[] pars = edges[i][j].getCopula().getParams();
					if (pars != null) {
						double val = round(pars[0]);
						if ((int) val == val) {
							out = Integer.toString((int) val);
						} else {
							out = String.valueOf(val);
						}
					}
				}
				if (j < edges.length - 1) {
					System.out.print(out + "\t\t\t&\t");
				} else {
					System.out.print(out + "\\\\");
				}
			}
			System.out.println();
		}

		System.out.println();
		System.out.println();

		System.out.println("Parameter 2 - Matrix");
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				String out = " - ";
				if (edges[i][j] != null) {
					double[] pars = edges[i][j].getCopula().getParams();
					if (pars != null && pars.length >= 2) {
						double val = round(pars[1]);
						if ((int) val == val) {
							out = Integer.toString((int) val);
						} else {
							out = String.valueOf(val);
						}
					}
				}
				if (j < edges.length - 1) {
					System.out.print(out + "\t\t\t&\t");
				} else {
					System.out.print(out + "\\\\");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Returns the pairwise log-likelihood matrix concerned to the RVine matrix.
	 * 
	 * @return pairwise weights matrix.
	 */
	public String[][] getLogliksMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return null;
		}

		String[][] out = new String[edges.length][edges.length];
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				out[i][j] = " - ";
				if (edges[i][j] != null) {
					double val = round(edges[i][j].getLogLik());
					if ((int) val == val) {
						out[i][j] = Integer.toString((int) val);
					} else {
						out[i][j] = String.valueOf(val);
					}
				}
			}
		}
		return out;
	}

	/**
	 * Prints the pairwise log-likelihood matrix concerned to the RVine matrix.
	 */
	public void printLogliksMatrix() {
		if (!built) {
			System.err
					.println("Use estimate(data, w) first to build the estimator!");
			return;
		}

		System.out.println("Pairwise - LogLiks - Matrix");
		for (int i = 0; i < edges.length; i++) {
			for (int j = 0; j < edges.length; j++) {
				String out = " - ";
				if (edges[i][j] != null) {
					double val = round(edges[i][j].getLogLik());
					if ((int) val == val) {
						out = Integer.toString((int) val);
					} else {
						out = String.valueOf(val);
					}
				}
				if (j < edges.length - 1) {
					System.out.print(out + "\t&\t");
				} else {
					System.out.print(out + "\\\\");
				}
			}
			System.out.println();
		}
	}

	/**
	 * Rounding function for better readability to the output.
	 * 
	 * @param val
	 *            Value to be rounded.
	 * @return the rounded value.
	 */
	private static double round(double val) {
		return Math.round(val * Math.pow(10, 4)) / Math.pow(10, 4);
	}

	// Getter and Setter

	/**
	 * Get the RVine-Matrix.
	 * 
	 * @return returns the RVine-Matrix.
	 */
	public int[][] getRVineMatrix() {
		return m;
	}

	/**
	 * Get the loaded copulas.
	 * 
	 * @return returns the loaded copulas as String array.
	 */
	public Copula[] getLoadedCopulas() {
		return ch.loadedCopulas();
	}

	/**
	 * Get the Edge-Matrix.
	 * 
	 * @return returns the Edge-Matrix.
	 */
	public Edge[][] getEdgeMatrix() {
		return edges;
	}

	/**
	 * Get the RVine.
	 * 
	 * @return returns the RVine as List of Graphs.
	 */
	public Graph[] getRegularVine() {
		return rvine;
	}

	/**
	 * Get the Copula selection array.
	 * 
	 * @return returns the Copula selection array.
	 */
	public boolean[] getSelected() {
		return selected;
	}

	// Options

	@OptionMetadata(
			displayName = "Print Help ",
			description = "Prints all the options",
			commandLineParamName = "help",
			commandLineParamSynopsis = "-help",
			commandLineParamIsFlag = true,
			displayOrder = 1)
	@ProgrammaticProperty
	public void setHelp(boolean help) {
		this.help = help;
	}

	public boolean getHelp() {
		return help;
	}

	@OptionMetadata(
			displayName = "Print Loaded Copulas",
			description = "Prints all loaded and ready-to-use copulas",
			commandLineParamName = "copulas",
			commandLineParamSynopsis = "-copulas",
			commandLineParamIsFlag = true,
			displayOrder = 2)
	@ProgrammaticProperty
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public boolean getLoaded() {
		return loaded;
	}

	@OptionMetadata(
			displayName = "Use Filepath",
			description = "Set the path to arff file (required).",
			commandLineParamName = "fp",
			commandLineParamSynopsis = "-fp <string>",
			commandLineParamIsFlag = false,
			displayOrder = 3)
	@ProgrammaticProperty
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getFilepath() {
		return filepath;
	}

	@OptionMetadata(displayName = "Copula Selection",
			description = "Pass copulas to use for RVine construction"
					+ "by comma-separated indices (default = all).",
			commandLineParamName = "copulaSelection",
			commandLineParamSynopsis = "-copulaSelection <string>",
			commandLineParamIsFlag = false,
			displayOrder = 4)
	@ProgrammaticProperty
	public void setCopulaSelection(String sel) {
		String[] sels = sel.split(",");
		int[] cop = new int[sels.length];
		for (int i = 0; i < sels.length; i++) {
			cop[i] = Integer.parseInt(sels[i].trim());
			if (cop[i] < 0 || cop[i] >= selected.length) {
				System.err.println("Failed to pass on options!");
			}
		}

		// pass on options if everything is correct
		for (int i = 0; i < selected.length; i++)
			selected[i] = false;
		for (int i = 0; i < cop.length; i++) {
			selected[cop[i]] = true;
		}
	}

	public String getCopulaSelection() {
		String out = "";
		boolean first = true;
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				if (first) {
					out += i;
					first = false;
				} else {
					out += "," + i;
				}
			}
		}
		return out;
	}

	@OptionMetadata(
			displayName = "Train Method",
			description = "Choose between Kendall, CV and Mixed.",
			commandLineParamName = "tm",
			commandLineParamSynopsis = "-tm",
			commandLineParamIsFlag = false,
			displayOrder = 5)
	public void setTrainMethod(TrainMethod tm) {
		this.trainMethod = tm;
	}

	public TrainMethod getTrainMethod() {
		return trainMethod;
	}

	@OptionMetadata(
			displayName = "Build Method",
			description = "Choose between Regular,"
					+ " Scattered_Indep and Threshold.",
			commandLineParamName = "bm",
			commandLineParamSynopsis = "-bm",
			commandLineParamIsFlag = false,
			displayOrder = 6)
	public void setBuildMethod(BuildMethod bm) {
		this.buildMethod = bm;
	}

	public BuildMethod getBuildMethod() {
		return buildMethod;
	}

	@OptionMetadata(
			displayName = "Threshold parameter",
			description = "Parameter for the threshold build method.",
			commandLineParamName = "t",
			commandLineParamSynopsis = "-t",
			commandLineParamIsFlag = false,
			displayOrder = 7)
	public void setThreshold(double t) {
		this.threshold = t;
	}

	public double getThreshold() {
		return threshold;
	}

	@OptionMetadata(
			displayName = "CV fold parameter",
			description = "Parameter for the amount of cv folds.",
			commandLineParamName = "cvfolds",
			commandLineParamSynopsis = "-cvfolds",
			commandLineParamIsFlag = false,
			displayOrder = 8)
	public void setCVFolds(int f) {
		if (f > 0)
			this.cvFolds = f;
	}

	public int getCVFolds() {
		return cvFolds;
	}

	@OptionMetadata(
			displayName = "Print Summary",
			description = "Print the RVine summary.",
			commandLineParamName = "sum",
			commandLineParamSynopsis = "-sum",
			commandLineParamIsFlag = true,
			displayOrder = 9)
	@ProgrammaticProperty
	public void setSum(boolean sum) {
		this.sum = sum;
	}

	public boolean getSum() {
		return sum;
	}

	@OptionMetadata(
			displayName = "Print RVine matrix",
			description = "Print the RVine matrix.",
			commandLineParamName = "rvm",
			commandLineParamSynopsis = "-rvm",
			commandLineParamIsFlag = true,
			displayOrder = 10)
	@ProgrammaticProperty
	public void setRVM(boolean rvm) {
		this.rvm = rvm;
	}

	public boolean getRVM() {
		return rvm;
	}

	@OptionMetadata(
			displayName = "Print Families matrix",
			description = "Print the RVine families matrix.",
			commandLineParamName = "fam",
			commandLineParamSynopsis = "-fam",
			commandLineParamIsFlag = true,
			displayOrder = 11)
	@ProgrammaticProperty
	public void setFAM(boolean fam) {
		this.fam = fam;
	}

	public boolean getFAM() {
		return fam;
	}

	@OptionMetadata(
			displayName = "Print Parameters matrices",
			description = "Print the RVine parameter matrices.",
			commandLineParamName = "parm",
			commandLineParamSynopsis = "-parm",
			commandLineParamIsFlag = true,
			displayOrder = 12)
	@ProgrammaticProperty
	public void setPARM(boolean parm) {
		this.parm = parm;
	}

	public boolean getPARM() {
		return parm;
	}

	@OptionMetadata(
			displayName = "Print Pair-LogLiks matrix",
			description = "Print the RVine Pair-Log-Likelihoods matrix.",
			commandLineParamName = "pllm",
			commandLineParamSynopsis = "-pllm",
			commandLineParamIsFlag = true,
			displayOrder = 13)
	@ProgrammaticProperty
	public void setPLLM(boolean pllm) {
		this.pllm = pllm;
	}

	public boolean getPLLM() {
		return pllm;
	}

	@OptionMetadata(
			displayName = "Print Kendall's tau matrix",
			description = "Print the RVine Kendall's tau matrix.",
			commandLineParamName = "taum",
			commandLineParamSynopsis = "-taum",
			commandLineParamIsFlag = true,
			displayOrder = 14)
	@ProgrammaticProperty
	public void setTAUM(boolean taum) {
		this.taum = taum;
	}

	public boolean getTAUM() {
		return taum;
	}

	@OptionMetadata(
			displayName = "Print Empirical Kendall's tau matrix",
			description = "Print the RVine empirical Kendall's tau matrix.",
			commandLineParamName = "etaum",
			commandLineParamSynopsis = "-etaum",
			commandLineParamIsFlag = true,
			displayOrder = 15)
	@ProgrammaticProperty
	public void setETAUM(boolean etaum) {
		this.etaum = etaum;
	}

	public boolean getETAUM() {
		return etaum;
	}

	@OptionMetadata(
			displayName = "Print timestamps",
			description = "Print timestamps during RVine construction.",
			commandLineParamName = "times",
			commandLineParamSynopsis = "-times",
			commandLineParamIsFlag = true,
			displayOrder = 16)
	@ProgrammaticProperty
	public void setTimestamps(boolean timestamps) {
		this.timestamps = timestamps;
	}

	public boolean getTimestamps() {
		return timestamps;
	}

	// OptionHandler

	/**
	 * Returns an enumeration describing the available options.
	 *
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		return Option.listOptionsForClass(this.getClass()).elements();
	}

	/**
	 * Gets the current settings of the Classifier.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	@Override
	public String[] getOptions() {
		return Option.getOptions(this, this.getClass());
	}

	/**
	 * Parses a given list of options.
	 *
	 * @param options
	 *            the list of options as an array of strings
	 * @exception Exception
	 *                if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {
		Option.setOptions(options, this, this.getClass());
	}

	// CommandLineRunnable

	@Override
	public void postExecution() throws Exception {
	}

	@Override
	public void preExecution() throws Exception {
	}

	@Override
	public void run(Object toRun, String[] options) throws Exception {
		if (!(toRun instanceof RegularVine)) {
			throw new IllegalArgumentException("Object to run is not a RVine!");
		}
		RegularVine rvine = (RegularVine) toRun;
		rvine.setOptions(options);

		if (help || options.length == 0) {
			for (Enumeration<Option> e = rvine.listOptions(); e
					.hasMoreElements();) {
				Option o = e.nextElement();
				System.out.println(o.synopsis() + " " + o.description());
			}
			return;
		}

		if (loaded) {
			Copula[] copulas = getLoadedCopulas();
			for (int i = 0; i < copulas.length; i++) {
				System.out.println(i + " - " + copulas[i].name());
			}
			return;
		}

		if (filepath.equals("")) {
			System.err
					.println("No source file specified! Use the fp"
							+ "command to pass a filepath.");
			return;
		}

		Instances data = new Instances(new BufferedReader(new FileReader(
				filepath)));
		rvine.buildEstimator(data);

		if (sum) {
			rvine.printSummary();
			System.out.println();
			System.out.println();
		}

		if (rvm) {
			rvine.printRVineMatrix();
			System.out.println();
			System.out.println();
		}

		if (fam) {
			rvine.printFamilyMatrix();
			System.out.println();
			System.out.println();
		}

		if (parm) {
			rvine.printParameterMatrices();
			System.out.println();
			System.out.println();
		}

		if (pllm) {
			rvine.printLogliksMatrix();
			System.out.println();
			System.out.println();
		}

		if (taum) {
			rvine.printTauMatrix();
			System.out.println();
			System.out.println();
		}

		if (etaum) {
			rvine.printEmpTauMatrix();
		}
	}
}
