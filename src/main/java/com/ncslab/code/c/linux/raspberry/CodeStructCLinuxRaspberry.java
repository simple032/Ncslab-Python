package com.ncslab.code.c.linux.raspberry;

import java.io.*;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.linux.CodeStructCLinux;

public class CodeStructCLinuxRaspberry extends CodeStructCLinux {
	public CodeStructCLinuxRaspberry(CodeModelC model) {
		super(model);
	}

	@Override
	public void writeCCodeFiles() {

		// codePath = codePathBase/Userid/modelId_RT
		String userPath = codePathBase + model.getUserId();

		File file = new File(userPath);
		if (!file.exists()) {
			file.mkdir();
		}

		String modelPath = userPath + "/" + model.getModelId();
		file = new File(modelPath);
		if (!file.exists()) {
			file.mkdir();
		}

		codePath = modelPath + "/";

		// write resource files
		// makefile
		// writeNCSLabFile("makefile");
		writeMakefile("makefile");
		// main data structure
		writeNCSLabFile("../../ncslabccode.hpp", "ncslabccode.hpp");
		// main function and timer
		writeNCSLabFile("../ncslabmain.cpp", "ncslabmain.cpp");
		// Define the API to access the main data structure
		writeNCSLabFile("../../DataApi.cpp", "DataApi.cpp");
		writeNCSLabFile("../../DataApi.hpp", "DataApi.hpp");

		writeNCSLabFile("../../util.cpp", "util.cpp", true);
        writeNCSLabFile("../../util.hpp", "util.hpp", true);

		writeNCSLabFile("../../ncslabdefines.hpp", "ncslabdefines.hpp");
		writeNCSLabFile("../../ncslabsfun.hpp", "ncslabsfun.hpp");
		// Implement the general file of the Netcon protocol
		writeNCSLabFile("../ServerThread.cpp", "ServerThread.cpp");
		writeNCSLabFile("../ServerThread.hpp", "ServerThread.hpp");
		writeNCSLabFile("../ClientThread.cpp", "ClientThread.cpp");
		writeNCSLabFile("../ClientThread.hpp", "ClientThread.hpp");
		writeNCSLabFile("../UploadThread.cpp", "UploadThread.cpp");
		writeNCSLabFile("../UploadThread.hpp", "UploadThread.hpp");

		writeNCSLabFile("hardware.h", "hardware.h");
		writeNCSLabFile("hardware.c", "hardware.c");

		writeNCSLabFile("../../ncs_serialport_pi.c", "ncs_serialport_pi.c");
		writeNCSLabFile("../../ncs_serialport.h", "ncs_serialport.h");

		// Matrix.cpp removed - Matrix.hpp is now header-only template
		writeNCSLabFile("../../Matrix.hpp", "Matrix.hpp");
		writeNCSLabFile("../../ricatti.cpp", "ricatti.cpp");
		writeNCSLabFile("../../ricatti.hpp", "ricatti.hpp");
		writeNCSLabFile("../../onestep.hpp", "onestep.hpp");

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
        writeNCSLabFile("../../mainccode.hpp", "mainccode.hpp");

		wirteDefineFile();

		switch (model.getSolver()) {
			case ode1:
				writeNCSLabFile("../../ode1.cpp", "onestep.cpp", true);
				break;
			case ode2:
				writeNCSLabFile("../../ode2.cpp", "onestep.cpp", true);
				break;
			case ode3:
				writeNCSLabFile("../../ode3.cpp", "onestep.cpp", true);
				break;
			case ode4:
				writeNCSLabFile("../../ode4.cpp", "onestep.cpp", true);
				break;
            case ode5:
                writeNCSLabFile("../../ode5.cpp", "onestep.cpp", true);
                break;
            case ode6:
                writeNCSLabFile("../../ode6.cpp", "onestep.cpp", true);
                break;
			default:
				break;
		}

	}



}
