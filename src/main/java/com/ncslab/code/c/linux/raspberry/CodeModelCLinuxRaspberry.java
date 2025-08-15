package com.ncslab.code.c.linux.raspberry;

import org.json.JSONObject;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.ModelJson;

public class CodeModelCLinuxRaspberry extends CodeModelC {
	
	private CodeStructCLinuxRaspberry codeRaspberry = new CodeStructCLinuxRaspberry(this);
	
	CodeModelCLinuxRaspberry(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn, mode);
	}
	
	// DTO-native constructor
	CodeModelCLinuxRaspberry(ModelJson modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}
	
	public static CodeModelCLinuxRaspberry createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
		CodeModelCLinuxRaspberry model = new CodeModelCLinuxRaspberry(jsonIn, mode);
		
		return model;
	}
	
	// DTO-native factory method
	public static CodeModelCLinuxRaspberry createFromDto(ModelJson modelDto, ModelMode mode) throws ModelException {
		CodeModelCLinuxRaspberry model = new CodeModelCLinuxRaspberry(modelDto, mode);
		
		return model;
	}
}
