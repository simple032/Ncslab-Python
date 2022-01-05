package code.c.raspberry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.json.JSONObject;

import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import code.CodeGenerationOption;
import code.CodeModel;
import line.Line;
import ncslablink.ErrorMessage;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import main.database.Algorithms;
import code.c.CodeModelC;
import code.c.CodeStructC;

public class CodeModelCRaspberry extends CodeModelC{
	
	private CodeStructCRaspberry codeRaspberry=new CodeStructCRaspberry(this);
	
	CodeModelCRaspberry(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}
	
	public static CodeModelCRaspberry createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCRaspberry model=new CodeModelCRaspberry(jsonIn,mode);
		
		return model;
	}
}
