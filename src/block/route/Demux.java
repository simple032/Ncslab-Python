package block.route;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.io.State;
import block.math.Matrix;
import code.c.CodeStructC;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class Demux extends Block {
	private int num;
	private boolean feedThrough = true;

	public Demux(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		
		this.num = paramValues.getInt("Outputs");				
		
		//һ�����룬һ�����
		for(int i=0; i<num; i++) {
			outputPortList.add(new OutputPort(this,i+1,feedThrough));
		}
		inputPortList.add(new InputPort(this,1,num));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode=getOutputPortVariable(0)+"=0";

		
		code.addInitCode(initCode);
	}
	
	
	
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
			
		String outputCode=getOutputPortVariable(0)+"=0";
		
		
		outputCode+=";\n";
		
		code.addOutputCode(outputCode);
	}
	
	public void generateDerivativeCodeM(CodeStructM code) {
		super.generateDerivativeCodeM(code);
		
		String derivativeCode="";
		

		derivativeCode+=");\n";
		
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
		
		for(int i=0; i<num; i++) {
			outputCode+=getOutputPortVariable(i)+"="
				+getInputPortVariable(0)+"["+i+"]"
				+";\n";
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
}
