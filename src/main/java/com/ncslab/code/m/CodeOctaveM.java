package com.ncslab.code.m;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.json.JSONObject;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.dto.communication.MfcalcResponseDto;
import com.ncslab.dto.ui.AppMessage;
import com.ncslab.dto.ui.FiguresData;
import com.ncslab.dto.ui.UIComponentMessage;

public class CodeOctaveM {
	/*
	 * M2PCode界面中无论是Command命令行还是file文件
	 * 处理方式均为：在octave中新建一个m文件
	 * 将前端的代码写入
	 * 运行后获取结果并保存
	 * 保存新建的m文件
	 */

	// 用户ID
	// 目前仅用于获取用户的OctaveClient
	@Getter
	@Setter
	int userId = -1; // 用户ID


	//获取的代码
	@Getter
	@Setter
    private String mainCode="";
	//OuputResult输出的命令行结果

    @Setter
	@Getter
    private String outputResult="";

    @Getter
    @Setter
    private FiguresData figureResult;
	@Getter
	@Setter
	private AppMessage app;
	@Getter
	@Setter
	private List<UIComponentMessage> uiComponents;
	//OutputMat输出的工作区
	@Getter
	@Setter
	private String OutputMat="";
	//OutputMat输出的工作区
	public String OutputFigFileUrl="";
	//OutputMat输出的工作区
	public String OutputDataFileUrl="";

	public int OutputFigBeginIndex=0;

	public int OutputFigEndIndex=0;

}
