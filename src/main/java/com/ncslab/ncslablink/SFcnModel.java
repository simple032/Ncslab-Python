package com.ncslab.ncslablink;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import com.ncslab.utils.Property;

public class SFcnModel {

	@Setter
    @Getter
    private int userId;
	@Setter
    @Getter
    private int modelId;

	@Setter
    @Getter
    private String blockName;
	@Setter
    @Getter
    private String functionName;

	@Setter
    @Getter
    protected String functionCode;

	@Setter
    @Getter
    private int parameterNum;
	private int inputPortNum;
	private int outputPortNum;

	protected String codePath;

    protected String codePathBase = Property.instance.getProperty("CCodePath");

	public SFcnModel(JSONObject jsonData) throws SFcnException {

		String sfcnDataString= jsonData.getString("sfcnData");
		JSONObject sfcnData = new JSONObject(sfcnDataString);
		this.userId = sfcnData.getInt("userId");
		this.modelId = sfcnData.getInt("modelId");
		this.blockName = sfcnData.getString("blockName");

		JSONObject paramList = sfcnData.getJSONObject("paramList");
		this.functionName = paramList.getString("FunctionName");
		String parameterString = paramList.getString("Parameters");
		if (parameterString.length()<1) {
			this.parameterNum = 0;
		}
		else {
			this.parameterNum = parameterString.split(",").length;
		}
		this.functionCode = paramList.getString("SFunctionCode");

		//检测函数名是否匹配
		int firstIndex = this.functionCode.indexOf("#define S_FUNCTION_NAME");
		int endIndex = this.functionCode.indexOf("\n", firstIndex);
		String functionNameInSource = this.functionCode.substring(firstIndex, endIndex).replace("#define S_FUNCTION_NAME", "").replaceAll(" ",	"");
		if(! functionNameInSource.equals(this.functionName)) {
			throw new SFcnException("Error: S-function name is mismatch. Name in source is \"#include S_FUNCTION_NAME "+functionNameInSource
					+"\", but name of the S-function on the dialog is \""+this.functionName+"\". Update the name in source");
		}

		//修改头文件为ncslab的头文件
		this.functionCode = this.functionCode.replaceFirst("\"simstruc.h\"","\"ncslabccode.h\"\n"
				+ "#include \"ncslabdefines.h\"\n"
				+ "#include \"Matrix.h\"\n"
				+ "#define CONTINUOUS_SAMPLE_TIME 0\n"
				+ "#define USE_DEFAULT_SIM_STATE 0\n");
		this.functionCode=this.functionCode.replaceFirst("#include \"cg_sfun.h\"", "#include \"ncslabsfun.h\"");
	}

	public String checkDimentions() throws SFcnException {

		String msg="";
//		JSONObject jsonData = new JSONObject();
		try {
			//字符串内容依次是parameterNum;inputPortNum|input port1 width,input port2 width ...;outputPortNum | output port1 width...;numContStates|numDiscStates;
			InputStream inputStream= new FileInputStream(codePath+"/sfcncompileresult.txt");

			msg= String.valueOf(inputStream.read());

			inputStream.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		System.out.println(msg);
		return msg;
	}
}
