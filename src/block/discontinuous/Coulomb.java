package block.discontinuous;
import org.json.JSONObject;

import block.Block;
import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
public class Coulomb extends Block{
	block.io.Parameter offset;
	block.io.Parameter gain;
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
	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Coulomb:("+getBlockId()+")"+getBlockName()+"*/\n";
			initCode+=offset.getInitCodeC();
			initCode+=gain.getInitCodeC();
			code.addInitCode(initCode);
		}
	 public void generateOutputCodeC(CodeStructC code) {
			String outputCode="/*Code for output of block Coulomb:("+getBlockId()+")"+getBlockName()+"*/\n";
			
			OutputPort out  = outputPortList.get(0);
			OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
			OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			switch(offset.getDataType()) {
			case REAL:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					outputCode+="if("+signal.getName()+">0) {\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=1*("+gain.getName()+"*fabs("+signal.getName()+")+"+offset.getName()+");}\n";
					outputCode+="else if("+ signal.getName()+"<0) {\n";	
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=(-1)*("+gain.getName()+"*fabs("+signal.getName()+")+"+offset.getName()+");}\n";
					outputCode+="else {\n";
					outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;}\n";
					break;
				case MATRIX:
					for(int i=0; i<ops.getHeight(); i++) {
						for(int j=0;j<ops.getWidth();j++) {
							outputCode+="if("+signal.getName()+"("+i+","+j+")>0) {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=1*("+gain.getName()+"*fabs("+signal.getName()+"("+i+","+j+"))+"+offset.getName()+");}\n";
							outputCode+="else if("+ signal.getName()+"("+i+","+j+")<0) {\n";	
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=(-1)*("+gain.getName()+"*fabs("+signal.getName()+"("+i+","+j+"))+"+offset.getName()+");}\n";
							outputCode+="else {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
						}
					}
					break;
				}
				break;
			case MATRIX:
				switch(ops.getOutputSignalC().getDataType()) {
				case REAL:
					for(int i=0; i<offset.getHeight(); i++) {
						for(int j=0;j<offset.getWidth();j++) {
							outputCode+="if("+signal.getName()+">0) {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=1*("+gain.getName()+"("+i+","+j+")*fabs("+signal.getName()+")+"+offset.getName()+"("+i+","+j+"));}\n";
							outputCode+="else if("+ signal.getName()+"<0) {\n";	
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=(-1)*("+gain.getName()+"("+i+","+j+")*fabs("+signal.getName()+")+"+offset.getName()+"("+i+","+j+"));}\n";
							outputCode+="else {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
						}
					}
					break;
				case MATRIX:
					for(int i=0; i<offset.getHeight(); i++) {
						for(int j=0;j<offset.getWidth();j++) {
							outputCode+="if("+signal.getName()+"("+i+","+j+")>0) {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=1*("+gain.getName()+"("+i+","+j+")*fabs("+signal.getName()+"("+i+","+j+"))+"+offset.getName()+"("+i+","+j+"));}\n";
							outputCode+="else if("+ signal.getName()+"("+i+","+j+")<0) {\n";	
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=(-1)*("+gain.getName()+"("+i+","+j+")*fabs("+signal.getName()+"("+i+","+j+"))+"+offset.getName()+"("+i+","+j+"));}\n";
							outputCode+="else {\n";
							outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
						}
					}
					break;
				}
				break;
			}
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
