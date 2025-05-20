package com.ncslab.block.continuous;

import lombok.Getter;
import org.json.JSONObject;

import Jama.Matrix;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.io.OutputSignal;

import java.util.Vector;

public class TransportDelay extends Block {

	private Parameter initialoutput;
	private Parameter delaytime;
    private Parameter bufferSize;
	OutputPort output;
	InputPort input;
	Matrix test;
	private double number[] = null;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("InitialOutput");
        parameterNames.add("DelayTime");
        parameterNames.add("BufferSize");

        outputNames.add("out1");
        inputNames.add("in1");
    }

	public TransportDelay(JSONObject blockIn, NCSLabModel model) {
		super(blockIn, model);

		initialoutput = new Parameter(this, 1, "InitialOutput", paramValues.getString("InitialOutput"));
		parameterList.add(initialoutput);

		delaytime = new Parameter(this, 2, "DelayTime", paramValues.getString("DelayTime"));
		test = delaytime.getMatrix();
        parameterList.add(delaytime);

        bufferSize = new Parameter(this, 3, "BufferSize", paramValues.optString("BufferSize","1024"));
        parameterList.add(bufferSize);

		input = new InputPort(this, 1);
		inputPortList.add(input);
		output = new OutputPort(this, 1, false);
		outputPortList.add(output);
	}

    private boolean isFixedStepSolver(String solver) {
        return solver.equals("ode1") || solver.equals("ode2") || solver.equals("ode3") ||
            solver.equals("ode4") || solver.equals("ode5") || solver.equals("ode6");
    }

    private int calculateBufferLength(double delay) {
        // Ensure buffer has enough space with a generous safety margin
        return Math.max(20, (int)(delay / model.getConfig().getFixedStep()) + 5);
    }

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode = "/*Code for initialization of block TransportDelay:(" + getBlockId() + ")" + getBlockName()
				+ "*/\n";
		initCode += delaytime.getInitCodeC();
		initCode += initialoutput.getInitCodeC();
        initCode += bufferSize.getInitCodeC();

        int bufferLength = calculateBufferLength(paramValues.getDouble("DelayTime"));
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        StringBuilder initCodeBuilder = new StringBuilder();
        if(isFixedStepSolver(model.getConfig().getSolver())) {
            // Initialize buffer at startup
            switch (signal.getDataType()) {
                case REAL:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            initCodeBuilder.append("  for(int i=0; i<").append(bufferLength).append("; i++) {\n");
                            initCodeBuilder.append("    Block").append(getBlockId()).append("_transport_delay_savedata[i] = ")
                                .append(initialoutput.getName()).append(";\n");
                            initCodeBuilder.append("  }\n");
                            break;
                        case MATRIX:
                            // Initialize all buffers at startup
                            for (int i = 0; i < delaytime.getHeight(); i++) {
                                for (int j = 0; j < delaytime.getWidth(); j++) {
                                    int bufferLen = calculateBufferLength(test.get(i, j));
                                    initCodeBuilder.append("  for(int m=0; m<").append(bufferLen).append("; m++) {\n");
                                    initCodeBuilder.append("    Block").append(getBlockId()).append("_transport_delay_savedata_")
                                        .append(i).append("_").append(j).append("_[m] = ").append(initialoutput.getName())
                                        .append("(").append(i).append(",").append(j).append(");\n");
                                    initCodeBuilder.append("  }\n");
                                }
                            }
                            break;
                    }
                case MATRIX: {
                    switch (delaytime.getDataType()) {
                        case REAL:
                            // Initialize buffer at startup
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int bufferLen = calculateBufferLength(paramValues.getDouble("DelayTime"));
                                    initCodeBuilder.append("  for(int m=0; m<").append(bufferLen).append("; m++) {\n");
                                    initCodeBuilder.append("    Block").append(getBlockId()).append("_transport_delay_savedata")
                                        .append("[m] = ").append(initialoutput.getName())
                                        .append(";\n");
                                    initCodeBuilder.append("  }\n");
                                }
                            }
                            break;
                        case MATRIX:
                            // Initialize buffer at startup
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int index = i * signal.getWidth() + j;
                                    initCodeBuilder.append("  for(int m=0; m<").append(bufferSize.getData().getIntValue()).append("; m++) {\n");
                                    initCodeBuilder.append("    Block").append(getBlockId()).append("yout[").append(index).append("][m] = ")
                                        .append(initialoutput.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    initCodeBuilder.append("    Block").append(getBlockId()).append("tout[").append(index).append("][m] = -1.0;\n");
                                    initCodeBuilder.append("  }\n");
                                }
                            }
                            break;
                    }
                }
            }
        }
        else{
            int rows, cols;
            if (signal.getDataType() == DataType.REAL && delaytime.getDataType() == DataType.MATRIX) {
                rows = delaytime.getHeight();
                cols = delaytime.getWidth();
            }
            else {
                rows = signal.getHeight();
                cols = signal.getWidth();
            }

            int totalElements = Math.max(1, rows * cols);

        }
        initCodeBuilder.append("init_buffer(&").append(getBufferName())
            .append(", ").append(bufferSize.getData().getIntValue())
            .append(", ").append(initialoutput.getName())
            .append(");\n");
        initCode += initCodeBuilder.toString();
		code.addInitCode(initCode);
	}

    public void generateArraysCodeC(CodeStructC code) {
        String arraysCode = "/*Define arrays for block transport_Delay:(" + getBlockId() + ")" + getBlockName()
            + "*/\n";
        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        String ss = this.model.getConfig().getSolver();
        if (isFixedStepSolver(ss)) {
            switch (signal.getDataType()) {
                case REAL:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            int bufferLength = calculateBufferLength(paramValues.getDouble("DelayTime"));
                            arraysCode += "double " + "Block" + getBlockId() + "_transport_delay_savedata["
                                + bufferLength + "];\n";
                            break;
                        case MATRIX:
                            for (int i = 0; i < delaytime.getHeight(); i++) {
                                for (int j = 0; j < delaytime.getWidth(); j++) {
                                    int bufferLen = calculateBufferLength(test.get(i, j));
                                    arraysCode += "double " + "Block" + getBlockId() + "_transport_delay_savedata_" + i + "_" + j
                                        + "_[" + bufferLen + "];\n";
                                }
                            }
                            break;
                    }
                    break;
                case MATRIX:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int bufferLen = calculateBufferLength(paramValues.getDouble("DelayTime"));
                                    arraysCode += "double " + "Block" + getBlockId() + "_transport_delay_savedata_" + i + "_" + j
                                        + "_[" + bufferLen + "];\n";
                                }
                            }
                            break;
                        case MATRIX:
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int bufferLen = calculateBufferLength(test.get(i, j));
                                    arraysCode += "double " + "Block" + getBlockId() + "_transport_delay_savedata_" + i + "_" + j
                                        + "_[" + bufferLen + "];\n";
                                }
                            }
                            break;
                    }
                    break;
            }
        } else {
            int rows, cols;
            if (signal.getDataType() == DataType.REAL && delaytime.getDataType() == DataType.MATRIX) {
                rows = delaytime.getHeight();
                cols = delaytime.getWidth();
            } else {
                rows = signal.getHeight();
                cols = signal.getWidth();
            }
            int totalElements = Math.max(1, rows * cols);

//            arraysCode += "double " + "Block" + getBlockId() + "tout[" + totalElements + "]["
//                + bufferSize.getData().getIntValue() + "];\n";
//            arraysCode += "double " + "Block" + getBlockId() + "yout[" + totalElements + "]["
//                + bufferSize.getData().getIntValue() + "];\n";

        }
        arraysCode += "Buffer " + getBufferName() + ";\n";
        code.addArraysCode(arraysCode);
    }

    /**
     * Generates derivative code for C code generation.
     * For Transport Delay, this function updates the delay buffer with new input values.
     *
     * @param code The CodeStructC object to add the derivative code to
     */
    public void generateDerivativeCodeC(CodeStructC code) {
        super.generateDerivativeCodeC(code);

        StringBuilder derivativeCode = new StringBuilder();
        derivativeCode.append("/*Code for Derivative of block Transport Delay:(").append(getBlockId()).append(")")
            .append(getBlockName()).append("*/\n");

        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        String solverType = this.model.getConfig().getSolver();

        if (isFixedStepSolver(solverType)) {
            // For fixed-step solvers
            switch (signal.getDataType()) {
                case REAL:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            int bufferLength = calculateBufferLength(paramValues.getDouble("DelayTime"));
                            // Initialize buffer at startup
                            // Shift buffer values
                            derivativeCode.append("for(int i=0; i<").append(bufferLength-1).append("; i++) {\n");
                            derivativeCode.append("  Block").append(getBlockId()).append("_transport_delay_savedata[i] = Block")
                                .append(getBlockId()).append("_transport_delay_savedata[i+1];\n");
                            derivativeCode.append("}\n");

                            // Store new value at the end of the buffer
                            derivativeCode.append("Block").append(getBlockId()).append("_transport_delay_savedata[")
                                .append(bufferLength-1).append("] = ").append(signal.getName()).append(";\n");
                            break;

                        case MATRIX:
                            // Update each buffer
                            for (int i = 0; i < delaytime.getHeight(); i++) {
                                for (int j = 0; j < delaytime.getWidth(); j++) {
                                    int bufferLen = calculateBufferLength(test.get(i, j));
                                    derivativeCode.append("for(int m=0; m<").append(bufferLen-1).append("; m++) {\n");
                                    derivativeCode.append("  Block").append(getBlockId()).append("_transport_delay_savedata_")
                                        .append(i).append("_").append(j).append("_[m] = Block").append(getBlockId())
                                        .append("_transport_delay_savedata_").append(i).append("_").append(j)
                                        .append("_[m+1];\n");
                                    derivativeCode.append("}\n");
                                    derivativeCode.append("Block").append(getBlockId()).append("_transport_delay_savedata_")
                                        .append(i).append("_").append(j).append("_[").append(bufferLen-1).append("] = ")
                                        .append(signal.getName()).append(";\n");
                                }
                            }
                            break;
                    }
                    break;

                case MATRIX:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            // Update each buffer
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int bufferLen = calculateBufferLength(paramValues.getDouble("DelayTime"));
                                    derivativeCode.append("for(int m=0; m<").append(bufferLen-1).append("; m++) {\n");
                                    derivativeCode.append("  Block").append(getBlockId()).append("_transport_delay_savedata_")
                                        .append(i).append("_").append(j).append("_[m] = Block").append(getBlockId())
                                        .append("_transport_delay_savedata_").append(i).append("_").append(j)
                                        .append("_[m+1];\n");
                                    derivativeCode.append("}\n");
                                    derivativeCode.append("Block").append(getBlockId()).append("_transport_delay_savedata_")
                                        .append(i).append("_").append(j).append("_[").append(bufferLen-1).append("] = ")
                                        .append(signal.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                }
                            }
                            break;
                        case MATRIX:
                            // Update each buffer
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int bufferLen = calculateBufferLength(test.get(i, j));
                                    derivativeCode.append("for(int m=0; m<").append(bufferLen-1).append("; m++) {\n");
                                    derivativeCode.append("  Block").append(getBlockId()).append("_transport_delay_savedata_")
                                        .append(i).append("_").append(j).append("_[m] = Block").append(getBlockId())
                                        .append("_transport_delay_savedata_").append(i).append("_").append(j)
                                        .append("_[m+1];\n");
                                    derivativeCode.append("}\n");
                                    derivativeCode.append("Block").append(getBlockId()).append("_transport_delay_savedata_")
                                        .append(i).append("_").append(j).append("_[").append(bufferLen-1).append("] = ")
                                        .append(signal.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                }
                            }
                            break;
                    }
                    break;
            }
        } else {
            // For variable-step solvers
            switch (signal.getDataType()) {
                case REAL:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            // Shift buffers
                            derivativeCode.append("insert_to_buffer(&").append(getBufferName())
                                .append(",").append("model.time")
                                .append(",").append(signal.getName()).append(");\n");
                            break;

                        case MATRIX:
                            // Update each buffer
                            for (int i = 0; i < delaytime.getHeight(); i++) {
                                for (int j = 0; j < delaytime.getWidth(); j++) {
                                    int index = i * delaytime.getWidth() + j;
                                    derivativeCode.append("for(int m=").append(bufferSize.getData().getIntValue()-2).append("; m>=0; m--) {\n");
                                    derivativeCode.append("  Block").append(getBlockId()).append("yout[").append(index).append("][m+1] = Block")
                                        .append(getBlockId()).append("yout[").append(index).append("][m];\n");
                                    derivativeCode.append("  Block").append(getBlockId()).append("tout[").append(index).append("][m+1] = Block")
                                        .append(getBlockId()).append("tout[").append(index).append("][m];\n");
                                    derivativeCode.append("}\n");
                                    derivativeCode.append("Block").append(getBlockId()).append("yout[").append(index).append("][0] = ")
                                        .append(signal.getName()).append(";\n");
                                    derivativeCode.append("Block").append(getBlockId()).append("tout[").append(index).append("][0] = model.time;\n");
                                }
                            }
                            break;
                    }
                    break;

                case MATRIX:
                    switch (delaytime.getDataType()) {
                        case REAL:
                        case MATRIX:
                            // Update each buffer
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int index = i * signal.getWidth() + j;
                                    derivativeCode.append("for(int m=").append(bufferSize.getData().getIntValue()-2).append("; m>=0; m--) {\n");
                                    derivativeCode.append("  Block").append(getBlockId()).append("yout[").append(index).append("][m+1] = Block")
                                        .append(getBlockId()).append("yout[").append(index).append("][m];\n");
                                    derivativeCode.append("  Block").append(getBlockId()).append("tout[").append(index).append("][m+1] = Block")
                                        .append(getBlockId()).append("tout[").append(index).append("][m];\n");
                                    derivativeCode.append("}\n");
                                    derivativeCode.append("Block").append(getBlockId()).append("yout[").append(index).append("][0] = ")
                                        .append(signal.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    derivativeCode.append("Block").append(getBlockId()).append("tout[").append(index).append("][0] = model.time;\n");
                                }
                            }
                            break;
                    }
                    break;
            }
        }

        code.addDerivativeCode(derivativeCode.toString());
    }

    /**
     * Generates output code for C code generation.
     * For Transport Delay, this function reads from the delay buffer to produce output.
     *
     * @param code The CodeStructC object to add the output code to
     */
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        outputCode.append("/*Code for output of block TransportDelay:(").append(getBlockId()).append(")")
            .append(getBlockName()).append("*/\n");

        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        String solverType = this.model.getConfig().getSolver();

        if (isFixedStepSolver(solverType)) {
            // For fixed-step solvers - just output the appropriate buffer value
            switch (signal.getDataType()) {
                case REAL:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            // Store new values
                            outputCode.append("insert_to_buffer(&").append(getBufferName())
                                .append(",").append("model.time")
                                .append(",").append(signal.getName()).append(");\n")
                                .append("double ").append("Block").append(getBlockId()).append("_target_time")
                                .append(" = model.time - ").append(delaytime.getName()).append(";\n")
                                .append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                .append(" = ").append("linear_interpolation(&").append(getBufferName())
                                .append(", ").append("Block").append(getBlockId()).append("_target_time")
                                .append(");\n");
                            break;

                        case MATRIX:
                            for (int i = 0; i < delaytime.getHeight(); i++) {
                                for (int j = 0; j < delaytime.getWidth(); j++) {
                                    outputCode.append("if(model.time < ").append(delaytime.getName())
                                        .append("(").append(i).append(",").append(j).append(")) {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    outputCode.append("} else {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = Block").append(getBlockId())
                                        .append("_transport_delay_savedata_").append(i).append("_").append(j).append("_[0];\n");
                                    outputCode.append("}\n");
                                }
                            }
                            break;
                    }
                    break;

                case MATRIX:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    outputCode.append("if(model.time < ").append(paramValues.getDouble("DelayTime")).append(") {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append(";\n");
                                    outputCode.append("} else {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = Block").append(getBlockId())
                                        .append("_transport_delay_savedata_").append(i).append("_").append(j).append("_[0];\n");
                                    outputCode.append("}\n");
                                }
                            }
                            break;

                        case MATRIX:
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    outputCode.append("if(model.time < ").append(delaytime.getName())
                                        .append("(").append(i).append(",").append(j).append(")) {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    outputCode.append("} else {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = Block").append(getBlockId())
                                        .append("_transport_delay_savedata_").append(i).append("_").append(j).append("_[0];\n");
                                    outputCode.append("}\n");
                                }
                            }
                            break;
                    }
                    break;
            }
        } else {
            // For variable-step solvers - perform interpolation to find the output
            switch (signal.getDataType()) {
                case REAL:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            // Calculate target time
                            outputCode
                                .append("double ").append("Block").append(getBlockId()).append("_target_time")
                                .append(" = model.time - ").append(delaytime.getName()).append(";\n")
                                .append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                .append(" = ").append("linear_interpolation(&").append(getBufferName())
                                .append(", ").append("Block").append(getBlockId()).append("_target_time")
                                .append(");\n");
                            break;

                        case MATRIX:
                            for (int i = 0; i < delaytime.getHeight(); i++) {
                                for (int j = 0; j < delaytime.getWidth(); j++) {
                                    int index = i * delaytime.getWidth() + j;

                                    // Calculate target time
                                    outputCode.append("double targetTime_").append(index).append(" = model.time - ")
                                        .append(delaytime.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    outputCode.append("if(targetTime_").append(index).append(" <= 0 || Block").append(getBlockId())
                                        .append("tout[").append(index).append("][0] < 0) {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    outputCode.append("} else {\n");

                                    // Find buffer index for interpolation
                                    outputCode.append("  int foundIndex_").append(index).append(" = -1;\n");
                                    outputCode.append("  for(int k=0; k<").append(bufferSize.getData().getIntValue()-1).append("; k++) {\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][k+1] < 0) break;\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][k] >= targetTime_")
                                        .append(index).append(" && \n");
                                    outputCode.append("       targetTime_").append(index).append(" >= Block").append(getBlockId())
                                        .append("tout[").append(index).append("][k+1]) {\n");
                                    outputCode.append("      foundIndex_").append(index).append(" = k;\n");
                                    outputCode.append("      break;\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("  }\n");

                                    // Perform linear interpolation
                                    outputCode.append("  if(foundIndex_").append(index).append(" >= 0) {\n");
                                    outputCode.append("    double t1 = Block").append(getBlockId()).append("tout[").append(index)
                                        .append("][foundIndex_").append(index).append("];\n");
                                    outputCode.append("    double t2 = Block").append(getBlockId()).append("tout[").append(index)
                                        .append("][foundIndex_").append(index).append("+1];\n");
                                    outputCode.append("    double y1 = Block").append(getBlockId()).append("yout[").append(index)
                                        .append("][foundIndex_").append(index).append("];\n");
                                    outputCode.append("    double y2 = Block").append(getBlockId()).append("yout[").append(index)
                                        .append("][foundIndex_").append(index).append("+1];\n");
                                    outputCode.append("    double alpha = 0.0;\n");
                                    outputCode.append("    if(fabs(t1 - t2) > 1e-10) {\n");
                                    outputCode.append("      alpha = (targetTime_").append(index).append(" - t2) / (t1 - t2);\n");
                                    outputCode.append("      alpha = fmax(0.0, fmin(1.0, alpha));\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("    ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = alpha * y1 + (1.0 - alpha) * y2;\n");
                                    outputCode.append("  } else {\n");
                                    outputCode.append("    // Use newest value if interpolation not possible\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][0] >= targetTime_")
                                        .append(index).append(") {\n");
                                    outputCode.append("      ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = Block").append(getBlockId())
                                        .append("yout[").append(index).append("][0];\n");
                                    outputCode.append("    } else {\n");
                                    outputCode.append("      ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("  }\n");
                                    outputCode.append("}\n");
                                }
                            }
                            break;
                    }
                    break;

                case MATRIX:
                    switch (delaytime.getDataType()) {
                        case REAL:
                            outputCode.append("{\n");
                            // Calculate target time once for all matrix elements
                            outputCode.append("double targetTime = model.time - ").append(delaytime.getName()).append(";\n");

                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int index = i * signal.getWidth() + j;

                                    outputCode.append("if(targetTime <= 0 || Block").append(getBlockId())
                                        .append("tout[").append(index).append("][0] < 0) {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append(";\n");
                                    outputCode.append("} else {\n");

                                    // Find buffer index for interpolation
                                    outputCode.append("  int foundIndex_").append(index).append(" = -1;\n");
                                    outputCode.append("  for(int k=0; k<").append(bufferSize.getData().getIntValue()-1).append("; k++) {\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][k+1] < 0) break;\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][k] >= targetTime && \n");
                                    outputCode.append("       targetTime >= Block").append(getBlockId())
                                        .append("tout[").append(index).append("][k+1]) {\n");
                                    outputCode.append("      foundIndex_").append(index).append(" = k;\n");
                                    outputCode.append("      break;\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("  }\n");

                                    // Perform linear interpolation
                                    outputCode.append("  if(foundIndex_").append(index).append(" >= 0) {\n");
                                    outputCode.append("    double t1 = Block").append(getBlockId()).append("tout[").append(index)
                                        .append("][foundIndex_").append(index).append("];\n");
                                    outputCode.append("    double t2 = Block").append(getBlockId()).append("tout[").append(index)
                                        .append("][foundIndex_").append(index).append("+1];\n");
                                    outputCode.append("    double y1 = Block").append(getBlockId()).append("yout[").append(index)
                                        .append("][foundIndex_").append(index).append("];\n");
                                    outputCode.append("    double y2 = Block").append(getBlockId()).append("yout[").append(index)
                                        .append("][foundIndex_").append(index).append("+1];\n");
                                    outputCode.append("    double alpha = 0.0;\n");
                                    outputCode.append("    if(fabs(t1 - t2) > 1e-10) {\n");
                                    outputCode.append("      alpha = (targetTime - t2) / (t1 - t2);\n");
                                    outputCode.append("      alpha = fmax(0.0, fmin(1.0, alpha));\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("    ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = alpha * y1 + (1.0 - alpha) * y2;\n");
                                    outputCode.append("  } else {\n");
                                    outputCode.append("    // Use newest value if interpolation not possible\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][0] >= targetTime) {\n");
                                    outputCode.append("      ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = Block").append(getBlockId())
                                        .append("yout[").append(index).append("][0];\n");
                                    outputCode.append("    } else {\n");
                                    outputCode.append("      ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append(";\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("  }\n");
                                    outputCode.append("}\n");
                                }
                            }
                            outputCode.append("}\n");
                            break;

                        case MATRIX:
                            for (int i = 0; i < signal.getHeight(); i++) {
                                for (int j = 0; j < signal.getWidth(); j++) {
                                    int index = i * signal.getWidth() + j;

                                    // Calculate target time
                                    outputCode.append("double targetTime_").append(index).append(" = model.time - ")
                                        .append(delaytime.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    outputCode.append("if(targetTime_").append(index).append(" <= 0 || Block").append(getBlockId())
                                        .append("tout[").append(index).append("][0] < 0) {\n");
                                    outputCode.append("  ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    outputCode.append("} else {\n");

                                    // Find buffer index for interpolation
                                    outputCode.append("  int foundIndex_").append(index).append(" = -1;\n");
                                    outputCode.append("  for(int k=0; k<").append(bufferSize.getData().getIntValue()-1).append("; k++) {\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][k+1] < 0) break;\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][k] >= targetTime_")
                                        .append(index).append(" && \n");
                                    outputCode.append("       targetTime_").append(index).append(" >= Block").append(getBlockId())
                                        .append("tout[").append(index).append("][k+1]) {\n");
                                    outputCode.append("      foundIndex_").append(index).append(" = k;\n");
                                    outputCode.append("      break;\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("  }\n");

                                    // Perform linear interpolation
                                    outputCode.append("  if(foundIndex_").append(index).append(" >= 0) {\n");
                                    outputCode.append("    double t1 = Block").append(getBlockId()).append("tout[").append(index)
                                        .append("][foundIndex_").append(index).append("];\n");
                                    outputCode.append("    double t2 = Block").append(getBlockId()).append("tout[").append(index)
                                        .append("][foundIndex_").append(index).append("+1];\n");
                                    outputCode.append("    double y1 = Block").append(getBlockId()).append("yout[").append(index)
                                        .append("][foundIndex_").append(index).append("];\n");
                                    outputCode.append("    double y2 = Block").append(getBlockId()).append("yout[").append(index)
                                        .append("][foundIndex_").append(index).append("+1];\n");
                                    outputCode.append("    double alpha = 0.0;\n");
                                    outputCode.append("    if(fabs(t1 - t2) > 1e-10) {\n");
                                    outputCode.append("      alpha = (targetTime_").append(index).append(" - t2) / (t1 - t2);\n");
                                    outputCode.append("      alpha = fmax(0.0, fmin(1.0, alpha));\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("    ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = alpha * y1 + (1.0 - alpha) * y2;\n");
                                    outputCode.append("  } else {\n");
                                    outputCode.append("    // Use newest value if interpolation not possible\n");
                                    outputCode.append("    if(Block").append(getBlockId()).append("tout[").append(index).append("][0] >= targetTime_")
                                        .append(index).append(") {\n");
                                    outputCode.append("      ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = Block").append(getBlockId())
                                        .append("yout[").append(index).append("][0];\n");
                                    outputCode.append("    } else {\n");
                                    outputCode.append("      ").append(outputPortList.get(0).getOutputSignalC().getName())
                                        .append("(").append(i).append(",").append(j).append(") = ")
                                        .append(initialoutput.getName()).append("(").append(i).append(",").append(j).append(");\n");
                                    outputCode.append("    }\n");
                                    outputCode.append("  }\n");
                                    outputCode.append("}\n");
                                }
                            }
                            break;
                    }
                    break;
            }
        }

        code.addOutputCode(outputCode.toString());
    }

	public void updateDimension() throws MatDimException {

		OutputPort out = outputPortList.get(0);
		InputPort in = inputPortList.get(0);
		OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (signal.getDataType()) {
		case REAL:
			switch (delaytime.getDataType()) {
			case REAL:
				out.setHeight(1);
				out.setWidth(1);
				out.getOutputSignalC().setHeight(1);
				out.getOutputSignalC().setWidth(1);
				out.getOutputSignalC().setDataType(DataType.REAL);
				break;
			case MATRIX:
				out.setHeight(delaytime.getHeight());
				out.setWidth(delaytime.getWidth());
				out.getOutputSignalC().setHeight(delaytime.getHeight());
				out.getOutputSignalC().setWidth(delaytime.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			}
			break;
		case MATRIX:
			switch (delaytime.getDataType()) {
			case REAL:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			case MATRIX:
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
				break;
			}
			break;
		}
	}

	public void checkDimension() throws MatDimException {
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		switch (initialoutput.getDataType()) {
		case REAL:
			switch (signal.getDataType()) {
			case REAL:

				break;
			case MATRIX:
				if (initialoutput.getHeight() != delaytime.getHeight()
						|| initialoutput.getWidth() != delaytime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match!");
					throw (e);
				}
				break;
			}
			break;
		case MATRIX:
			switch (signal.getDataType()) {
			case REAL:
				if (initialoutput.getHeight() != delaytime.getHeight()
						|| initialoutput.getWidth() != delaytime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match1!");
					throw (e);
				}
				break;
			case MATRIX:
				if (signal.getHeight() != delaytime.getHeight() || signal.getWidth() != delaytime.getWidth()
						|| signal.getHeight() != initialoutput.getHeight()
						|| signal.getWidth() != initialoutput.getWidth()
						|| initialoutput.getHeight() != delaytime.getHeight()
						|| initialoutput.getWidth() != delaytime.getWidth()) {
					MatDimException e = new MatDimException(
							"Block " + this.blockName + " input dimensions don't match!");
					throw (e);
				}
				break;
			}
			break;
		}

	}
}
