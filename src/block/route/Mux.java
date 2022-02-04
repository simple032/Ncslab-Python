package block.route;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import block.Block;
import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import block.io.OutputSignal;
import block.math.Matrix;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class Mux extends Block {
	private int num;
	
	private boolean feedThrough = true;
	
	public Mux(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		this.num = paramValues.getInt("Inputs");				
		
		//一锟斤拷锟斤拷锟诫，一锟斤拷锟斤拷锟�
		for(int i=0; i<num; i++) {
			inputPortList.add(new InputPort(this,i+1));
		}
		outputPortList.add(new OutputPort(this,1,feedThrough));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode=getOutputPortVariable(0)+"=0;\n";

		
		code.addInitCode(initCode);
	}
	
	
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
			
		//String outputCode=getOutputPortVariable(0)+"=0";
		
		
		//outputCode+=";\n";
		String outputCode="";
		int i=0;
		outputCode+=getOutputPortVariable(0)+"=[";
		for(InputPort inputPort:inputPortList) {
			outputCode+=getInputPortVariable(i)+" ";
			i++;
		}
		outputCode+="];\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		
		String derivativeCode="";
		

		//derivativeCode+=");\n";
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";

		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
		//y(k)=Cx(k)+Du(k)
		
		/*
		for(int i=0; i<num; i++) {
			outputCode+=getOutputPortVariable(0)+"["+i+"]="
				+getInputPortVariable(i)
				+";\n";
		}*/		
		
		int fetch=0;
		int i=0;
		for(InputPort inputPort:inputPortList) {
			OutputSignal signal=inputPort.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			if(signal.getDataType()==DataType.REAL) {
				outputCode+=getOutputPortVariable(0)+"(0,"+fetch+")="
						+getInputPortVariable(i)
						+";\n";
				
				fetch++;
				i++;
			}
			else if(signal.getHeight()==1){
				for(int j=0;j<signal.getWidth();j++) {
					outputCode+=getOutputPortVariable(0)+"(0,"+fetch+")="
							+getInputPortVariable(i)+"(0,"+j+")"
							+";\n";
					
					fetch++;
				}
				i++;
			}
			else if(signal.getWidth()==1){
				for(int j=0;j<signal.getHeight();j++) {
					outputCode+=getOutputPortVariable(0)+"(0,"+fetch+")="
							+getInputPortVariable(i)+"("+j+",0)"
							+";\n";
					
					fetch++;
				}
				i++;
			}
		}
		
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		
		code.addDerivativeCode(derivativeCode);
	}
	
	public void  generateUpdateCodeC(CodeStructC code) {
		String updateCode="/*Code for Derivative of Transfer Fcn:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		code.addUpdateCode(updateCode);	
	}
	
	public void updateDimension() throws MatDimException{
		super.updateDimension();
		int size=0;
		for(InputPort inputPort:inputPortList) {
			if(inputPort.isVector()==true||inputPort.isReal()==true) {
				size+=inputPort.getVectorSize();
			}
			else {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension error!\n Matrix is not applicable for demux\n");
				throw(e);
			}
		}
		
		OutputPort output=getOutputPortList().get(0);
		output.setHeight(1);
		output.setWidth(size);
		output.getOutputSignalC().setHeight(1);
		output.getOutputSignalC().setWidth(size);
		output.getOutputSignalC().setDataType(DataType.MATRIX);
	}
}
