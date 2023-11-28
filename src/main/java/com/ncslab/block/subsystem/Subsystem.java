package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import org.json.JSONObject;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
public class Subsystem extends Block{
	public Subsystem(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}
	public void generateOutputCodeC(CodeStructC code) {
		
	}
	  public void updateDimension() throws MatDimException{
		}
	public void checkDimension() throws MatDimException{
	}
}
