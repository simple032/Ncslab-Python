package block.subsystem;

import block.Block;
import org.json.JSONObject;
import block.Block;
import block.data.DataType;
import block.io.OutputPort;
import block.io.Parameter;
import code.c.CodeStructC;
import code.m.CodeStructM;
import block.io.InputPort;
import block.io.OutputSignal;
import ncslablink.MatDimException;
import ncslablink.NCSLabModel;
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
