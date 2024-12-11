package com.ncslab.block.elect;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Diode extends Block {
	protected Parameter vf;
	protected Parameter ron;
	protected Parameter goff;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        parameterNames.add("Vf");
        parameterNames.add("Ron");
        parameterNames.add("Goff");
    }

	public Diode(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);InputPort in;
		OutputPort out;

		out = new OutputPort(this,1,true);
		in = new InputPort(this,1);

		outputPortList.add(out);
		inputPortList.add(in);

		vf=new Parameter(this,1,"Vf",paramValues.getString("Vf"));
		ron=new Parameter(this,2,"Ron",paramValues.getString("Ron"));
		goff=new Parameter(this,3,"Goff",paramValues.getString("Goff"));

		parameterList.add(vf);
		parameterList.add(ron);
		parameterList.add(goff);

		System.out.println(paramValues);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block Diode:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=vf.getInitCodeC();
		initCode+=ron.getInitCodeC();
		initCode+=goff.getInitCodeC();
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Diode:("+getBlockId()+")"+getBlockName()+"*/\n";

		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();


		outputCode+="if("+ops.getOutputSignalC().getName()+">"+vf.getName()+"){\n";
		outputCode+=out.getOutputSignalC().getName()+"=";
		outputCode+="("+ops.getOutputSignalC().getName()+"-"+vf.getName()+")/("+ron.getName()+");\n";
		outputCode+="}\n";
		outputCode+="else if("+ops.getOutputSignalC().getName()+">0){\n";
		outputCode+=out.getOutputSignalC().getName()+"=0;\n";
		outputCode+="}\n";
		outputCode+="else{\n";
		outputCode+=out.getOutputSignalC().getName()+"="+ops.getOutputSignalC().getName()+"*"+goff.getName()+";\n";
		outputCode+="}\n";
		//outputCode+=out.getOutputSignalC().getName()+"=";
		//outputCode+=ops.getOutputSignalC().getName()+";\n";

		code.addOutputCode(outputCode);
	}

	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		//switch(signal.getDataType()) {
		//case MATRIX:
			out.setWidth(signal.getWidth());
			out.setHeight(signal.getHeight());
			out.getOutputSignalC().setHeight(signal.getWidth());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
			//break;
		//}
	}
}
