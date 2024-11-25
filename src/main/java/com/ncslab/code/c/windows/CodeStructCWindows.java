package com.ncslab.code.c.windows;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.utils.Property;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CodeStructCWindows extends CodeStructC {
	protected CodeStructCWindows(CodeModelC model) {
		super(model);
	}

    protected String codePathBase = Property.instance.getProperty("CCodePathWin")
        .replace("${M2PLAB_ROOT}",System.getenv("M2PLAB_ROOT"));


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
}
