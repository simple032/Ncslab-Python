package code.c.linux.raspberry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Vector;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.*;

import code.CodeModel;
import block.Block;
import block.io.OutputPort;
import block.io.OutputSignal;
import block.io.Parameter;
import block.io.State;
import block.io.InputPort;

import code.c.CodeStructC;
import code.c.CodeModelC;

public class CodeStructCLinuxRaspberry extends CodeStructC{
	public CodeStructCLinuxRaspberry(CodeModelC model) {
		super(model);
	}
	
	public void writeCCodeFiles() {

		//锟斤拷锟斤拷目锟斤拷锟侥硷拷锟叫碉拷位锟斤拷codePathBase/锟矫伙拷id/modelId
		String userPath=codePathBase+model.getUserId();

		File file=new File(userPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		String modelPath=userPath+"/"+model.getModelId()+"_RT";
		file=new File(modelPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		codePath=modelPath+"/";

		//写锟斤拷锟杰边碉拷锟斤拷源锟侥硷拷
		//makefile
		//writeNCSLabFile("makefile");
		writeMakefile("makefile");
		//锟斤拷锟斤拷锟捷结构
		writeNCSLabFile("../../ncslabccode.h","ncslabccode.h");
		//main锟斤拷锟斤拷锟皆硷拷锟斤拷时锟斤拷
		writeNCSLabFile("../../ncslabmain.c","ncslabmain.c");
		//锟斤拷锟斤拷锟斤拷锟斤拷锟捷结构锟侥接匡拷API锟斤拷锟斤拷
		writeNCSLabFile("../../DataApi.c","DataApi.c");
		writeNCSLabFile("../../DataApi.h","DataApi.h");
		
		writeNCSLabFile("../../util.c","util.c",true);
		
		writeNCSLabFile("../../ncslabdefines.h","ncslabdefines.h");
		writeNCSLabFile("../../ncslabsfun.h","ncslabsfun.h");
		//实锟斤拷Netcon协锟斤拷锟酵拷锟斤拷募锟�
		writeNCSLabFile("../ServerThread.c","ServerThread.c");
		writeNCSLabFile("../ServerThread.h","ServerThread.h");
		writeNCSLabFile("../ClientThread.c","ClientThread.c");
		writeNCSLabFile("../ClientThread.h","ClientThread.h");
		writeNCSLabFile("../UploadThread.c","UploadThread.c");
		writeNCSLabFile("../UploadThread.h","UploadThread.h");
		
		writeNCSLabFile("hardware.h","hardware.h");
		writeNCSLabFile("hardware.c","hardware.c");
		
		writeNCSLabFile("../../ncs_serialport_pi.c","ncs_serialport_pi.c");
		writeNCSLabFile("../../ncs_serialport.h","ncs_serialport.h");
		
		writeNCSLabFile("../../Matrix.c","Matrix.c");
		writeNCSLabFile("../../Matrix.h","Matrix.h");
		
		writeNCSLabFile("DEV_Config.c","DEV_Config.c");
		writeNCSLabFile("DEV_Config.h","DEV_Config.h");
		
		writeNCSLabFile("DAC8532.c","DAC8532.c");
		writeNCSLabFile("DAC8532.h","DAC8532.h");
		writeNCSLabFile("Debug.h","Debug.h");
		
		writeNCSLabFile("ADS1256.h","ADS1256.h");
		writeNCSLabFile("ADS1256.c","ADS1256.c");
		writeNCSLabFile("bcm2835.h","bcm2835.h");
		writeNCSLabFile("bcm2835.c","bcm2835.c");
		writeNCSLabFile("WiringPi.h","WiringPi.h");
		writeNCSLabFile("WiringPiSPI.h","WiringPiSPI.h");
		//写入生成的主代码ncslabccdoe.c
				for(Block block: model.getBlockList()) {
					if(block.isSFcnBlock()) {
						block.generateSourceFile();
					}
				}

		//写锟斤拷锟斤拷锟缴碉拷锟斤拷锟斤拷锟斤拷ncslabccdoe.c
		writeMainCodeFile();
		
		wirteDefineFile();
		
		switch(model.getSolver()) {
		case ode1:
			writeNCSLabFile("../../ode1.c","onestep.c",true);
			break;
		case ode2:
			writeNCSLabFile("../../ode2.c","onestep.c",true);
			break;
		case ode3:
			writeNCSLabFile("../../ode3.c","onestep.c",true);
			break;
		case ode4:
			writeNCSLabFile("../../ode4.c","onestep.c",true);
			break;
		}
		
	}
	
	public byte[] readExeFile() {
		return readFile("ncslab");
	}
	
	public boolean makeExeFile() {
		try {
			//锟斤拷锟斤拷make锟斤拷锟斤拷锟缴匡拷执锟叫达拷锟斤拷
			Process process=Runtime.getRuntime().exec("make", null, new File(codePath));
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
