package weka.estimators.vines;

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
	public void estimate(double[][] data, double[] w) {
		Graph g = new Graph();
		
		// initialize nodes
		for(int i=0; i<=data.length; i++){
			Node n = new Node(i);
			n.putData(i, data[i]);
			g.addNode(n);
		}
		
		// initialize edges
		for(int i=0; i<g.getNodeList().size(); i++){
			for(int j=i+1; j<g.getNodeList().size(); j++){
				Node a = g.getNodeList().get(i);
				Node b = g.getNodeList().get(j);
				Edge e = new Edge(a, b, 0);
				weight(e);
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
		Node[][] comps = Utils.connectedComponents(g);
		rvines = new RegularVine[comps.length];
		
		for(int i=0; i<comps.length; i++){
			comp = comps[i];
			double[][] compData = new double[comp.length][];
			double[] w2 = new double[comp.length];
			for(int j=0; j<comp.length; j++){
				compData[j] = comp[j].getData(comp[j].getCondSet().get(0));
				w2[j] = w[comp[j].getCondSet().get(0)];
			}
			
			rvines[i] = new RegularVine();
			try {
				rvines[i].setOptions(getOptions());
			} catch (Exception e) {
				e.printStackTrace();
			}
			rvines[i].estimate(compData, w2);
		}
	}
	
	@Override
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
	
	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
}
