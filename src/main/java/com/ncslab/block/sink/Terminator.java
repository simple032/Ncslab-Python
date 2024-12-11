package com.ncslab.block.sink;

import com.ncslab.ncslablink.MatDimException;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class Terminator extends SinkBlock{



    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {


        inputNames.add("in1");
    }
	public Terminator(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);

		//һ������
		inputPortList.add(new InputPort(this,1));
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if storeEnable>0\n";
		outputCode+=getBlockName()+"=["+getBlockName()
				+" Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()
				+"_Output"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+"]"
				+";\n";
		outputCode+="end\n";
		code.addOutputCode(outputCode);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		code.addGlobalDefineCode("global "+getBlockName()+";\n");
		initCode+=getBlockName()+"=[];\n";
		initCode+="ScopeNum=ScopeNum+1;\n";
		code.addInitCode(initCode);
	}

    public void checkDimension() throws MatDimException {
    }
}
