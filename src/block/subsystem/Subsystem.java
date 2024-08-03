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
//		int inSYSCount=paramValues.getInt("inSYSCount");
//		int outSYSCount=paramValues.getInt("outSYSCount");
//		for(int i=1;i<=inSYSCount;i++) {
//			inputPortList.add(new InputPort(this,i));
//		}
//		for(int i=1;i<=outSYSCount;i++) {
//			outputPortList.add(new OutputPort(this,i,true));
//		}
	}
	public void generateOutputCodeC(CodeStructC code) {
		
	}
	  public void updateDimension() throws MatDimException{
		}
	public void checkDimension() throws MatDimException{
	}
}
