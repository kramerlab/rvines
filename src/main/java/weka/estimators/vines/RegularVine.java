package weka.estimators.vines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import weka.core.Instances;
import weka.core.Option;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.OptionHandler;
import weka.estimators.MultivariateEstimator;
import weka.estimators.vines.copulas.Copula;

/**
 * This class represents the Regular Vine.
 * <br>
 * It is the main class of the vines package.
 * <br>
 * It uses the Graph class to store the Graphs for its dimensions.
 * <br>
 * It can be used to perform sampling (simulation) and log-density (pseudo) computation.
 * <br>
 * The implementation is based on J.F. Di&szlig;mann's diploma thesis (2010):
 * Statistical inference for regular vines and application.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class RegularVine implements MultivariateEstimator, OptionHandler {
	private boolean[] selected = new boolean[]{true, true, true, true, true, true, true, true};
	private CopulaHandler ch = new CopulaHandler();
	private Graph[] rvine;
	private int[][] m;
	private Edge[][] edges;
	private double[][] data;
	private boolean built = false;
	private boolean timestamps = false;
	
	public static void main(String[] args){
		RegularVine rvine = new RegularVine();
		double[][] data = loadData("src/main/data/random/random1.arff");
		if(data == null) return;
		
		double[] w = new double[data.length];
		for(int i=0; i<w.length; i++){
			w[i] = 1;
		}
		
		rvine.estimate(data, w);

		rvine.printSummary();
		
		System.out.println();
		System.out.println();
		
		rvine.printRVineMatrix();
		
		System.out.println();
		System.out.println();
		
		rvine.printFamilyMatrix();
		
		System.out.println();
		System.out.println();
		
		rvine.printParameterMatrices();
		
		System.out.println();
		System.out.println();
		
		rvine.printLogliksMatrix();
		
		System.out.println();
		System.out.println();
		
		rvine.printTauMatrix();
		
		System.out.println();
		System.out.println();
		
		rvine.printEmpTauMatrix();
	}
	
	public void printSummary(){
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return;
		}
		System.out.println("Regular Vine Summary :");
		System.out.println("Log-Likelihood : "+logDensity(data));
		// prepare statistics
		HashMap<String, Integer> stats = new HashMap<String, Integer>();
		
		for(int i=0;i<edges.length;i++){
			for(int j=0;j<edges.length;j++){
				if(edges[i][j] != null){
					String cop = edges[i][j].getCopula().name();
					if(!stats.containsKey(cop)){
						stats.put(cop, 1);
					}else{
						stats.put(cop, stats.get(cop)+1);
					}
				}
			}
		}
		System.out.println("Used Copulas : ");
		for(String cop : stats.keySet())
			System.out.println(cop+" : "+stats.get(cop));
		
		System.out.println();
		// print trees
		System.out.println("RVine Trees : ");
		for(int i=0; i<rvine.length; i++){
			System.out.println("Tree "+(i+1)+" : ");
			for(Edge e : rvine[i].getUndirectedEdgeList()){
				Copula c = e.getCopula();
				if(c.getParams() != null){
					System.out.print(e.getLabel()+" : "+c.name()+"(pars:{");
					for(int k=0; k<c.getParams().length-1; k++){
						System.out.print(round(c.getParams()[k])+",");
					}
					System.out.print(round(c.getParams()[c.getParams().length-1])+"},");
				}
				System.out.println("tau:"+round(c.tau())+", empTau:"+round(e.getWeight())+")");
			}
			System.out.println();
		}
	}
	
	private void printEmpTauMatrix() {
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return;
		}
		
		System.out.println("Empirical Tau - Matrix");
		for(int i=0;i<edges.length;i++){
			for(int j=0;j<edges.length;j++){
				String out = " - ";
				if(edges[i][j] != null){
					double val = round(edges[i][j].getWeight());
					if( (int) val == val){
						out = Integer.toString( (int) val);
					}else{
						out = String.valueOf(val);
					}
				}
				if(j<edges.length-1){
					System.out.print(out+"\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
	}

	private void printTauMatrix() {
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return;
		}
		
		System.out.println("Tau - Matrix");
		for(int i=0;i<edges.length;i++){
			for(int j=0;j<edges.length;j++){
				String out = " - ";
				if(edges[i][j] != null){
					double val = round(edges[i][j].getCopula().tau());
					if( (int) val == val){
						out = Integer.toString( (int) val);
					}else{
						out = String.valueOf(val);
					}
				}
				if(j<edges.length-1){
					System.out.print(out+"\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
	}
	
	public void printRVineMatrix(){
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return;
		}
		
		int[][] m = getRVineMatrix();
		
		System.out.println("RVine - Matrix");
		for(int i=0;i<m.length;i++){
			for(int j=0;j<m.length;j++){
				if(j < m.length-1){
					System.out.print(m[i][j]+"\t&\t");
				}else{
					System.out.print(m[i][j]+"\\\\");
				}
			}
			System.out.println();
		}
	}
	
	public void printFamilyMatrix(){
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return;
		}
		
		System.out.println("Family - Matrix");
		for(int i=0;i<edges.length;i++){
			for(int j=0;j<edges.length;j++){
				String out = " - ";
				if(edges[i][j] != null) out = edges[i][j].getCopula().token();
				
				if(j<edges.length-1){
					System.out.print(out+"\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
	}
	
	public void printParameterMatrices(){
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return;
		}
		
		System.out.println("Parameter 1 - Matrix");
		for(int i=0;i<edges.length;i++){
			for(int j=0;j<edges.length;j++){
				String out = " - ";
				if(edges[i][j] != null){
					double[] pars = edges[i][j].getCopula().getParams();
					if(pars != null){
						double val = round(pars[0]);
						if( (int) val == val){
							out = Integer.toString( (int) val);
						}else{
							out = String.valueOf(val);
						}
					}
				}
				if(j<edges.length-1){
					System.out.print(out+"\t\t\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		
		System.out.println("Parameter 2 - Matrix");
		for(int i=0;i<edges.length;i++){
			for(int j=0;j<edges.length;j++){
				String out = " - ";
				if(edges[i][j] != null){
					double[] pars = edges[i][j].getCopula().getParams();
					if(pars != null && pars.length >= 2){
						double val = round(pars[1]);
						if( (int) val == val){
							out = Integer.toString( (int) val);
						}else{
							out = String.valueOf(val);
						}
					}
				}
				if(j<edges.length-1){
					System.out.print(out+"\t\t\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
	}
	
	public void printLogliksMatrix(){
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return;
		}
		
		System.out.println("Pairwise - LogLiks - Matrix");
		for(int i=0;i<edges.length;i++){
			for(int j=0;j<edges.length;j++){
				String out = " - ";
				if(edges[i][j] != null){
					double val = round(edges[i][j].getLogLik());
					if( (int) val == val){
						out = Integer.toString( (int) val);
					}else{
						out = String.valueOf(val);
					}
				}
				if(j<edges.length-1){
					System.out.print(out+"\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
	}
	
	private static boolean testData(double[][] data){
		for(int i=0; i<data.length; i++){
			for(int j=0; j<data[i].length; j++){
				if(data[i][j] < 0 || data[i][j] > 1) return false; 
			}
		}
		return true;
	}
	
	public static double[][] loadData(String path){
		double[][] data;
		try{
			DataSource source = new DataSource(path);
			Instances instances = source.getDataSet();
			
			int k = instances.numAttributes();
			
			data = new double[k][];
			
			for(int i=0; i<k; i++){
				data[i] = instances.attributeToDoubleArray(i);
			}
			
			if(!testData(data)){
				System.err.println("Data does not fit the [0,1] interval!");
				return null;
			}
			
			return data;
		} catch (Exception e) {
			System.err.println("Unable to load data!");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Build the Regular Vine density estimator on the given data set and weights.
	 * <br>
	 * See the model selection algorithm presented
	 * in J.F. Di&szlig;mann's diploma thesis.
	 * 
	 * @param data the training data set to build the estimator on.
	 * @param w the attribute weights.
	 */
	@Override
	public void estimate(double[][] data, double[] w) {
		rvine = new Graph[data.length-1];
		this.data = data;
		Graph g = new Graph();
		Graph gNext = new Graph();
		
		double start = System.currentTimeMillis();
		double stamp = start;
		
		if(timestamps){
			System.out.println("Building T1 ...");
			System.out.print("\t Compute Kendall's tau... ");
		}
		
		// initialize nodes
		for(int i=1;i<=data.length;i++){
			Node n = new Node(i);
			n.putData(i, data[i-1]);
			g.addNode(n);
		}
		
		// initialize edges
		for(Node i : g.getNodeList()){
			for(Node j : g.getNodeList()){
				if(i != j){;
					Edge e = new Edge(i, j, 0);
					kendallWeight(e);
					g.addEdge(e);
				}
			}
		}
		
		if(timestamps){
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ "+(time-stamp)+"ms");
			System.out.print("\t Compute max. spanning tree... ");
			stamp = time;
		}
		
		// calculate maximal spanning tree of graph
		g = Utils.maxSpanTree(g);
		
		if(timestamps){
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ "+(time-stamp)+"ms");
			System.out.print("\t Compute fitting Copulae... ");
			stamp = time;
		}
		
		// fit copulas to the edges
		for(Edge e : g.getUndirectedEdgeList()){
			fitCopula(e, selected);
		}
		
		//add graph to rvine
		rvine[0] = g;
		
		//until regular vine is fully specified, do:
		for(int count = 1; count < data.length-1; count++){
			if(timestamps){
				double time = System.currentTimeMillis();
				System.out.println("finished! ~ "+(time-stamp)+"ms");
				System.out.println("Building T"+(count+1)+"... ");
				System.out.print("\t Merge Nodes... ");
				stamp = time;
			}
			
			//prepare next graph
			gNext = new Graph();
			
			//for all edges of MST, do
			for(Edge e : g.getUndirectedEdgeList()){
				gNext.addNode(mergeNodes(e));
			}
			
			if(timestamps){
				double time = System.currentTimeMillis();
				System.out.println("finished! ~ "+(time-stamp)+"ms");
				System.out.print("\t Compute Kendall's tau... ");
				stamp = time;
			}
			
			//calculate kendall's tau and add edges to graph,
			//for all possible edges (proximity condition)
			for(Node i : gNext.getNodeList()){
				for(Node j : gNext.getNodeList()){
					if(i != j){
						if(i.isIntersected(j)){
							Edge e = new Edge(i, j, 0);
							kendallWeight(e);
							gNext.addEdge(e);
						}
					}
				}
			}
			
			if(timestamps){
				double time = System.currentTimeMillis();
				System.out.println("finished! ~ "+(time-stamp)+"ms");
				System.out.print("\t Compute max. spanning tree... ");
				stamp = time;
			}
			
			//calculate maximal spanning tree of graph
			gNext = Utils.maxSpanTree(gNext);
			
			if(timestamps){
				double time = System.currentTimeMillis();
				System.out.println("finished! ~ "+(time-stamp)+"ms");
				System.out.print("\t Compute fitting Copulae... ");
				stamp = time;
			}
			
			// fit copulas to the edges
			for(Edge e : gNext.getUndirectedEdgeList()){
				fitCopula(e, selected);
			}
			
			//add graph to rvine
			rvine[count] = gNext;
			g = gNext;
		}
		
		// Use merge only to set last Edge label
		for(Edge e : g.getUndirectedEdgeList()){
			mergeNodes(e);
		}
		
		if(timestamps){
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ "+(time-stamp)+"ms");
			System.out.print("Building Matrices... ");
			stamp = time;
		}
		
		createRVineMatrix();
		built = true;
		
		if(timestamps){
			double time = System.currentTimeMillis();
			System.out.println("finished! ~ "+(time-stamp)+"ms");
			System.out.println("Total time: "+(time-start)+"ms");
			System.out.println();
		}
	}

	/**
	 * The log-likelihood for a given instance.
	 * <br>
	 * The method is based on an
	 * algorithm presented in J.F. Di&szlig;mann's diploma thesis.
	 * @param instance the instance.
	 * @return returns the log-likelihood for the instance.
	 */
	public double logDensity(double[] x){
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return 0;
		}
		double loglik = 0;
		int n = m.length;
		double[][] v = new double[n][n];
		Copula c;
		
		for(int k=n-2;k>=0;k--){
			//one dimensional transformed values
			c = edges[n-1][k].getCopula();
			v[m[k][k]-1][m[n-1][k]-1] = c.h2Function(x[m[k][k]-1], x[m[n-1][k]-1]);
			v[m[n-1][k]-1][m[k][k]-1] = c.h1Function(x[m[k][k]-1], x[m[n-1][k]-1]);
			if(m[k][k] > m[n-1][k]){
				loglik += Math.log(c.density(x[m[n-1][k]-1], x[m[k][k]-1]));
			}else{
				loglik += Math.log(c.density(x[m[k][k]-1], x[m[n-1][k]-1]));
			}
			
			for(int i=n-2;i>k;i--){
				//run path up to generate transformed values
				c = edges[i][k].getCopula();
				v[m[k][k]-1][m[i][k]-1] = c.h2Function(v[m[k][k]-1][m[i+1][k]-1], v[m[i][k]-1][m[i+1][k]-1]);
				v[m[i][k]-1][m[k][k]-1] = c.h1Function(v[m[k][k]-1][m[i+1][k]-1], v[m[i][k]-1][m[i+1][k]-1]);
				
				if(m[k][k] > m[i][k]){
					loglik += Math.log(c.density(v[m[i][k]-1][m[i+1][k]-1], v[m[k][k]-1][m[i+1][k]-1]));
				}else{
					loglik += Math.log(c.density(v[m[k][k]-1][m[i+1][k]-1], v[m[i][k]-1][m[i+1][k]-1]));
				}
			}
		}
		
		return loglik;
	}
	
	/**
	 * The log-likelihood for a several instances.
	 * <br>
	 * It calculates the sum of log-likelihoods of every instance using
	 * the log-likelihood function for single instances.
	 * @param instances the instances.
	 * @return returns the log-likelihood for the instances.
	 */
	public double logDensity(double[][] data){
		if(!built){
			System.err.println("Use estimate(data, w) first to build the estimator!");
			return 0;
		}
		double loglik = 0;
		double[] x = new double[data.length];
		for(int j=0; j<data[0].length; j++){
			for(int i=0; i<data.length; i++){
				x[i] = data[i][j];
			}
			loglik += logDensity(x);
		}
		return loglik;
	}
	
	/**
	 * Get the RVine.
	 * 
	 * @return returns the RVine as List of Graphs.
	 */
	public Graph[] getRegularVine(){
		return rvine;
	}
	
	/**
	 * Creates the RVine-Matrix stored in a global variable m.
	 * <br>
	 * See the RVine-Matrix creation algorithm presented
	 * in J.F. Di&szlig;mann's diploma thesis.
	 */
	private void createRVineMatrix(){
		int n = rvine.length+1;
		m = new int[n][n];
		edges = new Edge[n][n];
		
		TreeSet<Integer> B = new TreeSet<Integer>();
		TreeSet<Integer> items = new TreeSet<Integer>();
		
		//create an all items set
		for(int i=0;i<n;i++){
			items.add(i);
		}
		
		//Create CV, Set of constraint sets of RVine
		@SuppressWarnings("unchecked")
		ArrayList<Edge>[] CV = new ArrayList[n-1];
		
		for(int i=0;i<n-1;i++){
			CV[i] = new ArrayList<Edge>();
			CV[i].addAll(rvine[i].getUndirectedEdgeList());
		}
		//CV creation completed
		
		//Matrix creation loop
		for(int i=1;i<n;i++){
			Edge es = CV[n-1-i].get(0);
			ArrayList<Integer> x =  createConditionedSet(es.getFrom(), es.getTo());
			CV[n-1-i].remove(x);
			
			int xl = x.get(0);
			int xr = x.get(1);
			
			m[i-1][i-1] = xl;
			m[i][i-1] = xr;
			edges[i][i-1] = es;
			
			for(int k=i+2;k<=n;k++){
				Iterator<Edge> it = CV[n-k].iterator();
				Edge e = it.next();
				ArrayList<Integer> x2 = createConditionedSet(e.getFrom(), e.getTo());
				while(!x2.contains(xl)){
					e = it.next();
					x2 = createConditionedSet(e.getFrom(), e.getTo());
				}
				CV[n-k].remove(e);
				
				int xs = x2.get(0) == xl? x2.get(1) : x2.get(0);
				
				m[k-1][i-1] = xs;
				edges[k-1][i-1] = e;
			}
			B.add(xl);
		}
		items.removeAll(B);
		int x = items.first();
		m[n-1][n-1] = x;
	}
	
	private void kendallWeight(Edge e) {
		double[] a, b;
		
		// get the corresponding data from a merged Node
		int val1 = createConditionedSet(e.getFrom(), e.getTo()).get(0);
		a = e.getFrom().getData(val1);
		if(a == null){
			a = e.getTo().getData(val1);
		}
		
		// get the corresponding data from the other merged Node
		int val2 = createConditionedSet(e.getFrom(), e.getTo()).get(1);
		b = e.getFrom().getData(val2);
		if(b == null){
			b = e.getTo().getData(val2);
		}
		
		// set the edge weight
		e.setWeight(Utils.kendallsTau(a, b));
	}
	
	private void fitCopula(Edge e, boolean[] selected) {
		Copula[] copSet = ch.select(selected);
		double[] lls = new double[copSet.length];
		double[] a, b;
		
		// get the corresponding data from a merged Node
		int val1 = createConditionedSet(e.getFrom(), e.getTo()).get(0);
		a = e.getFrom().getData(val1);
		if(a == null){
			a = e.getTo().getData(val1);
		}
		
		// get the corresponding data from the other merged Node
		int val2 = createConditionedSet(e.getFrom(), e.getTo()).get(1);
		b = e.getFrom().getData(val2);
		if(b == null){
			b = e.getTo().getData(val2);
		}
		
		for(int i=0; i<copSet.length; i++){
			Copula c = copSet[i];
			lls[i] = c.mle(a, b);
		}
		
		int out = 0;
		for(int i=1; i<copSet.length; i++){
			if(lls[out] < lls[i]) out = i;
		}
		
		e.setCopula(copSet[out]);
		e.setLogLik(lls[out]);
	}
	
	/**
	 * Get the RVine-Matrix.
	 * @return returns the RVine-Matrix.
	 */
	public int[][] getRVineMatrix(){
		return m;
	}
	
	/**
	 * Get the Edge-Matrix.
	 * @return returns the Edge-Matrix.
	 */
	public Edge[][] getEdgeMatrix(){
		return edges;
	}
	
	/**
	 * Merges two Nodes of a given Edge to
	 * create the Node for the next dimension.
	 * 
	 * @param e the Edge from which the new Node is created.
	 * @return the merged Node.
	 */
	private Node mergeNodes(Edge e){
		// get the Node label
		Node out = createConstraintSet(e);
		double[] a,b;
		
		// get the corresponding data from a merged Node
		int val1 = out.getCondSet().get(0);
		a = e.getFrom().getData(val1);
		if(a == null){
			a = e.getTo().getData(val1);
		}
		
		// get the corresponding data from the other merged Node
		int val2 = out.getCondSet().get(1);
		b = e.getFrom().getData(val2);
		if(b == null){
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
	 * Creates pseudo observations from two observation arrays
	 * using the copula h-function.
	 * <br>
	 * array a and array b need to have the same size.
	 * 
	 * @param c the copula from which the h-function is used.
	 * @param a an array of observations.
	 * @param b another array of observations.
	 * 
	 * @return the pseudo observations, the values are constraint b|a.
	 */
	private double[] transformData1(Copula c, double[] a, double[] b){
		// assert that a and b have the same size
		if(!( a.length == b.length)){
			return null;
		}
		
		double[] out = new double[a.length];
		
		// use the copula h-function to create pseudo observations
		for(int i=0;i<a.length;i++){
			out[i] = c.h1Function(a[i], b[i]);
		}
		
		return out;
	}
	
	/**
	 * Creates pseudo observations from two observation arrays
	 * using the copula h-function.
	 * <br>
	 * array a and array b need to have the same size.
	 * 
	 * @param c the copula from which the h-function is used.
	 * @param a an array of observations.
	 * @param b another array of observations.
	 * 
	 * @return the pseudo observations, the values are constraint a|b.
	 */
	private double[] transformData2(Copula c, double[] a, double[] b){
		// assert that a and b have the same size
		if(!( a.length == b.length)){
			return null;
		}
		
		double[] out = new double[a.length];
		
		// use the copula h-function to create pseudo observations
		for(int i=0;i<a.length;i++){
			out[i] = c.h2Function(a[i], b[i]);
		}
		
		return out;
	}
	
	/**
	 * Creates a Node labeled with the constraint set from an Edge.
	 * <br>
	 * See the constraint set definitions presented
	 * in J.F. Di&szlig;mann's diploma thesis.
	 * @param e the Edge from which the new Node is created.
	 * @return the new Node.
	 */
	private Node createConstraintSet(Edge e){
		TreeSet<Integer> U_a = new TreeSet<Integer>(e.getFrom().set());
		TreeSet<Integer> U_b = new TreeSet<Integer>(e.getTo().set());
		
		TreeSet<Integer> D = new TreeSet<Integer>(U_a);
		D.retainAll(U_b);
		
		TreeSet<Integer> C = new TreeSet<Integer>(U_a);
		C.addAll(U_b);
		C.removeAll(D);
		
		Node n = new Node(C,D);
		
		e.setLabel(n.getName());
		
		return n;
	}
	
	/**
	 * Creates the conditioned set from two Nodes.
	 * <br>
	 * See the constraint set definitions presented
	 * in J.F. Di&szlig;mann's diploma thesis.
	 * @param a a Node.
	 * @param b another Node.
	 * @return conditioned set created by both nodes.
	 */
	private static ArrayList<Integer> createConditionedSet(Node a, Node b){
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
	
	private static double round(double val){
		return Math.round(val*Math.pow(10, 4))/Math.pow(10, 4);
	}

	@Override
	public Enumeration<Option> listOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOptions(String[] options) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getOptions() {
		// TODO Auto-generated method stub
		return null;
	}
}
