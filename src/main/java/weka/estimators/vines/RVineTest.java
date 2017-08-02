package weka.estimators.vines;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import weka.core.Instances;
import weka.estimators.vines.functions.Abs;

public class RVineTest {
	public static void main(String[] args){
		for(int k=0; k<25; k++){
			System.out.println("Run "+(k+1)+":");
			try{
				RegularVine rvine = new RegularVine();
				Instances inst = new Instances(new BufferedReader(new FileReader("./src/main/data/daxreturns.arff")));
				double percent = 70;
				
				RegularVine.TrainMethod trainMethod = RegularVine.TrainMethod.KENDALL;
				RegularVine.BuildMethod buildMethod = RegularVine.BuildMethod.REGULAR;
				
				inst.randomize(new Random());
				
				int trainSize = (int) Math.round(inst
						.numInstances() * percent / 100);
				int testSize = inst.numInstances() - trainSize;
				Instances train = new Instances(inst, 0, trainSize);
				Instances test = new Instances(inst, trainSize, testSize);
				
				rvine.selected[0] = true;
				rvine.selected[1] = true;
				rvine.selected[2] = true;
				rvine.selected[3] = true;
				rvine.selected[4] = true;
				rvine.selected[5] = true;
				rvine.selected[6] = true;
				rvine.selected[7] = false;
		
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				double trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				double trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.KENDALL;
				buildMethod = RegularVine.BuildMethod.SCATTERED_INDEP;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.KENDALL;
				buildMethod = RegularVine.BuildMethod.THRESHOLD;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.CV;
				buildMethod = RegularVine.BuildMethod.REGULAR;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.CV;
				buildMethod = RegularVine.BuildMethod.SCATTERED_INDEP;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.CV;
				buildMethod = RegularVine.BuildMethod.THRESHOLD;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.MIXED;
				buildMethod = RegularVine.BuildMethod.REGULAR;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.MIXED;
				buildMethod = RegularVine.BuildMethod.SCATTERED_INDEP;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.MIXED;
				buildMethod = RegularVine.BuildMethod.THRESHOLD;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.buildEstimator(train);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void main2(String[] args){
		Graph g = new Graph();
		g.addNode(new Node(0));
		g.addNode(new Node(1));
		g.addNode(new Node(2));
		g.addNode(new Node(3));
		g.addNode(new Node(4));
		g.addNode(new Node(5));
		g.addNode(new Node(6));
		g.addNode(new Node(7));
		
		for(int i=0; i<g.getNodeList().size(); i++){
			for(int j=i+1; j<g.getNodeList().size(); j++){
				Node a = g.getNodeList().get(i);
				Node b = g.getNodeList().get(j);
				Edge e = new Edge(a, b, Math.random());
				g.addEdge(e);
			}
		}
		
		for(Node n : g.getNodeList()){
			System.out.print(n.getName()+" : ");
			for(Edge e : g.getGraph().get(n)){
				System.out.print(e.getTo()+":"+e.getWeight()+", ");
			}
			System.out.println();
		}
		
		g = VineUtils.maxSpanTree(g, new Abs());
		
		// filter edged beyond threshold
		for(Node n : g.getNodeList()){
			ArrayList<Edge> rem = new ArrayList<Edge>();
			for(Edge e : g.getGraph().get(n)){
				if(e.getWeight() < 0.7 ){
					rem.add(e);
				}
			}
			g.getGraph().get(n).removeAll(rem);
		}
		
		System.out.println();
		System.out.println();
		
		for(Node n : g.getNodeList()){
			System.out.print(n.getName()+" : ");
			for(Edge e : g.getGraph().get(n)){
				System.out.print(e.getTo()+":"+e.getWeight()+", ");
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println();
		
		Node[][] comps = VineUtils.connectedComponents(g);
		
		for(int i=0; i<comps.length; i++){
			System.out.println("Comp "+(i+1));
			for(int j=0; j<comps[i].length; j++){
				System.out.println(comps[i][j].getName());
			}
		}
	}
}
