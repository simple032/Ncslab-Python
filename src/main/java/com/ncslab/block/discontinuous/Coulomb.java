package com.ncslab.block.discontinuous;
import lombok.Getter;

import org.apache.velocity.VelocityContext;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.s;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

import java.util.Vector;

public class Coulomb extends Block{
	Parameter offset;
	Parameter gain;

	VelocityContext context;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("offset");
        parameterNames.add("gain");
        outputNames.add("out1");
        inputNames.add("in1");
    }

	public Coulomb(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		offset=new Parameter(this,1,"offset",paramValues.getString("offset"));
		gain=new Parameter(this,2,"gain",paramValues.getString("gain"));
		parameterList.add(offset);
		parameterList.add(gain);
	}

	private void prepareContext() {
		context = new VelocityContext();
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

		context.put("block", this); // 当前Block对象（含getBlockId()）
		context.put("inputPortList", inputPortList); // 输入端口列表
		context.put("outputPortList", outputPortList); // 输出端口列表
		context.put("offset", offset); // 偏移量参数对象
		context.put("gain", gain); // 增益参数对象
		context.put("signal",signal);
		context.put("ops", ops);
		context.put("realDataType", DataType.REAL); // 实数类型标识
		context.put("matrixDataType", DataType.MATRIX); // 矩阵类型标识
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=offset.getInitCodeM();
		initCode+=gain.getInitCodeM();
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		String outputCode="";
		switch(gain.getDataType()) {
		case REAL:
			switch(ops.getOutputSignalC().getDataType()) {
			case REAL:
				outputCode+=out.getOutputSignalC().getName()+"="+"sign("+signal.getName()+")*("+gain.getName()+"*abs("+signal.getName()+")+"+offset.getName()+");\n";
	        break;
			case MATRIX:
				for(int i=1; i<ops.getHeight()+1; i++) {
					for(int j=1;j<ops.getWidth()+1;j++) {
				outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+"sign("+signal.getName()+"("+i+","+j+"))*("+gain.getName()+"*abs("+signal.getName()+"("+i+","+j+"))+"+offset.getName()+");\n";
					}
				}
				break;
			}
			break;
		case MATRIX:
			switch(ops.getOutputSignalC().getDataType()) {
			case REAL:
				for(int i=1; i<gain.getHeight()+1; i++) {
					for(int j=1;j<gain.getWidth()+1;j++) {
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+"sign("+signal.getName()+")*("+gain.getName()+"("+i+","+j+")*abs("+signal.getName()+")+"+offset.getName()+"("+i+","+j+"));\n";
					}
				}
				break;
			case MATRIX:
				for(int i=1; i<ops.getHeight()+1; i++) {
					for(int j=1;j<ops.getWidth()+1;j++) {
						outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+"sign("+signal.getName()+"("+i+","+j+"))*("+gain.getName()+"("+i+","+j+")*abs("+signal.getName()+"("+i+","+j+"))+"+offset.getName()+"("+i+","+j+"));\n";
					}
				}
				break;
			}
			break;
		}
		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code){
		super.generateInitCodeC(code);
        prepareContext();
		String initCode = TemplateManager.renderTemplate("c/discontinuous/Coulomb/init.vm", context);
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code){
		super.generateOutputCodeC(code);
		String outputCode = TemplateManager.renderTemplate("c/discontinuous/Coulomb/output.vm", context);
		code.addOutputCode(outputCode);
	}

	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(offset.getWidth()!=gain.getWidth()||offset.getHeight()!=gain.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
			}
			if(offset.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
				out.setHeight(offset.getHeight());
				out.setWidth(offset.getWidth());
				out.getOutputSignalC().setHeight(offset.getHeight());
				out.getOutputSignalC().setWidth(offset.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
			}
			else if(offset.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());
			}
			else{
				if(offset.getWidth()!=signal.getWidth()||offset.getHeight()!=signal.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the Coulomb dimension!\n \n");
				throw(e);
				}
				out.setHeight(offset.getHeight());
				out.setWidth(offset.getWidth());
				out.getOutputSignalC().setHeight(offset.getHeight());
				out.getOutputSignalC().setWidth(offset.getWidth());
				out.getOutputSignalC().setDataType(offset.getDataType());
			}
	  }
	 public void checkDimension() throws MatDimException{
		}
}
