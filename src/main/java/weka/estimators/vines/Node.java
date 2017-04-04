package weka.estimators.vines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * This is the Node class.
 * <br>
 * In the Node class, we store the labeling of the Nodes and
 * the (pseudo) observations.
 * <br>
 * We also store the Nodes, where this Node is merged from, which enables
 * to check the proximity condition.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class Node implements Comparable<Node>{
	private String name;
	private ArrayList<Integer> condSet;
	private ArrayList<Integer> constrSet;
	private HashMap<Integer, double[]> data;
	private HashMap<Integer, double[]> rankNormData;
	private TreeSet<Node> mergedFrom;
	
	/**
	 * Constructor
	 * @param name name as int value
	 */
	public Node(int name){
		condSet = new ArrayList<Integer>();
		constrSet = new ArrayList<Integer>();
		this.data = new HashMap<Integer, double[]>();
		this.rankNormData = new HashMap<Integer, double[]>();
		condSet.add(name);
		this.name = condSet.toString();
	}
	
	/**
	 * Constructor
	 * @param condSet The conditioned set as Integer-TreeSet
	 * @param constrSet The constraining/conditioning set as Integer-TreeSet
	 */
	public Node(TreeSet<Integer> condSet, TreeSet<Integer> constrSet){
		this.condSet = new ArrayList<Integer>(condSet);
		this.constrSet = new ArrayList<Integer>(constrSet);
		this.data = new HashMap<Integer, double[]>();
		this.rankNormData = new HashMap<Integer, double[]>();
		
		Collections.sort(this.condSet);
		Collections.sort(this.constrSet);
		
		name = condSet.toString();
		if(!constrSet.isEmpty()){
			name += " | "+constrSet.toString();
		}
	}
	
	/**
	 * Get the union of conditioned and constraining/conditioning set.
	 * <br>
	 * The union is used to calculate the constrained set, when
	 * merging to Nodes.
	 * @return The set union as Integer-TreeSet
	 */
	public TreeSet<Integer> set(){
		TreeSet<Integer> out = new TreeSet<Integer>(condSet);
		out.addAll(constrSet);
		return out;
	}
	
	/**
	 * Get the conditioned set.
	 * @return The conditioned set as Integer-TreeSet
	 */
	public ArrayList<Integer> getCondSet(){
		return condSet;
	}
	
	/**
	 * Get the constraining/conditioning set.
	 * @return The constraining/conditioning  set as Integer-TreeSet
	 */
	public ArrayList<Integer> getConstrSet(){
		return constrSet;
	}
	
	/**
	 * Get the name.
	 * @return The name of the Node.
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Get the String representation of the Node.
	 * @return returns the Node as String.
	 */
	public String toString(){
		return name;
	}

	/**
	 * Get the data.
	 * @return The (pseudo) observations as HashMap.
	 */
	public HashMap<Integer, double[]> getData() {
		return data;
	}
	
	/**
	 * Get the rank normalized data.
	 * @return The rank normalized observations as HashMap.
	 */
	public HashMap<Integer, double[]> getRankNormData() {
		return rankNormData;
	}

	/**
	 * Get the data with a key.
	 * @return The key corresponding double array.
	 */
	public double[] getData(int key){
		return data.get(key);
	}
	
	/**
	 * Get the rank normalized data with a key.
	 * @return The key corresponding double array.
	 */
	public double[] getRankNormData(int key){
		return rankNormData.get(key);
	}
	
	/**
	 * Set the data.
	 * @param data The data as HashMap.
	 */
	public void setData(HashMap<Integer, double[]> data) {
		for(int key : data.keySet()){
			putData(key, data.get(key));
		}
	}
	
	/**
	 * Put a key,value pair into the data.
	 * @param key The key for the data HashMap.
	 * @param data The data for the data HashMap.
	 */
	public void putData(int key, double[] data) {
		this.data.put(key, data);
		this.rankNormData.put(key, Utils.rankNormalization(data));
	}
	
	/**
	 * Get the parent Nodes.
	 * @return Get the parent Nodes as Node-TreeSet.
	 */
	public TreeSet<Node> getMergedFrom() {
		return mergedFrom;
	}

	/**
	 * Set the parent Nodes.
	 * <br>
	 * Because all higher dimensional Nodes are extracted from an Edge,
	 * we use the Edge to extract the parents.
	 * @param e Set the parent Nodes as Edge.
	 */
	public void setMergedFrom(Edge e) {
		TreeSet<Node> mergedFrom = new TreeSet<Node>();
		mergedFrom.add(e.getFrom());
		mergedFrom.add(e.getTo());
		this.mergedFrom = mergedFrom;
	}
	
	/**
	 * Check the proximity condition.
	 * <br>
	 * The proximity condition in words is a
	 * parent intersection.
	 * @param n A Node to check the intersection with.
	 * @return The result whether Node n is intersected with this Node or not.
	 */
	public boolean isIntersected(Node n){
		TreeSet<Node> nSet = new TreeSet<Node>(n.getMergedFrom());
		nSet.retainAll(mergedFrom);
		return !(nSet.isEmpty());
	}

	@Override
	public int compareTo(Node o) {
		Integer a = hashCode();
		Integer b = o.hashCode();
		return a.compareTo(b);
	}
}
