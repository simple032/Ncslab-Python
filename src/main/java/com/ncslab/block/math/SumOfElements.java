package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.SumOfElementsDto;
import com.ncslab.block.math.MathBlock;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.util.TemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SumOfElements extends MathBlock {

    private Parameter sequence;
    boolean allDimensions = true;
    private Parameter dimension;    
    private Parameter sumOver;
    
    /**
     * DTO-NATIVE Constructor - Creates SumOfElements block directly from BlockDto DTO
     */
    public SumOfElements(SumOfElementsDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: SumOfElements block created successfully - " + blockDto.getBlockName());

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        paraseParamValues();
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "+");
        PARAMETER_DEFAULTS.put("SumOver", "All dimensions");
        PARAMETER_DEFAULTS.put("ElementsDimension", "1");
    }

    static {
        inputNames.add("in1");
        outputNames.add("out1");
    }

    public SumOfElements(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        paraseParamValues();
    }

    private void paraseParamValues() {
        sequence = getParameterByName("Inputs");

        // SumOfElements has ONE input port (can be scalar/vector/matrix)
        inputPortList.add(new InputPort(this, 1));
        sumOver = getParameterByName("SumOver");
        allDimensions = "All dimensions".equals(sumOver.getInitString());
        dimension = getParameterByName("ElementsDimension");
    }

    @Override
    public void calculateInit() {
        // Initialization logic for SumOfElements block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();
        Data outputData;

        String seq = sequence.getInitString();

        // Determine operations: addition (+) or subtraction (-)
        boolean isAdd = seq.length() == 1 && seq.charAt(0) == '+';
        boolean isSubtract = seq.length() == 1 && seq.charAt(0) == '-';

        if (inputData.getDataType() == DataType.MATRIX) {
            Jama.Matrix inputMatrix = inputData.getMatrix();

            if (allDimensions) {
                // Collapse all elements to scalar with sum/subtraction
                double result = 0.0;
                for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                    for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                        if (isAdd) {
                            result += inputMatrix.get(i, j);
                        } else if (isSubtract) {
                            result -= inputMatrix.get(i, j);
                        } else {
                            // Apply sequence operators to each element
                            for (int k = 0; k < seq.length(); k++) {
                                char op = seq.charAt(k);
                                if (op == '+') {
                                    result += inputMatrix.get(i, j);
                                } else if (op == '-') {
                                    result -= inputMatrix.get(i, j);
                                }
                            }
                        }
                    }
                }
                outputData = new Data(1, 1);
                outputData.setInitValue(result);
            } else if (dimension.getInitString() == "2") {
                // Sum along columns (output is row vector)
                int cols = inputMatrix.getColumnDimension();
                Jama.Matrix outputMatrix = new Jama.Matrix(1, cols);

                for (int j = 0; j < cols; j++) {
                    double result = 0.0;
                    for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                        if (isAdd) {
                            result += inputMatrix.get(i, j);
                        } else if (isSubtract) {
                            result -= inputMatrix.get(i, j);
                        } else {
                            for (int k = 0; k < seq.length(); k++) {
                                char op = seq.charAt(k);
                                if (op == '+') {
                                    result += inputMatrix.get(i, j);
                                } else if (op == '-') {
                                    result -= inputMatrix.get(i, j);
                                }
                            }
                        }
                    }
                    outputMatrix.set(0, j, result);
                }
                outputData = new Data(outputMatrix);
            } else {
                // Sum along rows (output is column vector)
                int rows = inputMatrix.getRowDimension();
                Jama.Matrix outputMatrix = new Jama.Matrix(rows, 1);

                for (int i = 0; i < rows; i++) {
                    double result = 0.0;
                    for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                        if (isAdd) {
                            result += inputMatrix.get(i, j);
                        } else if (isSubtract) {
                            result -= inputMatrix.get(i, j);
                        } else {
                            for (int k = 0; k < seq.length(); k++) {
                                char op = seq.charAt(k);
                                if (op == '+') {
                                    result += inputMatrix.get(i, j);
                                } else if (op == '-') {
                                    result -= inputMatrix.get(i, j);
                                }
                            }
                        }
                    }
                    outputMatrix.set(i, 0, result);
                }
                outputData = new Data(outputMatrix);
            }
        } else {
            // Scalar input
            double value = inputData.getInitValue();
            if (seq.length() == 1) {
                if (seq.charAt(0) == '+') {
                    // Copy unchanged
                    outputData = new Data(value);
                } else {
                    // Negate
                    outputData = new Data(-value);
                }
            } else {
                // Apply sequence operators
                double result = 0.0;
                for (int k = 0; k < seq.length(); k++) {
                    char op = seq.charAt(k);
                    if (op == '+') {
                        result += value;
                    } else if (op == '-') {
                        result -= value;
                    }
                }
                outputData = new Data(result);
            }
        }

        out.setData(outputData);
    }

    public void generateOutputCodeC(CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add block-specific computed context
        context.put("allDimensions", isAllDimensions());

        String codeStr = TemplateManager.renderTemplate("c/math/SumOfElements/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private boolean isAllDimensions() {
        return allDimensions;
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        int inputHeight = inputSignal.getHeight();
        int inputWidth = inputSignal.getWidth();

        if (isAllDimensions()) {
            // Collapse all dimensions to scalar
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
            // Pass through input CDataType (sum preserves data type)
            out.getOutputSignalC().setCDataType(inputSignal.getCDataType());
        } else {
            // Sum along specific dimension
            if (dimension.getInitString() == "2") {
                // Sum along columns: output is row vector (1 x inputWidth)
                out.setHeight(1);
                out.setWidth(inputWidth);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(inputWidth);
                out.getOutputSignalC().setDataType(inputWidth > 1 ? DataType.MATRIX : DataType.REAL);
                // Pass through input CDataType
                out.getOutputSignalC().setCDataType(inputSignal.getCDataType());
            } else {
                // Sum along rows: output is column vector (inputHeight x 1)
                out.setHeight(inputHeight);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(inputHeight);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(inputHeight > 1 ? DataType.MATRIX : DataType.REAL);
                // Pass through input CDataType
                out.getOutputSignalC().setCDataType(inputSignal.getCDataType());
            }
        }
    }

    public void checkDimension() throws MatDimException {
    }
}
