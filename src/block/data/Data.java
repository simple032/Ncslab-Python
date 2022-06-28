package block.data;

import Jama.Matrix;
import com.greenpineyu.fel.*;

/*所有数据的通用类，包括Signal, Parameter和State，支持标量和Matrix*/
public class Data {
	
	private DataType dataType=DataType.REAL;
	private double initValue=0;
	private Matrix initMatrix=null;
	
	static FelEngine fel=new FelEngineImpl();
	static {
		setupFel();
	}
	
	static private void setupFel() {
		fel.getContext().set("pi", 3.1415926);
	}
	
	public Data() {
		this(1,1);
	}
	
	public Data(int height,int width) {
		//setupFel();
		
		if(height>1||width>1) {
			this.dataType=DataType.MATRIX;
			initMatrix=new Matrix(height,width);
		}
		else {
			initValue=0;
		}
		
	}
	
	/*根据从前端传递来的字符串建立数据*/
	public Data(String dataString) {
		
		//setupFel();
		
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
		else {
			//使用fel进行表达式分析
			initValue=Double.parseDouble(fel.eval(dataString).toString());
		}
		
	}

	
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
	
	/*使用正则表达式来解析矩阵，实验性的*/
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
	            //childMat[i][j] = Double.parseDouble(doubleString);
	        	//使用fel进行表达式分析
	        	childMat[i][j] = Double.parseDouble(fel.eval(doubleString).toString());
	        }
	    }
	    
	    return new Matrix(childMat);
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
	
	/*检查这个数据是否为0，在传入参数的时候比较有效*/
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
