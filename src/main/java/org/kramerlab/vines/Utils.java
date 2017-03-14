package org.kramerlab.vines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.kramerlab.copulae.*;

/**
 * This class contains utility function.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class Utils{
	private static boolean debug = false;
	
	/**
	 * Get the maximum spanning tree.
	 * <br>
	 * This function is specialized for Regular Vines.
	 * For that purpose, it uses absolute Edge weights to
	 * calculate the maximum spanning tree.
	 * <br>
	 * The result is a spanning tree, which represents the
	 * most correlating variable pairs for the RVine.
	 * <br>
	 * It uses the idea of Prim's MST algorithm,
	 * but searching for the maximum spanning tree instead
	 * of the minimum spanning tree.
	 * @param g a connected Graph.
	 * @return returns the maximum spanning tree of g.
	 */
	public static Graph maxSpanTree(Graph g){
		if(g == null){
			return null;
		}
		if(g.isEmpty()){
			return null;
		}
		
		Graph maxST = new Graph();
		
		ArrayList<Node> nodeList = g.getNodeList();
		for(Node n: nodeList){
			maxST.addNode(n);
		}
		
		ArrayList<Node> nodeListMaxST = new ArrayList<Node>();
		nodeListMaxST.add(nodeList.get((int) (Math.random()*nodeList.size())));
		
		while(!(nodeListMaxST.size() == nodeList.size())){
			ArrayList<Edge> edges = new ArrayList<Edge>();
			for(Node n : nodeListMaxST){
				edges.addAll(g.getGraph().get(n));
			}
			
			Edge maxEdge = null;
			for(Edge e : edges){
				if(! nodeListMaxST.contains(e.getTo()) ){
					if(maxEdge == null){
						maxEdge = e;
					}else{
						if(Math.abs(maxEdge.getWeight())
								< Math.abs(e.getWeight())){
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
	 * Calculates the empirical Kendall's tau.
	 * <br>
	 * Both random variables need to be rank normalized to get a 
	 * reliable Kendall's tau value.
	 * @param a a rank normalized random variable.
	 * @param b another rank normalized random variable.
	 * @return returns the empirical Kendall's tau for a and b.
	 */
	public static double kendallsTau(double[] a, double[] b){
		if(!( a.length == b.length)){
			return Double.NaN;
		}
		
		//Sort lists with respect to a
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		
		for(int i=0;i<a.length;i++){
			double in = a[i];
			int k;
			for(k=0;k<x.size();k++){
				if(in < x.get(k)){
					break;
				}
			}
			x.add(k, in);
			y.add(k, b[i]);
		}
		if (debug){
			System.out.println("a = "+a);
			System.out.println("b = "+b);
			System.out.println("x = "+x);
			System.out.println("y = "+y);
		}
		//Begin counting:
		
		int P = 0; // number of concordant pairs
		int Q = 0; // number of discordant pairs
		int T = 0; // number of ties only in a
		int U = 0; // number of ties only in b
		//if a tie appears in a and b it is not added to either T or U
		
		if (debug){
			System.out.println();
		}
		for(int i=0;i<x.size()-1;i++){
			for(int j=i+1;j<y.size();j++){
				if (debug){
					System.out.println("Comparing "+i+" to "+j);
				}
				if(x.get(i) < x.get(j) && y.get(i) < y.get(j)){
					P++;
				}
				if(x.get(i) < x.get(j) && y.get(i) > y.get(j)){
					Q++;
				}
				if(x.get(i).equals(x.get(j)) && !(y.get(i).equals(y.get(j)))){
					T++;
				}
				if(!(x.get(i).equals(x.get(j))) && y.get(i).equals(y.get(j))){
					U++;
				}
				if (debug){
					System.out.println("P: "+P +", Q: "+Q+", T: "+T+", U:"+U);
				}
			}
		}
		if (debug){
			System.out.println();
			System.out.println("P: "+P +", Q: "+Q+", T: "+T+", U:"+U);
		}
		double n = (P+Q+T);
		double m = (P+Q+U);
		return (P-Q) / Math.sqrt(n*m);
	}
	
	/**
	 * This is a placeholder for a goodness of fit test.
	 * <br>
	 * It test the copulae for fitting between a and b.
	 * The best copula will be returned with its parameters.
	 * <br>
	 * Because the RVine is currently using only Gauss copulae,
	 * this method returns the MLE on the Gauss copula function.
	 * 
	 * @param copulae an array of copula families,
	 * that shall participate on the GOF-Test.
	 * @param a an observation array.
	 * @param b another observation array.
	 * @return returns the copula with its parameters that fits best.
	 */
	public static Copula goodnessOfFit(Copula[] copulae,
			double[] a, double[] b, double tau){
		return GumbelCopula.mle(a, b, tau);
	}
	
	/**
	 * Log-Likelihood calculation for copulae.
	 * <br>
	 * It is used to calculate the copula log-likelihood for the MLE.
	 * 
	 * @param c a copula, whose log-likelihood is calculated.
	 * @param a an observation array.
	 * @param b another observation array.
	 * @return returns the log-likelihood.
	 */
	public static double logLikelihood(Copula c, double[] a, double[] b){
		double logLik = 0;
		
		for(int i=0;i<a.length;i++){
			logLik += Math.log(c.density(a[i], b[i]));
		}
		return logLik;
	}
	
	/**
	 * Get rank normalized data.
	 * @param data the data to be rank normalized.
	 * @return the rank normalized data.
	 */
	public static double[] rankNormalization(double[] data){
		
		// Get a copy of the list, with values sorted from least to greatest.
		// That is: S[i] <= S[2] <= ... <= S[N]
		// Thus, the index of each list item equates to its ranking position
		
		ArrayList<Double> S = new ArrayList<Double>();
		
		for(double e : data){
			S.add(e);
		}
		
		Collections.sort(S);
		
		// Create a lookup table from value to ranking position
		// Correct for ties in the sorted list by average as we go.
		
		double tieCount = 0;
		double tieTotal = 0;
		double tieValue = Double.NaN;
		HashMap<Double, Double> table = new HashMap<Double, Double>();
		
		for(int i=1; i<=S.size(); i++){
			if(S.get(i-1) == tieValue){
				tieTotal += i;
				tieCount++;
			}else{
				table.put(S.get(i-1), (double) i);
				
				// If we are coming off a run of ties, find the avg
				// and use that as the ranking position for that value
				
				if(tieCount > 1){
					table.put(tieValue, tieTotal/tieCount);
				}
				
				tieCount = 1;
				tieTotal = i;
				tieValue = S.get(i-1);
			}
		}
		
		// Check if S ended in a run of ties and treat as above
		
		if(tieCount > 1){
			table.put(tieValue, tieTotal/tieCount);
		}
		
		// Now, create a new list of ranking positions corresponding
		// to the order of the original list of values.
		
		double[] out = new double[data.length];
		double maxRank = tieTotal/tieCount;
		
		for(int i=0; i<data.length; i++){
			// normalize with dividing by max rank
			out[i] = table.get(data[i])/maxRank;
		}
		
		return out;
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
	public static ArrayList<Integer> createConditionedSet(Node a, Node b){
		TreeSet<Integer> U_a = new TreeSet<Integer>(a.set());
		TreeSet<Integer> U_b = new TreeSet<Integer>(b.set());
		TreeSet<Integer> D = new TreeSet<Integer>(U_a);
		D.retainAll(U_b);
		
		TreeSet<Integer> C = new TreeSet<Integer>(U_a);
		C.addAll(U_b);
		C.removeAll(D);
		
		return  new ArrayList<Integer>(C);
	}
	
	/**
	 * Correction function.
	 * <br>
	 * Corrects the value of x.
	 * <br>
	 * It is used for infinity value handling.
	 * @param x the value to be corrected.
	 * @return returns the correction of x.
	 */
	public static double laplaceCorrection(double x){
		x = Math.min(x,0.999999999999);
		x = Math.max(x,0.000000000001);
		return x;
	}
	
	public static double simpsonIntegrate(UnivariateFunction f, int N, double lb, double ub){
	      double h = (ub - lb) / N;     // step size
	      
	      double sum = f.value(lb) + f.value(ub);
	      
	      for (int i = 1; i < N; i++) {
	         double x = lb + h * i;
	         sum += 2 * f.value(x);
	      }
	      
	      for (int i = 1; i <= N; i++) {
	         double x = lb + h * (i-1);
	         double y = lb + h * i;
	         sum += 4 * f.value((x+y)/2);
	      }
	      
	      return sum * h / 6.0;
	}
}
