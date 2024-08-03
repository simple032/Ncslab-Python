package code.m;

import org.json.JSONObject;

import ncslablink.ModelException;
import ncslablink.ModelMode;

public class CodeOctaveM {
	/*
	 * M2PCode界面中无论是Command命令行还是file文件
	 * 处理方式均为：在octave中新建一个m文件
	 * 将前端的代码写入
	 * 运行后获取结果并保存
	 * 保存新建的m文件
	 */
	
	
	
	//获取的代码
	public String mainCode="";
	//OuputResult输出的命令行结果
	public String OutputResult="";
	//OutputMat输出的工作区
	public String OutputMat="";
	//OutputMat输出的工作区
	public String OutputFigFileUrl="";
	//OutputMat输出的工作区
	public String OutputDataFileUrl="";
	
	public int OutputFigBeginIndex=0;
	
	public int OutputFigEndIndex=0;
	
	public String setMainCode(String Code) {
		return mainCode = Code;
	}
	
	public String getMainCode() {
		return mainCode;
	}
	
	public String getOutputResult() {
		return OutputResult;
	}
	
	public String getOutputMat() {
		return OutputMat;
	}
	
}
