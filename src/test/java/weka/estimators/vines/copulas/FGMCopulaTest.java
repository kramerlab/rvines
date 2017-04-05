package weka.estimators.vines.copulas;

import java.io.BufferedReader;
import java.io.FileReader;

import junit.framework.TestCase;
import weka.estimators.vines.copulas.FGMCopula;

public class FGMCopulaTest extends TestCase {
	
	public static double[][][] readIn(String fn){
		double[][][] vals = new double[20][10][10];
		
		try{
			BufferedReader br = new BufferedReader(new FileReader("src/test/data/CopulaData/FGMData/FGM"+fn+".test"));
			String str = br.readLine();
			
			while(str != null && !str.equals("[1] \"#\"") ){
				str = str.substring(5, str.length()-1);
				String[] strs = str.split(" ");
				int p = Integer.parseInt(strs[0]);
				int i = Integer.parseInt(strs[1]);
				int j = Integer.parseInt(strs[2]);
				double val = Double.parseDouble(strs[3]);
				
				vals[p][i][j] = val;
				str = br.readLine();
			}
			
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return vals;
	}
	
	public static double[] readInTau(){
		double[] vals = new double[20];
		
		try{
			BufferedReader br = new BufferedReader(new FileReader("src/test/data/CopulaData/FGMData/FGMTau.test"));
			String str = br.readLine();
			
			while(str != null && !str.equals("[1] \"#\"") ){
				str = str.substring(5, str.length()-1);
				String[] strs = str.split(" ");
				int p = Integer.parseInt(strs[0]);
				double val = Double.parseDouble(strs[1]);
				
				vals[p] = val;
				str = br.readLine();
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return vals;
	}
	
	public void testCDF(){
		double[][][] vals = readIn("CDF");
		FGMCopula c = new FGMCopula(new double[]{0});
		
		for(int p=1; p<20; p++){
			double par = p/10.0-1;
			
			c.setParams(new double[]{par});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(Math.abs(c.C(i/10.0, j/10.0) - vals[p][i][j]) < 0.00001 );
				}
			}
		}
	}
	
	public void testDensity(){
		double[][][] vals = readIn("PDF");
		FGMCopula c = new FGMCopula(new double[]{0});
		
		for(int p=1; p<20; p++){
			double par = p/10.0-1;
			
			c.setParams(new double[]{par});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(Math.abs(c.density(i/10.0, j/10.0) - vals[p][i][j]) < 0.00001 );
				}
			}
		}
	}
	
	public void testH1Function(){
		double[][][] vals = readIn("H1");
		FGMCopula c = new FGMCopula(new double[]{0});
		
		for(int p=1; p<20; p++){
			double par = p/10.0-1;
			
			c.setParams(new double[]{par});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(Math.abs(c.h1Function(i/10.0, j/10.0) - vals[p][i][j]) < 0.00001 );
				}
			}
		}
	}
	
	public void testH2Function(){
		double[][][] vals = readIn("H2");
		FGMCopula c = new FGMCopula(new double[]{0});
		
		for(int p=1; p<20; p++){
			double par = p/10.0-1;
			
			c.setParams(new double[]{par});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					assert(Math.abs(c.h2Function(i/10.0, j/10.0) - vals[p][i][j]) < 0.00001 );
				}
			}
		}
	}
	
	public void testInverseH1Function(){
		double[][][] vals = readIn("H1inverse");
		FGMCopula c = new FGMCopula(new double[]{0});
		
		for(int p=1; p<20; p++){
			double par = p/10.0-1;
			
			c.setParams(new double[]{par});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					// the R package 'vines' uses the uniroot function for inversion,
					// which is a bit weaker than our method
					// (because ours works very good in the given [0, 1] interval)
					assert(Math.abs(c.h1inverse(i/10.0, j/10.0) - vals[p][i][j]) < 0.01 );
				}
			}
		}
	}
	
	public void testInverseH2Function(){
		double[][][] vals = readIn("H2inverse");
		FGMCopula c = new FGMCopula(new double[]{0});
		
		for(int p=1; p<20; p++){
		double par = p/10.0-1;
		
		c.setParams(new double[]{par});
			for(int i=1; i<10; i++){
				for(int j=1; j<10; j++){
					// the R package 'vines' uses the uniroot function for inversion,
					// which is a bit weaker than our method
					// (because ours works very good in the given [0, 1] interval)
					assert(Math.abs(c.h2inverse(i/10.0, j/10.0) - vals[p][i][j]) < 0.01 );
				}
			}
		}
	}
	
	public void testTau(){
		double[] vals = readInTau();
		FGMCopula c = new FGMCopula(new double[]{0});
		
		for(int p=1; p<20; p++){
			double par = p/10.0-1;
			
			c.setParams(new double[]{par});
			assert(Math.abs(c.tau() - vals[p]) < 0.00001 );
		}
	}
}