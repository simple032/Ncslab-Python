package com.ncslab.code.c.windows;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.code.CodeGenerationOption;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.database.Algorithms;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;

public class CodeModelCWindows extends CodeModelC {
	private CodeStructCWindows codeWindows=new CodeStructCWindows(this);
	
	CodeModelCWindows(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeWindows;
	}
	
	public static CodeModelCWindows createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCWindows model=new CodeModelCWindows(jsonIn,mode);
		
		return model;
	}
	
}
