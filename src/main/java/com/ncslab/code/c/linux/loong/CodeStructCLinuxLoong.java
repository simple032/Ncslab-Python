package com.ncslab.code.c.linux.loong;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.utils.Property;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CodeStructCLinuxLoong extends CodeStructC{
	public CodeStructCLinuxLoong(CodeModelC model) {
		super(model);
		codePathBase= Property.instance.getProperty("CCodePathLoong");
	}

	public void writeCCodeFiles() {

		//锟斤拷锟斤拷目锟斤拷锟侥硷拷锟叫碉拷位锟斤拷codePathBase/锟矫伙拷id/modelId
		String userPath=codePathBase+model.getUserId();

		File file=new File(userPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		String modelPath=userPath+"/"+model.getModelName();
		file=new File(modelPath);
		if(file.exists()==false) {
			file.mkdir();
		}

		codePath=modelPath+"/";

        writeNCSLabFile("makefile");

        writeNCSLabFile("Matrix.hpp");
        writeNCSLabFile("util.hpp");
        writeNCSLabFile("mainccode.hpp");
        writeNCSLabFile("onestep.hpp");
        writeNCSLabFile("ncslab.hpp");
        writeNCSLabFile("ncslabccode.hpp");
        writeNCSLabFile("ncslabmain.cpp");
        writeNCSLabFile("DataApi.cpp");
        writeNCSLabFile("DataApi.hpp");
        writeNCSLabFile("util.cpp");
        writeNCSLabFile("ncslabdefines.hpp");

        writeNCSLabFile("ServerThread.cpp");
        writeNCSLabFile("ServerThread.hpp");
        writeNCSLabFile("ClientThread.cpp");
        writeNCSLabFile("ClientThread.hpp");
        writeNCSLabFile("UploadThread.cpp");
        writeNCSLabFile("UploadThread.hpp");

		// writeNCSLabFile("../../Debug.h","Debug.h");
		// writeNCSLabFile("../../DEV_Config.c","DEV_Config.c");
		// writeNCSLabFile("../../DEV_Config.h","DEV_Config.h");
		// writeNCSLabFile("../../ADS1256.c","ADS1256.c");
		// writeNCSLabFile("../../ADS1256.h","ADS1256.h");
		// writeNCSLabFile("../../DAC8532.c","DAC8532.c");
		// writeNCSLabFile("../../DAC8532.h","DAC8532.h");

		// writeNCSLabFile("../../ncs_serialport_pi.c","ncs_serialport_pi.c");
		// writeNCSLabFile("../../ncs_serialport.h","ncs_serialport.h");

        writeNCSLabFile("EtherCAT.hpp");
        writeNCSLabFile("EtherCAT.cpp");
        writeNCSLabFile("EtherCATAnalog.hpp");
        writeNCSLabFile("EtherCATDigital.hpp");
        writeNCSLabFile("EtherCATSh.hpp");
        writeNCSLabFile("EtherCATServo.hpp");


        writeNCSLabFile("SOEM/soem/ethercatbase.c");
        writeNCSLabFile("SOEM/soem/ethercatbase.h");
        writeNCSLabFile("SOEM/soem/ethercatcoe.c");
        writeNCSLabFile("SOEM/soem/ethercatcoe.h");
        writeNCSLabFile("SOEM/soem/ethercatconfig.c");
        writeNCSLabFile("SOEM/soem/ethercatconfig.h");
        writeNCSLabFile("SOEM/soem/ethercatconfiglist.h");
        writeNCSLabFile("SOEM/soem/ethercatdc.c");
        writeNCSLabFile("SOEM/soem/ethercatdc.h");
        writeNCSLabFile("SOEM/soem/ethercateoe.c");
        writeNCSLabFile("SOEM/soem/ethercateoe.h");
        writeNCSLabFile("SOEM/soem/ethercatfoe.c");
        writeNCSLabFile("SOEM/soem/ethercatfoe.h");
        writeNCSLabFile("SOEM/soem/ethercat.h");
        writeNCSLabFile("SOEM/soem/ethercatmain.c");
        writeNCSLabFile("SOEM/soem/ethercatmain.h");
        writeNCSLabFile("SOEM/soem/ethercatprint.c");
        writeNCSLabFile("SOEM/soem/ethercatprint.h");
        writeNCSLabFile("SOEM/soem/ethercatsoe.c");
        writeNCSLabFile("SOEM/soem/ethercatsoe.h");
        writeNCSLabFile("SOEM/soem/ethercattype.h");
        writeNCSLabFile("SOEM/soem/osal.h");
        writeNCSLabFile("SOEM/soem/osal.c");
        writeNCSLabFile("SOEM/soem/osal_defs.h");
        writeNCSLabFile("SOEM/soem/oshw.h");
        writeNCSLabFile("SOEM/soem/nicdrv.c");
        writeNCSLabFile("SOEM/soem/nicdrv.h");
        writeNCSLabFile("SOEM/soem/oshw.c");

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
			writeNCSLabFile("../../ode4.cpp","onestep.cpp",true);
			break;
		}

		try {
			//Runtime.getRuntime().exec("python /home/pi/.config/antostart/GetPiId.py");
			String command = "sudo chmod -R 777 " + codePathBase;
			Runtime.getRuntime().exec("sudo chmod -R 777 " + codePathBase);
//			Runtime.getRuntime().exec("sudo chmod -R 777 /home/pi/NetConTop/NCSLabLink/CCode");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public byte[] readExeFile() {
		return readFile("ncslab");
	}

	public boolean makeExeFile() {
		try {
			//锟斤拷锟斤拷make锟斤拷锟斤拷锟缴匡拷执锟叫达拷锟斤拷
			Process process=Runtime.getRuntime().exec("make -j8", null, new File(codePath));
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
