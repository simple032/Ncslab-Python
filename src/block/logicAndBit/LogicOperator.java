package block.logicAndBit;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import code.c.CodeStructC;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class LogicOperator extends Block{
	private double num;

	  public LogicOperator(JSONObject blockIn,NCSLabModel model) {
			super(blockIn,model);
			OutputPort output=new OutputPort(this,1,true);
			output.setDimThrough(false);
			outputPortList.add(output);
			paraseParamValues();		
	  }
	  
	  public void paraseParamValues( ) {
			num = paramValues.getDouble("Inputs");
			for(int i=0;i<num;i++) {
				inputPortList.add(new InputPort(this,i+1));
			}
		}
	  
	  public void generateOutputCodeC(CodeStructC code) {
			String outputCode="/*Code for output of block Logical operator:("+getBlockId()+")"+getBlockName()+"*/\n";
			OutputPort out  = outputPortList.get(0);
			OutputSignal signal1=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			String operator=paramValues.getString("Operator");
			OutputSignal signal[]=new OutputSignal[(int) num];
			for(int i = 0; i < num; i++) {
				signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			}
			switch(signal1.getDataType()) {
			case REAL:
				switch(operator) {
				case "AND":
					 outputCode+="double pro"+getBlockId()+"=1.0;\n";
					 for(OutputSignal x:signal) {
						 outputCode+="pro"+getBlockId()+"=pro"+getBlockId()+"*"+x.getName()+";\n";
					 }
					 outputCode+="if(pro"+getBlockId()+"==0){\n";
					 outputCode+=out.getOutputSignalC().getName()+"=0;}else{\n";
					 outputCode+=out.getOutputSignalC().getName()+"=1.0;}\n";
					break;
				case "OR":
					 outputCode+="double sum"+getBlockId()+"=0.0;\n";
					 for(OutputSignal x:signal) {
						 outputCode+="sum"+getBlockId()+"=sum"+getBlockId()+"+"+x.getName()+";\n";
					 }
					 outputCode+="if(sum"+getBlockId()+"==0){\n";
					 outputCode+=out.getOutputSignalC().getName()+"=0;}else{\n";
					 outputCode+=out.getOutputSignalC().getName()+"=1.0;}\n";
					break;
				case "NAND":
					outputCode+="double pro"+getBlockId()+"=1.0;\n";
					 for(OutputSignal x:signal) {
						 outputCode+="pro"+getBlockId()+"=pro"+getBlockId()+"*"+x.getName()+";\n";
					 }
					 outputCode+="if(pro"+getBlockId()+"==0){\n";
					 outputCode+=out.getOutputSignalC().getName()+"=1.0;}else{\n";
					 outputCode+=out.getOutputSignalC().getName()+"=0.0;}\n";
					break;
				case "NOR":
					 outputCode+="double sum"+getBlockId()+"=0.0;\n";
					 for(OutputSignal x:signal) {
						 outputCode+="sum"+getBlockId()+"=sum"+getBlockId()+"+"+x.getName()+";\n";
					 }
					 outputCode+="if(sum"+getBlockId()+"==0){\n";
					 outputCode+=out.getOutputSignalC().getName()+"=1.0;}else{\n";
					 outputCode+=out.getOutputSignalC().getName()+"=0;}\n";
					break;
				case "XOR":
					 outputCode+="int sum"+getBlockId()+"=0;\n";
					 for(OutputSignal x:signal) {
						 outputCode+="if("+x.getName()+"!=0){\n";
						 outputCode+="sum"+getBlockId()+"=sum"+getBlockId()+"+1;}\n";
					 }
					 outputCode+="if((sum"+getBlockId()+"%2)==0){\n";
					 outputCode+=out.getOutputSignalC().getName()+"=0.0;}else{\n";
					 outputCode+=out.getOutputSignalC().getName()+"=1;}\n";
					break;
				case "NXOR":
					outputCode+="int sum"+getBlockId()+"=0;\n";
					 for(OutputSignal x:signal) {
						 outputCode+="if("+x.getName()+"!=0){\n";
						 outputCode+="sum"+getBlockId()+"=sum"+getBlockId()+"+1;}\n";
					 }
					 outputCode+="if((sum"+getBlockId()+"%2)==0){\n";
					 outputCode+=out.getOutputSignalC().getName()+"=1;}else{\n";
					 outputCode+=out.getOutputSignalC().getName()+"=0;}\n";
					break;
				case "NOT":
					outputCode+="if("+signal1.getName()+"==0) {\n";
					outputCode+=out.getOutputSignalC().getName()+"=1;}else{\n";
					outputCode+=out.getOutputSignalC().getName()+"=0;}\n";
					break;
				}
				break;
			case MATRIX:
				switch(operator){
				case "AND":
					outputCode+="double pro"+getBlockId()+"["+signal1.getHeight()+"]["+signal1.getWidth()+"];\n";
					for(int i=0; i < signal1.getHeight(); i++) {
						for(int j=0; j < signal1.getWidth(); j++) {
							outputCode+="pro"+getBlockId()+"["+i+"]["+j+"]=1.0;\n";
							 for(OutputSignal x:signal) {
								 outputCode+="pro"+getBlockId()+"["+i+"]["+j+"]=pro"+getBlockId()+"["+i+"]["+j+"]*"+x.getName()+"("+i+","+j+");\n";
							 }
							 outputCode+="if(pro"+getBlockId()+"["+i+"]["+j+"]==0){\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0;}else{\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1.0;}\n";
					   }
					}
				break;
				case "OR":
					outputCode+="double sum"+getBlockId()+"["+signal1.getHeight()+"]["+signal1.getWidth()+"];\n";
					for(int i=0; i < signal1.getHeight(); i++) {
						for(int j=0; j < signal1.getWidth(); j++) {
							outputCode+="sum"+getBlockId()+"["+i+"]["+j+"]=0.0;\n";
							 for(OutputSignal x:signal) {
								 outputCode+="sum"+getBlockId()+"["+i+"]["+j+"]=sum"+getBlockId()+"["+i+"]["+j+"]+"+x.getName()+"("+i+","+j+");\n";
							 }
							 outputCode+="if(sum"+getBlockId()+"["+i+"]["+j+"]==0){\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0;}else{\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1.0;}\n";
					   }
					}
					break;
				case "NAND":
					outputCode+="double pro"+getBlockId()+"["+signal1.getHeight()+"]["+signal1.getWidth()+"];\n";
					for(int i=0; i < signal1.getHeight(); i++) {
						for(int j=0; j < signal1.getWidth(); j++) {
							outputCode+="pro"+getBlockId()+"["+i+"]["+j+"]=1.0;\n";
							 for(OutputSignal x:signal) {
								 outputCode+="pro"+getBlockId()+"["+i+"]["+j+"]=pro"+getBlockId()+"["+i+"]["+j+"]*"+x.getName()+"("+i+","+j+");\n";
							 }
							 outputCode+="if(pro"+getBlockId()+"["+i+"]["+j+"]==0){\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1;}else{\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
					   }
					}
					break;
				case "NOR":
					outputCode+="double sum"+getBlockId()+"["+signal1.getHeight()+"]["+signal1.getWidth()+"];\n";
					for(int i=0; i < signal1.getHeight(); i++) {
						for(int j=0; j < signal1.getWidth(); j++) {
							outputCode+="sum"+getBlockId()+"["+i+"]["+j+"]=0.0;\n";
							 for(OutputSignal x:signal) {
								 outputCode+="sum"+getBlockId()+"["+i+"]["+j+"]=sum"+getBlockId()+"["+i+"]["+j+"]+"+x.getName()+"("+i+","+j+");\n";
							 }
							 outputCode+="if(sum"+getBlockId()+"["+i+"]["+j+"]==0){\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1.0;}else{\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
					   }
					}
					break;
				case "XOR":
					outputCode+="int sum"+getBlockId()+"["+signal1.getHeight()+"]["+signal1.getWidth()+"];\n";
					for(int i=0; i < signal1.getHeight(); i++) {
						for(int j=0; j < signal1.getWidth(); j++) {
							outputCode+="sum"+getBlockId()+"["+i+"]["+j+"]=0;\n";
							for(OutputSignal x:signal) {
								 outputCode+="if("+x.getName()+"("+i+","+j+")!=0){\n";
								 outputCode+="sum"+getBlockId()+"["+i+"]["+j+"]=sum"+getBlockId()+"["+i+"]["+j+"]+1;}\n";
							 }
							 outputCode+="if((sum"+getBlockId()+"["+i+"]["+j+"]%2)==0){\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0.0;}else{\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1;}\n";
					   }
					}
					break;
				case "NXOR":
					outputCode+="int sum"+getBlockId()+"["+signal1.getHeight()+"]["+signal1.getWidth()+"];\n";
					for(int i=0; i < signal1.getHeight(); i++) {
						for(int j=0; j < signal1.getWidth(); j++) {
							outputCode+="sum"+getBlockId()+"["+i+"]["+j+"]=0;\n";
							for(OutputSignal x:signal) {
								 outputCode+="if("+x.getName()+"("+i+","+j+")!=0){\n";
								 outputCode+="sum"+getBlockId()+"["+i+"]["+j+"]=sum"+getBlockId()+"["+i+"]["+j+"]+1;}\n";
							 }
							 outputCode+="if((sum"+getBlockId()+"["+i+"]["+j+"]%2)==0){\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1;}else{\n";
							 outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
					   }
					}
					break;
				case "NOT":
					for(int i=0; i < signal1.getHeight(); i++) {
						for(int j=0; j < signal1.getWidth(); j++) {
							outputCode+="if("+signal1.getName()+"("+i+","+j+")==0) {\n";
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=1;}else{\n";
							outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=0;}\n";
						}
					}
				}
				
			
				break;
			}
			code.addOutputCode(outputCode);
		}
	  
	  public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			OutputSignal signal[]=new OutputSignal[(int) num];
			for(int i = 0; i < num; i++) {
				signal[i] = inputPortList.get(i).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			}
			int m=signal[0].getHeight();
			int n=signal[0].getWidth();
			int v=1;
			for(OutputSignal x:signal) {
				if((x.getHeight()!=m)||(x.getWidth()!=n)) {
					v=0;
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions doesn't match !\n \n");
					throw(e);
				}
			}
			
			if(v==1) {
				out.setHeight(signal[0].getHeight());
				out.setWidth(signal[0].getWidth());
				out.getOutputSignalC().setHeight(signal[0].getHeight());
				out.getOutputSignalC().setWidth(signal[0].getWidth());
				out.getOutputSignalC().setDataType(signal[0].getDataType());	
			}	
		}
		public void checkDimension() throws MatDimException{
			if(paramValues.getString("Operator").equals("NOT") && num>1) {
				MatDimException e=new MatDimException("when Block "+this.blockName+ "operater is NOT,there must be one input!\n \n");
				throw(e);
			}
		}
}
