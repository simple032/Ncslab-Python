package com.ncslab.code.c.linux.loong;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;

public class CodeModelCLinuxLoong extends CodeModelC{

	private CodeStructCLinuxLoong codeLoong=new CodeStructCLinuxLoong(this);

	CodeModelCLinuxLoong(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}

	protected CodeStructC getCodeStructC() {
		return codeLoong;
	}

	public static CodeModelCLinuxLoong createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxLoong model=new CodeModelCLinuxLoong(jsonIn,mode);

		return model;
	}
}
