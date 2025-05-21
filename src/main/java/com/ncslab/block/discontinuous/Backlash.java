package com.ncslab.block.discontinuous;
import lombok.Getter;
import org.json.JSONObject;
import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;

import Jama.Matrix;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Backlash extends Block{
	Parameter backlashWidth;
	Parameter initialOutput;

	private State xState;

	VelocityContext context;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("BacklashWidth");
        parameterNames.add("InitialOutput");
        outputNames.add("out1");
        inputNames.add("in1");
    }

	public Backlash(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		//һ�����룬һ�����
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		backlashWidth=new Parameter(this,1,"BacklashWidth",paramValues.getString("BacklashWidth"));
		initialOutput=new Parameter(this,2,"InitialOutput",paramValues.getString("InitialOutput"));
		parameterList.add(backlashWidth);
		parameterList.add(initialOutput);


	}

	private void prepareContext() {
		context = new VelocityContext();
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

		context.put("block", this);
		context.put("inputPortList", inputPortList);
		context.put("outputPortList", outputPortList);
		context.put("backlashWidth", backlashWidth);
		context.put("initialOutput", initialOutput);
		context.put("xState", xState);
		context.put("signal",signal);
		context.put("ops", ops);
		context.put("matrixDataType", DataType.MATRIX);
	}
	 public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
        prepareContext();
		String initCode = TemplateManager.renderTemplate("c/discontinuous/Backlash/init.vm", context);
		code.addInitCode(initCode);
	}


	 public void generateOutputCodeC(CodeStructC code) {
		String outputCode = TemplateManager.renderTemplate("c/discontinuous/Backlash/output.vm", context);
		code.addOutputCode(outputCode);
	 }

	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal.getDataType()==DataType.REAL) {
			xState=new State(this,1,"save_data",backlashWidth.getHeight(),backlashWidth.getWidth());}
			else {
				xState=new State(this,1,"save_data",signal.getHeight(),signal.getWidth());
			}
			stateList.add(xState);
			if(backlashWidth.getWidth()!=initialOutput.getWidth()||backlashWidth.getHeight()!=initialOutput.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
				throw(e);
			}
			if(backlashWidth.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
				out.setHeight(backlashWidth.getHeight());
				out.setWidth(backlashWidth.getWidth());
				out.getOutputSignalC().setHeight(backlashWidth.getHeight());
				out.getOutputSignalC().setWidth(backlashWidth.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);
			}
			else if(backlashWidth.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());
			}
			else{
				if(backlashWidth.getWidth()!=signal.getWidth()||backlashWidth.getHeight()!=signal.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the gain dimension!\n \n");
				throw(e);
				}
				out.setHeight(backlashWidth.getHeight());
				out.setWidth(backlashWidth.getWidth());
				out.getOutputSignalC().setHeight(backlashWidth.getHeight());
				out.getOutputSignalC().setWidth(backlashWidth.getWidth());
				out.getOutputSignalC().setDataType(backlashWidth.getDataType());
			}
	  }
	 public void checkDimension() throws MatDimException{
		}
}
