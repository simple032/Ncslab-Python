package com.ncslab.code.c.linux.pc;

import org.json.JSONObject;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.core.ModelDto;

// TODO: currently this class is not used
public class CodeModelCLinuxPC extends CodeModelC{
	
	private CodeStructCLinuxPC codeRaspberry=new CodeStructCLinuxPC(this);
	
	CodeModelCLinuxPC(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}

	// DTO-native constructor
	CodeModelCLinuxPC(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}

	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}

	public static CodeModelCLinuxPC createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxPC model=new CodeModelCLinuxPC(jsonIn,mode);

		return model;
	}

	// DTO-native factory method
	public static CodeModelCLinuxPC createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
		CodeModelCLinuxPC model = new CodeModelCLinuxPC(modelDto, mode);

		return model;
	}
}
