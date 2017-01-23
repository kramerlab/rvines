package org.kramerlab.vines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import copulae.Copula;
import copulae.GaussCopula;
import weka.classifiers.AbstractClassifier;
import weka.core.*;

/**
 * This class represents the Regular Vine.
 * <br>
 * It is the main class of the vines package.
 * <br>
 * It uses the Graph class to store the Graphs for its dimensions.
 * <br>
 * It uses the Legend class to store the variable names for visualization.
 * <br>
 * It can be used to perform sampling and classification.
 * <br>
 * Actually the RVine is only implemented using Gauss copulae.
 * <br>
 * The implementation is based on J.F. Di&szlig;mann's diploma thesis (2010):
 * Statistical inference for regular vines and application.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class RegularVine extends AbstractClassifier {
	private static final long serialVersionUID = 1L;
	private ArrayList<Graph> rvine;
	private Legend legend;
	private int classIndex;
	private HashMap<Double, Integer> classValues;
	private int[][] m;
	private double[][] p;
	
	/**
	 * Build the RVine classifier on a given training dataset.
	 * <br>
	 * See the model selection algorithm presented
	 * in J.F. Di&szlig;mann's diploma thesis.
	 * 
	 * @param data the training dataset to build the classifier on.
	 */
	public void buildClassifier(Instances data) throws Exception{
		rvine = new ArrayList<Graph>();
		Graph g = new Graph();
		legend = new Legend();
		classIndex = data.classIndex();
		classValues = new HashMap<Double, Integer>();
		
		// create legend and add nodes to graph
		// add rank normalized data to nodes
		// create interval for classification
		for(int i=1;i<=data.numAttributes();i++){
			int x = legend.add(data.attribute(i-1).name());
			Node n = g.addNode(x);
			n.putData(x, data.attributeToDoubleArray(i-1));
			if(data.classAttribute().index() == (i-1)){
				double[] attr = data.attributeToDoubleArray(i-1);
				for(int k=0;k<attr.length;k++){
					classValues.put(attr[k], 0);
				}
			}
		}
		
		//calculate kendall's tau and add edges to graph
		for(Node i : g.getNodeList()){
			for(Node j : g.getNodeList()){
				if(i != j){
					Double tau = Utils.kendallsTau(i.getRankNormData(i.getCondSet().get(0)), j.getRankNormData(j.getCondSet().get(0)));
					g.addEdge(i, j, tau);
				}
			}
		}
		
		//calculate maximal spanning tree of graph
		g = Utils.maxSpanTree(g);
		
		//add graph to rvine
		rvine.add(g);
		
		//until regular vine is fully specified, do:
		for(int count = 1; count < data.numAttributes()-1; count++){
			//System.out.println("Built T"+count);
			
			//prepare next graph
			Graph g2 = new Graph();
			
			//for all edges of MST, do
			for(Edge e : g.getUndirectedEdgeList()){
				ArrayList<Integer> condSet = Utils.createConditionedSet(e.getFrom(), e.getTo());
				double[] a,b;

				//get necessary data
				int val1 = condSet.get(0);
				a = e.getFrom().getData(val1);
				if(a == null){
					a = e.getTo().getData(val1);
				}
				
				int val2 = condSet.get(1);
				b = e.getFrom().getData(val2);
				if(b == null){
					b = e.getTo().getData(val2);
				}

				//estimate best copula with parameters
				Copula c = Utils.goodnessOfFit(null, a, b);

				e.setCopula(c);

				//create new node out of the edges
				Node n = mergeNodes(e);
				//System.out.println("Created Node: "+n);
				g2.addNode(n);
			}
			
			//calculate kendall's tau and add edges to graph,
			//for all possible edges (proximity condition)
			for(Node i : g2.getNodeList()){
				for(Node j : g2.getNodeList()){
					if(i != j){
						if(i.isIntersected(j)){
							ArrayList<Integer> condSet = Utils.createConditionedSet(i, j);
							double[] a,b;
							
							//get necessary data
							int val1 = condSet.get(0);
							a = i.getRankNormData(val1);
							if(a == null){
								a = j.getRankNormData(val1);
							}
							
							int val2 = condSet.get(1);
							b = i.getRankNormData(val2);
							if(b == null){
								b = j.getRankNormData(val2);
							}
							
							Double tau = Utils.kendallsTau(a, b);
							g2.addEdge(i, j, tau);
						}
					}
				}
			}
			
			//calculate maximal spanning tree of graph
			g2 = Utils.maxSpanTree(g2);
			
			//add graph to rvine
			rvine.add(g2);
			g = g2;
		}
		//set copulae for last iteration
		//for all edges of MST, do
		for(Edge e : g.getUndirectedEdgeList()){
			ArrayList<Integer> condSet = Utils.createConditionedSet(e.getFrom(), e.getTo());
			double[] a,b;
			
			//get necessary data
			int val1 = condSet.get(0);
			a = e.getFrom().getData(val1);
			if(a == null){
				a = e.getTo().getData(val1);
			}
			
			int val2 = condSet.get(1);
			b = e.getFrom().getData(val2);
			if(b == null){
				b = e.getTo().getData(val2);
			}
			
			//estimate best copula with parameters
			Copula c = Utils.goodnessOfFit(null, a, b);
			e.setCopula(c);
		}
		
		createRVineMatrix();
		createParameterMatrix();
	}
	
	/**
	 * Classifies a given instance if the class variable is set.
	 * 
	 * @param instance the instance to be classified.
	 * @return returns the estimated class.
	 */
	public double classifyInstance(Instance instance){
		// returns the instance with highest log-likelihood
		int n = instance.numAttributes();
		double[] in = instance.toDoubleArray();
		double maxClass = Double.NEGATIVE_INFINITY;
		double maxVal = Double.NEGATIVE_INFINITY;

		Instance newInst = new DenseInstance(n);
		for(int i=0;i<n;i++){
			if(i != classIndex){
				newInst.setValue(i, in[i]);
			}
		}
		for(Double key : classValues.keySet()){
			newInst.setValue(classIndex, key);
			double loglik = logLikelihood(newInst);
			if( loglik > maxVal ){
				maxVal = loglik;
				maxClass = key;
			}
		}
		
		return maxClass;
	}
	
	/**
	 * Get the RVine.
	 * 
	 * @return returns the RVine as List of Graphs.
	 */
	public ArrayList<Graph> getRegularVine(){
		return rvine;
	}
	
	/**
	 * Get the Legend.
	 * 
	 * @return returns the RVine Legend.
	 */
	public ArrayList<String> getLegend(){
		return legend.getLegend();
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
		double[] a_new = transformData(e.getCopula(), a, b);
		double[] b_new = transformData(e.getCopula(), b, a);
		
		out.putData(val1, a_new);
		out.putData(val2, b_new);
		out.setMergedFrom(e);
		
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
		
		return new Node(C,D);
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
	private double[] transformData(Copula c, double[] a, double[] b){
		// assert that a and b have the same size
		if(!( a.length == b.length)){
			return null;
		}
		
		double[] out = new double[a.length];
		
		// use the copula h-function to create pseudo observations
		for(int i=0;i<a.length;i++){
			out[i] = c.hFunction(a[i], b[i]);
		}
		
		return out;
	}
	
	/**
	 * Creates the RVine-Matrix stored in a global variable m.
	 * <br>
	 * See the RVine-Matrix creation algorithm presented
	 * in J.F. Di&szlig;mann's diploma thesis.
	 */
	private void createRVineMatrix(){
		int n = rvine.size()+1;
		m = new int[n][n];
		
		TreeSet<Integer> B = new TreeSet<Integer>();
		TreeSet<Integer> items = new TreeSet<Integer>();
		
		//create an all items set
		for(int i=1;i<=n;i++){
			items.add(i);
		}
		
		//Create CV, Set of constraint sets of RVine
		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<Integer>>[] CV = new ArrayList[n-1];
		//use nodes of T_i+1 as edges of T_i
		for(int i=0;i<n-2;i++){
			CV[i] = new ArrayList<ArrayList<Integer>>();
			for(Node a : rvine.get(i+1).getNodeList()){
				CV[i].add(a.getCondSet());
			}
		}
		//add edge of T_n-1
		CV[n-2] = new ArrayList<ArrayList<Integer>>();
		Node a = rvine.get(n-2).getNodeList().get(0);
		Node b = rvine.get(n-2).getNodeList().get(1);
		Node ab = createConstraintSet(new Edge(a, b, 0));
		CV[n-2].add(ab.getCondSet());
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

				for(Edge e : rvine.get(n-1-i).getUndirectedEdgeList()){
					Node ab = createConstraintSet(e);
					// if we found the corresponding edge
					if(ab.getCondSet().equals(cond)){
						// add the parameter to the parameter-matrix
						p[i][k] = e.getCopula().getParams()[0];
					}
				}
			}
		}
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
	 * Creates a sample instance.
	 * <br>
	 * It can be used either for random sampling or for sampling specific
	 * entries only.
	 * <br>
	 * x shall be a complete instance, where we can flag in the given array
	 * if we use the value of x for the new instance or
	 * if we re-sample the x value.
	 * <br>
	 * Only the flagged as given values from x will affect the sampling.
	 * The other values will be overwritten with sampled values.
	 * <br>
	 * The main part of the sampling approach is based on the sampling
	 * algorithm presented in J.F. Di&szlig;mann's diploma thesis.
	 * @param x an instance
	 * @param given a boolean flag for each value of
	 * x if it shall be used as given value.
	 * @return returns the sampled instance.
	 */
	public double[] createSample(double[] x, boolean[] given){
		int n = m.length;
		double[] u = new double[n];
		double[][] v = new double[n][n];
		
		//random observations of uniform(0,1) distribution
		for(int i=0;i<n;i++){
			u[i] = Math.random();
		}
		
		if(!given[m[n-1][n-1]-1]){
			x[m[n-1][n-1]-1] = u[m[n-1][n-1]-1];
		}
		
		for(int k=n-2;k>=0;k--){
			if(!given[m[k][k]-1]){
				for(int i=k+1;i<n-1;i++){
					//run path down to get x_i with inverse h-function
					u[m[k][k]-1] = GaussCopula.inverseHFunction(u[m[k][k]-1], v[m[i][k]-1][m[i+1][k]-1], p[i][k]);
				}
				//level 0, get x value from h-inverse of adjacent x-value (last entry in current column)
				x[m[k][k]-1] = GaussCopula.inverseHFunction(u[m[k][k]-1], x[m[n-1][k]-1], p[n-1][k]);
			}
			//one dimensional transformed values
			v[m[k][k]-1][m[n-1][k]-1] = GaussCopula.hFunction(x[m[k][k]-1], x[m[n-1][k]-1], p[n-1][k]);
			v[m[n-1][k]-1][m[k][k]-1] = GaussCopula.hFunction(x[m[n-1][k]-1], x[m[k][k]-1], p[n-1][k]);
			
			for(int i=n-2;i>k;i--){
				//run path up to generate transformed values
				v[m[k][k]-1][m[i][k]-1] = GaussCopula.hFunction(v[m[k][k]-1][m[i+1][k]-1], v[m[i][k]-1][m[i+1][k]-1], p[i][k]);
				v[m[i][k]-1][m[k][k]-1] = GaussCopula.hFunction(v[m[i][k]-1][m[i+1][k]-1], v[m[k][k]-1][m[i+1][k]-1], p[i][k]);
			}
		}
		
		return x;
	}
	
	/**
	 * Creates a completely random sampled instance.
	 * <br>
	 * It used the createSample-function with a default x-array,
	 * which are all flagged as not given.
	 * @return returns a random sampled instance.
	 */
	public double[] createRandomSample(){
		int n = rvine.size()+1;
		return createSample(new double[n], new boolean[n]);
	}
	
	/**
	 * The log-likelihood for a given instance.
	 * <br>
	 * The method is based on an
	 * algorithm presented in J.F. Di&szlig;mann's diploma thesis.
	 * @param instance the instance.
	 * @return returns the log-likelihood for the instance.
	 */
	public double logLikelihood(Instance instance){
		double loglik = 0;
		int n = m.length;
		double[] x = instance.toDoubleArray();
		double[][] v = new double[n][n];
		
		for(int k=n-2;k>=0;k--){
			//one dimensional transformed values
			v[m[k][k]-1][m[n-1][k]-1] = GaussCopula.hFunction(x[m[k][k]-1], x[m[n-1][k]-1], p[n-1][k]);
			v[m[n-1][k]-1][m[k][k]-1] = GaussCopula.hFunction(x[m[n-1][k]-1], x[m[k][k]-1], p[n-1][k]);
			loglik += Math.log(GaussCopula.density(x[m[k][k]-1], x[m[n-1][k]-1], p[n-1][k]));
			
			for(int i=n-2;i>k;i--){
				//run path up to generate transformed values
				v[m[k][k]-1][m[i][k]-1] = GaussCopula.hFunction(v[m[k][k]-1][m[i+1][k]-1], v[m[i][k]-1][m[i+1][k]-1], p[i][k]);
				v[m[i][k]-1][m[k][k]-1] = GaussCopula.hFunction(v[m[i][k]-1][m[i+1][k]-1], v[m[k][k]-1][m[i+1][k]-1], p[i][k]);
				loglik += Math.log(GaussCopula.density(v[m[k][k]-1][m[i+1][k]-1], v[m[i][k]-1][m[i+1][k]-1], p[i][k]));
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
	public double logLikelihood(Instances instances){
		double loglik = 0;
		for(Instance i : instances){
			loglik += logLikelihood(i);
		}
		return loglik;
	}
}
