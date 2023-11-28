package com.ncslab.code.c.linux.raspberry;

import java.io.*;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.c.CodeModelC;

public class CodeStructCLinuxRaspberry extends CodeStructC {
	public CodeStructCLinuxRaspberry(CodeModelC model) {
		super(model);
	}

	public void writeCCodeFiles() {

		// 锟斤拷锟斤拷目锟斤拷锟侥硷拷锟叫碉拷位锟斤拷codePathBase/锟矫伙拷id/modelId
		String userPath = codePathBase + model.getUserId();

		File file = new File(userPath);
		if (file.exists() == false) {
			file.mkdir();
		}

		String modelPath = userPath + "/" + model.getModelId() + "_RT";
		file = new File(modelPath);
		if (file.exists() == false) {
			file.mkdir();
		}

		codePath = modelPath + "/";

		// write resource files
		// makefile
		// writeNCSLabFile("makefile");
		writeMakefile("makefile");
		// main data structure
		writeNCSLabFile("../../ncslabccode.h", "ncslabccode.h");
		// main function and timer
		writeNCSLabFile("../../ncslabmain.c", "ncslabmain.c");
		// Define the API to access the main data structure
		writeNCSLabFile("../../DataApi.c", "DataApi.c");
		writeNCSLabFile("../../DataApi.h", "DataApi.h");

		writeNCSLabFile("../../util.c", "util.c", true);

		writeNCSLabFile("../../ncslabdefines.h", "ncslabdefines.h");
		writeNCSLabFile("../../ncslabsfun.h", "ncslabsfun.h");
		// Implement the general file of the Netcon protocol
		writeNCSLabFile("../ServerThread.c", "ServerThread.c");
		writeNCSLabFile("../ServerThread.h", "ServerThread.h");
		writeNCSLabFile("../ClientThread.c", "ClientThread.c");
		writeNCSLabFile("../ClientThread.h", "ClientThread.h");
		writeNCSLabFile("../UploadThread.c", "UploadThread.c");
		writeNCSLabFile("../UploadThread.h", "UploadThread.h");

		writeNCSLabFile("hardware.h", "hardware.h");
		writeNCSLabFile("hardware.c", "hardware.c");

		writeNCSLabFile("../../ncs_serialport_pi.c", "ncs_serialport_pi.c");
		writeNCSLabFile("../../ncs_serialport.h", "ncs_serialport.h");

		writeNCSLabFile("../../Matrix.c", "Matrix.c");
		writeNCSLabFile("../../Matrix.h", "Matrix.h");

		writeNCSLabFile("DEV_Config.c", "DEV_Config.c");
		writeNCSLabFile("DEV_Config.h", "DEV_Config.h");

		writeNCSLabFile("DAC8532.c", "DAC8532.c");
		writeNCSLabFile("DAC8532.h", "DAC8532.h");
		writeNCSLabFile("Debug.h", "Debug.h");

		writeNCSLabFile("ADS1256.h", "ADS1256.h");
		writeNCSLabFile("ADS1256.c", "ADS1256.c");
		writeNCSLabFile("bcm2835.h", "bcm2835.h");
		writeNCSLabFile("bcm2835.c", "bcm2835.c");
		writeNCSLabFile("wiringPi.h", "wiringPi.h");
		writeNCSLabFile("wiringPiSPI.h", "wiringPiSPI.h");

		for (Block block : model.getBlockList()) {
			if (block.isSFcnBlock()) {
				block.generateSourceFile();
			}
		}

		// write the main code file ncslabccdoe.c
		writeMainCodeFile();

		wirteDefineFile();

		switch (model.getSolver()) {
			case ode1:
				writeNCSLabFile("../../ode1.c", "onestep.c", true);
				break;
			case ode2:
				writeNCSLabFile("../../ode2.c", "onestep.c", true);
				break;
			case ode3:
				writeNCSLabFile("../../ode3.c", "onestep.c", true);
				break;
			case ode4:
				writeNCSLabFile("../../ode4.c", "onestep.c", true);
				break;
			default:
				break;
		}

	}

	public byte[] readExeFile() {
		return readFile("ncslab");
	}

	public boolean makeExeFile() {
		try {
			// start make, generate executable file
			Process process = Runtime.getRuntime().exec("make", null, new File(codePath));
			// get OutputStream and errStream of the process, in case of blocking
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			BufferedReader inOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null, outLine = null;
			StringBuilder errStr = new StringBuilder();
			StringBuilder outStr = new StringBuilder();

			while ((outLine = inOut.readLine()) != null || (line = in.readLine()) != null) {
				if (outLine != null) {
					outStr.append(outLine);
					System.out.println(outLine);
				}
				if (line != null) {
					errStr.append(line);
					System.err.println(line);
				}
			}

			// wait for the make process to terminate
			process.waitFor();

			if (process.exitValue() == 0) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
