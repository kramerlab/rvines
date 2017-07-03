package weka.estimators.vines;

import junit.framework.*;
import weka.estimators.vines.Edge;
import weka.estimators.vines.Graph;
import weka.estimators.vines.Node;
import weka.estimators.vines.Utils;
import weka.estimators.vines.functions.Abs;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is a JUnit Test for the Utils class.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class UtilsTest extends TestCase {

    /**
     * Test for Prim algorithm
     */
    public void testMaxSpan(){
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
    	
    	g = Utils.maxSpanTree(g, new Abs());
    	
    	int nodes = g.getNodeList().size();
    	int entries = 0;
    	for(ArrayList<Edge> edges : g.getGraph().values()){
    		entries += edges.size();
    	}
    	
    	assertEquals(nodes, g.getGraph().keySet().size());
    	assertEquals((nodes-1)*2, entries);
    	
    	g = new Graph();
    	g = Utils.maxSpanTree(g, new Abs());
    	assertEquals(null, g);
    	
    	g = new Graph();
    	g = Utils.maxSpanTree(null, new Abs());
    	assertEquals(null, g);
    }

    /**
     * Test for Kendall's tau algorithm
     */
    public void testKendallsTau(){
		double[] a = new double[5];
		double[] b = new double[5];
		
		a[0] = 1.5;
		a[1] = 3.5;
		a[2] = 2;
		a[3] = 1;
		a[4] = -1;
		
		b[0] = 2.5;
		b[1] = 0;
		b[2] = 0.5;
		b[3] = 2.5;
		b[4] = -0.5;
		
		assertEquals(Utils.kendallsTau(a,b), -0.10540925533894598);
    }
    
    /**
     * Test for rank normalization algorithm
     */
    public void testRankNormalization(){
		double[] y1 = new double[]{4.9, 4.4, 5.1, 4.3, 4.7};
		double[] y2 = new double[]{9.9, 8.5, 9.6, 8.8, 9.1};
		double[] y3 = new double[]{7.0, 6.2, 6.9, 6.0, 7.7};
		double[] y4 = new double[]{2.0, 3.0, 3.0, 5.0, 5.5, 8.0, 10.0, 10.0};
		double[] y5 = new double[]{1.5, 1.5, 4.0, 3.0, 1.0, 5.0, 5.0, 9.5};
		
		double[] x1 = Utils.rankNormalization(y1);
		double[] x2 = Utils.rankNormalization(y2);
		double[] x3 = Utils.rankNormalization(y3);
		double[] x4 = Utils.rankNormalization(y4);
		double[] x5 = Utils.rankNormalization(y5);
		
		assertTrue(Arrays.equals(x1, new double[]{0.8, 0.4, 1.0, 0.2, 0.6}));
		assertTrue(Arrays.equals(x2, new double[]{1.0, 0.2, 0.8, 0.4, 0.6}));
		assertTrue(Arrays.equals(x3, new double[]{0.8, 0.4, 0.6, 0.2, 1.0}));
		assertTrue(Arrays.equals(x4, new double[]{0.13333333333333333, 0.3333333333333333, 0.3333333333333333, 0.5333333333333333, 0.6666666666666666, 0.8, 1.0, 1.0}));
		assertTrue(Arrays.equals(x5, new double[]{0.3125, 0.3125, 0.625, 0.5, 0.125, 0.8125, 0.8125, 1.0}));
    }
}
