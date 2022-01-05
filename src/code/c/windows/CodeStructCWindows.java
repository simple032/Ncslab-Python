package code.c.windows;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import code.c.CodeModelC;
import code.c.CodeStructC;

public class CodeStructCWindows extends CodeStructC {
	public CodeStructCWindows(CodeModelC model) {
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
		writeNCSLabFile("ncslabccode.h");
		//main锟斤拷锟斤拷锟皆硷拷锟斤拷时锟斤拷
		writeNCSLabFile("ncslabmain.c");
		//锟斤拷锟斤拷锟斤拷锟斤拷锟捷结构锟侥接匡拷API锟斤拷锟斤拷
		writeNCSLabFile("DataApi.c");
		writeNCSLabFile("DataApi.h");
		
		writeNCSLabFile("util.c");

		//实锟斤拷Netcon协锟斤拷锟酵拷锟斤拷募锟�
		writeNCSLabFile("ServerThread.c");
		writeNCSLabFile("ServerThread.h");
		writeNCSLabFile("ClientThread.c");
		writeNCSLabFile("ClientThread.h");
		writeNCSLabFile("UploadThread.c");
		writeNCSLabFile("UploadThread.h");

		//写锟斤拷锟斤拷锟缴碉拷锟斤拷锟斤拷锟斤拷ncslabccdoe.c
		writeMainCodeFile();
		
		wirteDefineFile();
		
		switch(model.getSolver()) {
		case ode1:
			writeNCSLabFile("../ode1.c","onestep.c");
			break;
		case ode2:
			writeNCSLabFile("../ode2.c","onestep.c");
			break;
		case ode3:
			writeNCSLabFile("../ode3.c","onestep.c");
			break;
		case ode4:
			writeNCSLabFile("../ode4.c","onestep.c");
			break;
		}
		
	}
	
	public byte[] readExeFile() {
		return readFile("ncslab.exe");
	}
	
	public boolean makeExeFile() {
		try {
			//锟斤拷锟斤拷make锟斤拷锟斤拷锟缴匡拷执锟叫达拷锟斤拷
			Process process=Runtime.getRuntime().exec("mingw32-make", null, new File(codePath));
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
}
