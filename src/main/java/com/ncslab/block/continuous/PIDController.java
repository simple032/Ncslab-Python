package com.ncslab.block.continuous;

import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;

import java.util.Vector;

public class PIDController extends Block {

	Parameter cparaP;
	Parameter cparaI;
	Parameter cparaD;
	Parameter cparaN;
    Parameter limitOutput;
	Parameter lowerSaturationLimit = null;
	Parameter upperSaturationLimit = null;
	State stateIntegral;
	State stateFilter;

    Parameter externalReset;
    Parameter sampleTime;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("P");
        parameterNames.add("I");
        parameterNames.add("D");
        parameterNames.add("N");
        parameterNames.add("LimitOutput");
        parameterNames.add("LowerSaturationLimit");
        parameterNames.add("UpperSaturationLimit");
        parameterNames.add("AntiWindupMode");
        parameterNames.add("Kb");
        parameterNames.add("ZeroCross");
        outputNames.add("out1");
        inputNames.add("in1");
    }

	public PIDController(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		// 一个输入，一个输出
		inputPortList.add(new InputPort(this, 1));
		outputPortList.add(new OutputPort(this, 1, true));
		cparaP = new Parameter(this, parameterList.size() + 1, "P", paramValues.getString("P"));
		parameterList.add(cparaP);
		cparaI = new Parameter(this, parameterList.size() + 1, "I", paramValues.getString("I"));
		parameterList.add(cparaI);
		cparaD = new Parameter(this, parameterList.size() + 1, "D", paramValues.getString("D"));
		parameterList.add(cparaD);
		cparaN = new Parameter(this, parameterList.size() + 1, "N", paramValues.getString("N"));
		parameterList.add(cparaN);


        stateIntegral = new State(this, 1, "stateIntegral", cparaP.getHeight(), cparaP.getWidth());
        stateFilter = new State(this, 2, "stateFilter", cparaP.getHeight(), cparaP.getWidth());

        stateList.add(stateIntegral);
        stateList.add(stateFilter);

        limitOutput = new Parameter(this, parameterList.size() + 1, "LimitOutput", paramValues.getString("LimitOutput"));
        parameterList.add(limitOutput);
		if (limitOutput.equals("on")) {
			lowerSaturationLimit = new Parameter(this, parameterList.size() + 1, "LowerSaturationLimit",
					paramValues.getString("LowerSaturationLimit"));
			parameterList.add(lowerSaturationLimit);
			upperSaturationLimit = new Parameter(this, parameterList.size() + 1, "UpperSaturationLimit",
					paramValues.getString("UpperSaturationLimit"));
			parameterList.add(upperSaturationLimit);
		}

		// 兼容旧版本，设置成0.01s
        sampleTime=new Parameter(this,parameterList.size()+1,"sampleTime",paramValues.optString("sampleTime", "0.01"));
        parameterList.add(sampleTime);

        // 兼容旧版本，设置成off
		externalReset=new Parameter(this,parameterList.size()+1,"externalReset",paramValues.optString("externalReset", "off"));
        parameterList.add(externalReset);
        if(externalReset.equals("on")) {
            inputPortList.add(new InputPort(this,2));
        }
	}

	// define arrays to save data
	@Override
	public void generateArraysCodeC(CodeStructC code) {
		String arraysCode = "/*Define arrays for block discrete_Delay:(" + getBlockId() + ")" + getBlockName() + "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		// arraysCode+="double "+"Block"+getBlockId()+"save_data[5];\n";
		if (signal.getDataType() == DataType.MATRIX) {
			arraysCode += "double " + "Block" + getBlockId() + "save_data[" + signal.getHeight() + "]["
					+ signal.getWidth() + "*5];\n";
		} else if (signal.getDataType() == DataType.REAL && cparaP.getDataType() == DataType.REAL) {
			arraysCode += "double " + "Block" + getBlockId() + "save_data[5];\n";
		} else {
			arraysCode += "double " + "Block" + getBlockId() + "save_data[" + cparaP.getHeight() + "]["
					+ cparaP.getWidth() + "*5];\n";
		}
		code.addArraysCode(arraysCode);
	}

	@Override
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
        context.put("block", this); // 当前Block对象（包含blockId和blockName）
        context.put("cparaP", cparaP); // P参数对象
        context.put("cparaI", cparaI); // I参数对象
        context.put("cparaD", cparaD); // D参数对象
        context.put("cparaN", cparaN); // 噪声参数对象
        context.put("limitOutput", limitOutput); // 输出限制开关
        context.put("lowerSaturationLimit", lowerSaturationLimit); // 下限对象
        context.put("upperSaturationLimit", upperSaturationLimit); // 上限对象
        context.put("stateIntegral", stateIntegral); // 积分状态对象
        context.put("stateFilter", stateFilter); // 滤波状态对象
        context.put("realDataType", DataType.REAL); // 实数类型标识

        String codeStr = TemplateManager.renderTemplate("c/continuous/PIDController/init.vm", context);
        code.addInitCode(codeStr);
	}


	@Override
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode = "/*Code for output of block PID Controller:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (cparaP.getDataType()) {
			case REAL:
				switch (signal.getDataType()) {
					case REAL:
						outputCode += "if(sfcnIsMajorStep()) {\n";
						outputCode += "Block" + getBlockId() + "save_data[0]=" + cparaP.getName() + "*"
								+ signal.getName() + ";\n";
						outputCode += "Block" + getBlockId() + "save_data[1]=" + cparaD.getName() + "*"
								+ signal.getName() + ";\n";
						outputCode += "Block" + getBlockId() + "save_data[2]=" + cparaI.getName() + "*"
								+ signal.getName() + ";\n";
						outputCode += "Block" + getBlockId() + "save_data[3]=(Block" + getBlockId() + "save_data[1]-"
								+ stateFilter.getName() + ")*" + cparaN.getName() + ";\n";
						outputCode += "Block" + getBlockId() + "save_data[4]=" + "Block" + getBlockId() + "save_data[0]"
								+ "+" + stateIntegral.getName() + "+Block" + getBlockId() + "save_data[3]" + ";\n";

                        //zhou_20240507 add externalReset
                        if(externalReset.equals("on")) {
                            outputCode+="if("+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"){\n";
                            outputCode+="memset(Block"+getBlockId()+"save_data, 0, sizeof(Block"+getBlockId()+"save_data));\n";
                            outputCode+="}\n";
                        }

                        if (limitOutput.equals("on")) {
							outputCode += "if(Block" + getBlockId() + "save_data[4]>" + upperSaturationLimit.getName()
									+ "){\n";
							outputCode += this.getOutputPortVariable(0) + "=" + upperSaturationLimit.getName() + ";}\n";
							outputCode += "else if(Block" + getBlockId() + "save_data[4]<"
									+ lowerSaturationLimit.getName() + "){\n";
							outputCode += this.getOutputPortVariable(0) + "=" + lowerSaturationLimit.getName() + ";}\n";
							outputCode += "else{\n";
							outputCode += this.getOutputPortVariable(0) + "=" + "Block" + getBlockId()
									+ "save_data[4];}\n";
						} else {
							outputCode += this.getOutputPortVariable(0) + "=" + "Block" + getBlockId()
									+ "save_data[4];\n";
						}
						outputCode += "}\n";
						break;
					case MATRIX:
						for (int i = 0; i < signal.getHeight(); i++) {
							for (int j = 0; j < signal.getWidth(); j++) {
								outputCode += "if(sfcnIsMajorStep()) {\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5]="
										+ cparaP.getName() + "*" + signal.getName() + "(" + i + "," + j + ");\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+1]="
										+ cparaD.getName() + "*" + signal.getName() + "(" + i + "," + j + ");\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+2]="
										+ cparaI.getName() + "*" + signal.getName() + "(" + i + "," + j + ");\n";
								outputCode += "}\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+3]=(Block"
										+ getBlockId() + "save_data[" + i + "][" + j + "*5+1]-" + stateFilter.getName()
										+ "(" + i + "," + j + "))*" + cparaN.getName() + ";\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+4]=" + "Block"
										+ getBlockId() + "save_data[" + i + "][" + j + "*5]" + "+"
										+ stateIntegral.getName() + "(" + i + "," + j + ")+Block" + getBlockId()
										+ "save_data[" + i + "][" + j + "*5+3];\n";

                                //zhou_20240507 add externalReset
                                if(externalReset.equals("on")) {
                                    outputCode+="if("+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"){\n";
                                    outputCode+="memset(Block"+getBlockId()+"save_data, 0, sizeof(Block"+getBlockId()+"save_data));\n";
                                    outputCode+="}\n";
                                }

                                if (limitOutput.equals("on")) {
									outputCode += "if(Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+4]>"
											+ upperSaturationLimit.getName() + "){\n";
									outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")="
											+ upperSaturationLimit.getName() + ";}\n";
									outputCode += "else if(Block" + getBlockId() + "save_data[" + i + "][" + j
											+ "*5+4]<" + lowerSaturationLimit.getName() + "){\n";
									outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")="
											+ lowerSaturationLimit.getName() + ";}\n";
									outputCode += "else{\n";
									outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")=" + "Block"
											+ getBlockId() + "save_data[" + i + "][" + j + "*5+4];}\n";
								} else {
									outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")=" + "Block"
											+ getBlockId() + "save_data[" + i + "][" + j + "*5+4];\n";
								}
								outputCode += "}\n";
							}
						}
						break;
				}
				break;
			case MATRIX:
				switch (signal.getDataType()) {
					case REAL:
						for (int i = 0; i < cparaP.getHeight(); i++) {
							for (int j = 0; j < cparaP.getWidth(); j++) {
								outputCode += "if(sfcnIsMajorStep()) {\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5]="
										+ cparaP.getName() + "(" + i + "," + j + ")*" + signal.getName() + ";\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+1]="
										+ cparaD.getName() + "(" + i + "," + j + ")*" + signal.getName() + ";\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+2]="
										+ cparaI.getName() + "(" + i + "," + j + ")*" + signal.getName() + ";\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+3]=(Block"
										+ getBlockId() + "save_data[" + i + "][" + j + "*5+1]-" + stateFilter.getName()
										+ "(" + i + "," + j + "))*" + cparaN.getName() + "(" + i + "," + j + ");\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+4]=" + "Block"
										+ getBlockId() + "save_data[" + i + "][" + j + "*5]" + "+"
										+ stateIntegral.getName() + "(" + i + "," + j + ")+Block" + getBlockId()
										+ "save_data[" + i + "][" + j + "*5+3];\n";

                                //zhou_20240507 add externalReset
                                if(externalReset.equals("on")) {
                                    outputCode+="if("+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"){\n";
                                    outputCode+="memset(Block"+getBlockId()+"save_data, 0, sizeof(Block"+getBlockId()+"save_data));\n";
                                    outputCode+="}\n";
                                }

                                outputCode = generateLimitOutputCode(outputCode, i, j);
                            }
						}
						break;
					case MATRIX:
						for (int i = 0; i < cparaP.getHeight(); i++) {
							for (int j = 0; j < cparaP.getWidth(); j++) {
								outputCode += "if(sfcnIsMajorStep()) {\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5]="
										+ cparaP.getName() + "(" + i + "," + j + ")*" + signal.getName() + "(" + i + ","
										+ j + ");\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+1]="
										+ cparaD.getName() + "(" + i + "," + j + ")*" + signal.getName() + "(" + i + ","
										+ j + ");\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+2]="
										+ cparaI.getName() + "(" + i + "," + j + ")*" + signal.getName() + "(" + i + ","
										+ j + ");\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+3]=(Block"
										+ getBlockId() + "save_data[" + i + "][" + j + "*5+1]-" + stateFilter.getName()
										+ "(" + i + "," + j + "))*" + cparaN.getName() + "(" + i + "," + j + ");\n";
								outputCode += "Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+4]=" + "Block"
										+ getBlockId() + "save_data[" + i + "][" + j + "*5]" + "+"
										+ stateIntegral.getName() + "(" + i + "," + j + ")+Block" + getBlockId()
										+ "save_data[" + i + "][" + j + "*5+3];\n";

                                //zhou_20240507 add externalReset
                                if(externalReset.equals("on")) {
                                    outputCode+="if("+inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+"){\n";
                                    outputCode+="memset(Block"+getBlockId()+"save_data, 0, sizeof(Block"+getBlockId()+"save_data));\n";
                                    outputCode+="}\n";
                                }

                                outputCode = generateLimitOutputCode(outputCode, i, j);
                            }
						}
						break;
				}
				break;
		}
		code.addOutputCode(outputCode);
	}

    private String generateLimitOutputCode(String outputCode, int i, int j) {
        if (limitOutput.equals("on")) {
            outputCode += "if(Block" + getBlockId() + "save_data[" + i + "][" + j + "*5+4]>"
                    + upperSaturationLimit.getName() + "(" + i + "," + j + ")){\n";
            outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")="
                    + upperSaturationLimit.getName() + "(" + i + "," + j + ");}\n";
            outputCode += "else if(Block" + getBlockId() + "save_data[" + i + "][" + j
                    + "*5+4]<" + lowerSaturationLimit.getName() + "(" + i + "," + j + ")){\n";
            outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")="
                    + lowerSaturationLimit.getName() + "(" + i + "," + j + ");}\n";
            outputCode += "else{\n";
            outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")=" + "Block"
                    + getBlockId() + "save_data[" + i + "][" + j + "*5+4];}\n";
        } else {
            outputCode += this.getOutputPortVariable(0) + "(" + i + "," + j + ")=" + "Block"
                    + getBlockId() + "save_data[" + i + "][" + j + "*5+4];\n";
        }
        outputCode += "}\n";
        return outputCode;
    }

    // TODO:待迁移到Velocity
//    @Override
//    public void generateOutputCodeC(CodeStructC code) {
//        VelocityContext context = new VelocityContext();
//        context.put("block", this); // Block对象（含getBlockId()）
//        context.put("inputPortList", inputPortList); // 输入端口列表（至少包含2个端口，若启用externalReset）
//        context.put("cparaP", cparaP); // P参数对象（含getDataType()、getHeight()、getWidth()）
//        context.put("cparaD", cparaD); // D参数对象
//        context.put("cparaI", cparaI); // I参数对象
//        context.put("cparaN", cparaN); // N参数对象
//        context.put("stateIntegral", stateIntegral); // 积分状态对象（含getName()、getDerivativeName()）
//        context.put("stateFilter", stateFilter); // 滤波状态对象
//        context.put("upperSaturationLimit", upperSaturationLimit); // 上限对象
//        context.put("lowerSaturationLimit", lowerSaturationLimit); // 下限对象
//        context.put("externalReset", externalReset); // 外部重置开关（"on"/"off"）
//        context.put("limitOutput", limitOutput); // 限幅开关（"on"/"off"）
//        context.put("realDataType", DataType.REAL); // 实数类型标识
//        context.put("matrixDataType", DataType.MATRIX); // 矩阵类型标识
//        String outputCode = TemplateManager.renderTemplate("c/continuous/PIDController/output.vm", context);
//        code.addOutputCode(outputCode);
//    }

    @Override
	public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this); // 当前Block对象（含getBlockId()）
        context.put("inputPortList", inputPortList); // 输入端口列表
        context.put("cparaP", cparaP); // P参数对象（含getDataType()）
        context.put("stateIntegral", stateIntegral); // 积分状态对象（含getName()、getDerivativeName()、getHeight()等）
        context.put("stateFilter", stateFilter); // 滤波状态对象
        context.put("realDataType", DataType.REAL); // 实数类型标识

        String derivativeCode = TemplateManager.renderTemplate("c/continuous/PIDController/derivative.vm", context);
		code.addDerivativeCode(derivativeCode);
	}

	@Override
	public void generateUpdateCodeC(CodeStructC code) {
        context.put("block", this); // 当前Block对象（含getBlockId()）
        context.put("inputPortList", inputPortList); // 输入端口列表
        context.put("cparaP", cparaP); // P参数对象（含getDataType()）
        context.put("stateIntegral", stateIntegral); // 积分状态对象（含getName()、getDerivativeName()、getHeight()等）
        context.put("stateFilter", stateFilter); // 滤波状态对象
        context.put("realDataType", DataType.REAL); // 实数类型标识

        String updateCode = TemplateManager.renderTemplate("c/continuous/PIDController/update.vm", context);
		code.addUpdateCode(updateCode);
	}

	@Override
	public void updateDimension() throws MatDimException {
		OutputPort out = outputPortList.get(0);
		InputPort in = inputPortList.get(0);
		OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if (signal.getDataType() != DataType.REAL) {
			stateIntegral = new State(this, 1, "stateIntegral", signal.getHeight(), signal.getWidth());
			stateFilter = new State(this, 2, "stateFilter", signal.getHeight(), signal.getWidth());
		}
		stateList.set(0, stateIntegral);
		stateList.set(1, stateFilter);
		if (cparaP.getWidth() != cparaD.getWidth() ||
				cparaP.getWidth() != cparaI.getWidth() ||
				cparaP.getWidth() != cparaN.getWidth() ||
				cparaP.getHeight() != cparaD.getHeight() ||
				cparaP.getHeight() != cparaI.getHeight() ||
				cparaP.getHeight() != cparaN.getHeight()) {
			MatDimException e = new MatDimException("Block " + this.blockName
					+ " input dimension doesn't match!All input dimension must be same!\n \n");
			throw (e);
		}
		if (limitOutput.equals("on")) {
			if (lowerSaturationLimit.getHeight() != cparaP.getHeight() ||
					upperSaturationLimit.getHeight() != cparaP.getHeight() ||
					lowerSaturationLimit.getWidth() != cparaP.getWidth() ||
					upperSaturationLimit.getWidth() != cparaP.getWidth()) {
				MatDimException e = new MatDimException("Block " + this.blockName
						+ " input dimension doesn't match!All input dimension must be same!\n \n");
				throw (e);
			}
		}

		if (cparaP.getDataType() == DataType.MATRIX && signal.getDataType() == DataType.REAL) {
			out.setHeight(cparaP.getHeight());
			out.setWidth(cparaP.getWidth());
			out.getOutputSignalC().setHeight(cparaP.getHeight());
			out.getOutputSignalC().setWidth(cparaP.getWidth());
			out.getOutputSignalC().setDataType(DataType.MATRIX);
		} else if (cparaP.getDataType() == DataType.REAL && signal.getDataType() == DataType.MATRIX) {
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
		} else {
			if (cparaP.getWidth() != signal.getWidth() || cparaP.getHeight() != signal.getHeight()) {
				MatDimException e = new MatDimException(
						"Block " + this.blockName + " input dimension doesn't match the P dimension!\n \n");
				throw (e);
			}
			out.setHeight(cparaP.getHeight());
			out.setWidth(cparaP.getWidth());
			out.getOutputSignalC().setHeight(cparaP.getHeight());
			out.getOutputSignalC().setWidth(cparaP.getWidth());
			out.getOutputSignalC().setDataType(cparaP.getDataType());
		}
	}

	// public void checkDimension() throws MatDimException {}
}
