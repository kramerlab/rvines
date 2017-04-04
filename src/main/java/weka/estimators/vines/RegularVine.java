package weka.estimators.vines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
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
public class RegularVine implements MultivariateEstimator {
	private boolean[] selected = new boolean[]{true, true, true, true, true, true, true, true, true, true, true, true, true, true};
	private Graph[] rvine;
	private int[][] m;
	private Copula[][] copulae;
	private double[][] p;
	private double[][] tau;
	private double[][] pairLogLiks;
	
	public static void main(String[] args){
		RegularVine rvine = new RegularVine();
		try{
			DataSource source = new DataSource("src/test/data/daxreturns.arff");//args[0]);
			Instances instances = source.getDataSet();
			
			int k = instances.numAttributes();
			
			double[][] data = new double[k][];
			double[] w = new double[k];
			
			for(int i=0; i<k; i++){
				data[i] = instances.attributeToDoubleArray(i);
				w[i] = 1;
			}
			
			rvine.estimate(data, w);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int[][] m = rvine.getRVineMatrix();
		
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
		
		System.out.println();
		System.out.println();
		
		double[][] p = rvine.getParameterMatrix();
		
		System.out.println("Parameter - Matrix");
		for(int i=0;i<p.length;i++){
			for(int j=0;j<p.length;j++){
				double val = p[i][j];
				String out = "";
				if( (int) val == val){
					out = Integer.toString( (int) val);
				}else{
					out = String.valueOf(val);
				}
				if(j<p.length-1){
					System.out.print(out+"\t\t\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		
		double[][] pll = rvine.getPairLogLiksMatrix();
		
		System.out.println("Pairwise - LogLiks - Matrix");
		for(int i=0;i<pll.length;i++){
			for(int j=0;j<pll.length;j++){
				double val = pll[i][j];
				String out = "";
				if( (int) val == val){
					out = Integer.toString( (int) val);
				}else{
					out = String.valueOf(val);
				}
				if(j<pll.length-1){
					System.out.print(out+"\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
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
		Graph g = new Graph();
		Graph gNext = new Graph();
		
		// initialize nodes
		for(int i=0;i<data.length;i++){
			Node n = new Node(i);
			n.putData(i, data[i]);
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
		
		// calculate maximal spanning tree of graph
		g = Utils.maxSpanTree(g);
		
		// fit copulas to the edges
		for(Edge e : g.getUndirectedEdgeList()){
			fitCopula(e, selected);
		}
		
		//add graph to rvine
		rvine[0] = g;
		
		//until regular vine is fully specified, do:
		for(int count = 1; count < data.length-1; count++){
			//prepare next graph
			gNext = new Graph();
			
			//for all edges of MST, do
			for(Edge e : g.getUndirectedEdgeList()){
				gNext.addNode(mergeNodes(e));
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
			
			//calculate maximal spanning tree of graph
			gNext = Utils.maxSpanTree(gNext);
			
			// fit copulas to the edges
			for(Edge e : gNext.getUndirectedEdgeList()){
				fitCopula(e, selected);
			}
			
			//add graph to rvine
			rvine[count] = gNext;
			g = gNext;
		}
		
		createRVineMatrix();
		createParameterMatrix();
	}

	/**
	 * The log-likelihood for a given instance.
	 * <br>
	 * The method is based on an
	 * algorithm presented in J.F. Di&szlig;mann's diploma thesis.
	 * @param instance the instance.
	 * @return returns the log-likelihood for the instance.
	 */
	@Override
	public double logDensity(double[] x){
		double loglik = 0;
		int n = m.length;
		double[][] v = new double[n][n];
		Copula c;
		
		for(int k=n-2;k>=0;k--){
			//one dimensional transformed values
			c = copulae[n-1][k];
			v[m[k][k]][m[n-1][k]] = c.h2Function(x[m[k][k]], x[m[n-1][k]]);
			v[m[n-1][k]][m[k][k]] = c.h1Function(x[m[k][k]], x[m[n-1][k]]);
			if(m[k][k] > m[n-1][k]){
				loglik += Math.log(c.density(x[m[n-1][k]], x[m[k][k]]));
				pairLogLiks[n-1][k] += Math.log(c.density(x[m[n-1][k]], x[m[k][k]]));
			}else{
				loglik += Math.log(c.density(x[m[k][k]], x[m[n-1][k]]));
				pairLogLiks[n-1][k] += Math.log(c.density(x[m[k][k]], x[m[n-1][k]]));
			}
			
			for(int i=n-2;i>k;i--){
				//run path up to generate transformed values
				c = copulae[i][k];
				v[m[k][k]][m[i][k]] = c.h2Function(v[m[k][k]][m[i+1][k]], v[m[i][k]][m[i+1][k]]);
				v[m[i][k]][m[k][k]] = c.h1Function(v[m[k][k]][m[i+1][k]], v[m[i][k]][m[i+1][k]]);
				
				if(m[k][k] > m[i][k]){
					loglik += Math.log(c.density(v[m[i][k]][m[i+1][k]], v[m[k][k]][m[i+1][k]]));
					pairLogLiks[i][k] += Math.log(c.density(v[m[i][k]][m[i+1][k]], v[m[k][k]][m[i+1][k]]));
				}else{
					loglik += Math.log(c.density(v[m[k][k]][m[i+1][k]], v[m[i][k]][m[i+1][k]]));
					pairLogLiks[i][k] += Math.log(c.density(v[m[k][k]][m[i+1][k]], v[m[i][k]][m[i+1][k]]));
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
		
		TreeSet<Integer> B = new TreeSet<Integer>();
		TreeSet<Integer> items = new TreeSet<Integer>();
		
		//create an all items set
		for(int i=0;i<n;i++){
			items.add(i);
		}
		
		//Create CV, Set of constraint sets of RVine
		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<Integer>>[] CV = new ArrayList[n-1];
		
		for(int i=0;i<n-1;i++){
			CV[i] = new ArrayList<ArrayList<Integer>>();
			for(Edge e : rvine[i].getUndirectedEdgeList()){
				CV[i].add(createConditionedSet(e.getFrom(), e.getTo()));
			}
		}
		//CV creation completed
		
		//Matrix creation loop
		for(int i=1;i<n;i++){
			ArrayList<Integer> x =  CV[n-1-i].get(0);
			CV[n-1-i].remove(x);
			
			int xl = x.get(0);
			int xr = x.get(1);
			
			m[i-1][i-1] = xl;
			m[i][i-1] = xr;
			
			for(int k=i+2;k<=n;k++){
				Iterator<ArrayList<Integer>> it = CV[n-k].iterator();
				ArrayList<Integer> x2 = it.next();
				while(!x2.contains(xl)){
					x2 = it.next();
				}
				CV[n-k].remove(x2);
				
				int xs = x2.get(0) == xl? x2.get(1) : x2.get(0);
				
				m[k-1][i-1] = xs;
			}
			B.add(xl);
		}
		items.removeAll(B);
		int x = items.first();
		m[n-1][n-1] = x;
	}
	
	
	/**
	 * Creates the Parameter-Matrix if the RVine-Matrix is set.
	 * It is stored in a global variable p.
	 */
	private void createParameterMatrix(){
		int n = m.length;
		p = new double[n][n];
		tau = new double[n][n];
		copulae = new Copula[n][n];
		pairLogLiks  = new double[n][n];
		
		for(int i=1;i<n;i++){
			for(int k=0;k<i;k++){
				/* cond is the conditioned part of the edge.
				 * it is used to get the corresponding edge from the
				 * rvine edge set.
			 	 */
				ArrayList<Integer> cond = new ArrayList<Integer>();
				cond.add(m[k][k]);
				cond.add(m[i][k]);
				Collections.sort(cond);

				for(Edge e : rvine[n-1-i].getUndirectedEdgeList()){
					ArrayList<Integer> cond2 = createConditionedSet(e.getFrom(), e.getTo());
					// if we found the corresponding edge
					if(cond2.equals(cond)){
						// add the parameter to the parameter-matrix
						copulae[i][k] = e.getCopula();
						tau[i][k] = e.getWeight();
						if(e.getCopula().getParams().length > 0)
							p[i][k] = e.getCopula().getParams()[0];
					}
				}
			}
		}
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
	
	private static void fitCopula(Edge e, boolean[] selected) {
		Copula[] copSet = CopulaHandler.select(selected);
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
	}
	
	/**
	 * Get the RVine-Matrix.
	 * @return returns the RVine-Matrix.
	 */
	public int[][] getRVineMatrix(){
		return m;
	}
	
	
	/**
	 * Get the Parameter-Matrix.
	 * @return returns the Parameter-Matrix.
	 */
	public double[][] getParameterMatrix(){
		return p;
	}
	
	
	/**
	 * Get the Copula-Matrix.
	 * @return returns the Copula-Matrix.
	 */
	public Copula[][] getCopulaMatrix(){
		return copulae;
	}
	
	
	/**
	 * Get the Tau-Matrix.
	 * @return returns the Tau-Matrix.
	 */
	public double[][] getTauMatrix(){
		return tau;
	}
	
	/**
	 * Get the Pair-LogLiks-Matrix.
	 * @return returns the Pair-LogLiks-Matrix.
	 */
	public double[][] getPairLogLiksMatrix(){
		return pairLogLiks;
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
}
