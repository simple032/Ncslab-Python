package code.c.linux.pc;

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
