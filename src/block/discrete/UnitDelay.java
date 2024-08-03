package block.discrete;
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
import block.io.State;
public class UnitDelay extends Block{
	block.io.Parameter sampleTime;
	block.io.Parameter initialCondition;
	public UnitDelay(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		outputPortList.add(new OutputPort(this,1,false));
		sampleTime=new Parameter(this,1,"sampleTime",paramValues.getString("SampleTime"));
		initialCondition=new Parameter(this,2,"initialCondition",paramValues.getString("InitialCondition"));
		parameterList.add(sampleTime);
		parameterList.add(initialCondition);	
  }
	 //define arrays to save data
	 public void generateArraysCodeC(CodeStructC code) {
		 String arraysCode="/*Define arrays for block discrete_unit_Delay:("+getBlockId()+")"+getBlockName()+"*/\n";
		 OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		 arraysCode+="double "+"Block"+getBlockId()+"_unit_delay_savedata["+signal.getHeight()+"]["+signal.getWidth()+"*2];\n";
		 code.addArraysCode(arraysCode);
	 }
	 
	 public void generateInitCodeC(CodeStructC code) {
			super.generateInitCodeC(code);
			String initCode="/*Code for initialization of block Unit Delay:("+getBlockId()+")"+getBlockName()+"*/\n";
			initCode+=sampleTime.getInitCodeC();
			initCode+=initialCondition.getInitCodeC();
			code.addInitCode(initCode);
			}
	 public void generateOutputCodeC (CodeStructC code){
		  String outputCode="/*Code for output of block Unit Delay:("+getBlockId()+")"+getBlockName()+"*/\n";
			  
		  OutputPort out  = outputPortList.get(0);
		  OutputPort ops = inputPortList.get(0).getLinkedLine().getLinkedOutputPort();
		  OutputSignal signal=inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();	  
		  outputCode+="{real_T currentTime = model.time;\n";
		  outputCode+="real_T sampleTimeTmp = "+sampleTime.getName()+"==-1?model.stepSize:"+sampleTime.getName()+";\n";
		  switch(signal.getDataType()) {
		  case REAL:
//			  outputCode+="if(fabs((int)(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001) {\n";
			  outputCode+="if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001) {\n";
			  outputCode+="Block"+getBlockId()+"_unit_delay_savedata[0][(int)(currentTime/sampleTimeTmp)%2]="+signal.getName()+";}\n";
//			  outputCode+="Block"+getBlockId()+"_unit_delay_savedata[0][(int)(currentTime/"+sampleTime.getName()+")%2]="+signal.getName()+";}\n";
//			  outputCode+="if(currentTime<"+sampleTime.getName()+") {\n";
			  outputCode+="if(currentTime<sampleTimeTmp) {\n";
			  outputCode+=out.getOutputSignalC().getName()+"="+initialCondition.getName()+";}\n";
			  outputCode+="else {\n";
			  outputCode+="if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001) {\n";
			  outputCode+=out.getOutputSignalC().getName()+"=Block"+getBlockId()+"_unit_delay_savedata[0][(int)(currentTime/sampleTimeTmp+1)%2];}}\n";
//			  outputCode+="if(fabs((int)(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001) {\n";
//			  outputCode+=out.getOutputSignalC().getName()+"=Block"+getBlockId()+"_unit_delay_savedata[0][(int)(currentTime/"+sampleTime.getName()+"+1)%2];}}\n";
			  break;
		  case MATRIX:
			  for(int i=0; i<ops.getHeight(); i++) {
					for(int j=0;j<ops.getWidth();j++) {
			outputCode+="if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001) {\n";
//			outputCode+="if(fabs((int)(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001) {\n";
			outputCode+="Block"+getBlockId()+"_unit_delay_savedata["+i+"][(int)(currentTime/sampleTimeTmp)%2+2*"+j+"]="+signal.getName()+"("+i+","+j+");}\n";
//			outputCode+="Block"+getBlockId()+"_unit_delay_savedata["+i+"][(int)(currentTime/"+sampleTime.getName()+")%2+2*"+j+"]="+signal.getName()+"("+i+","+j+");}\n";
//			outputCode+="if(currentTime<"+sampleTime.getName()+") {\n";
			outputCode+="if(currentTime<sampleTimeTmp) {\n";
			outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")="+initialCondition.getName()+";}\n";
			outputCode+="else {\n";
			outputCode+="if(fabs(floor(currentTime/sampleTimeTmp+0.5)-currentTime/sampleTimeTmp)<0.000001) {\n";
//			outputCode+="if(fabs((int)(currentTime/"+sampleTime.getName()+"+0.5)-currentTime/"+sampleTime.getName()+")<0.000001) {\n";
//			outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=Block"+getBlockId()+"_unit_delay_savedata["+i+"][!((int)(currentTime/"+sampleTime.getName()+")%2)+2*"+j+"];}}\n";
			outputCode+=out.getOutputSignalC().getName()+"("+i+","+j+")=Block"+getBlockId()+"_unit_delay_savedata["+i+"][!((int)(currentTime/sampleTimeTmp)%2)+2*"+j+"];}}\n";
				   }	
				}
			  break;
			  }
		  outputCode+="}\n";
		  code.addOutputCode(outputCode);	
		  }
	 public void updateDimension() throws MatDimException{
			OutputPort out  = outputPortList.get(0);
			InputPort in  = inputPortList.get(0);
			OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		    if(sampleTime.getDataType()!=DataType.REAL||initialCondition.getDataType()!=DataType.REAL) {
						MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be a real double scalar(period)!\n \n");
						throw(e);	
					}
			if((Double.parseDouble(paramValues.getString("SampleTime").trim())*1000000)%(model.getConfig().getFixedStep()*1000000)>0.000001) {
				  MatDimException e=new MatDimException("Parameter(sampleTime) of Block "+this.blockName+" must be an integer multiple of the fixed-step size!\n \n");
					throw(e);
		  }
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
		}
	 
	public void checkDimension() throws MatDimException{
	}  
}
