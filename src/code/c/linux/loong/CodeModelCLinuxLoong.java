package code.c.linux.loong;

import code.c.CodeModelC;
import code.c.CodeStructC;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import org.json.JSONObject;

public class CodeModelCLinuxLoong extends CodeModelC{
	
	private CodeStructCLinuxLoong codeLoong=new CodeStructCLinuxLoong(this);

	CodeModelCLinuxLoong(JSONObject jsonIn,ModelMode mode) throws ModelException{
		super(jsonIn,mode);
	}
	
	protected CodeStructC getCodeStructC() {
		return codeLoong;
	}
	
	public static CodeModelCLinuxLoong createFromJSON(JSONObject jsonIn,ModelMode mode) throws ModelException {
		CodeModelCLinuxLoong model=new CodeModelCLinuxLoong(jsonIn,mode);
		
		return model;
	}
}
