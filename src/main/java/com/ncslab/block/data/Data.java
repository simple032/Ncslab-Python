package com.ncslab.block.data;

import Jama.Matrix;
//import com.greenpineyu.fel.*;
import com.ncslab.code.m.MfcalcClient;
import com.ncslab.code.m.MfcalcClientManager;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.jexl3.JexlException;
import org.apache.velocity.VelocityContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*所有数据的通用类，包括Signal, Parameter和State，支持标量和Matrix*/
public class Data {

    @Getter
    private DataType dataType = DataType.REAL;
    @Setter
    @Getter
    private double initValue = 0;
    @Setter
    @Getter
    private int intValue = 0;

	private Matrix initMatrix = null;
    @Setter
    @Getter
	private String initString = "";
    @Setter
    @Getter
    private String dataString = "";

    @Setter
    @Getter
    private static List<String> temp_variable_names = new ArrayList<>();

    VelocityContext context = new VelocityContext();

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

        initString = inString.trim();

        dataString = parseExpression(inString);
        if (isStringMatrix(dataString)) {
            System.out.println("Matrix: " + dataString);
            dataType = DataType.MATRIX;
            initMatrix = parseMatrix(dataString);
        } else {
            try {
                initValue = Double.parseDouble(dataString);
                intValue = (int) initValue;
            } catch (NumberFormatException | JexlException ee) {
                // 如果解析失败，将 initString 设置为 dataString
                initString = dataString;
            }
        }
	}

    public Data(Matrix initMatrix) {
        this.initMatrix = initMatrix;
        this.dataType = DataType.MATRIX;
    }

    public Data(double initValue) {
        this.initValue = initValue;
    }

    private static String generateRandomVariableName() {
        // 生成一个随机的变量名
        return "temp_var_" + Math.round(Math.random()*100000000);
    }

	private static String parseExpression(String dataString) {
		// 使用M2PCode解析表达式
        MfcalcClient client = MfcalcClientManager.getClientForUser("18");
        String result = dataString;

        boolean founded = false;
        if (client != null) {
            JSONArray variables = MfcalcClient.getLocalVariables();
            if (variables != null) {
                for(int i=0;i<variables.length();i++) {
                    JSONObject variable = variables.getJSONObject(i);
                    if(variable.getString("name").equals(dataString)){
                        founded = true;
                        result = variable.getString("value");
                        break;
                    }
                }
            }
            if(!founded) {
                String variableName = generateRandomVariableName();
                JSONObject jo = client.runCommand(variableName + "=" + dataString + ";\n");

                if (jo != null && jo.getString("status").equals("success")) {
                    JSONObject variable = client.getVariable(variableName).getJSONObject("data");
                    if (variable.getString("name").equals(variableName)) {
                        result = variable.getString("value");
                    }
                }else{
                    result = dataString;
                }
                client.runCommand("clear " + variableName + "\n");
                // TODO:将变量名添加到临时变量列表中，以便在程序结束时批量清除，但是M2PCode还无法实现
//                temp_variable_names.add(variableName);
            }
        }
        return result.trim();
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
				 childMat[i][j] = Double.parseDouble(doubleString);
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

		context.put("name", name);
		context.put("dataType", dataType);
		context.put("realDataType", DataType.REAL);
		context.put("matrixDataType", DataType.MATRIX);
		if (dataType == DataType.MATRIX) {
			context.put("rows", initMatrix.getRowDimension());
			context.put("cols", initMatrix.getColumnDimension());
		}

		return TemplateManager.renderTemplate("c/data/Data/define.vm", context);
	}

	public double[] getDoubleArray() {
		// 获取二维数组
        double[][] array2D = initMatrix.getArray();

        // 提取第一行作为一维数组
        return array2D[0];
	}

	public double[][] getDoubleMatrix() {
		return initMatrix.getArray();
	}

    public Matrix getMatrix() {
        return initMatrix;
    }

    public void setMatrix(Matrix matrix) {
        this.initMatrix = matrix;
    }

    public Data negative() {
        Data result = null;
        switch (this.getDataType()) {
            case REAL:
                result = new Data(-initValue);
                break;
            case MATRIX:
                result = new Data(initMatrix.times(-1));
                break;
        }
        return result;
    }

    public Data times(Data data){
        Data result;
        if (getDataType() == DataType.REAL && data.getDataType() == DataType.REAL) {
            result = new Data(initValue * data.getInitValue());
        }else if(getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL){
            result = new Data(initMatrix.times(data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            result = new Data(data.getMatrix().times(initValue));
        } else {
            result = new Data(initMatrix.times(data.getMatrix()));
        }
        return result;
    }

    public Data arrayTimes(Data data){
        Data result;
        if (getDataType() == DataType.REAL && data.getDataType() == DataType.REAL) {
            result = new Data(initValue * data.getInitValue());
        }else if(getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL){
            result = new Data(initMatrix.times(data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            result = new Data(data.getMatrix().times(initValue));
        } else {
            result = new Data(initMatrix.arrayTimes(data.getMatrix()));
        }
        return result;
    }

    public Data plus(Data data) {
        Data result;
        if (getDataType() == DataType.REAL && data.getDataType() == DataType.REAL) {
            result = new Data(initValue + data.getInitValue());
        } else if (getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL) {
            // 修复：手动实现矩阵加标量
            result = new Data(addScalar(initMatrix, data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            // 修复：手动实现标量加矩阵
            result = new Data(addScalar(data.getMatrix(), initValue));
        } else {
            result = new Data(initMatrix.plus(data.getMatrix()));
        }
        return result;
    }

    public Data minus(Data data) {
        Data result;
        if (getDataType() == DataType.REAL && data.getDataType() == DataType.REAL) {
            result = new Data(initValue - data.getInitValue());
        } else if (getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL) {
            // 修复：手动实现矩阵减标量
            result = new Data(subtractScalar(initMatrix, data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            // 修复：手动实现标量减矩阵
            result = new Data(subtractMatrixFromScalar(initValue, data.getMatrix()));
        } else {
            result = new Data(initMatrix.minus(data.getMatrix()));
        }
        return result;
    }

    // 辅助方法：矩阵加标量
    private Matrix addScalar(Matrix matrix, double scalar) {
        Matrix result = matrix.copy();
        for (int i = 0; i < result.getRowDimension(); i++) {
            for (int j = 0; j < result.getColumnDimension(); j++) {
                result.set(i, j, result.get(i, j) + scalar);
            }
        }
        return result;
    }

    // 辅助方法：矩阵减标量
    private Matrix subtractScalar(Matrix matrix, double scalar) {
        Matrix result = matrix.copy();
        for (int i = 0; i < result.getRowDimension(); i++) {
            for (int j = 0; j < result.getColumnDimension(); j++) {
                result.set(i, j, result.get(i, j) - scalar);
            }
        }
        return result;
    }

    // 辅助方法：标量减矩阵
    private Matrix subtractMatrixFromScalar(double scalar, Matrix matrix) {
        Matrix result = matrix.copy();
        for (int i = 0; i < result.getRowDimension(); i++) {
            for (int j = 0; j < result.getColumnDimension(); j++) {
                result.set(i, j, scalar - result.get(i, j));
            }
        }
        return result;
    }

    public Data divide(Data data) {
        Data result;
        if (getDataType() == DataType.REAL && data.getDataType() == DataType.REAL) {
            result = new Data(initValue / data.getInitValue());
        } else if (getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL) {
            result = new Data(initMatrix.times(1.0 / data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            // 标量除以矩阵：对每个元素进行除法
            Matrix reciprocal = data.getMatrix().copy();
            for (int i = 0; i < reciprocal.getRowDimension(); i++) {
                for (int j = 0; j < reciprocal.getColumnDimension(); j++) {
                    reciprocal.set(i, j, initValue / reciprocal.get(i, j));
                }
            }
            result = new Data(reciprocal);
        } else {
            // 矩阵除以矩阵：逐元素除法
            result = new Data(initMatrix.arrayRightDivide(data.getMatrix()));
        }
        return result;
    }

    public Data power(double exponent) {
        Data result;
        if (getDataType() == DataType.REAL) {
            result = new Data(Math.pow(initValue, exponent));
        } else {
            // 矩阵的幂运算：对每个元素进行幂运算
            Matrix matrix = initMatrix.copy();
            for (int i = 0; i < matrix.getRowDimension(); i++) {
                for (int j = 0; j < matrix.getColumnDimension(); j++) {
                    matrix.set(i, j, Math.pow(matrix.get(i, j), exponent));
                }
            }
            result = new Data(matrix);
        }
        return result;
    }

    public Data transpose() {
        if (getDataType() == DataType.REAL) {
            return this; // 标量的转置是其自身
        } else {
            return new Data(initMatrix.transpose());
        }
    }

    public Data inverse() {
        if (getDataType() == DataType.REAL) {
            return new Data(1.0 / initValue);
        } else {
            return new Data(initMatrix.inverse());
        }
    }

    public double determinant() {
        if (getDataType() == DataType.REAL) {
            return initValue;
        } else {
            return initMatrix.det();
        }
    }

    public double trace() {
        if (getDataType() == DataType.REAL) {
            return initValue;
        } else {
            return initMatrix.trace();
        }
    }

    public int[] size() {
        if (getDataType() == DataType.REAL) {
            return new int[]{1, 1};
        } else {
            return new int[]{initMatrix.getRowDimension(), initMatrix.getColumnDimension()};
        }
    }

    public Data abs(){
        if(getDataType() == DataType.REAL){
            return new Data(Math.abs(initValue));
        }else{
            Data result = new Data(initMatrix.copy());
            for(int i = 0; i < initMatrix.getRowDimension(); i++){
                for(int j = 0; j < initMatrix.getColumnDimension(); j++){
                    result.initMatrix.set(i, j, Math.abs(initMatrix.get(i, j)));
                }
            }
            return result;
        }
    }
}
