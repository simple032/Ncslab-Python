package com.ncslab.code.c.windows;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.utils.Property;

import java.io.*;

public class CodeStructCWindows extends CodeStructC {
	protected CodeStructCWindows(CodeModelC model) {
		super(model);
	}

    protected String codePathBase = Property.instance.getProperty("CCodePathWin")
        .replace("${M2PLAB_ROOT}",System.getenv("M2PLAB_ROOT"));


	public byte[] readExeFile() {
		return readFile("ncslab.exe");
	}

}

