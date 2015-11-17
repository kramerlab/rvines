package org.kramerlab.vines;

import java.util.ArrayList;

/**
 * This is the Legend class.
 * <br>
 * It stores the names of the Nodes as Legend for visualization.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class Legend {
	private ArrayList<String> legend;
	
	/**
	 * Constructor
	 */
	public Legend(){
		this.legend = new ArrayList<String>();
	}
	
	/**
	 * Add a name to the Legend.
	 * <br>
	 * Because the Nodes are re-labeled in as integer values
	 * as ascending series beginning with 0,
	 * we use the array list index as reference.
	 * 
	 * @param name The name of a Node.
	 */
	public int add(String name){
		legend.add(name);
		return legend.size();
	}
	
	/**
	 * Get the Legend.
	 * 
	 * @returns returns the Legend.
	 */
	public ArrayList<String> getLegend(){
		return legend;
	}
	
	/**
	 * Get the String representation of the Legend.
	 * @return returns the Legend as String.
	 */
	public String toString(){
		String out = "";
		
		for(int key=0;key<legend.size();key++){
			out += (key+1)+" : "+legend.get(key)+"\n";
		}
		
		return out;
	}
}
