package com.ncslab.code.c.linux.pc;

import org.json.JSONObject;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;

// TODO: currently this class is not used
public class CodeModelCLinuxPC extends CodeModelC{
	
	private CodeStructCLinuxPC codeRaspberry=new CodeStructCLinuxPC(this);
	
	CodeModelCLinuxPC(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}
	
	public static CodeModelCLinuxPC createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxPC model=new CodeModelCLinuxPC(jsonIn,mode);
		
		return model;
	}
}
