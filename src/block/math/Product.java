package block.math;

import org.json.JSONObject;
import java.util.Vector;

import block.Block;
import block.io.OutputPort;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;
import block.io.InputPort;

public class Product extends Block{
	
	private String seq;
	public Product(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//构建一个输出
		outputPortList.add(new OutputPort(this,1,true));
		paraseParamValues();
	}
	
	//根据输入数构建输入个数
	public void paraseParamValues() {
	//此处获取的是product模块的参数可为“*/"或"**"或"//"等
	seq=paramValues.getString("Inputs");
	
	for(int i=0; i<seq.length();i++) {
		inputPortList.add(new InputPort(this,i+1));
	   }
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String ouputCode="Block"+this.getBlockId()+"_Output1=1";
		
		for(int i=0;i<seq.length();i++) {
			if(seq.charAt(i)=='*') {
				ouputCode+="*";
			}
			if(seq.charAt(i)=='/') {
				ouputCode+="/";
			}
			ouputCode+="Block"+getInputPortList().get(i).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()
					+"_Output"+getInputPortList().get(i).getLinkedLine().getLinkedOutputPort().getNumber();
		}
		
		ouputCode+=";\n";
		
		code.addOutputCode(ouputCode);
	}
}
