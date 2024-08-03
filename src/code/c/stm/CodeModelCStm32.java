package code.c.stm;

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
