package code.c.windows;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.json.JSONObject;

import block.Block;
import code.CodeGenerationOption;
import code.c.CodeModelC;
import code.c.CodeStructC;
import main.database.Algorithms;
import ncslablink.ModelException;
import ncslablink.ModelMode;

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
