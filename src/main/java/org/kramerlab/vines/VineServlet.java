package org.kramerlab.vines;

import java.io.*;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;

import org.kramerlab.copulae.Copula;

import javax.json.*;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * This is the Servlet to apply Regular Vines
 * using a webserver for visualization.
 * <br>
 * We use tomcat and d3.js for the RVine visualization.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class VineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private RegularVine rvine;
	private Instances instances;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
    		throws ServletException, IOException {
		
		PrintWriter out = resp.getWriter();
		resp.setContentType("application/json");

		if(req.getHeader("MyRequest").equals("initialize")){
			initialize();
			out.println( createInitializingObject() );
		}else{
			if(req.getHeader("MyRequest").equals("General Information")){
				out.println( createGeneralInformationObject() );
			}else{
				String header = req.getHeader("MyRequest");
				String id = header.substring(1, header.length());
				out.println( buildJsonObject(Integer.parseInt(id)));
			}
		}
		
		out.flush();
		out.close();
	}
	
	/**
	 * Initializes the RVine on a dataset given by a hard-coded path.
	 */
	private void initialize(){
		try {
			RegularVine rvine = new RegularVine();
//			CSVLoader loader = new CSVLoader();
//
//			String[] options = new String[1]; 
//			options[0] = "-H";
//			loader.setOptions(options);
//			loader.setSource(new File(
//					"Evaluations/daxreturns/train1_normalized.csv"));
//			instances = loader.getDataSet();
			
			DataSource source = new DataSource(
					"src"+File.separator+"main"+File.separator+
					"data"+File.separator+"daxreturns.arff");
			instances = source.getDataSet();
			instances.setClassIndex(0);
			rvine.buildClassifier(instances);
			
			this.rvine = rvine;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a Json Object combining the RVine Legend
	 * and general information.
	 * @return returns the Json representation the RVine Legend
	 * and general information.
	 */
	private JsonObject createInitializingObject(){
		JsonObjectBuilder job = Json.createObjectBuilder();
		
		JsonArrayBuilder legend = Json.createArrayBuilder();
	
		ArrayList<String> legend_str = rvine.getLegend();
		
		for(int i=0;i<legend_str.size();i++){
			legend.add((i+1)+" : "+legend_str.get(i));
		}
		
		job.add("legend", legend);
		job.add("main", createGeneralInformation());
		
		return job.build();
	}
	
	/**
	 * Generates a Json Object for general information.
	 * <br>
	 * It uses the createGeneralInformation-function to create the builder.
	 * It builds the object from the builder.
	 * @return returns the Json representation of general information.
	 */
	private JsonObject createGeneralInformationObject(){
		return createGeneralInformation().build();
	}
	
	/**
	 * Generates a Json Object Builder for general information.
	 * @return returns the Json Builder representation of general information.
	 */
	private JsonObjectBuilder createGeneralInformation(){
		JsonObjectBuilder main = Json.createObjectBuilder();
		JsonObjectBuilder copulae = Json.createObjectBuilder();
		JsonObjectBuilder modelInfo = Json.createObjectBuilder();
		
		/*create copula information.
		It is still static because it was
		not necessary for us.*/
		copulae.add("Gauss", "all");
		copulae.add("T", 0);
		copulae.add("Gumbel", 0);
		copulae.add("Clayton", 0);
		copulae.add("Frank", 0);
		
		//create model information
		modelInfo.add("Log-Likelihood", ( (int)(rvine.logLikelihood(instances)*1000) )*0.001);
		modelInfo.add("Trees",rvine.getRegularVine().size());
		modelInfo.add("Other Information","and so on");
		
		main.add("Used Copulae", copulae);
		main.add("Model Information", modelInfo);
		return main;
	}
	
	/**
	 * Generates a Json Object of the Graph to the given dimension.
	 * @param dim the dimension of the Graph to be jsonified.
	 * @return returns the Json representation of the Graph.
	 */
	private JsonObject buildJsonObject(int dim){
		Graph g = rvine.getRegularVine().get(dim-1);
		
		if(g.isEmpty()){
			return null;
		}
		
		JsonArrayBuilder nodes = Json.createArrayBuilder();
		JsonArrayBuilder edges = Json.createArrayBuilder();
		
		ArrayList<Node> nodeList = g.getNodeList();
		
		for( Node n : nodeList ){
			JsonObjectBuilder node = Json.createObjectBuilder();
			node.add("id", nodeList.indexOf(n));
			node.add("name", n.getName());
			nodes.add(node);
		}
		
		for( Edge e : g.getUndirectedEdgeList() ){
			JsonObjectBuilder edge = Json.createObjectBuilder();
			edge.add("source",nodeList.indexOf(e.getFrom()));
			edge.add("target",nodeList.indexOf(e.getTo()));
			edge.add("label", e.getCopula().name());
			edges.add(edge);
		}
		
		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("nodes", nodes);
		job.add("edges", edges);
		
		return job.build();
	}
	
	/**
	 * The main function
	 */
	public static void main(String[] args){
		VineServlet vs = new VineServlet();
		double start = System.currentTimeMillis();
		vs.initialize();
		double end = System.currentTimeMillis();
		
		System.out.println("Time : "+(end-start)+" ms");
		
		System.out.println("Log-Likelihood : "
				+vs.rvine.logLikelihood(vs.instances));
		System.out.println();
		int[][] m = vs.rvine.getRVineMatrix();
		
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
		double[][] p = vs.rvine.getParameterMatrix();
		
		System.out.println("Parameter - Matrix");
		for(int i=0;i<p.length;i++){
			for(int j=0;j<p.length;j++){
				double val = Math.round(p[i][j]*1000)/1000.0;
				String out = "";
				if( (int) val == val){
					out = Integer.toString( (int) val);
				}else{
					out = String.valueOf(val);
				}
				if(j<p.length-1){
					System.out.print(out+"\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		Copula[][] cop = vs.rvine.getCopulaMatrix();
		
		System.out.println("Mode - Matrix");
		for(int i=0;i<p.length;i++){
			for(int j=0;j<p.length;j++){
				Copula c = cop[i][j];
				String out = "-";
				if(c != null){
					out = c.name();
				}
				
				if(j<p.length-1){
					System.out.print(out+"\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		double[][] tau = vs.rvine.getTauMatrix();
		
		System.out.println("Tau - Matrix");
		for(int i=0;i<p.length;i++){
			for(int j=0;j<p.length;j++){
				double out = tau[i][j];
				
				if(j<p.length-1){
					System.out.print(out+"\t&\t");
				}else{
					System.out.print(out+"\\\\");
				}
			}
			System.out.println();
		}
		
//		System.out.println();
//		System.out.println("Random Sample");
//		double[] x = vs.rvine.createRandomSample();
//		for(int i=0;i<x.length;i++){
//				System.out.print(x[i]+"\t");
//		}
//		
//		System.out.println();
//		for(int i=0;i<20;i++){
//			System.out.println(vs.instances.get(0).classValue()+" predicted: "
//					+vs.rvine.classifyInstance(vs.instances.get(0)));
//		}
	}
}
