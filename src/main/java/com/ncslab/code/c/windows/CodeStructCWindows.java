package com.ncslab.code.c.windows;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.utils.Property;

import java.io.*;
import java.util.Optional;

abstract public class CodeStructCWindows extends CodeStructC {
	protected CodeStructCWindows(CodeModelC model) {
		super(model);
	}

    protected String codePathBase = Property.instance.getProperty("CCodePathWin")
        .replace("${M2PLAB_ROOT}", Optional.ofNullable(System.getenv("M2PLAB_ROOT")).orElse(""));

    public void writeCCodeFiles(){
        super.writeCCodeFiles();
        System.out.println("Write CCode Files in CodeStructCWindows");
        //实锟斤拷Netcon协锟斤拷锟酵拷锟斤拷募锟�
        writeNCSLabFile("ServerThread.cpp");
        writeNCSLabFile("ServerThread.hpp");
        writeNCSLabFile("ClientThread.cpp");
        writeNCSLabFile("ClientThread.hpp");
        writeNCSLabFile("UploadThread.cpp");
        writeNCSLabFile("UploadThread.hpp");

        writeNCSLabFile("ncslabccode.hpp");
        //main锟斤拷锟斤拷锟皆硷拷锟斤拷时锟斤拷
        writeNCSLabFile("ncslabmain.cpp");
        //锟斤拷锟斤拷锟斤拷锟斤拷锟捷结构锟侥接匡拷API锟斤拷锟斤拷
        writeNCSLabFile("DataApi.cpp");
        writeNCSLabFile("DataApi.hpp");

        writeNCSLabFile("ncslabdefines.hpp");
    }

	public byte[] readExeFile() {
		return readFile("ncslab.exe");
	}

}

