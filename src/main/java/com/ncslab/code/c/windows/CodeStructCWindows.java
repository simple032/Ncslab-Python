package com.ncslab.code.c.windows;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.utils.Property;

public class CodeStructCWindows extends CodeStructC {
	public CodeStructCWindows(CodeModelC model) {
		super(model);
	}

    private String codePathBase = Property.instance.getProperty("CCodePathWin")
        .replace("${M2PLAB_ROOT}",System.getenv("M2PLAB_ROOT"));

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
		writeNCSLabFile("ncslabccode.hpp");
		//main锟斤拷锟斤拷锟皆硷拷锟斤拷时锟斤拷
		writeNCSLabFile("ncslabmain.cpp");
		//锟斤拷锟斤拷锟斤拷锟斤拷锟捷结构锟侥接匡拷API锟斤拷锟斤拷
		writeNCSLabFile("DataApi.cpp");
		writeNCSLabFile("DataApi.hpp");

		writeNCSLabFile("../util.cpp","util.cpp");
        writeNCSLabFile("../util.hpp", "util.hpp");

        writeNCSLabFile("../Matrix.cpp","Matrix.cpp");
        writeNCSLabFile("../Matrix.hpp", "Matrix.hpp");

        //实锟斤拷Netcon协锟斤拷锟酵拷锟斤拷募锟�
		writeNCSLabFile("ServerThread.cpp");
		writeNCSLabFile("ServerThread.hpp");
		writeNCSLabFile("ClientThread.cpp");
		writeNCSLabFile("ClientThread.hpp");
		writeNCSLabFile("UploadThread.cpp");
		writeNCSLabFile("UploadThread.hpp");

        writeNCSLabFile("ncslabdefines.hpp");
		//写锟斤拷锟斤拷锟缴碉拷锟斤拷锟斤拷锟斤拷ncslabccdoe.c
		writeMainCodeFile();
        writeNCSLabFile("../mainccode.hpp", "mainccode.hpp");

		wirteDefineFile();

		switch(model.getSolver()) {
		case ode1:
			writeNCSLabFile("../ode1.cpp","onestep.cpp");
			break;
		case ode2:
			writeNCSLabFile("../ode2.cpp","onestep.cpp");
			break;
		case ode3:
			writeNCSLabFile("../ode3.cpp","onestep.cpp");
			break;
		case ode4:
			writeNCSLabFile("../ode4.cpp","onestep.cpp");
			break;
        case ode5:
            writeNCSLabFile("../ode5.cpp","onestep.cpp");
            break;
        default:
            System.err.println("Unsupported solver: "+model.getSolver());
            break;
		}
        writeNCSLabFile("../onestep.hpp","onestep.hpp");

	}

	public byte[] readExeFile() {
		return readFile("ncslab.exe");
	}

	public boolean makeExeFile() {
		try {
			//锟斤拷锟斤拷make锟斤拷锟斤拷锟缴匡拷执锟叫达拷锟斤拷
//            String cmakeCommand[] = {"cmake","-G", "\"MinGW Makefiles\"", "."};
//            Process makeProcess=Runtime.getRuntime().exec(cmakeCommand, null, new File(codePath));
//            makeProcess.waitFor();
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

    public void removeAllFiles(){
        String userPath=codePathBase+model.getUserId();

        String modelPath=userPath+"/"+model.getModelId();

        codePath=modelPath+"/";

        File dir = new File(codePath);
        if(dir.exists() && dir.isDirectory()){
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 递归删除子文件夹
                    if (file.isFile()) {
                        // 删除文件
                        file.delete();
                    }
                }
            }
        }
        dir.delete();
    }
}
