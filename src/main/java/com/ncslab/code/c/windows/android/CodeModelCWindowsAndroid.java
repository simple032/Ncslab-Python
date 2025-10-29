package com.ncslab.code.c.windows.android;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import com.ncslab.dto.core.ModelDto;

public class CodeModelCWindowsAndroid extends CodeModelC {

	private CodeStructCWindowsAndroid codeStruct = new CodeStructCWindowsAndroid(this);

	CodeModelCWindowsAndroid(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn, mode);
	}

	// DTO-native constructor
	CodeModelCWindowsAndroid(ModelDto modelDto, ModelMode mode) throws ModelException{
		super(modelDto, mode);
	}

	protected CodeStructC getCodeStructC() {
		return codeStruct;
	}

	public static CodeModelCWindowsAndroid createFromJSON(JSONObject jsonIn, ModelMode mode) throws ModelException {
		CodeModelCWindowsAndroid model = new CodeModelCWindowsAndroid(jsonIn, mode);

		return model;
	}

	// DTO-native factory method
	public static CodeModelCWindowsAndroid createFromDto(ModelDto modelDto, ModelMode mode) throws ModelException {
		CodeModelCWindowsAndroid model = new CodeModelCWindowsAndroid(modelDto, mode);

		return model;
	}
}
