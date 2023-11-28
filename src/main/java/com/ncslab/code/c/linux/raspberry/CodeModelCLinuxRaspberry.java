package com.ncslab.code.c.linux.raspberry;

import org.json.JSONObject;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;

public class CodeModelCLinuxRaspberry extends CodeModelC {
	
	private CodeStructCLinuxRaspberry codeRaspberry = new CodeStructCLinuxRaspberry(this);
	
	CodeModelCLinuxRaspberry(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn, mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}
	
	public static CodeModelCLinuxRaspberry createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
		CodeModelCLinuxRaspberry model = new CodeModelCLinuxRaspberry(jsonIn, mode);
		
		return model;
	}
}
