package com.ncslab.code.c.linux.pc.simulation;

import java.io.*;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.c.CodeModelC;

public class CodeStructCLinuxPCSimulation extends CodeStructC{
	public CodeStructCLinuxPCSimulation(CodeModelC model) {
		super(model);
	}

	// TODO: this file is similar to its super implementation
	@Override
	public void writeCCodeFiles() {

		// target folder path: codePathBase/userId/modelId
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

		// write resource files
		// makefile
		 //writeNCSLabFile("makefile");
		writeMakefile("makefile");
//		writeNCSLabFile("CMakeLists.txt","CMakeLists.txt", true);
		// main function and timer
		writeNCSLabFile("../../../ncslabmainsimu.cpp","ncslabmain.cpp", true);
		// write the header file for generated main code
        writeNCSLabFile("../../../ncslabccode.hpp", "ncslabccode.hpp");
        writeNCSLabFile("../../../mainccode.hpp","mainccode.hpp", true);

		writeNCSLabFile("../../../util.hpp","util.hpp",true);
		writeNCSLabFile("../../../util.cpp","util.cpp",true);

		writeNCSLabFile("../../../ncslabdefines.hpp","ncslabdefines.hpp", true);
		writeNCSLabFile("../../../ncslabsfun.hpp","ncslabsfun.hpp", true);
		writeNCSLabFile("../../../results.cpp","results.cpp",true);
		writeNCSLabFile("../../../results.hpp","results.hpp",true);

		// Matrix.cpp removed - Matrix.hpp is now header-only template
		writeNCSLabFile("../../../Matrix.hpp","Matrix.hpp", true);
		writeNCSLabFile("../../../ricatti.cpp","ricatti.cpp", true);
		writeNCSLabFile("../../../ricatti.hpp","ricatti.hpp", true);
		writeNCSLabFile("../../../onestep.hpp","onestep.hpp", true);


		// Implement the general file of the Netcon protocol
		 writeNCSLabFile("../../ServerThread.cpp","ServerThread.cpp");
		 writeNCSLabFile("../../ServerThread.hpp","ServerThread.hpp");
		 writeNCSLabFile("../../ClientThread.cpp","ClientThread.cpp");
		 writeNCSLabFile("../../ClientThread.hpp","ClientThread.hpp");
		 writeNCSLabFile("../../UploadThread.cpp","UploadThread.cpp");
		 writeNCSLabFile("../../UploadThread.hpp","UploadThread.hpp");

//		writeNCSLabFile("../../../ncs_serialport_pi.c","ncs_serialport_pi.c");
//		writeNCSLabFile("../../../ncs_serialport.h","ncs_serialport.h");

		for(Block block: model.getBlockList()) {
			if(block.isSFcnBlock()) {
				block.generateSourceFile();
			}
		}

		// write the main code file ncslabccdoe.c
		writeMainCodeFile();

		wirteDefineFile();

		switch(model.getSolver()) {
		case ode1:
			writeNCSLabFile("../../../ode1.cpp","onestep.cpp",true);
			break;
		case ode2:
			writeNCSLabFile("../../../ode2.cpp","onestep.cpp",true);
			break;
		case ode3:
			writeNCSLabFile("../../../ode3.cpp","onestep.cpp",true);
			break;
		case ode4:
			writeNCSLabFile("../../../ode4.cpp","onestep.cpp",true);
			break;
		case ode45:
			writeNCSLabFile("../../../ode45.cpp","onestep.cpp",true);
			break;
		case ode23:
			writeNCSLabFile("../../../ode23.cpp","onestep.cpp",true);
			break;
		case ode5:
			writeNCSLabFile("../../../ode5.cpp","onestep.cpp",true);
			break;
		case ode6:
			writeNCSLabFile("../../../ode6.cpp","onestep.cpp",true);
			break;
		default:
			writeNCSLabFile("../../../ode45.cpp","onestep.cpp",true);
			break;
		}

		writeNCSWrittenFiles();// write the files in <code>CodeStructC.writtenFileSet</code>.
	}

	// TODO: similar to its super implementation
	@Override
	public byte[] readExeFile() {
		return readFile("ncslab");
	}

//	@Override
//	public boolean makeExeFile() {
//		try {
//			// start make, generate executable file
//			// Process process=Runtime.getRuntime().exec("make", null, new File(codePath));
//			// using cmake instead of make
////			String cmakeCommand[] = {"cmake","."};
////			Process makeProcess=Runtime.getRuntime().exec(cmakeCommand, null, new File(codePath));
////			makeProcess.waitFor();
//
//			Process process=Runtime.getRuntime().exec(maketool, null, new File(codePath));
//			// get OutputStream and errStream of the process, in case of blocking
//			BufferedReader in=new BufferedReader(new InputStreamReader(process.getErrorStream()));
//			BufferedReader inOut=new BufferedReader(new InputStreamReader(process.getInputStream()));
//			String line=null,outLine=null;
//			StringBuilder errStr=new StringBuilder();
//			StringBuilder outStr=new StringBuilder();
//
//			while((outLine=inOut.readLine())!=null||(line=in.readLine())!=null) {
//				if(outLine!=null) {
//					outStr.append(outLine);
//					System.out.println(outLine);
//				}
//				if(line!=null) {
//					errStr.append(line);
//					System.err.println(line);
//				}
//			}
//
//			// wait for the make process to terminate
//			process.waitFor();
//
//			if(process.exitValue()==0) {
//				return true;
//			}
//
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//		}
//
//		return false;
//	}

	@Override
	protected void writeNCSWrittenFiles(){
		super.writeNCSWrittenFiles();
	}
}
