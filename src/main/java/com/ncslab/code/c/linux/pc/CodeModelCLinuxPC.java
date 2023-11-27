package com.ncslab.code.c.linux.pc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.CodeGenerationOption;
import com.ncslab.code.CodeModel;
import com.ncslab.line.Line;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.database.Algorithms;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;

// TODO: currently this class is not used
public class CodeModelCLinuxPC extends CodeModelC{
	
	private CodeStructCLinuxPC codeRaspberry=new CodeStructCLinuxPC(this);
	
	CodeModelCLinuxPC(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}
	
	public static CodeModelCLinuxPC createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxPC model=new CodeModelCLinuxPC(jsonIn,mode);
		
		return model;
	}
}
