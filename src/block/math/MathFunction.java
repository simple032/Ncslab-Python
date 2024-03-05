package block.math;
import org.json.JSONObject;

import block.Block;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
public class MathFunction extends Block{
	
	private String seq;
	
	public MathFunction(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,true));
		//����һ������
		inputPortList.add(new InputPort(this,1));
		
		seq=paramValues.getString("Operator");
	}
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		
		String outputCode="j=" + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
		outputCode+="Block" + this.getBlockId()+"_Output1="+seq+"(j);\n";
		code.addOutputCode(outputCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block MathFunction:("+getBlockId()+")"+getBlockName()+"*/\n";
		
		OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+seq+"("+signal.getName()+");\n";
		
		if(seq.equals("transpose")) {
			switch(signal.getDataType()) {
			case REAL:
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+signal.getName()+";\n";
				break;
			case MATRIX:
				int width=signal.getWidth();
				int height=signal.getHeight();
				
				outputCode+="for(int i=0;i<"+width+";i++){\n";
				outputCode+="for(int j=0;j<"+height+";j++){\n";
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"(i,j)="+signal.getName()+"(j,i);\n";
				outputCode+="}\n";
				outputCode+="}\n";
				break;
			}
		}
		else {
			switch(signal.getDataType()) {
			case REAL:
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+seq+"("+signal.getName()+");\n";
				break;
			case MATRIX:
				int width=signal.getWidth();
				int height=signal.getHeight();
				
				outputCode+="for(int i=0;i<"+height+";i++){\n";
				outputCode+="for(int j=0;j<"+width+";j++){\n";
				outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"(i,j)="+seq+"("+signal.getName()+"(i,j));\n";
				outputCode+="}\n";
				outputCode+="}\n";
				
				break;
			}
		}
		
		code.addOutputCode(outputCode);
	}
	
	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(seq.equals("transpose")) {
			out.setHeight(signal.getWidth());
			out.setWidth(signal.getHeight());
			out.getOutputSignalC().setHeight(signal.getWidth());
			out.getOutputSignalC().setWidth(signal.getHeight());
			out.getOutputSignalC().setDataType(signal.getDataType());
		}
		else {
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());	
		}
	}
	
	public void checkDimension() throws MatDimException{
	}
}
