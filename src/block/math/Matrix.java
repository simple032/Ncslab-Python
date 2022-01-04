package block.math;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import block.Block;
import block.io.OutputPort;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;
import block.io.InputPort;

public class Matrix {
	
	public double elements[][];
	public int row;
	public int column;
	private boolean scalar=false;
	
	public Matrix(int row, int column) {
		this.row = row;
		this.column = column;
		this.elements = new double[row][column];
	}
	
	//convert MATLAB-string to matrix
	public Matrix(String str) {
//		String regEx = "[' ']+"; // һ�������ո�  
//		Pattern p = Pattern.compile(regEx);  
//		Matcher m = p.matcher(numStr);
//		JSONArray numArray=new JSONArray(m.replaceAll(",").trim());
//		
		//System.out.println(str);
		if("".equals(str)) {
			this.row = 0;
			this.column = 0;
			this.elements = null;
		}else {
			//maybe just a number	
			try {
				this.row = 1;
				this.column = 1;
				double value = Double.parseDouble(str);
				this.elements = new double[1][];
				this.elements[0] = new double[1];
				this.elements[0][0] = value;
				this.scalar = true;
			}catch(NumberFormatException nfe) {
						
				String newstr = str.replaceAll(" ","").trim();
		        newstr = newstr.substring(1, newstr.length()-1);
				String[] strs = newstr.split(";");
				this.row = strs.length;
				elements = new double[this.row][];
				for(int i=0; i< elements.length; i++){
				    String[] substrs = strs[i].split(",");		   
				    if(i==0) {
				    	this.column = substrs.length;
				    }
				    else if(this.column != substrs.length) {
				    	
				    }	    	
				    
				    elements[i] = new double[this.column];

				    for(int j=0; j<substrs.length; j++){
				    	elements[i][j] = Double.parseDouble(substrs[j]);
				    }
				}
				
				this.scalar = this.row*this.column==1;
				
			}			
			
		}					

		
	}
	
	public void add(Matrix mat) {
		if(this.row != mat.row || this.column != mat.column) {
			
		}else {
			
		}
	}
	
	public void product(Matrix mat) {
		if(this.column != mat.row) {
			
		}else {
			
		}
	}
	
	public void product(Vector vec) {
		if(this.column != vec.size()) {
			
		}else {
			
		}
	}
	
	public boolean isScalar() {
		return this.scalar;
	}
	
	public int length() {
		return this.row*this.column;
	}
	
	public void print() {
		for(int i=0;i<row;i++) {
			for(int j=0;j<column;j++) {
				System.out.print(" a["+i+"]["+j+"]="+elements[i][j]);
			}
		}
	}
}
