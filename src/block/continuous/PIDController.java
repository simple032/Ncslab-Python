package block.continuous;

import org.json.JSONObject;

import block.data.DataType;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import block.io.State;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;

public class PIDController extends block.Block{
	
	Parameter cparaP;
	Parameter cparaI;
	Parameter cparaD;
	Parameter cparaN;
	Parameter lowerSaturationLimit=null;
	Parameter upperSaturationLimit=null;
	State stateIntegral;
	State stateFilter;
	public PIDController(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		//一个输入，一个输出
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,true));
		cparaP=new Parameter(this,parameterList.size()+1,"P",paramValues.getString("P"));
		parameterList.add(cparaP);
		cparaI=new Parameter(this,parameterList.size()+1,"I",paramValues.getString("I"));
		parameterList.add(cparaI);
		cparaD=new Parameter(this,parameterList.size()+1,"D",paramValues.getString("D"));
		parameterList.add(cparaD);
		cparaN=new Parameter(this,parameterList.size()+1,"N",paramValues.getString("N"));
		parameterList.add(cparaN);
		
		/*stateIntegral=new State(this,1,"integral");
		stateList.add(stateIntegral);
		stateFilter=new State(this,2,"filter");
		stateList.add(stateFilter);*/
		

		if(paramValues.getString("LimitOutput").equals("on")) {
			lowerSaturationLimit=new Parameter(this,parameterList.size()+1,"LowerSaturationLimit",paramValues.getString("LowerSaturationLimit"));
			parameterList.add(lowerSaturationLimit);
			upperSaturationLimit=new Parameter(this,parameterList.size()+1,"UpperSaturationLimit",paramValues.getString("UpperSaturationLimit"));
			parameterList.add(upperSaturationLimit);
		}
	}
	 //define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block discrete_Delay:("+getBlockId()+")"+getBlockName()+"*/\n";
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		// arraysCode+="double "+"Block"+getBlockId()+"save_data[5];\n";
		 if(signal.getDataType()==DataType.MATRIX) {
			 arraysCode+="double "+"Block"+getBlockId()+"save_data["+signal.getHeight()+"]["+signal.getWidth()+"*5];\n"; 
		 }else if(signal.getDataType()==DataType.REAL&&cparaP.getDataType()==DataType.REAL) {
		 arraysCode+="double "+"Block"+getBlockId()+"save_data[5];\n";
		 }else {
		 arraysCode+="double "+"Block"+getBlockId()+"save_data["+cparaP.getHeight()+"]["+cparaP.getWidth()+"*5];\n";
		 }
		 code.addArraysCode(arraysCode);
	 }
	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		String initCode="/*Code for initialization of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		initCode+=cparaP.getInitCodeC();
		initCode+=cparaI.getInitCodeC();
		initCode+=cparaD.getInitCodeC();
		initCode+=cparaN.getInitCodeC();
		if(paramValues.getString("LimitOutput").equals("on")) {
			initCode+=lowerSaturationLimit.getInitCodeC();
			initCode+=upperSaturationLimit.getInitCodeC();
		}
		if(cparaP.getDataType()==DataType.REAL&&stateIntegral.getDataType()==DataType.REAL) {
			  initCode+=stateIntegral.getName()+"=0;\n";
			  initCode+=stateFilter.getName()+"=0;\n";
		}else{
			  for(int i=0; i<stateIntegral.getHeight(); i++) {
					for(int j=0;j<stateIntegral.getWidth();j++) {	
						initCode+=stateIntegral.getName()+"("+i+","+j+")=0;\n";
						initCode+=stateFilter.getName()+"("+i+","+j+")=0;\n";
						}
					}
				}
		code.addInitCode(initCode);
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 switch(cparaP.getDataType()) {
		 case REAL:
			 switch(signal.getDataType()) {
			 case REAL:
				 outputCode+="if(mp->majorStep>0) {\n";
		            outputCode+="Block"+getBlockId()+"save_data[0]="+cparaP.getName()+"*"+signal.getName()+";\n";
		            outputCode+="Block"+getBlockId()+"save_data[1]="+cparaD.getName()+"*"+signal.getName()+";\n";
		            outputCode+="Block"+getBlockId()+"save_data[2]="+cparaI.getName()+"*"+signal.getName()+";\n";
		            outputCode+="Block"+getBlockId()+"save_data[3]=(Block"+getBlockId()+"save_data[1]-"+stateFilter.getName()+")*"+cparaN.getName()+";\n";
		            outputCode+="Block"+getBlockId()+"save_data[4]="+"Block"+getBlockId()+"save_data[0]"+"+"+stateIntegral.getName()+"+Block"+getBlockId()+"save_data[3]"+";\n";
		           
		            if(paramValues.getString("LimitOutput").equals("on")) {
		            outputCode+="if(Block"+getBlockId()+"save_data[4]>"+upperSaturationLimit.getName()+"){\n";
		            outputCode+=this.getOutputPortVariable(0)+"="+upperSaturationLimit.getName()+";}\n";
		            outputCode+="else if(Block"+getBlockId()+"save_data[4]<"+lowerSaturationLimit.getName()+"){\n";
		            outputCode+=this.getOutputPortVariable(0)+"="+lowerSaturationLimit.getName()+";}\n";
		            outputCode+="else{\n";
		            outputCode+=this.getOutputPortVariable(0)+"="+"Block"+getBlockId()+"save_data[4];}\n";
		             }else {
			        outputCode+=this.getOutputPortVariable(0)+"="+"Block"+getBlockId()+"save_data[4];\n";
		                  }
		            outputCode+="}\n";
		         break;
			 case MATRIX:
				 for(int i=0; i<signal.getHeight(); i++) {
						for(int j=0;j<signal.getWidth();j++) {
							  outputCode+="if(mp->majorStep>0) {\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5]="+cparaP.getName()+"*"+signal.getName()+"("+i+","+j+");\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+1]="+cparaD.getName()+"*"+signal.getName()+"("+i+","+j+");\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+2]="+cparaI.getName()+"*"+signal.getName()+"("+i+","+j+");\n";
					            outputCode+="}\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+3]=(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+1]-"+stateFilter.getName()+"("+i+","+j+"))*"+cparaN.getName()+";\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5]"+"+"+stateIntegral.getName()+"("+i+","+j+")+Block"+getBlockId()+"save_data["+i+"]["+j+"*5+3];\n";
					            if(paramValues.getString("LimitOutput").equals("on")) {
					            outputCode+="if(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]>"+upperSaturationLimit.getName()+"){\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+upperSaturationLimit.getName()+";}\n";
					            outputCode+="else if(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]<"+lowerSaturationLimit.getName()+"){\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+lowerSaturationLimit.getName()+";}\n";
					            outputCode+="else{\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4];}\n";
					             }else {
						        outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4];\n";
					           }
					            outputCode+="}\n";
						  }
						}
				 break;
		  }
			 break;
		 case MATRIX:
			 switch(signal.getDataType()) {
			 case REAL:
				 for(int i=0; i<cparaP.getHeight(); i++) {
						for(int j=0;j<cparaP.getWidth();j++) {
							  outputCode+="if(mp->majorStep>0) {\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5]="+cparaP.getName()+"("+i+","+j+")*"+signal.getName()+";\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+1]="+cparaD.getName()+"("+i+","+j+")*"+signal.getName()+";\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+2]="+cparaI.getName()+"("+i+","+j+")*"+signal.getName()+";\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+3]=(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+1]-"+stateFilter.getName()+"("+i+","+j+"))*"+cparaN.getName()+"("+i+","+j+");\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5]"+"+"+stateIntegral.getName()+"("+i+","+j+")+Block"+getBlockId()+"save_data["+i+"]["+j+"*5+3];\n";
					            if(paramValues.getString("LimitOutput").equals("on")) {
					            outputCode+="if(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]>"+upperSaturationLimit.getName()+"("+i+","+j+")){\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+upperSaturationLimit.getName()+"("+i+","+j+");}\n";
					            outputCode+="else if(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]<"+lowerSaturationLimit.getName()+"("+i+","+j+")){\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+lowerSaturationLimit.getName()+"("+i+","+j+");}\n";
					            outputCode+="else{\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4];}\n";
					             }else {
						        outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4];\n";
					           }
					            outputCode+="}\n";
						  }
						}
				 break;
			 case MATRIX:
				 for(int i=0; i<cparaP.getHeight(); i++) {
						for(int j=0;j<cparaP.getWidth();j++) {
							  outputCode+="if(mp->majorStep>0) {\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5]="+cparaP.getName()+"("+i+","+j+")*"+signal.getName()+"("+i+","+j+");\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+1]="+cparaD.getName()+"("+i+","+j+")*"+signal.getName()+"("+i+","+j+");\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+2]="+cparaI.getName()+"("+i+","+j+")*"+signal.getName()+"("+i+","+j+");\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+3]=(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+1]-"+stateFilter.getName()+"("+i+","+j+"))*"+cparaN.getName()+"("+i+","+j+");\n";
					            outputCode+="Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5]"+"+"+stateIntegral.getName()+"("+i+","+j+")+Block"+getBlockId()+"save_data["+i+"]["+j+"*5+3];\n";
					            if(paramValues.getString("LimitOutput").equals("on")) {
					            outputCode+="if(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]>"+upperSaturationLimit.getName()+"("+i+","+j+")){\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+upperSaturationLimit.getName()+"("+i+","+j+");}\n";
					            outputCode+="else if(Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4]<"+lowerSaturationLimit.getName()+"("+i+","+j+")){\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+lowerSaturationLimit.getName()+"("+i+","+j+");}\n";
					            outputCode+="else{\n";
					            outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4];}\n";
					             }else {
						        outputCode+=this.getOutputPortVariable(0)+"("+i+","+j+")="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5+4];\n";
					           }
					            outputCode+="}\n";
						  }
						}
				 break;
			 }
			 break;
		}
		code.addOutputCode(outputCode);
	}
	
	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of PID Controller:("+getBlockId()+")"+getBlockName()+"*/\n";
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 if(signal.getDataType()==DataType.REAL&&cparaP.getDataType()==DataType.REAL) {
			 derivativeCode+=stateIntegral.getDerivativeName()+"="+"Block"+getBlockId()+"save_data[2];\n";
			 derivativeCode+=stateFilter.getDerivativeName()+"="+"Block"+getBlockId()+"save_data[3];\n"; 
		 }else {
			 for(int i=0; i<stateIntegral.getHeight(); i++) {
					for(int j=0;j<stateIntegral.getWidth();j++) {	
						derivativeCode+=stateIntegral.getDerivativeName()+"("+i+","+j+")="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5+2];\n";
						derivativeCode+=stateFilter.getDerivativeName()+"("+i+","+j+")="+"Block"+getBlockId()+"save_data["+i+"]["+j+"*5+3];\n";
						}
					} 
		 }
		code.addDerivativeCode(derivativeCode);
	}
	public void  generateUpdateCodeC(CodeStructC code) {
 		String updateCode="/*Code for Update of "+ ":("+getBlockId()+")"+getBlockName()+"*/\n";
 		OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
 		if(signal.getDataType()==DataType.REAL&&cparaP.getDataType()==DataType.REAL) {
 			updateCode+=stateIntegral.getName()+"="+stateIntegral.getName()+"+"+stateIntegral.getDerivativeName()+"*model.stepSize;\n";
 			updateCode+=stateFilter.getName()+"="+stateFilter.getName()+"+"+stateFilter.getDerivativeName()+"*model.stepSize;\n";
 		}else {
 			 for(int i=0; i<stateFilter.getHeight(); i++) {
					for(int j=0;j<stateFilter.getWidth();j++) {
						updateCode+=stateIntegral.getName()+"("+i+","+j+")="+stateIntegral.getName()+"("+i+","+j+")+"+stateIntegral.getDerivativeName()+"("+i+","+j+")*model.stepSize;\n";
			 			updateCode+=stateFilter.getName()+"("+i+","+j+")="+stateFilter.getName()+"("+i+","+j+")+"+stateFilter.getDerivativeName()+"("+i+","+j+")*model.stepSize;\n";
				     	}
					}
 		}
 		code.addUpdateCode(updateCode);	
 	}
	   public void updateDimension() throws MatDimException{
		    OutputPort out  = outputPortList.get(0);
		    InputPort in  = inputPortList.get(0);
		    OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		    if(signal.getDataType()==DataType.REAL) {
		    	stateIntegral=new State(this,1,"stateIntegral",cparaP.getHeight(),cparaP.getWidth());
		    	stateFilter=new State(this,2,"stateFilter",cparaP.getHeight(),cparaP.getWidth());
		    	}
				else {
				stateIntegral=new State(this,1,"stateIntegral",signal.getHeight(),signal.getWidth());	
				stateFilter=new State(this,2,"stateFilter",signal.getHeight(),signal.getWidth());
				}
		        stateList.add(stateIntegral);
				stateList.add(stateFilter);
				if(cparaP.getWidth()!=cparaD.getWidth()||
				   cparaP.getWidth()!=cparaI.getWidth()||
				   cparaP.getWidth()!=cparaN.getWidth()||
				   cparaP.getHeight()!=cparaD.getHeight()||
				   cparaP.getHeight()!=cparaI.getHeight()|| 
				   cparaP.getHeight()!=cparaN.getHeight()) {
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match!All input dimension must be same!\n \n");
					throw(e);
					}
				if(paramValues.getString("LimitOutput").equals("on")) {
					if(lowerSaturationLimit.getHeight()!=cparaP.getHeight()||
					 upperSaturationLimit.getHeight()!=cparaP.getHeight()||
					 lowerSaturationLimit.getWidth()!=cparaP.getWidth()||
					 upperSaturationLimit.getWidth()!=cparaP.getWidth()) {
					MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match!All input dimension must be same!\n \n");
					throw(e);
					}
				}
		    
		    if(cparaP.getDataType()==DataType.MATRIX&&signal.getDataType()==DataType.REAL) {
				out.setHeight(cparaP.getHeight());
				out.setWidth(cparaP.getWidth());
				out.getOutputSignalC().setHeight(cparaP.getHeight());
				out.getOutputSignalC().setWidth(cparaP.getWidth());
				out.getOutputSignalC().setDataType(DataType.MATRIX);	
			}
			else if(cparaP.getDataType()==DataType.REAL&&signal.getDataType()==DataType.MATRIX) {
				out.setHeight(signal.getHeight());
				out.setWidth(signal.getWidth());
				out.getOutputSignalC().setHeight(signal.getHeight());
				out.getOutputSignalC().setWidth(signal.getWidth());
				out.getOutputSignalC().setDataType(signal.getDataType());	
			}
			else{
				if(cparaP.getWidth()!=signal.getWidth()||cparaP.getHeight()!=signal.getHeight()) {
				MatDimException e=new MatDimException("Block "+this.blockName+" input dimension doesn't match the P dimension!\n \n");
				throw(e);
				}
				out.setHeight(cparaP.getHeight());
				out.setWidth(cparaP.getWidth());
				out.getOutputSignalC().setHeight(cparaP.getHeight());
				out.getOutputSignalC().setWidth(cparaP.getWidth());
				out.getOutputSignalC().setDataType(cparaP.getDataType());	
			}
	     }
	   public void checkDimension() throws MatDimException{
	    }   
}
