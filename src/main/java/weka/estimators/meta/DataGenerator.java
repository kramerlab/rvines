package weka.estimators.meta;

import java.util.ArrayList;

import umontreal.ssj.probdist.*;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.estimators.vines.RegularVine;

public class DataGenerator {
	public static Instances randomGenerate(int attrSize, int dataSize){
		ContinuousDistribution[] dists = new ContinuousDistribution[attrSize];
		
		for(int i=0; i<attrSize; i++){
			dists[i] = randomDist();
		}
		
		// generate completely random data
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for(int i=0; i<attrSize; i++){
			attributes.add(new Attribute(""+(i+1)));
		}
		
		Instances dataRaw = new Instances("RandomInstances", attributes , 0);
		for(int i=0; i<1000; i++){
			// generate random Instance
			double[] raw = new double[attrSize];
			for(int j=0; j<attrSize; j++){
				raw[j] = dists[j].cdf(Math.random());
			}
			
			dataRaw.add(new DenseInstance(1.0, raw));
		}
		
		// train random rvine on random data
		
		RegularVine rvine = new RegularVine();
		rvine.trainMethod = RegularVine.TrainMethod.RANDOM;
		
		try {
			rvine.buildEstimator(dataRaw);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// create random dependent samples
		
		attributes.add(new Attribute("Density"));
		
		Instances out = new Instances("Generated", attributes, 0);
		
		for(int i=0; i<dataSize; i++){
			// generate random Instance
			double[] raw = new double[attrSize];
			boolean[] given = new boolean[attrSize];
			for(int j=0; j<attrSize; j++){
				raw[j] = dists[j].cdf(Math.random());
				given[j] = true;
			}
			
			Instance sample = rvine.createSample(raw, given);
			double dens = rvine.logDensity(sample);
			
			System.out.println(dens);
			
			sample.insertAttributeAt(sample.numAttributes());
			sample.setValue(sample.numAttributes()-1, dens);
			
			out.add(sample);
		}
		
		System.out.println(rvine.summary());
		for(int i=0; i<dists.length;i++){
			System.out.println(i+" : "+dists[i].toString());
		}
		
		return out;
	}
	
	private static ContinuousDistribution randomDist(){
		double r = Math.random()*100;
		
		if(r < 10) return new CauchyDist(Math.random()*10-5, Math.random()*3);
		if(r < 20) return new ExponentialDist(Math.random()*3);
		if(r < 30) return new FrechetDist(Math.random()*3, Math.random()*3, Math.random()*10-5);
		if(r < 40) return new GumbelDist(Math.random()*3, Math.random()*10-5);
		if(r < 50) return new LaplaceDist(Math.random()*10-5, Math.random()*3);
		if(r < 60) return new NormalDist(Math.random()*10-5, Math.random()*3);
		if(r < 70) return new StudentDistQuick((int) Math.random()*10+1);
		if(r < 80) return new UniformDist(-1, 1);
		if(r < 90) return new LogisticDist(Math.random()*10-5, Math.random()*3);
		return new InverseGammaDist(Math.random()*3, Math.random()*3);
	}
}
