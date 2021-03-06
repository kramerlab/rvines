package weka.estimators.vines;

import java.io.Serializable;

import weka.estimators.vines.copulas.Copula;

/**
 * This is the Edge class to for the RVine.
 * <br>
 * For every Edge we assert two nodes that are connected with the Edge,
 * a weight value and a copula function.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class Edge implements Serializable{
	private static final long serialVersionUID = -2199722053484457966L;
	private Node from;
	private Node to;
	private double weight;
	private Copula copula;
	private String label;
	private double logLik;
	
	/**
	 * Constructor
	 * @param from a Node where the Edge comes from.
	 * @param to a Node where the Edge goes to.
	 * @param weight the Kendall't tau value as weight.
	 */
	public Edge(Node from, Node to, double weight){
		this.from = from;
		this.to = to;
		this.setWeight(weight);
	}
	
	/**
	 * Get the Node where the Edge comes from.
	 * @return returns the 'from' - Node.
	 */
	public Node getFrom(){
		return from;
	}
	
	/**
	 * Get the Node where the Edge goes to.
	 * @return returns the 'to' - Node.
	 */
	public Node getTo(){
		return to;
	}

	/**
	 * Get the Edge weight.
	 * @return returns the Kendall's tau value as weight.
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Set the Edge weight.
	 * @param weight the Kendall't tau value as weight.
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	/**
	 * Get the String representation of the Edge.
	 * @return returns the Edge as String.
	 */
	public String toString(){
		return "("+from.toString()+", "+to.toString()+", "+weight+")";
	}

	/**
	 * Get the copula from the Edge.
	 * @return returns the Edge's copula.
	 */
	public Copula getCopula() {
		return copula;
	}

	/**
	 * Set the copula from the Edge.
	 * @param copula the Edge's copula.
	 */
	public void setCopula(Copula copula) {
		this.copula = copula;
	}

	/**
	 * Get the edge label.
	 * @return edge label as string.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Set the edge label.
	 * @param label the edge label.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Get the log-likelihood which
	 * was produces during construction.
	 * @return log-likelihood.
	 */
	public double getLogLik() {
		return logLik;
	}

	/**
	 * Set the log-likelihood which
	 * was produces during construction.
	 * @param logLik log-likelihood.
	 */
	public void setLogLik(double logLik) {
		this.logLik = logLik;
	}
}
