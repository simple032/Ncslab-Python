package com.ncslab.code.c.linux.pc;

import java.io.File;
import java.io.BufferedReader;
import java.io.*;

import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.c.CodeModelC;

public class CodeStructCLinuxPC extends CodeStructC{
	public CodeStructCLinuxPC(CodeModelC model) {
		super(model);
	}

	public void writeCCodeFiles() {

		//锟斤拷锟斤拷目锟斤拷锟侥硷拷锟叫碉拷位锟斤拷codePathBase/锟矫伙拷id/modelId
		String userPath=codePathBase+model.getUserId();

		File file=new File(userPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		String modelPath=userPath+"/"+model.getModelId();
		file=new File(modelPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		codePath=modelPath+"/";

		//写锟斤拷锟杰边碉拷锟斤拷源锟侥硷拷
		//makefile
		writeNCSLabFile("makefile");
		//锟斤拷锟斤拷锟捷结构
		writeNCSLabFile("../../ncslabccode.hpp","ncslabccode.hpp");
		//main锟斤拷锟斤拷锟皆硷拷锟斤拷时锟斤拷
		writeNCSLabFile("../../ncslabmain.cpp","ncslabmain.cpp");
		//锟斤拷锟斤拷锟斤拷锟斤拷锟捷结构锟侥接匡拷API锟斤拷锟斤拷
		writeNCSLabFile("../../DataApi.cpp","DataApi.cpp");
		writeNCSLabFile("../../DataApi.hpp","DataApi.hpp");

		writeNCSLabFile("../../util.cpp","util.cpp");
        writeNCSLabFile("../../util.hpp", "util.hpp");

		writeNCSLabFile("../../ncslabdefines.hpp","ncslabdefines.hpp");

		//实锟斤拷Netcon协锟斤拷锟酵拷锟斤拷募锟�
		writeNCSLabFile("../ServerThread.cpp","ServerThread.cpp");
		writeNCSLabFile("../ServerThread.hpp","ServerThread.hpp");
		writeNCSLabFile("../ClientThread.cpp","ClientThread.cpp");
		writeNCSLabFile("../ClientThread.hpp","ClientThread.hpp");
		writeNCSLabFile("../UploadThread.cpp","UploadThread.cpp");
		writeNCSLabFile("../UploadThread.hpp","UploadThread.hpp");

		writeNCSLabFile("../../Matrix.cpp","Matrix.cpp");
        writeNCSLabFile("../../Matrix.hpp", "Matrix.hpp");

//		writeNCSLabFile("../../Debug.h","Debug.h");
//		writeNCSLabFile("../../DEV_Config.c","DEV_Config.c");
//		writeNCSLabFile("../../DEV_Config.h","DEV_Config.h");
//		writeNCSLabFile("../../ADS1256.c","ADS1256.c");
//		writeNCSLabFile("../../ADS1256.h","ADS1256.h");
//		writeNCSLabFile("../../DAC8532.c","DAC8532.c");
//		writeNCSLabFile("../../DAC8532.h","DAC8532.h");
//
//		writeNCSLabFile("../../ncs_serialport_pi.c","ncs_serialport_pi.c");
//		writeNCSLabFile("../../ncs_serialport.h","ncs_serialport.h");

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
		case ode6:
			writeNCSLabFile("../../ode6.cpp","onestep.cpp");
			break;
		default:
			break;
		}

		writeNCSLabFile("../../onestep.hpp","onestep.hpp");

		writeNCSWrittenFiles(); // write the files in <code>CodeStructC.writtenFileSet</code>.
	}

	public byte[] readExeFile() {
		return readFile("ncslab");
	}

	public boolean makeExeFile() {
		try {
			//锟斤拷锟斤拷make锟斤拷锟斤拷锟缴匡拷执锟叫达拷锟斤拷
			Process process=Runtime.getRuntime().exec(maketool, null, new File(codePath));
			//锟斤拷取OutputStream锟斤拷errStream锟斤拷锟斤拷锟斤拷锟饺★拷锟斤拷锟绞憋拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
			BufferedReader in=new BufferedReader(new InputStreamReader(process.getErrorStream()));
			BufferedReader inOut=new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line=null,outLine=null;
			StringBuilder errStr=new StringBuilder();
			StringBuilder outStr=new StringBuilder();

			while((outLine=inOut.readLine())!=null||(line=in.readLine())!=null) {
				if(outLine!=null) {
					outStr.append(outLine);
					System.out.println(outLine);
				}
				if(line!=null) {
					errStr.append(line);
					System.err.println(line);
				}
			}

			//锟饺达拷makefile锟斤拷锟斤拷锟�
			process.waitFor();

			if(process.exitValue()==0) {
				return true;
			}

		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	protected void writeNCSWrittenFiles(){
		super.writeNCSWrittenFiles();
	}
}
