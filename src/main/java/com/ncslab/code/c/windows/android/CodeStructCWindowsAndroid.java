package com.ncslab.code.c.windows.android;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.c.windows.CodeStructCWindows;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CodeStructCWindowsAndroid extends CodeStructCWindows {
	public CodeStructCWindowsAndroid(CodeModelC model) {
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
		writeNCSLabFile("ncslabccode.hpp", "ncslabccode.hpp");
		// main function and timer
		writeNCSLabFile("src/ncslabmain.cpp", "ncslabmain.cpp");
		// Define the API to access the main data structure
//		writeNCSLabFile("lib/DataApi.o", "DataApi.o");
        writeNCSLabFile("DataApi.cpp", "DataApi.cpp");
		writeNCSLabFile("include/DataApi.hpp", "DataApi.hpp");

//		writeNCSLabFile("lib/util.o", "util.o", true);
        writeNCSLabFile("util.cpp", "util.cpp");
        writeNCSLabFile("util.hpp", "util.hpp");

		writeNCSLabFile("include/ncslabdefines.hpp", "ncslabdefines.hpp");
		writeNCSLabFile("include/ncslabsfun.hpp", "ncslabsfun.hpp");

        // Implement the general file of the Netcon protocol
//		writeNCSLabFile("lib/ServerThread.o", "ServerThread.o");
        writeNCSLabFile("../../linux/ServerThread.cpp", "ServerThread.cpp");
		writeNCSLabFile("include/ServerThread.hpp", "ServerThread.hpp");
//		writeNCSLabFile("lib/ClientThread.o", "ClientThread.o");
        writeNCSLabFile("../../linux/ClientThread.cpp", "ClientThread.cpp");
		writeNCSLabFile("include/ClientThread.hpp", "ClientThread.hpp");
//		writeNCSLabFile("lib/UploadThread.o", "UploadThread.o");
        writeNCSLabFile("../../linux/UploadThread.cpp", "UploadThread.cpp");
		writeNCSLabFile("include/UploadThread.hpp", "UploadThread.hpp");

//		writeNCSLabFile("lib/Matrix.o", "Matrix.o");
        // Matrix.cpp removed - Matrix.hpp is now header-only template
		writeNCSLabFile("Matrix.hpp", "Matrix.hpp");
//		writeNCSLabFile("lib/ricatti.o", "ricatti.o");
//		writeNCSLabFile("../../ricatti.hpp", "ricatti.hpp");
		writeNCSLabFile("onestep.hpp", "onestep.hpp");

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
