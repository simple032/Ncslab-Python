package com.ncslab.code.c.windows.simulation;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeModelC;
public class CodeStructCWindowsSimulationRT extends CodeStructCWindowsSimulation {
	public CodeStructCWindowsSimulationRT(CodeModelC model) {
		super(model);
	}

	@Override
	public void writeCCodeFiles() {
        super.writeCCodeFiles();
        System.out.println("Write CCode Files in CodeStructCWindowsSimulationRT (real-time mode)");
        writeMakefile("makefile");

        // Use real-time main entry instead of standard one
        writeNCSLabFile("../../ncslabmainsimurt.cpp","ncslabmain.cpp", true);

		writeNCSWrittenFiles();
	}
}
