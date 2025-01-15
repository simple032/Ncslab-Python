package com.ncslab.code.c.windows.pc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.windows.CodeStructCWindows;

public class CodeStructCWindowsPC extends CodeStructCWindows {
	public CodeStructCWindowsPC(CodeModelC model) {
		super(model);
	}

	@Override
	public void writeCCodeFiles() {
        super.writeCCodeFiles();
        System.out.println("Write CCode Files in CodeStructCWindowsPC");
		//锟斤拷锟斤拷目锟斤拷锟侥硷拷锟叫碉拷位锟斤拷codePathBase/锟矫伙拷id/modelId
//		String userPath=codePathBase+model.getUserId();
//
//		File file=new File(userPath);
//		if(!file.exists()) {
//			file.mkdir();
//		}
//
//		String modelPath=userPath+"/"+model.getModelId();
//		file=new File(modelPath);
//        if(!file.exists()) {
//			file.mkdir();
//		}
//
//		codePath=modelPath+"/";

		//写锟斤拷锟杰边碉拷锟斤拷源锟侥硷拷
		//makefile
		writeNCSLabFile("makefile");
		//锟斤拷锟斤拷锟捷结构
        //main函数以及定时器
        writeNCSLabFile("ncslabmain.cpp");

	}

}
