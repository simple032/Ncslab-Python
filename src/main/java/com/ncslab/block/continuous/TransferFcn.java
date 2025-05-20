package com.ncslab.block.continuous;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import lombok.Getter;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.ncslab.util.TemplateManager;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TransferFcn extends Block {
	private double D=0;
	private boolean feedThrough=false;

    private Parameter numParam;
    private Parameter denParam;

	private double[] num;
	private double[] den;

	private Vector<State> xStateList=new Vector<State>();

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("Numerator");
        parameterNames.add("Denominator");

        outputNames.add("out1");
        inputNames.add("in1");
    }

	public TransferFcn(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);

		parseVector();

		for(int i=0;i<num.length;i++) {
			State xState=new State(this,i+1,"x"+(i+1));
			xStateList.add(xState);
			stateList.add(xState);
		}

		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,feedThrough));
	}

	private void parseVector() {

        numParam = new Parameter(this, 1, "Numerator", paramValues.getString("Numerator"));
        parameterList.add(numParam);
        denParam = new Parameter(this, 2, "Denominator", paramValues.getString("Denominator"));
		parameterList.add(denParam);

        num=numParam.getDoubleArray();
        den=denParam.getDoubleArray();

		//归一化
		double unit=den[0];
		for(int i=0;i<den.length;i++) {
			den[i]=den[i]/unit;
		}

		for(int i=0;i<num.length;i++) {
			num[i]=num[i]/unit;
		}

		//System.out.println(""+num+den);

		if(num.length==den.length) {
			feedThrough=true;
			D=num[0]/den[0];

			for(int i=0;i<num.length;i++) {
				num[i]=num[i]-D*den[i];
			}

			double[] numShort=new double[num.length-1];
            System.arraycopy(num, 1, numShort, 0, num.length - 1);

			num=numShort;
		}

		double[] denShort=new double[den.length-1];
        System.arraycopy(den, 1, denShort, 0, den.length - 1);
		den=denShort;

		double[] numShort=new double[den.length];
		for(int i=0;i<den.length;i++) {
			if(i<den.length-num.length) {
				numShort[i]=0;
			}
			else {
				numShort[i]=num[i-(den.length-num.length)];
			}
		}
		num=numShort;

	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("states", xStateList);

		String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/init.vm", context);
		code.addInitCode(codeStr);
	}


	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("states", xStateList);
		context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
		context.put("feedThrough", feedThrough);
		context.put("D", D);
		context.put("inputs", getInputPortVariables());
		context.put("outputs", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("states", xStateList);
		context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));
		context.put("inputs", getInputPortVariables());

		String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("states", xStateList);

		String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/init.vm", context);
		code.addInitCode(codeStr);
	}

	public void generateOutputCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("states", xStateList);
		context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
		context.put("feedThrough", feedThrough);
		context.put("D", D);
		context.put("inputs", getInputPortVariables());
		context.put("outputs", getOutputPortVariables());

		String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/output.vm", context);
		code.addOutputCode(codeStr);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		VelocityContext context = new VelocityContext();
		context.put("block", this);
context.put("realDataType", DataType.REAL);
		context.put("states", xStateList);
		context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));
		context.put("inputs", getInputPortVariables());

		String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/derivative.vm", context);
		code.addDerivativeCode(codeStr);
	}
}
