package weka.estimators.vines.copulas;

import java.io.BufferedReader;
import java.io.FileReader;

import junit.framework.TestCase;
import weka.estimators.vines.copulas.FrankCopula;

public class FrankCopulaTest extends TestCase {
	
	public static double[][][][] readIn(String fn){
		double[][][][] vals = new double[2][20][10][10];
		
		try{
			BufferedReader br = new BufferedReader(new FileReader("src/test/data/CopulaData/FrankData/Frank"+fn+".test"));
			String str = br.readLine();
			
			while(str != null && !str.equals("[1] \"#\"") ){
				str = str.substring(5, str.length()-1);
				String[] strs = str.split(" ");
				int p = Integer.parseInt(strs[0])+10;
				int i = Integer.parseInt(strs[1]);
				int j = Integer.parseInt(strs[2]);
				double val = Double.parseDouble(strs[3]);
				
				vals[0][p][i][j] = val;
				str = br.readLine();
			}
			
			if(str.equals("[1] \"#\""))
				str = br.readLine();
			
			while(str != null){
				str = str.substring(5, str.length()-1);
				String[] strs = str.split(" ");
				int p = Integer.parseInt(strs[0])+10;
				int i = Integer.parseInt(strs[1]);
				int j = Integer.parseInt(strs[2]);
				double val = Double.parseDouble(strs[3]);
				
				vals[1][p][i][j] = val;
				str = br.readLine();
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return vals;
	}
	
	public static double[][] readInTau(){
		double[][] vals = new double[2][20];
		
		try{
			BufferedReader br = new BufferedReader(new FileReader("src/test/data/CopulaData/FrankData/FrankTau.test"));
			String str = br.readLine();
			
			while(str != null && !str.equals("[1] \"#\"") ){
				str = str.substring(5, str.length()-1);
				String[] strs = str.split(" ");
				int p = Integer.parseInt(strs[0])+10;
				double val = Double.parseDouble(strs[1]);
				
				vals[0][p] = val;
				str = br.readLine();
			}
			
			if(str.equals("[1] \"#\""))
				str = br.readLine();
			
			while(str != null){
				str = str.substring(5, str.length()-1);
				String[] strs = str.split(" ");
				int p = Integer.parseInt(strs[0])+10;
				double val = Double.parseDouble(strs[1]);
				
				vals[1][p] = val;
				str = br.readLine();
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return vals;
	}
	
	public void testCDF(){
		double[][][][] vals = readIn("CDF");
		FrankCopula c = new FrankCopula(new double[]{0});
		
		for(int run=0; run < 2; run++){
			for(int p=1; p<20; p++){
				double par = p-10;
				if(run == 1) par = par/10.0;
				
				if(par == 0) continue;
				
				c.setParams(new double[]{par});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.C(i/10.0, j/10.0) - vals[run][p][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testDensity(){
		double[][][][] vals = readIn("PDF");
		FrankCopula c = new FrankCopula(new double[]{0});
		
		for(int run=0; run < 2; run++){
			for(int p=1; p<20; p++){
				double par = p-10;
				if(run == 1) par = par/10.0;
				
				if(par == 0) continue;
				
				c.setParams(new double[]{par});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.density(i/10.0, j/10.0) - vals[run][p][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testH1Function(){
		double[][][][] vals = readIn("H1");
		FrankCopula c = new FrankCopula(new double[]{0});
		
		for(int run=0; run < 2; run++){
			for(int p=1; p<20; p++){
				double par = p-10;
				if(run == 1) par = par/10.0;
				
				if(par == 0) continue;
				
				c.setParams(new double[]{par});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.h1Function(i/10.0, j/10.0) - vals[run][p][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testH2Function(){
		double[][][][] vals = readIn("H2");
		FrankCopula c = new FrankCopula(new double[]{0});
		
		for(int run=0; run < 2; run++){
			for(int p=1; p<20; p++){
				double par = p-10;
				if(run == 1) par = par/10.0;
				
				if(par == 0) continue;
				
				c.setParams(new double[]{par});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.h2Function(i/10.0, j/10.0) - vals[run][p][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testInverseH1Function(){
		double[][][][] vals = readIn("H1inverse");
		FrankCopula c = new FrankCopula(new double[]{0});
		
		for(int run=0; run < 2; run++){
			for(int p=1; p<20; p++){
				double par = p-10;
				if(run == 1) par = par/10.0;
				
				if(par == 0) continue;
				
				c.setParams(new double[]{par});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.h1inverse(i/10.0, j/10.0) - vals[run][p][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testInverseH2Function(){
		double[][][][] vals = readIn("H2inverse");
		FrankCopula c = new FrankCopula(new double[]{0});
		
		for(int run=0; run < 2; run++){
			for(int p=1; p<20; p++){
				double par = p-10;
				if(run == 1) par = par/10.0;
				
				if(par == 0) continue;
				
				c.setParams(new double[]{par});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.h2inverse(i/10.0, j/10.0) - vals[run][p][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testTau(){
		double[][] vals = readInTau();
		FrankCopula c = new FrankCopula(new double[]{0});
		
		for(int run=0; run < 2; run++){
			for(int p=1; p<20; p++){
				double par = p-10;
				if(run == 1) par = par/10.0;
				
				if(par == 0) continue;
				
				c.setParams(new double[]{par});
				// can't get more precise, simpson's rule is too weak.
				assert(Math.abs(c.tau() - vals[run][p]) < 0.01 );
			}
		}
	}
}

