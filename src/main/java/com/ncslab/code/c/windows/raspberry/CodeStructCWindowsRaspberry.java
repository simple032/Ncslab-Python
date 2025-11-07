package com.ncslab.code.c.windows.raspberry;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.c.windows.CodeStructCWindows;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CodeStructCWindowsRaspberry extends CodeStructCWindows {
	public CodeStructCWindowsRaspberry(CodeModelC model) {
		super(model);
	}

	@Override
	public void writeCCodeFiles() {

		// codePath = codePathBase/Userid/modelId_RT
		String userPath = codePathBase + model.getUserId();

		File file = new File(userPath);
		if (file.exists() == false) {
			file.mkdir();
		}

		String modelPath = userPath + "/" + model.getModelId() ;//+ "_RT";
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
		writeNCSLabFile("include/ncslabccode.hpp", "ncslabccode.hpp");
		// main function and timer
		writeNCSLabFile("src/ncslabmain.cpp", "ncslabmain.cpp");
		// Define the API to access the main data structure
//		writeNCSLabFile("lib/DataApi.o", "DataApi.o");
        writeNCSLabFile("src/DataApi.cpp", "DataApi.cpp");
		writeNCSLabFile("include/DataApi.hpp", "DataApi.hpp");

//		writeNCSLabFile("lib/util.o", "util.o", true);
        writeNCSLabFile("src/util.cpp", "util.cpp");
        writeNCSLabFile("include/util.hpp", "util.hpp");

		writeNCSLabFile("include/ncslabdefines.hpp", "ncslabdefines.hpp");
		writeNCSLabFile("include/ncslabsfun.hpp", "ncslabsfun.hpp");

        // Implement the general file of the Netcon protocol
//		writeNCSLabFile("lib/ServerThread.o", "ServerThread.o");
        writeNCSLabFile("src/ServerThread.cpp", "ServerThread.cpp");
		writeNCSLabFile("include/ServerThread.hpp", "ServerThread.hpp");
//		writeNCSLabFile("lib/ClientThread.o", "ClientThread.o");
        writeNCSLabFile("src/ClientThread.cpp", "ClientThread.cpp");
		writeNCSLabFile("include/ClientThread.hpp", "ClientThread.hpp");
//		writeNCSLabFile("lib/UploadThread.o", "UploadThread.o");
        writeNCSLabFile("src/UploadThread.cpp", "UploadThread.cpp");
		writeNCSLabFile("include/UploadThread.hpp", "UploadThread.hpp");

        writeNCSLabFile("src/hardware.c", "hardware.c");
		writeNCSLabFile("include/hardware.h", "hardware.h");
//		writeNCSLabFile("lib/hardware.o", "hardware.o");

		writeNCSLabFile("lib/ncs_serialport_pi.o", "ncs_serialport_pi.o");
		writeNCSLabFile("include/ncs_serialport.h", "ncs_serialport.h");

//		writeNCSLabFile("lib/Matrix.o", "Matrix.o");
        // Matrix.cpp removed - Matrix.hpp is now header-only template
		writeNCSLabFile("include/Matrix.hpp", "Matrix.hpp");
//		writeNCSLabFile("lib/ricatti.o", "ricatti.o");
//		writeNCSLabFile("../../ricatti.hpp", "ricatti.hpp");
		writeNCSLabFile("include/onestep.hpp", "onestep.hpp");

		writeNCSLabFile("lib/DEV_Config.o", "DEV_Config.o");
		writeNCSLabFile("include/DEV_Config.h", "DEV_Config.h");

		writeNCSLabFile("lib/DAC8532.o", "DAC8532.o");
		writeNCSLabFile("include/DAC8532.h", "DAC8532.h");
		writeNCSLabFile("include/Debug.h", "Debug.h");

		writeNCSLabFile("include/ADS1256.h", "ADS1256.h");
		writeNCSLabFile("lib/ADS1256.o", "ADS1256.o");
//		writeNCSLabFile("include/bcm2835.h", "bcm2835.h");
//		writeNCSLabFile("src/bcm2835.c", "bcm2835.c");
		writeNCSLabFile("include/wiringPi.h", "wiringPi.h");
		writeNCSLabFile("include/wiringPiSPI.h", "wiringPiSPI.h");


		for (Block block : model.getBlockList()) {
			if (block.isSFcnBlock()) {
				block.generateSourceFile();
			}
		}

		// write the main code file ncslabccdoe.c
		writeMainCodeFile();
        writeNCSLabFile("include/mainccode.hpp", "mainccode.hpp");

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
//				writeNCSLabFile("../../ode4.o", "onestep.o");
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
