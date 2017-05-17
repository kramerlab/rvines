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
import java.net.URL;
import java.util.ArrayList;

@SuppressWarnings("restriction")
public class RVineVisualization extends JFrame {
	private static final long serialVersionUID = -3023824577865793303L;

	private final JFXPanel jfxPanel = new JFXPanel();
	private WebEngine engine;
	private URL index = getClass().getResource("index.html");
	private final JPanel panel = new JPanel(new BorderLayout());
	private RegularVine rvine;

	public RVineVisualization(RegularVine rvine, String name) {
		super();
		this.rvine = rvine;
		this.setTitle(name);

		initComponents();
	}

	private void initComponents() {
		start();
		panel.add(jfxPanel, BorderLayout.CENTER);

		getContentPane().add(panel);

		setPreferredSize(new Dimension(1024, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
	}

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

				engine.load("file://" + index.getPath());

				engine.getLoadWorker().exceptionProperty()
						.addListener(new ChangeListener<Throwable>() {

							public void changed(
									ObservableValue<? extends Throwable> o,
									Throwable old, final Throwable value) {
								if (engine.getLoadWorker().getState() == FAILED) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											JOptionPane
													.showMessageDialog(
															panel,
															(value != null) ? engine
																	.getLocation()
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

	public static class JavaHelper {
		Graph[] gs;

		JavaHelper(Graph[] gs) {
			this.gs = gs;
		}

		public String loadGraph(String tree) {
			int num = Integer.parseInt(tree.substring(1));
			return buildJsonObject(gs[num]).toString();
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