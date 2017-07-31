package weka.estimators.vines;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import weka.core.Instances;

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
				double[][] train = RegularVine.transform(new Instances(inst,
						0, trainSize));
				double[][] test = RegularVine.transform(new Instances(inst,
						trainSize, testSize));
				
				double[] w = new double[train.length];
				for(int i=0; i<w.length; i++){
					w[i] = 1;
				}
				
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
				rvine.estimate(train, w);
				double trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.KENDALL;
				buildMethod = RegularVine.BuildMethod.SCATTERED_INDEP;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.estimate(train, w);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.CV;
				buildMethod = RegularVine.BuildMethod.REGULAR;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.estimate(train, w);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.CV;
				buildMethod = RegularVine.BuildMethod.SCATTERED_INDEP;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.estimate(train, w);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.MIXED;
				buildMethod = RegularVine.BuildMethod.REGULAR;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.estimate(train, w);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
				trainMethod = RegularVine.TrainMethod.MIXED;
				buildMethod = RegularVine.BuildMethod.SCATTERED_INDEP;
				
				rvine.trainMethod = trainMethod;
				rvine.buildMethod = buildMethod;
				
				trainTimeStart = System.currentTimeMillis();
				rvine.estimate(train, w);
				trainTimeElapsed = System.currentTimeMillis()
						- trainTimeStart;
				
				System.out.println("TrainMethod:"+trainMethod+", BuildMethod:"+buildMethod+", TestDensity:"+rvine.logDensity(test)+", TrainTime:"+trainTimeElapsed);
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
