package com.ncslab.code.c.linux.raspberry;

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
import line.Line;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import main.database.Algorithms;
import com.ncslab.code.c.CodeModelC;
import com.ncslab.code.c.CodeStructC;

public class CodeModelCLinuxRaspberry extends CodeModelC{

	private CodeStructCLinuxRaspberry codeRaspberry=new CodeStructCLinuxRaspberry(this);

	CodeModelCLinuxRaspberry(JSONObject jsonIn, ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}

	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}

	public static CodeModelCLinuxRaspberry createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxRaspberry model=new CodeModelCLinuxRaspberry(jsonIn,mode);

		return model;
	}
}
