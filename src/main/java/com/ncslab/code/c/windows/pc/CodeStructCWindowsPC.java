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

		//锟斤拷锟斤拷目锟斤拷锟侥硷拷锟叫碉拷位锟斤拷codePathBase/锟矫伙拷id/modelId
		String userPath=codePathBase+model.getUserId();

		File file=new File(userPath);
		if(!file.exists()) {
			file.mkdir();
		}

		String modelPath=userPath+"/"+model.getModelId();
		file=new File(modelPath);
        if(!file.exists()) {
			file.mkdir();
		}


		codePath=modelPath+"/";

		//写锟斤拷锟杰边碉拷锟斤拷源锟侥硷拷
		//makefile
		writeNCSLabFile("makefile");
		//锟斤拷锟斤拷锟捷结构
		writeNCSLabFile("../ncslabccode.hpp");
        //main锟斤拷锟斤拷锟皆硷拷锟斤拷时锟斤拷
		writeNCSLabFile("../ncslabmain.cpp");
		//锟斤拷锟斤拷锟斤拷锟斤拷锟捷结构锟侥接匡拷API锟斤拷锟斤拷
		writeNCSLabFile("../DataApi.cpp");
		writeNCSLabFile("../DataApi.hpp");

		writeNCSLabFile("../../util.cpp","util.cpp");
        writeNCSLabFile("../../util.hpp", "util.hpp");

        writeNCSLabFile("../../Matrix.cpp","Matrix.cpp");
        writeNCSLabFile("../../Matrix.hpp", "Matrix.hpp");

        //实锟斤拷Netcon协锟斤拷锟酵拷锟斤拷募锟�
		writeNCSLabFile("../ServerThread.cpp");
		writeNCSLabFile("../ServerThread.hpp");
		writeNCSLabFile("../ClientThread.cpp");
		writeNCSLabFile("../ClientThread.hpp");
		writeNCSLabFile("../UploadThread.cpp");
		writeNCSLabFile("../UploadThread.hpp");

        writeNCSLabFile("../ncslabdefines.hpp");
		//写锟斤拷锟斤拷锟缴碉拷锟斤拷锟斤拷锟斤拷ncslabccdoe.c
		writeMainCodeFile();
        writeNCSLabFile("../../mainccode.hpp", "mainccode.hpp");

		wirteDefineFile();

		switch(model.getSolver()) {
		case ode1:
			writeNCSLabFile("../../ode1.cpp","onestep.cpp");
			break;
		case ode2:
			writeNCSLabFile("../../ode2.cpp","onestep.cpp");
			break;
		case ode3:
			writeNCSLabFile("../../ode3.cpp","onestep.cpp");
			break;
		case ode4:
			writeNCSLabFile("../../ode4.cpp","onestep.cpp");
			break;
        case ode5:
            writeNCSLabFile("../../ode5.cpp","onestep.cpp");
            break;
        default:
            System.err.println("Unsupported solver: "+model.getSolver());
            break;
		}
        writeNCSLabFile("../../onestep.hpp","onestep.hpp");

	}

}
