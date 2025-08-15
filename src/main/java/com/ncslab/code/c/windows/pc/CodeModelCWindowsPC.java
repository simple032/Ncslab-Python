package com.ncslab.code.c.windows.pc;

import org.json.JSONObject;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.dto.ModelJson;

public class CodeModelCWindowsPC extends CodeModelC {
	private CodeStructCWindowsPC codeStruct=new CodeStructCWindowsPC(this);

	CodeModelCWindowsPC(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	// DTO-native constructor
	CodeModelCWindowsPC(ModelJson modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}

	@Override
	protected CodeStructCWindowsPC getCodeStructC() {
		return codeStruct;
	}

	public static CodeModelCWindowsPC createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCWindowsPC model=new CodeModelCWindowsPC(jsonIn,mode);

		return model;
	}
	
	// DTO-native factory method
	public static CodeModelCWindowsPC createFromDto(ModelJson modelDto, ModelMode mode) throws ModelException {
		CodeModelCWindowsPC model = new CodeModelCWindowsPC(modelDto, mode);
		
		return model;
	}

}
