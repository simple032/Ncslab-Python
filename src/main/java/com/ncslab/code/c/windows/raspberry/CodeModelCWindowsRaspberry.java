package com.ncslab.code.c.windows.raspberry;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import com.ncslab.dto.core.ModelDto;

public class CodeModelCWindowsRaspberry extends CodeModelC {

	private CodeStructCWindowsRaspberry codeStruct = new CodeStructCWindowsRaspberry(this);

	CodeModelCWindowsRaspberry(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn, mode);
	}

	// DTO-native constructor
	CodeModelCWindowsRaspberry(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}

	protected CodeStructC getCodeStructC() {
		return codeStruct;
	}

	public static CodeModelCWindowsRaspberry createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
		CodeModelCWindowsRaspberry model = new CodeModelCWindowsRaspberry(jsonIn, mode);

		return model;
	}

	// DTO-native factory method
	public static CodeModelCWindowsRaspberry createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
		CodeModelCWindowsRaspberry model = new CodeModelCWindowsRaspberry(modelDto, mode);

		return model;
	}
}
