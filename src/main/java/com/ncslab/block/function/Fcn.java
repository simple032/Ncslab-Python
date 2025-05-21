package com.ncslab.block.function;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.util.TemplateManager;
import org.apache.velocity.VelocityContext;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Vector;

public class Fcn extends Block{

	private String expression;


    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

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
}
