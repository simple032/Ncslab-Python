package com.ncslab.code.c.windows;

import org.json.JSONObject;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;

public class CodeModelCWindows extends CodeModelC {
	private CodeStructCWindows codeWindows=new CodeStructCWindows(this);
	
	CodeModelCWindows(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeWindows;
	}
	
	public static CodeModelCWindows createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCWindows model=new CodeModelCWindows(jsonIn,mode);
		
		return model;
	}
	
}
