package com.ncslab.block.data;

import Jama.Matrix;
//import com.greenpineyu.fel.*;
import com.ncslab.code.m.MfcalcClient;
import com.ncslab.code.m.MfcalcClientManager;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.dto.communication.MfcalcVariableDto;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;

import java.util.ArrayList;
import java.util.List;

/*所有数据的通用类，包括Signal, Parameter和State，支持标量和Matrix*/
@Slf4j
public class Data {
    private Object value;

    @Getter
    private DataType dataType = DataType.REAL;
    public void setInitValue(double initValue) {
        value = initValue;
	}
    public double getInitValue() {
		return parseDoubleWithInfinity(value.toString());
	}
    public int getIntValue() {
		return parseIntegerWithInfinity(value.toString());
	}    

	private Matrix initMatrix = null;
    @Setter
    @Getter
	private String initString = "";
    private String dataString = "";
    public String getDataString() {
        return value.toString();
    }
    public void setDataString(String dataString) {
        value = dataString;
    }

    @Setter
    @Getter
    private static List<String> temp_variable_names = new ArrayList<>();

    VelocityContext context = new VelocityContext();

	public Data() {
		this(1, 1);
	}

	public Data(int height, int width) {
		// setupFel();

		if (height > 1 || width > 1) {
			this.dataType = DataType.MATRIX;
			initMatrix = new Matrix(height, width);
            value = initMatrix;
		} else {
            value = 0;
		}

	}

	/* 根据从前端传递来的字符串建立数据 */
	// public Data(String inString) {

    //     initString = inString.trim();

    //     dataString = initString;
    //     if (isStringMatrix(dataString)) {
    //         System.out.println("Matrix: " + dataString);
    //         dataType = DataType.MATRIX;
    //         initMatrix = parseMatrix(dataString);
    //     } else {
    //         try {
    //             getInitValue() = Double.parseDouble(dataString);
    //             intValue = (int) getInitValue();
    //         } catch (NumberFormatException ee) {
    //             // 如果解析失败，将 initString 设置为 dataString
    //             initString = dataString;
    //         }
    //     }
	// }

    /* 根据从前端传递来的字符串建立数据 */
	public Data(String inString) {

        initString = inString.trim();

        dataString = parseExpression(inString);

        if (isStringMatrix(dataString)) {
            dataType = DataType.MATRIX;
            initMatrix = parseMatrix(dataString);
            value = initMatrix;
            return;
        }

        // FIX: Parse scalar numeric value and set getInitValue() for C code generation
        try {
            value = parseDoubleWithInfinity(dataString);
        } catch (NumberFormatException e) {
            // If not parseable as number, keep as string (for expressions, variable names, etc.)
            value = dataString;
        }
	}

    public Data(Matrix initMatrix) {
        this.initMatrix = initMatrix;
        this.dataType = DataType.MATRIX;
        value = initMatrix;
    }

    public Data(double initValue) {
        value = initValue;
    }

    private static String generateRandomVariableName() {
        // 生成一个随机的变量名
        return "temp_var_" + Math.round(Math.random()*100000000);
    }

	private static String parseExpression(String dataString) {
		// Get user ID from thread-local context (set by WebSocket endpoint)
        String userId = com.ncslab.util.UserContext.getUserId();
        if (userId == null) {
            // Fallback to default user ID for backward compatibility
            userId = "18";
            // Note: Consider adding logging here for debugging in production
            System.out.println("Warning: No user context set in Data.parseExpression(), using default user ID: 18");
        }

        MfcalcClient client = MfcalcClientManager.getClientForUser(userId);
        String result = dataString;

        boolean founded = false;
        if (client != null) {
            List<MfcalcVariableDto> variables = MfcalcClient.getLocalVariables(); 
            if (variables != null) {
                for (MfcalcVariableDto variable : variables) {
                    if (variable.getName().equals(dataString)) {
                        founded = true;
                        result = variable.getValue().toString();
                        break;
                    }
                }
            }
            if(!founded) {
                String variableName = generateRandomVariableName();
                MfcalcResponseDto commandResponse = client.runCommand(variableName + "=" + dataString + ";");
                if (commandResponse != null && commandResponse.isSuccess()) {
                    MfcalcResponseDto variableResponse = client.getVariable(variableName);
                    log.debug("Get variable '{}' response: {}", variableName, variableResponse);
                    if (variableResponse != null && variableResponse.getMessageType().equals("variable_value")){
                        List<MfcalcVariableDto> latestVariables =  variableResponse.getVariables();
                        for(MfcalcVariableDto variableDto : latestVariables) {                            
                            if(variableDto != null && variableDto.getName().equals(variableName)) {
                                result = variableDto.getValue().toString();
                                log.info("Parsed expression '{}' to value: {}", dataString, result);
                                break;
                            }
                        }
                        
                    }
                    client.runCommand("clear " + variableName);
                }                 
                // TODO:将变量名添加到临时变量列表中，以便在程序结束时批量清除，但是M2PCode还无法实现
                // temp_variable_names.add(variableName);
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
				childMat[i][j] = parseDoubleWithInfinity(doubleString);
			}
		}

		return new Matrix(childMat);
	}

	/**
	 * Parse a string to double, handling special infinity values.
	 * Converts "inf", "-inf", "Inf", "-Inf" to proper Java infinity constants.
	 *
	 * @param str String representation of a number or infinity
	 * @return Parsed double value
	 * @throws NumberFormatException if string cannot be parsed
	 */
	private static double parseDoubleWithInfinity(String str) {
		if (str == null || str.isEmpty()) {
			throw new NumberFormatException("Empty string cannot be parsed as double");
		}

		// Normalize the string
		String normalized = str.trim().toLowerCase();

		// Handle infinity cases
		if (normalized.equals("inf") || normalized.equals("infinity") || normalized.equals("+inf")) {
			return Double.POSITIVE_INFINITY;
		} else if (normalized.equals("-inf") || normalized.equals("-infinity")) {
			return Double.NEGATIVE_INFINITY;
		} else if (normalized.equals("nan")) {
			return Double.NaN;
		}

		// Standard parsing for regular numbers
		return Double.parseDouble(str);
	}

	/**
	 * Parse a string to integer, handling special infinity and NaN values.
	 * Converts "inf", "-inf", "nan" to appropriate integer representations.
	 *
	 * Note: Since integers cannot represent infinity or NaN, we map them to:
	 * - "inf" / "+inf" / "Infinity" → Integer.MAX_VALUE
	 * - "-inf" / "-Infinity" → Integer.MIN_VALUE
	 * - "nan" / "NaN" → 0
	 *
	 * @param str String representation of a number or infinity
	 * @return Parsed integer value
	 * @throws NumberFormatException if string cannot be parsed
	 */
	private static int parseIntegerWithInfinity(String str) {
		if (str == null || str.isEmpty()) {
			throw new NumberFormatException("Empty string cannot be parsed as integer");
		}

		// Normalize the string
		String normalized = str.trim().toLowerCase();

		// Handle infinity cases - map to integer bounds
		if (normalized.equals("inf") || normalized.equals("infinity") || normalized.equals("+inf")) {
			return Integer.MAX_VALUE;
		} else if (normalized.equals("-inf") || normalized.equals("-infinity")) {
			return Integer.MIN_VALUE;
		} else if (normalized.equals("nan")) {
			return 0; // NaN maps to 0 for integers
		}

		// Try to parse as double first (handles scientific notation, then convert to int)
		try {
			double doubleValue = Double.parseDouble(str);
			// Check if the double value is within integer range
			if (doubleValue > Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			} else if (doubleValue < Integer.MIN_VALUE) {
				return Integer.MIN_VALUE;
			}
			return (int) doubleValue;
		} catch (NumberFormatException e) {
			// Fall back to direct integer parsing
			return Integer.parseInt(str);
		}
	}

    public String getInitCodeM(String name) {
		String code = "";
		switch (this.dataType) {
			case REAL:
				code += name + "=" + value.toString() + ";\n";
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
				if (getInitValue() == 0) {
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
				code.append(String.format("%s = %f;\n", name, getInitValue()));
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
                result = new Data(-getInitValue());
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
            result = new Data(getInitValue() * data.getInitValue());
        }else if(getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL){
            result = new Data(initMatrix.times(data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            result = new Data(data.getMatrix().times(getInitValue()));
        } else {
            result = new Data(initMatrix.times(data.getMatrix()));
        }
        return result;
    }

    public Data arrayTimes(Data data){
        Data result;
        if (getDataType() == DataType.REAL && data.getDataType() == DataType.REAL) {
            result = new Data(getInitValue() * data.getInitValue());
        }else if(getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL){
            result = new Data(initMatrix.times(data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            result = new Data(data.getMatrix().times(getInitValue()));
        } else {
            result = new Data(initMatrix.arrayTimes(data.getMatrix()));
        }
        return result;
    }

    public Data plus(Data data) {
        Data result;
        if (getDataType() == DataType.REAL && data.getDataType() == DataType.REAL) {
            result = new Data(getInitValue() + data.getInitValue());
        } else if (getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL) {
            // 修复：手动实现矩阵加标量
            result = new Data(addScalar(initMatrix, data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            // 修复：手动实现标量加矩阵
            result = new Data(addScalar(data.getMatrix(), getInitValue()));
        } else {
            result = new Data(initMatrix.plus(data.getMatrix()));
        }
        return result;
    }

    public Data minus(Data data) {
        Data result;
        if (getDataType() == DataType.REAL && data.getDataType() == DataType.REAL) {
            result = new Data(getInitValue() - data.getInitValue());
        } else if (getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL) {
            // 修复：手动实现矩阵减标量
            result = new Data(subtractScalar(initMatrix, data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            // 修复：手动实现标量减矩阵
            result = new Data(subtractMatrixFromScalar(getInitValue(), data.getMatrix()));
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
            result = new Data(getInitValue() / data.getInitValue());
        } else if (getDataType() == DataType.MATRIX && data.getDataType() == DataType.REAL) {
            result = new Data(initMatrix.times(1.0 / data.getInitValue()));
        } else if (getDataType() == DataType.REAL && data.getDataType() == DataType.MATRIX) {
            // 标量除以矩阵：对每个元素进行除法
            Matrix reciprocal = data.getMatrix().copy();
            for (int i = 0; i < reciprocal.getRowDimension(); i++) {
                for (int j = 0; j < reciprocal.getColumnDimension(); j++) {
                    reciprocal.set(i, j, getInitValue() / reciprocal.get(i, j));
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
            result = new Data(Math.pow(getInitValue(), exponent));
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
            return new Data(1.0 / getInitValue());
        } else {
            return new Data(initMatrix.inverse());
        }
    }

    public double determinant() {
        if (getDataType() == DataType.REAL) {
            return getInitValue();
        } else {
            return initMatrix.det();
        }
    }

    public double trace() {
        if (getDataType() == DataType.REAL) {
            return getInitValue();
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
            return new Data(Math.abs(getInitValue()));
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
