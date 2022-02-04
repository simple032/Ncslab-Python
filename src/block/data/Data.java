package block.data;

import Jama.Matrix;

public class Data {
	
	private DataType dataType=DataType.REAL;
	private double initValue=0;
	private Matrix initMatrix=null;
	
	private static String formatDataString(String dataString) {
		dataString=dataString.trim();
		return dataString;
	}
	
	public static boolean isStringMatrix(String matrixString) {
		
		if(matrixString.startsWith("[")&&matrixString.endsWith("]")) {
			return true;
		}
		
		return false;
	}
	
	public int getWidth() {
		switch(this.getDataType()) {
		case REAL:
			return 1;
		case MATRIX:
			return initMatrix.getColumnDimension();
		}
		
		return 1;
	}
	
	public int getHeight() {
		switch(this.getDataType()) {
		case REAL:
			return 1;
		case MATRIX:
			return initMatrix.getRowDimension();
		}
		
		return 1;
	}
	
	private static Matrix parseMatrix(String matrixString) {
		matrixString=matrixString.replaceAll("\\[\\s*", "");
		matrixString=matrixString.replaceAll("\\s*\\]", "");
		
		String[] parentMat = matrixString.split("\\s*;\\s*");
	    double[][] childMat = new double[parentMat.length][];
	    for (int i = 0; i < parentMat.length; i++) {
	        String[] child = parentMat[i].split("(\\s*\\,\\s*)|(\\s+)");
	        childMat[i] = new double[child.length];
	        for (int j = 0; j < child.length; j++) {
	        	String doubleString=child[j].replaceAll("\\s+", "");
	            childMat[i][j] = Double.parseDouble(doubleString);
	        }
	    }
	    
	    return new Matrix(childMat);
	}
	
	public Data(int height,int width) {
		if(height>1||width>1) {
			this.dataType=DataType.MATRIX;
			initMatrix=new Matrix(height,width);
		}
		else {
			initValue=0;
		}
	}
	
	public Data() {
		this(1,1);
	}
	
	public Data(String dataString) {
		
		dataString=formatDataString(dataString);
		
		try {
			initValue=Double.parseDouble(dataString);
			dataType=DataType.REAL;
			
			return;
		}
		catch(NumberFormatException e) {
			
		}
		
		if(isStringMatrix(dataString)) {
			System.out.println("Matrix: "+dataString);
			dataType=DataType.MATRIX;
			
			initMatrix=parseMatrix(dataString);
		}
		
	}
	
	public double getInitValue() {
		return this.initValue;
	}
	
	public String getInitCodeM(String name) {
		String code="";
		switch(this.dataType) {
		case REAL:
			code+=name+"="+initValue+";\n";
			break;
		case MATRIX:
			for(int i=0;i<initMatrix.getRowDimension();i++) {
				for(int j=0;j<initMatrix.getColumnDimension();j++) {
					code+=name+"("+(i+1)+","+(j+1)+")="+initMatrix.get(i, j)+";\n";
				}
			}
			break;
		}
		return code;
	}
	
	public boolean isZero() {
		boolean zero=true;
		switch(this.getDataType()) {
		case REAL:
			if(initValue==0) {
				zero=true;
			}
			else {
				zero=false;
			}
			break;
		case MATRIX:
			for(int i=0;i<initMatrix.getRowDimension();i++) {
				for(int j=0;j<initMatrix.getColumnDimension();j++) {
					if(initMatrix.get(i,j)!=0) {
						zero=false;
					}
				}
			}
			break;
		}
		
		return zero;
	}
	
	public String getInitCodeC(String name) {
		String code="";
		
		switch(this.dataType) {
		case REAL:
			code+=name+"="+initValue+";\n";
			break;
		case MATRIX:
			//code+=name+"=0.000000"+";\n";
			for(int i=0;i<initMatrix.getRowDimension();i++) {
				for(int j=0;j<initMatrix.getColumnDimension();j++) {
					code+=name+"("+i+","+j+")="+initMatrix.get(i, j)+";\n";
				}
			}
			
			break;
		}
		
		return code;
	}
	
	public String getDefineCodeC(String name) {
		String code="";
		switch(dataType) {
		case REAL:
			code+="REAL "+name+";\n";
			break;
		case MATRIX:
			//code+="REAL "+name+"["+initMatrix.getRowDimension()+"]["+initMatrix.getColumnDimension()+"]"+";\n";
			code+="Matrix "+name+"("+initMatrix.getRowDimension()+","+initMatrix.getColumnDimension()+")"+";\n";
			break;
		}
		return code;
		
	}
	
	public DataType getDataType() {
		return this.dataType;
	}
	
}
