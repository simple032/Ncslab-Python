package code.c.linux;

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

public class CodeModelCLinux extends CodeModelC{
	
	private CodeStructCLinux codeRaspberry=new CodeStructCLinux(this);
	
	CodeModelCLinux(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeRaspberry;
	}
	
	public static CodeModelCLinux createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinux model=new CodeModelCLinux(jsonIn,mode);
		
		return model;
	}
}
