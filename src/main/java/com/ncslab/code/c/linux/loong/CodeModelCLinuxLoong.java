package com.ncslab.code.c.linux.loong;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;

public class CodeModelCLinuxLoong extends CodeModelC{

	private CodeStructCLinuxLoong codeLoong=new CodeStructCLinuxLoong(this);

	CodeModelCLinuxLoong(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	// DTO-native constructor
	CodeModelCLinuxLoong(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}

	protected CodeStructC getCodeStructC() {
		return codeLoong;
	}

	public static CodeModelCLinuxLoong createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxLoong model=new CodeModelCLinuxLoong(jsonIn,mode);

		return model;
	}
	
	// DTO-native factory method
	public static CodeModelCLinuxLoong createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
		CodeModelCLinuxLoong model = new CodeModelCLinuxLoong(modelDto, mode);
		
		return model;
	}
}
