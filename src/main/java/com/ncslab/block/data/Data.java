package com.ncslab.block.data;

import Jama.Matrix;
//import com.greenpineyu.fel.*;
import com.ncslab.expression.ExpressionCalculator;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

/*所有数据的通用类，包括Signal, Parameter和State，支持标量和Matrix*/
public class Data {

	@Getter
    private DataType dataType = DataType.REAL;
	@Getter
    @Setter
    private double initValue = 0;
    @Getter
    private int intValue = 0;
	private Matrix initMatrix = null;
	@Getter
	private String initString = "";
    @Getter
    private String dataString = "";

//	static FelEngine fel = new FelEngineImpl();
//	static {
//		setupFel();
//	}
//
//	static private void setupFel() {
//		fel.getContext().set("pi", 3.1415926);
//	}

	public Data() {
		this(1, 1);
	}

	public Data(int height, int width) {
		// setupFel();

		if (height > 1 || width > 1) {
			this.dataType = DataType.MATRIX;
			initMatrix = new Matrix(height, width);
		} else {
			initValue = 0;
		}

	}

	/* 根据从前端传递来的字符串建立数据 */
	public Data(String inString) {

		// setupFel();
        dataString = formatDataString(inString);

		if (isStringMatrix(dataString)) {
			System.out.println("Matrix: " + dataString);
			dataType = DataType.MATRIX;

			initMatrix = parseMatrix(dataString);
		} else {
			try {
                initValue = parseExpression(dataString);
                intValue = (int) initValue;
			} catch (NumberFormatException|org.apache.commons.jexl3.JexlException e) {
				// 如果解析失败，将 initString 设置为 dataString
				initString = dataString;
			}
		}
	}

    // 用于检查字符串是否为有效的数字
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(\\.\\d*)?([eE][-+]?\\d+)?");
    private static double parseExpression(String inString) throws NumberFormatException {
        double value;
        if (NUMBER_PATTERN.matcher(inString).matches()) {
            value = Double.parseDouble(inString);
        } else {
            Object result = ExpressionCalculator.calculateExpression(inString);
            value = Double.parseDouble(result.toString());
        }
        return value;
    }

	private static String formatDataString(String dataString) {
		return dataString.trim();
	}

	public static boolean isStringMatrix(String matrixString) {
        return matrixString.startsWith("[") && matrixString.endsWith("]");
    }

	public int getWidth() {
		switch (this.getDataType()) {
			case REAL:
				return 1;
			case MATRIX:
				return initMatrix.getColumnDimension();
		}

		return 1;
	}

	public int getHeight() {
		switch (this.getDataType()) {
			case REAL:
				return 1;
			case MATRIX:
				return initMatrix.getRowDimension();
		}

		return 1;
	}

	/* 使用正则表达式来解析矩阵，实验性的 */
	private static Matrix parseMatrix(String matrixString) {
		matrixString = matrixString.replaceAll("\\[\\s*", "");
		matrixString = matrixString.replaceAll("\\s*\\]", "");

		String[] parentMat = matrixString.split("\\s*;\\s*");
		double[][] childMat = new double[parentMat.length][];
		for (int i = 0; i < parentMat.length; i++) {
			String[] child = parentMat[i].split("(\\s*\\,\\s*)|(\\s+)");
			childMat[i] = new double[child.length];
			for (int j = 0; j < child.length; j++) {
				String doubleString = child[j].replaceAll("\\s+", "");
				 childMat[i][j] = parseExpression(doubleString);
				// 使用fel进行表达式分析
//				childMat[i][j] = Double.parseDouble(fel.eval(doubleString).toString());
			}
		}

		return new Matrix(childMat);
	}

    public String getInitCodeM(String name) {
		String code = "";
		switch (this.dataType) {
			case REAL:
				code += name + "=" + initValue + ";\n";
				break;
			case MATRIX:
				for (int i = 0; i < initMatrix.getRowDimension(); i++) {
					for (int j = 0; j < initMatrix.getColumnDimension(); j++) {
						code += name + "(" + (i + 1) + "," + (j + 1) + ")=" + initMatrix.get(i, j) + ";\n";
					}
				}
				break;
		}
		return code;
	}

	/* 检查这个数据是否为0，在传入参数的时候比较有效 */
	public boolean isZero() {
		boolean zero = true;
		switch (this.getDataType()) {
			case REAL:
				if (initValue == 0) {
					zero = true;
				} else {
					zero = false;
				}
				break;
			case MATRIX:
				for (int i = 0; i < initMatrix.getRowDimension(); i++) {
					for (int j = 0; j < initMatrix.getColumnDimension(); j++) {
						if (initMatrix.get(i, j) != 0) {
							zero = false;
						}
					}
				}
				break;
		}

		return zero;
	}

	public String getInitCodeC(String name) {
		StringBuilder code = new StringBuilder();

		switch (this.dataType) {
			case REAL:
				code.append(String.format("%s = %f;\n", name, initValue));
				break;
			case MATRIX:
				for (int i = 0; i < initMatrix.getRowDimension(); i++) {
					for (int j = 0; j < initMatrix.getColumnDimension(); j++) {
						code.append(String.format("%s(%d,%d) = %f;\n", name, i, j, initMatrix.get(i, j)));
					}
				}
				break;
		}

		return code.toString();
	}

	public String getDefineCodeC(String name) {
		String code = "";
		switch (dataType) {
			case REAL:
				code += String.format("REAL %s;\n", name);
				break;
			case MATRIX:
				// code+="REAL
				// "+name+"["+initMatrix.getRowDimension()+"]["+initMatrix.getColumnDimension()+"]"+";\n";
				code += String.format("Matrix %s(%d,%d);\n", name, initMatrix.getRowDimension(),
						initMatrix.getColumnDimension());
				break;
		}
		return code;

	}

}
