package block.math;

import org.json.JSONObject;

import block.Block;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import ncslablink.NCSLabModel;

public class Gain extends Block{
	//block.io.Parameter gain;
	Matrix gain;
	boolean multiplication = false;
	public Gain(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
		
		this.gain = new Matrix(paramValues.getString("Gain"));
		this.multiplication = "Matrix(*)".equals(paramValues.getString("Multiplication"));

		InputPort in;
		OutputPort out;
		
		if(!this.multiplication) {
			out = new OutputPort(this,1,true);
			in = new InputPort(this,1);
		}else {			
			out = new OutputPort(this,1,true,gain.row);
			in = new InputPort(this,1,gain.column);
		}
		//һ�����
		
		outputPortList.add(out);
		
		//һ������
		inputPortList.add(in);
		
//		gain=new Parameter(this,1,"value");
//		parameterList.add(gain);
		
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		//String code="Block"+this.getBlockId()+"_Output1="+paramValues.getDouble("Gain")+"*Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()+"_Output"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()+";\n";
		String ouputCode=outputPortList.get(0).getOutputSignalC().getName()+"="+paramValues.getDouble("Gain")+"*"+inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
		
		code.addOutputCode(ouputCode);
	}
	
	
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		
		String initCode="/*Code for initialization of block Gain:("+getBlockId()+")"+getBlockName()+"*/\n";
		//initCode+=gain.getName()+"="+paramValues.getDouble("Gain")+";\n"; 
		
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		OutputPort out  = outputPortList.get(0);
		OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		
		if(!this.multiplication) {
			if(out.getWidth()==1 && ops.getWidth()==1) {
				outputCode+=out.getOutputSignalC().getName()+"=";
				outputCode+="+"+gain.elements[0][0]+"*"+ops.getOutputSignalC().getName()+";\n";
			}else {			
				
				System.out.println("ops.width="+ops.getWidth());
				System.out.println("out.width="+out.getWidth());
				for(int i=0; i<ops.getWidth(); i++) {
					outputCode+=out.getOutputSignalC().getName()+"["+i+"]=";
					outputCode+=gain.elements[0][0]+"*"+ops.getOutputSignalC().getName()+"["+i+"];\n";
				}
			}	
		}else {			
			if(ops.getWidth()!=gain.column) {
				System.out.println("ops.width="+ops.getWidth()+" is not equal to "+gain.row);
				return;
			}			
				
			for(int i=0; i<out.getWidth(); i++) {
				outputCode+=out.getOutputSignalC().getName()+"["+i+"]=";
				for(int j=0; j<ops.getWidth(); j++) {
					outputCode+="+"+gain.elements[i][j]+"*"+ops.getOutputSignalC().getName()+"["+j+"]";
				}
				outputCode+=";\n";
			}
					
		}		
		code.addOutputCode(outputCode);
	}
	
	
	public void updateDimension() {
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		if(!this.multiplication) {			
			out.setWidth(in.getWidth());
			out.getOutputSignalC().setWidth(in.getWidth());
		}else {
			
		}		
	}
}
