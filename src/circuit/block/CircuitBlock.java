package circuit.block;

import java.util.Vector;

import org.json.JSONObject;

import circuit.block.io.CircuitPort;
import ncslablink.NCSLabModel;

public class CircuitBlock {
	protected String blockType;
	protected String blockName;

	protected int blockId = 0;

	// Block所在画布的位置，不在子系统时为modelName，存在子系统时为modelName/subsystem
	protected String blockPath;
	// Block的参数，因为不同的block有不同的参数，因此以原生的json格式存储
	protected JSONObject paramValues;
	// 指向上级Model模型的指针
	protected NCSLabModel model;

	// 电气端口的列表
	protected Vector<CircuitPort> circuitPortList = new Vector<CircuitPort>();

	protected CircuitBlock(JSONObject blockIn, NCSLabModel model) {
		this.blockType = blockIn.getString("blockType");
		this.blockName = blockIn.getString("blockName");
		this.paramValues = blockIn.getJSONObject("paramValues");
		this.model = model;
		this.blockPath = blockIn.getString("blockPath");
		
		circuitPortList.add(new CircuitPort(this,"LConn1",1));
		circuitPortList.add(new CircuitPort(this,"RConn1",1));
	}
	
	public String getBlockName() {
		return this.blockName;
	}
	
	public Vector<CircuitPort> getCurcuitPortList(){
		return this.circuitPortList;
	}
	
	public String getBlockType() {
		return this.blockType;
	}
}
