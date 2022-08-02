package block.elect;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class DiodeCurrent extends Block {
	protected Parameter vf;
	protected Parameter ron;
	protected Parameter goff;
	
	public DiodeCurrent(JSONObject blockJSON,NCSLabModel model) {
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
		
		String initCode="/*Code for initialization of block DiodeCurrent:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=vf.getInitCodeC();
		initCode+=ron.getInitCodeC();
		initCode+=goff.getInitCodeC();
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Diode:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		
		
		outputCode+="if("+ops.getOutputSignalC().getName()+">0){\n";
		outputCode+=out.getOutputSignalC().getName()+"=";
		outputCode+="("+ops.getOutputSignalC().getName()+")*("+ron.getName()+")+"+vf.getName()+";\n";
		outputCode+="}\n";
		outputCode+="else{\n";
		outputCode+=out.getOutputSignalC().getName()+"="+ops.getOutputSignalC().getName()+"/"+goff.getName()+";\n";
		outputCode+="}\n";
		/*
		outputCode+="if("+ops.getOutputSignalC().getName()+">"+vf.getName()+"){\n";
		outputCode+=out.getOutputSignalC().getName()+"=";
		outputCode+="("+ops.getOutputSignalC().getName()+"-"+vf.getName()+")/("+ron.getName()+");\n";
		outputCode+="}\n";
		outputCode+="else if("+ops.getOutputSignalC().getName()+">0){\n";
		outputCode+=out.getOutputSignalC().getName()+"=0;\n";
		outputCode+="}\n";
		outputCode+="else{\n";
		outputCode+=out.getOutputSignalC().getName()+"="+ops.getOutputSignalC().getName()+"*"+goff.getName()+";\n";
		outputCode+="}\n";*/
		
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
