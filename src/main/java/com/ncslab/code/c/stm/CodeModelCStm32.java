package com.ncslab.code.c.stm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

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

public class CodeModelCStm32 extends CodeModelC{

	private CodeStructCStm32 codeStm32=new CodeStructCStm32(this);

	CodeModelCStm32(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}

	protected CodeStructC getCodeStructC() {
		return codeStm32;
	}

	public static CodeModelCStm32 createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCStm32 model=new CodeModelCStm32(jsonIn,mode);

		return model;
	}
}
