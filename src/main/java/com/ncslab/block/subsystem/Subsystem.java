package com.ncslab.block.subsystem;

import com.ncslab.block.Block;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
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
