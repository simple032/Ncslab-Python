package com.ncslab.block.math;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.math.ProductOfElementsDto;
import com.ncslab.block.math.MathBlock;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.BlockCreationException;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;
import com.ncslab.util.TemplateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ProductOfElements extends MathBlock {

    private String seq;
    boolean allDimensions = true;
    boolean multiplication = false;
    private int dimension;

    // === Static Parameter Definitions ===
    
    // Parameter defaults matching database format
    
    
    /**
     * DTO-NATIVE Constructor - Creates ProductOfElements block directly from BlockDto DTO
     */
    public ProductOfElements(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: ProductOfElements block created successfully - " + blockDto.getBlockName());
    }

    /**
     * Factory method to create ProductOfElements block from ProductOfElementsDto.
     *
     * @param dto The ProductOfElementsDto containing block configuration
     * @param model The NCSLabModel this block belongs to
     * @return New ProductOfElements block instance
     * @throws BlockCreationException if block creation fails
     */
    public static ProductOfElements createFromDto(ProductOfElementsDto dto, NCSLabModel model) throws BlockCreationException {
        return new ProductOfElements(dto, model);
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Inputs", "*");  // Single multiplication input
        PARAMETER_DEFAULTS.put("Multiplication", "Element-wise(.*)");
        PARAMETER_DEFAULTS.put("MultiplyOver", "All dimensions");
        PARAMETER_DEFAULTS.put("ElementsDimension", "1");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as input");
        
        // SIMULINK parameter names
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {
        // Port names
        outputNames.add("out1");
    }

    public ProductOfElements(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
        
        // Use name-based parameter access
        Parameter multiplicationParam = getParameterByName("Multiplication");
        this.multiplication = "Matrix(*)".equals(multiplicationParam.getInitString());

        OutputPort output = new OutputPort(this, 1, true);
        output.setDimThrough(false);
        outputPortList.add(output);

        paraseParamValues();
    }

    // Parse parameter values
    public void paraseParamValues() {
        Parameter inputsParam = getParameterByName("Inputs");
        seq = inputsParam.getInitString();

        // ProductOfElements has ONE input port (can be scalar/vector/matrix)
        inputPortList.add(new InputPort(this, 1));

        Parameter multiplyOverParam = getParameterByName("MultiplyOver");
        allDimensions = "All dimensions".equals(multiplyOverParam.getInitString());

        Parameter dimensionParam = getParameterByName("ElementsDimension");
        dimension = Integer.parseInt(dimensionParam.getInitString());
    }
    
    @Override
    public void calculateOutput(double t) {
        // SIMULINK ProductOfElements block: multiplies/divides elements together
        OutputPort out = outputPortList.get(0);
        Data inputData = inputPortList.get(0).getData();
        Data outputData;

        // Determine operation: multiplication (*) or division (/)
        boolean isMultiply = seq.length() == 1 && seq.charAt(0) == '*';
        boolean isDivide = seq.length() == 1 && seq.charAt(0) == '/';

        if (inputData.getDataType() == DataType.MATRIX) {
            Jama.Matrix inputMatrix = inputData.getMatrix();

            if (allDimensions) {
                // Collapse all elements to scalar
                double result = 1.0;
                for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                    for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                        if (isMultiply) {
                            result *= inputMatrix.get(i, j);
                        } else if (isDivide) {
                            result /= inputMatrix.get(i, j);
                        } else {
                            // Apply sequence operators to each element
                            for (int k = 0; k < seq.length(); k++) {
                                char op = seq.charAt(k);
                                if (op == '*') {
                                    result *= inputMatrix.get(i, j);
                                } else if (op == '/') {
                                    result /= inputMatrix.get(i, j);
                                }
                            }
                        }
                    }
                }
                outputData = new Data(1, 1);
                outputData.setInitValue(result);
            } else if (dimension == 1) {
                // Product/division along rows (each column becomes one element)
                int cols = inputMatrix.getColumnDimension();
                Jama.Matrix outputMatrix = new Jama.Matrix(1, cols);

                for (int j = 0; j < cols; j++) {
                    double result = 1.0;
                    for (int i = 0; i < inputMatrix.getRowDimension(); i++) {
                        if (isMultiply) {
                            result *= inputMatrix.get(i, j);
                        } else if (isDivide) {
                            result /= inputMatrix.get(i, j);
                        } else {
                            for (int k = 0; k < seq.length(); k++) {
                                char op = seq.charAt(k);
                                if (op == '*') {
                                    result *= inputMatrix.get(i, j);
                                } else if (op == '/') {
                                    result /= inputMatrix.get(i, j);
                                }
                            }
                        }
                    }
                    outputMatrix.set(0, j, result);
                }
                outputData = new Data(outputMatrix);
            } else {
                // Product/division along columns (each row becomes one element)
                int rows = inputMatrix.getRowDimension();
                Jama.Matrix outputMatrix = new Jama.Matrix(rows, 1);

                for (int i = 0; i < rows; i++) {
                    double result = 1.0;
                    for (int j = 0; j < inputMatrix.getColumnDimension(); j++) {
                        if (isMultiply) {
                            result *= inputMatrix.get(i, j);
                        } else if (isDivide) {
                            result /= inputMatrix.get(i, j);
                        } else {
                            for (int k = 0; k < seq.length(); k++) {
                                char op = seq.charAt(k);
                                if (op == '*') {
                                    result *= inputMatrix.get(i, j);
                                } else if (op == '/') {
                                    result /= inputMatrix.get(i, j);
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
                if (seq.charAt(0) == '*') {
                    // Copy unchanged
                    outputData = new Data(value);
                } else {
                    // Invert (1 / value)
                    outputData = new Data(1.0 / value);
                }
            } else {
                // Apply sequence operators
                double result = 1.0;
                for (int k = 0; k < seq.length(); k++) {
                    char op = seq.charAt(k);
                    if (op == '*') {
                        result *= value;
                    } else if (op == '/') {
                        result /= value;
                    }
                }
                outputData = new Data(result);
            }
        }

        out.setData(outputData);
    }

    @Override
    public void generateOutputCodeC(com.ncslab.code.c.CodeStructC code) {
        // Populate all standard template variables first
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);

        // Add block-specific context
        context.put("sequence", seq);
        context.put("allDimensions", allDimensions);
        context.put("dimension", dimension);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/math/ProductOfElements/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        OutputSignal inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        int inputHeight = inputSignal.getHeight();
        int inputWidth = inputSignal.getWidth();

        if (allDimensions) {
            // Collapse all dimensions to scalar
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        } else {
            // Product along specific dimension
            if (dimension == 1) {
                // Product along dimension 1 (down rows): output is row vector (1 x inputWidth)
                out.setHeight(1);
                out.setWidth(inputWidth);
                out.getOutputSignalC().setHeight(1);
                out.getOutputSignalC().setWidth(inputWidth);
                out.getOutputSignalC().setDataType(inputWidth > 1 ? DataType.MATRIX : DataType.REAL);
            } else {
                // Product along dimension 2 (across columns): output is column vector (inputHeight x 1)
                out.setHeight(inputHeight);
                out.setWidth(1);
                out.getOutputSignalC().setHeight(inputHeight);
                out.getOutputSignalC().setWidth(1);
                out.getOutputSignalC().setDataType(inputHeight > 1 ? DataType.MATRIX : DataType.REAL);
            }
        }
    }

    public void checkDimension() throws MatDimException {
    }
}