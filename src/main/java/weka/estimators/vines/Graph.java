package weka.estimators.vines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class represents a Graph for the RVine.
 * <br>
 * One Graph represents one dimension of the RVine.
 * <br>
 * It uses the Node and Edge classes.
 * The Edges got put in as directed, but they get mirrored automatically to
 * get an undirected Graph.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class Graph implements Serializable{
	private static final long serialVersionUID = 715115521149996147L;
	private ArrayList<Node> nodeList;
	private HashMap<Node, ArrayList<Edge>> adjacencyList;
	
	/**
	 * Constructor
	 * <br>
	 * Initializes the Node list and the adjacency list.
	 */
	public Graph(){
		nodeList = new ArrayList<Node>();
		adjacencyList = new HashMap<Node, ArrayList<Edge>>();
	}
	
	/**
	 * Adds a Node to the Node list.
	 * <br>
	 * Initializes a row in the adjacency list for the added Node.
	 * @param n The Node to be added.
	 */
	public void addNode(Node n){
		nodeList.add(n);
		adjacencyList.put(n, new ArrayList<Edge>());
	}
	
	/**
	 * Adds an Edge to the Graph.
	 * <br>
	 * The Edge needs to be initialized with a from-Node and a to-Node.
	 * <br>
	 * A mirrored Edge will be added automatically
	 * to create an undirected Edge.
	 * @param e The Edge to be added.
	 */
	public void addEdge(Edge e){
		Node from = e.getFrom();
		Node to = e.getTo();
		// create mirrored Edge.
		Edge e2 = new Edge(to, from, e.getWeight());
		// Add both to the corresponding edge list.
		ArrayList<Edge> edgeList = adjacencyList.get(from);
		ArrayList<Edge> edgeList2 = adjacencyList.get(to);
		edgeList.add(e);
		edgeList2.add(e2);
	}
	
	/**
	 * Adds an Edge to the Graph by a from Node, a to Node and an Edge weight.
	 * <br>
	 * A mirrored Edge will be added automatically
	 * to create an undirected Edge.
	 * @param from a Node where the Edge comes from.
	 * @param to a Node where the Edge goes to.
	 * @param weight the Kendall't tau value as weight.
	 */
	public void addEdge(Node from, Node to, double weight){
		Edge e = new Edge(from, to, weight);
		addEdge(e);
	}
	
	/**
	 * Get the Node list.
	 * @return returns the Node list.
	 */
	public ArrayList<Node> getNodeList(){
		return nodeList;
	}
	
	/**
	 * Get the Graph.
	 * @return returns the Graph as adjacency list.
	 */
	public HashMap<Node, ArrayList<Edge>> getGraph(){
		return adjacencyList;
	}
	
	/**
	 * Empty check.
	 * @return returns whether the Graph is empty or not.
	 */
	public boolean isEmpty(){
		return nodeList.isEmpty();
	}
	
	/**
	 * Get an undirected Edge list.
	 * <br>
	 * Because every Edge is stored with its mirrored Edge,
	 * we extract an undirected Edge list out of the
	 * double directed Edge set.
	 * @return returns an undirected Edge list.
	 */
	public ArrayList<Edge> getUndirectedEdgeList(){
		ArrayList<Edge> out = new ArrayList<Edge>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		while(nodes.size() < nodeList.size()){
			// iterate over nodes.
			for(Node n : nodeList){
				nodes.add(n);
				// iterate over node corresponding edges.
				for(Edge e : adjacencyList.get(n)){
					// don't add the back-edges.
					if(!nodes.contains(e.getTo())){
						out.add(e);
					}
				}
			}
		}
		return out;
	}
}
