package com.ncslab.code.c.windows;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.utils.Property;

import java.io.*;
import java.util.Optional;

public class CodeStructCWindows extends CodeStructC {
	protected CodeStructCWindows(CodeModelC model) {
		super(model);
	}

    protected String codePathBase = Property.instance.getProperty("CCodePathWin")
        .replace("${M2PLAB_ROOT}", Optional.ofNullable(System.getenv("M2PLAB_ROOT")).orElse(""));


	public byte[] readExeFile() {
		return readFile("ncslab.exe");
	}

}

