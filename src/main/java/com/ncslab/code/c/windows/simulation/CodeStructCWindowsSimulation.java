package com.ncslab.code.c.windows.simulation;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.windows.CodeStructCWindows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.Arrays;

public class CodeStructCWindowsSimulation extends CodeStructCWindows {
	public CodeStructCWindowsSimulation(CodeModelC model) {
		super(model);
	}

	// TODO: this file is similar to its super implementation
	@Override
	public void writeCCodeFiles() {
        super.writeCCodeFiles();
        System.out.println("Write CCode Files in CodeStructCWindowsSimulation");
		// write resource files
		// makefile
//        writeNCSLabFile("makefile");
        writeMakefile("makefile");

        writeNCSLabFile("../../util.cpp", "util.cpp", true);
        writeNCSLabFile("../../util.hpp", "util.hpp", true);
        writeNCSLabFile("../../ncslabmainsimu.cpp","ncslabmain.cpp", true);
        writeNCSLabFile("../../ncslab_cuda_probe.cpp", "ncslab_cuda_probe.cpp", true);
        writeNCSLabFile("../../ncslab_cuda_cublas.cpp", "ncslab_cuda_cublas.cpp", true);
        writeNCSLabFile("../../ncslab_cuda_cusolver_stub.cpp", "ncslab_cuda_cusolver_stub.cpp", true);
        writeNCSLabFile("../../ncslab_cuda_device.cu", "ncslab_cuda_device.cu", true);
        writeNCSLabFile("../../ncslab_cuda_device_stub.cpp", "ncslab_cuda_device_stub.cpp", true);

		// write the main code file ncslabccdoe.c

		writeNCSWrittenFiles();// write the files in <code>CodeStructC.writtenFileSet</code>.
	}
}
