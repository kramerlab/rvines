package weka.estimators.vines;

import weka.core.Instances;

public class RegularVineForest extends RegularVine{
	private static final long serialVersionUID = -4041681384371075830L;
	
	/** threshold to split base tree*/
	protected double threshold = 0.5;
	/** rvine models on base level*/
	protected RegularVine[] rvines;
	/** components on base level*/
	protected 
	Node[] comp;
	
	/**
	 * Constructor
	 */
	public RegularVineForest(){
		super();
	}

	@Override
	public void buildEstimator(Instances data) {
		Graph g = new Graph();
		
		// initialize nodes
		for(int i=0;i<data.numAttributes();i++){
			Node n = new Node(i);
			n.putData(i, data.attributeToDoubleArray(i));
			g.addNode(n);
		}
		
		// initialize edges
		for(int i=0; i<g.getNodeList().size(); i++){
			for(int j=i+1; j<g.getNodeList().size(); j++){
				Node a = g.getNodeList().get(i);
				Node b = g.getNodeList().get(j);
				Edge e = new Edge(a, b, 0);
				weightEdge(e);
				g.addEdge(e);
			}
		}
		
		// filter edged beyond threshold
		for(Node n : g.getNodeList()){
			for(Edge e : g.getGraph().get(n)){
				if(! (e.getWeight() < threshold) ){
					g.getGraph().get(n).remove(e);
				}
			}
		}
		
		// get connected components
		Node[][] comps = VineUtils.connectedComponents(g);
		rvines = new RegularVine[comps.length];
		
		for(int i=0; i<comps.length; i++){
			comp = comps[i];
			double[][] compData = new double[comp.length][];
			double[] w2 = new double[comp.length];
			for(int j=0; j<comp.length; j++){
				compData[j] = comp[j].getData(comp[j].getCondSet().get(0));
			}
			
			rvines[i] = new RegularVine();
			try {
				rvines[i].setOptions(getOptions());
			} catch (Exception e) {
				e.printStackTrace();
			}
			//rvines[i].buildEstimator(compData);
		}
	}
	
	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
}
