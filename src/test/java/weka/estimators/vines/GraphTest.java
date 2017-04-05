package weka.estimators.vines;

import junit.framework.*;
import weka.estimators.vines.Edge;
import weka.estimators.vines.Graph;
import weka.estimators.vines.Node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is a JUnit Test for the Graph class.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GraphTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GraphTest( String testName ){
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite(){
        return new TestSuite( GraphTest.class );
    }
    
    /**
     * Test the class creation
     * Test if graph is empty
     */
    public void testGraphClass(){
    	Graph g = new Graph();
    	
    	assertEquals(Graph.class, g.getClass());
    	assertEquals(new ArrayList<Node>(), g.getNodeList());
    	assertEquals(new HashMap<Node, ArrayList<Edge>>(), g.getGraph());
    }
    
    /**
     * Test for adding nodes
     */
    public void testAddNodes(){
    	Graph g = new Graph();
    	
    	ArrayList<Node> nodes = new ArrayList<Node>();
    	
    	Node a,b,c,d,e;
    	a = new Node(1);
    	b = new Node(2);
    	c = new Node(3);
    	d = new Node(4);
    	e = new Node(5);
    	
    	nodes.add(a);
    	nodes.add(b);
    	nodes.add(c);
    	nodes.add(d);
    	nodes.add(e);
    	
    	g.addNode(a);
    	g.addNode(b);
    	g.addNode(c);
    	g.addNode(d);
    	g.addNode(e);
    	
    	assertEquals(nodes, g.getNodeList());
    }
    
    /**
     * Test for adding nodes as string
     */
    public void testAddNodesString(){
    	Graph g = new Graph();
    	ArrayList<Node> nodes = new ArrayList<Node>();
    	Node a = new Node(1);
    	
    	nodes.add(a);
    	
    	g.addNode(0);
    	
    	assertNotSame(nodes, g.getNodeList());
    	assertEquals("[0]", g.getNodeList().get(0).getName());
    }
    
    /**
     * Test for adding edges
     */
    public void testAddEdges(){
    	Graph g = new Graph();
    	
    	Node a,b,c,d,e;
    	a = new Node(1);
    	b = new Node(2);
    	c = new Node(3);
    	d = new Node(4);
    	e = new Node(5);
    	
    	g.addNode(a);
    	g.addNode(b);
    	g.addNode(c);
    	g.addNode(d);
    	g.addNode(e);
    	
    	Edge ab,ac,ad,ae,bc,bd,be,cd,ce,de;
    	
    	ab = new Edge(a, b, 0.5);
    	ac = new Edge(a, c, 2);
    	ad = new Edge(a, d, 0.7);
    	ae = new Edge(a, e, 0.9);
    	bc = new Edge(b, c, 1.1);
    	bd = new Edge(b, d, 2.1);
    	be = new Edge(b, e, 1.3);
    	cd = new Edge(c, d, 1.5);
    	ce = new Edge(c, e, 0.3);
    	de = new Edge(d, e, 1.6);
    	
    	g.addEdge(ab);
    	g.addEdge(ac);
    	g.addEdge(ad);
    	g.addEdge(ae);
    	g.addEdge(bc);
    	g.addEdge(bd);
    	g.addEdge(be);
    	g.addEdge(cd);
    	g.addEdge(ce);
    	g.addEdge(de);
    	
    	//Graph.class is undirected,
    	//it will automatically create a back-edge
    	//That's why we can't assert equality of a created adjacency list
    	//and the myGraph adjacency list
    	
    	int nodes = g.getNodeList().size();
    	int entries = 0;
    	for(ArrayList<Edge> edges : g.getGraph().values()){
    		entries += edges.size();
    	}
    	
    	assertEquals(nodes, g.getGraph().keySet().size());
    	assertEquals(nodes*(nodes-1), entries);
    }
}
