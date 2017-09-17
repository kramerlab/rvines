package weka.gui.explorer;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import static javafx.concurrent.Worker.State.FAILED;
import netscape.javascript.JSObject;

import javax.json.*;
import javax.swing.*;

import weka.estimators.vines.Edge;
import weka.estimators.vines.Graph;
import weka.estimators.vines.Node;
import weka.estimators.vines.RegularVine;

import java.awt.*;
import java.util.ArrayList;

/**
 * Frame to visualize vine graphs with JavaScript jsd3 inside a
 * swing frame using javafx libraries.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
@SuppressWarnings("restriction")
public class RVineVisualization extends JFrame {
	private static final long serialVersionUID = -3023824577865793303L;

	private final JFXPanel jfxPanel = new JFXPanel();
	private WebEngine engine;
	private final JPanel panel = new JPanel(new BorderLayout());
	private RegularVine rvine;

	/**
	 * HTML / JavaScript as String.
	 */
	private String htmlStr = "<!DOCTYPE html>\n"
			+"<html lang=\"en\">\n"
			+"\n"
			+"<head>\n"
			+"<meta charset=\"utf-8\">\n"
			+"<script src=\"http://d3js.org/d3.v3.min.js\" "
			+ "charset=\"utf-8\"></script>\n"
			+"<style>\n"
			+"#wrapper {\n"
			+"	width: 1200px; \n"
			+"	overflow: hidden; /* add this to contain floated children */\n"
			+"}\n"
			+"\n"
			+"#menubar {\n"
			+"	width: 200px;\n"
			+"	float: left;\n"
			+"}\n"
			+"\n"
			+"#content {\n"
			+"	float: right;\n"
			+"	width: 1000px;\n"
			+"	height: 500px;\n"
			+"}\n"
			+"\n"
			+"h1 {\n"
			+"	text-align: center;\n"
			+"}\n"
			+"\n"
			+"ul {\n"
			+"	list-style-type: none;\n"
			+"	margin: 0;\n"
			+"	padding: 0;\n"
			+"}\n"
			+"\n"
			+"table, th, td {\n"
			+"	border: 1px solid black;\n"
			+"	border-collapse: collapse;\n"
			+"}\n"
			+"\n"
			+"th, td {\n"
			+"	padding: 15px;\n"
			+"}\n"
			+"\n"
			+".node circle {\n"
			+"	cursor: pointer;\n"
			+"	stroke: grey;\n"
			+"	stroke-width: 1px;\n"
			+"	fill: white;\n"
			+"}\n"
			+"\n"
			+".node text {\n"
			+"	font: 10px sans-serif;\n"
			+"	pointer-events: none;\n"
			+"	text-anchor: middle;\n"
			+"}\n"
			+"\n"
			+"line.link {\n"
			+"	fill: none;\n"
			+"	stroke-width: 1.5px;\n"
			+"}\n"
			+"</style>\n"
			+"\n"
			+"<script type=\"text/javascript\">\n"
			+"//initialize function\n"
			+"function initialize(){\n"
			+"	var trees = jHelper.graphSize();\n"
			+"	//create menubar entries \n"
			+"	for(var i=0;i<trees;i++){\n"
			+"		var sel = document.getElementById(\"opts\");\n"
			+"		var opt = document.createElement(\"option\");\n"
			+"		opt.appendChild(document.createTextNode(\"T\"+(i+1)));\n"
			+"		sel.appendChild(opt);\n"
			+"	}\n"
			+"	request(\"T1\");\n"
			+"}\n"
			+"\n"
			+"//request function \n"
			+"function request(req) {\n"
			+"		var rawData = jHelper.loadGraph(req);\n"
			+"        var data = JSON.parse(rawData);\n"
			+"       	document.getElementById(\"contentTitle\")"
			+ ".innerHTML = req;\n"
			+"\n"
			+"       	d3.select(\"svg\").remove();\n"
			+"    	setD3Configs();\n"
			+"    	update(data);\n"
			+"}\n"
			+"\n"
			+"//d3 configuration\n"
			+"var width = 960,\n"
			+"    height = 500,\n"
			+"    colors = d3.scale.category10(),\n"
			+"    svg,\n"
			+"    link,\n"
			+"    node;\n"
			+"\n"
			+"var force = d3.layout.force()\n"
			+"    .linkDistance(80)\n"
			+"    .charge(-120)\n"
			+"    .gravity(.05)\n"
			+"    .size([width, height])\n"
			+"    .on(\"tick\", tick)\n"
			+"\n"
			+"function setD3Configs(){\n"
			+"	svg = d3.select(\"#content\").append(\"svg\")\n"
			+"	    .attr(\"width\", width)\n"
			+"	    .attr(\"height\", height);\n"
			+"\n"
			+"	link = svg.selectAll(\".link\"),\n"
			+"	node = svg.selectAll(\".node\");\n"
			+"}\n"
			+"\n"
			+"function update(data) {\n"
			+"  var nodes = data.nodes,\n"
			+"  	  links = data.edges;\n"
			+"\n"
			+"  // Restart the force layout.\n"
			+"  force\n"
			+"      .nodes(nodes)\n"
			+"      .links(links)\n"
			+"      .start();\n"
			+"\n"
			+"  // Update links.\n"
			+"  link = link.data(links, function(d) { "
			+ "return d.source.id+\" \"+d.target.id; });\n"
			+"\n"
			+"  link.exit().remove();\n"
			+"\n"
			+"  link.enter().append(\"line\", \".node\")\n"
			+"      .attr(\"class\", \"link\")\n"
			+"	  .attr(\"stroke\", function(d){return colors(d.label);});\n"
			+"\n"
			+"  // Update nodes.\n"
			+"  node = node.data(nodes, function(d) { return d.id; });\n"
			+"\n"
			+"  node.exit().remove();\n"
			+"\n"
			+"  var nodeEnter = node.enter().append(\"g\")\n"
			+"      .attr(\"class\", \"node\")\n"
			+"      .call(force.drag);\n"
			+"\n"
			+"  nodeEnter.append(\"circle\")\n"
			+"      .attr(\"r\", function(d) { "
			+ "return Math.sqrt(d.size) / 10 || 10.0; });\n"
			+"\n"
			+"  nodeEnter.append(\"text\")\n"
			+"      .attr(\"dy\", \".35em\")\n"
			+"      .text(function(d) { return d.name; });\n"
			+"}\n"
			+"\n"
			+"function tick() {\n"
			+"  link.attr(\"x1\", function(d) { return d.source.x; })\n"
			+"      .attr(\"y1\", function(d) { return d.source.y; })\n"
			+"      .attr(\"x2\", function(d) { return d.target.x; })\n"
			+"      .attr(\"y2\", function(d) { return d.target.y; });\n"
			+"\n"
			+"  node.attr(\"transform\", function(d) { "
			+ "return \"translate(\" + d.x + \",\" + d.y + \")\"; });\n"
			+"}\n"
			+"</script>\n"
			+"\n"
			+"</head>\n"
			+"\n"
			+"<body>\n"
			+"\n"
			+"	<h1>Visualization for Regular Vines</h1>\n"
			+"\n"
			+"	<div id=\"wrapper\">\n"
			+"		<div id=\"menubar\" style=\"text-align: center\">\n"
			+"			<select id=\"opts\" onchange=\"request(this.value);\">\n"
			+"			</select>\n"
			+"		</div>\n"
			+"		<div id=\"content\" style=\"text-align: center\">\n"
			+"			<h2 id=\"contentTitle\"></h2>\n"
			+"		</div>\n"
			+"	</div>\n"
			+"\n"
			+"</body>\n"
			+"</html>";
	
	/**
	 * Constructor.
	 * 
	 * @param rvine
	 *            the regular vine to visualize.
	 * @param name
	 *            the name of the frame to open.
	 */
	public RVineVisualization(RegularVine rvine, String name) {
		super();
		this.rvine = rvine;
		this.setTitle(name);

		initComponents();
	}

	/**
	 * initialize the frame.
	 */
	private void initComponents() {
		start();
		panel.add(jfxPanel, BorderLayout.CENTER);

		getContentPane().add(panel);
		setPreferredSize(new Dimension(1024, 600));
		pack();
	}

	/**
	 * Start the HTML / JavaScript Code inside the swing frame.
	 */
	private void start() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				WebView view = new WebView();
				engine = view.getEngine();

				// Enable Javascript.
				engine.setJavaScriptEnabled(true);

				// A Worker load the page
				Worker<Void> worker = engine.getLoadWorker();

				// Listening to the status of worker
				worker.stateProperty().addListener(new ChangeListener<State>() {
					@Override
					public void changed(
							ObservableValue<? extends State> observable, //
							State oldValue, State newValue) {

						// When load successed.
						if (newValue == Worker.State.SUCCEEDED) {
							// Get window object of page.
							JSObject jsobj = (JSObject) engine
									.executeScript("window");

							// Set member for 'window' object.
							// In Javascript access: window.jHelper....
							jsobj.setMember("jHelper",
									new JavaHelper(rvine.getRegularVine()));
							
							// initialize since window.onLoad is not working.
							engine.executeScript("initialize();");
						}
					}
				});

				engine.loadContent(htmlStr);

				engine.getLoadWorker().exceptionProperty()
						.addListener(new ChangeListener<Throwable>() {

							public void changed(
									ObservableValue<? extends Throwable> o,
									Throwable old, final Throwable value) {
								if (engine.getLoadWorker().getState() == FAILED) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane.showMessageDialog(
													panel,
													(value != null) ?
														engine.getLocation()
													    + "\n"
														+ value.getMessage()
													: engine.getLocation()
													    + "\nUnexpected error.",
														"Loading error...",
														JOptionPane.ERROR_MESSAGE);
										}
									});
								}
							}
						});

				jfxPanel.setScene(new Scene(view));
			}
		});
	}

	/**
	 * Bridge Class to communicate with JavaScript.
	 */
	public static class JavaHelper {
		Graph[] gs;

		JavaHelper(Graph[] gs) {
			this.gs = gs;
		}

		public String loadGraph(String tree) {
			int num = Integer.parseInt(tree.substring(1));
			return buildJsonObject(gs[num-1]).toString();
		}

		public int graphSize() {
			return gs.length;
		}
	}

	/**
	 * Generates a Json Object of the Graph to the given dimension.
	 * 
	 * @param dim
	 *            the dimension of the Graph to be jsonified.
	 * @return returns the Json representation of the Graph.
	 */
	private static JsonObject buildJsonObject(Graph g) {
		if (g.isEmpty()) {
			return null;
		}

		JsonArrayBuilder nodes = Json.createArrayBuilder();
		JsonArrayBuilder edges = Json.createArrayBuilder();

		ArrayList<Node> nodeList = g.getNodeList();

		for (Node n : nodeList) {
			JsonObjectBuilder node = Json.createObjectBuilder();
			node.add("id", nodeList.indexOf(n));
			node.add("name", n.getName());
			nodes.add(node);
		}

		for (Edge e : g.getUndirectedEdgeList()) {
			JsonObjectBuilder edge = Json.createObjectBuilder();
			edge.add("source", nodeList.indexOf(e.getFrom()));
			edge.add("target", nodeList.indexOf(e.getTo()));
			edge.add("label", e.getCopula().name());
			edges.add(edge);
		}

		JsonObjectBuilder job = Json.createObjectBuilder();
		job.add("nodes", nodes);
		job.add("edges", edges);

		return job.build();
	}
}