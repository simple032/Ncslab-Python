package com.ncslab.block.function;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
//import com.ncslab.util.M2PCodeFunctionEvaluator;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.apache.commons.jexl3.*;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Fcn extends Block{

	private final String expression;

//    private M2PCodeFunctionEvaluator.Evaluator evaluator;

    
    
    

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("Expression", "u");
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "double");
        PARAMETER_DEFAULTS.put("SaturateOnIntegerOverflow", "off");
    }

    static {
        
        outputNames.add("out1");
        inputNames.add("in1");
    }

	public Fcn(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,true));
		//����һ������
		inputPortList.add(new InputPort(this,1));
        expression = paramValues.getString("Expression");
	}

    /**
     * DTO-NATIVE Constructor - Creates Fcn block directly from BlockDto DTO
     */
    public Fcn(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);

        expression = paramValues.getString("Expression");
        
        System.out.println("DTO-NATIVE: Fcn block created successfully - " + blockDto.getBlockName());
    }


	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);

		String outputCode="";
		code.addOutputCode(outputCode);
	}

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("expression", expression);

        String codeStr = TemplateManager.renderTemplate("c/function/Fcn/output.vm", context);
        code.addOutputCode(codeStr);
    }

	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);

        out.setHeight(1);
        out.setWidth(1);
        out.getOutputSignalC().setHeight(1);
        out.getOutputSignalC().setWidth(1);
        out.getOutputSignalC().setDataType(DataType.REAL);
	}

	public void checkDimension() throws MatDimException{
	}

    @Override
    public void calculateOutput(double t) {
//        OutputSignal out = outputPortList.get(0).getOutputSignalC();
//        OutputSignal in = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
//
//        // 计算结果
//        double []inputData = new double[in.getHeight()];
//        for(int i=0;i<in.getHeight();i++){
//            inputData[i] = in.getData().getMatrix().get(i,0);
//        }
//        double result = evaluator.evaluate(inputData);
//        // 设置输出信号的值
//        out.getData().setInitValue(result);
    }

//    @Override
//    public void calculateInit() {
//        evaluator = new M2PCodeFunctionEvaluator.Evaluator(expression);
//    }
}
