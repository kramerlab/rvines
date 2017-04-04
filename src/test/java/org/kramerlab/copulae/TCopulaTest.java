package org.kramerlab.copulae;

import java.io.BufferedReader;
import java.io.FileReader;

import junit.framework.TestCase;
import weka.estimators.vines.copulas.TCopula;

public class TCopulaTest extends TestCase {
	
	public static double[][][][] readIn(String fn){
		double[][][][] vals = new double[20][8][10][10];
		
		try{
			BufferedReader br = new BufferedReader(new FileReader("src/test/data/CopulaData/TData/T"+fn+".test"));
			String str = br.readLine();
			
			while(str != null && !str.equals("[1] \"#\"") ){
				str = str.substring(5, str.length()-1);
				String[] strs = str.split(" ");
				int p = Integer.parseInt(strs[0]);
				int v = Integer.parseInt(strs[1]) - 3;
				int i = Integer.parseInt(strs[2]);
				int j = Integer.parseInt(strs[3]);
				double val = Double.parseDouble(strs[4]);
				
				vals[p][v][i][j] = val;
				str = br.readLine();
			}
			
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return vals;
	}
	
	public static double[][] readInTau(){
		double[][] vals = new double[20][8];
		
		try{
			BufferedReader br = new BufferedReader(new FileReader("src/test/data/CopulaData/TData/TTau.test"));
			String str = br.readLine();
			
			while(str != null && !str.equals("[1] \"#\"") ){
				str = str.substring(5, str.length()-1);
				String[] strs = str.split(" ");
				int p = Integer.parseInt(strs[0]);
				int v = Integer.parseInt(strs[1]) - 3;
				double val = Double.parseDouble(strs[2]);
				
				vals[p][v] = val;
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
		TCopula c = new TCopula(new double[]{0, 1});
		
		for(int p=1; p<20; p++){
			for(int v=3; v<=10; v++){
				double par = p/10.0-1;
				int par2 = v;
				
				c.setParams(new double[]{par, par2});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						if(Math.abs(c.C(i/10.0, j/10.0) - vals[p][v-3][i][j]) >= 0.00001){
							System.out.println(par+" "+par2+" "+i+" "+j);
							System.out.println(c.C(i/10.0, j/10.0)+" "+vals[p][v-3][i][j]);
							System.out.println();
						}
						// assert(Math.abs(c.C(i/10.0, j/10.0) - vals[p][v-3][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testDensity(){
		double[][][][] vals = readIn("PDF");
		TCopula c = new TCopula(new double[]{0, 1});
		
		for(int p=1; p<20; p++){
			for(int v=3; v<=10; v++){
				double par = p/10.0-1;
				int par2 = v;
				
				c.setParams(new double[]{par, par2});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.density(i/10.0, j/10.0) - vals[p][v-3][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testH1Function(){
		double[][][][] vals = readIn("H1");
		TCopula c = new TCopula(new double[]{0, 1});
		
		for(int p=1; p<20; p++){
			for(int v=3; v<=10; v++){
				double par = p/10.0-1;
				int par2 = v;
				
				c.setParams(new double[]{par, par2});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.h1Function(i/10.0, j/10.0) - vals[p][v-3][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testH2Function(){
		double[][][][] vals = readIn("H2");
		TCopula c = new TCopula(new double[]{0, 1});
		
		for(int p=1; p<20; p++){
			for(int v=3; v<=10; v++){
				double par = p/10.0-1;
				int par2 = v;
				
				c.setParams(new double[]{par, par2});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.h2Function(i/10.0, j/10.0) - vals[p][v-3][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testInverseH1Function(){
		double[][][][] vals = readIn("H1inverse");
		TCopula c = new TCopula(new double[]{0, 1});
		
		for(int p=1; p<20; p++){
			for(int v=3; v<=10; v++){
				double par = p/10.0-1;
				int par2 = v;
				
				c.setParams(new double[]{par, par2});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.h1inverse(i/10.0, j/10.0) - vals[p][v-3][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testInverseH2Function(){
		double[][][][] vals = readIn("H2inverse");
		TCopula c = new TCopula(new double[]{0, 1});
		
		for(int p=1; p<20; p++){
			for(int v=3; v<=10; v++){
				double par = p/10.0-1;
				int par2 = v;
				
				c.setParams(new double[]{par, par2});
				for(int i=1; i<10; i++){
					for(int j=1; j<10; j++){
						assert(Math.abs(c.h2inverse(i/10.0, j/10.0) - vals[p][v-3][i][j]) < 0.00001 );
					}
				}
			}
		}
	}
	
	public void testTau(){
		double[][] vals = readInTau();
		TCopula c = new TCopula(new double[]{0, 1});
		
		for(int p=1; p<20; p++){
			for(int v=3; v<=10; v++){
				double par = p/10.0-1;
				int par2 = v;
				
				c.setParams(new double[]{par, par2});
				assert(Math.abs(c.tau() - vals[p][v-3]) < 0.00001 );
			}
		}
	}
}

