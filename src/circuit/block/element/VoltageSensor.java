package circuit.block.element;

import java.util.Vector;

import org.json.JSONObject;

import block.source.Constant;
import circuit.block.BlockModeType;
import circuit.block.CircuitBlock;
import circuit.block.io.BlockVoltage;
import circuit.block.io.PortCurrent;
import ncslablink.NCSLabModel;
import block.Block;
import block.io.InputPort;
import block.io.OutputPort;
import block.math.Add;

public class VoltageSensor extends CircuitBlock {
	
	private Constant currentSource;
	
	private Add add;
	
	public VoltageSensor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		circuitPortList.get(1).setName("RConn2");
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
	}
	
	protected void setupBlockList() {
		setupEquivilentBlockModels();
	}
	
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.LinkOnly;
	}
	
	private void setupEquivilentBlockModels() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		
		JSONObject dcCurrentSourceJSON=new JSONObject();
		dcCurrentSourceJSON.put("blockType", "Constant");
		dcCurrentSourceJSON.put("blockName", this.blockName.replaceAll(" ", "_")+"_v0");
		dcCurrentSourceJSON.put("blockPath", this.blockPath);
		JSONObject dcVoltageSourceParamValues=new JSONObject();
		dcVoltageSourceParamValues.put("Value", "0");
		dcCurrentSourceJSON.put("paramValues", dcVoltageSourceParamValues);
		
		currentSource=new Constant(dcCurrentSourceJSON,this.model);
		this.blockList.add(currentSource);
		
		//System.out.println(dcVoltageSourceJSON);
		//设置CurrentSource模块作为输出,与其他的Circuit模块相连
		this.setOutputBlock(currentSource);
		this.setupInputBlock(null);
		
		//Add模块的输出作为外界的输出,Add模块计算环路中其他模块的电压和
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Add");
		addJSON.put("blockName", this.blockName+"_Add");
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		String inputs="";
		for(BlockVoltage voltage:getVoltageList()) {
			if(voltage.getSign()) {
				inputs+="+";
			}
			else {
				inputs+="-";
			}
		}
		addParamValues.put("Inputs", inputs);
		addJSON.put("paramValues", addParamValues);
		
		add=new Add(addJSON,this.model);
		this.blockList.add(add);
		//Add模块的输出作为外界的输出,加入到列表中
		outputBlockList.add(add);
	}
	
	//建立Add模块与其他模块之间的关系,作为Voltage Sensor的输出
	protected void setupBlockListConnections() {
		super.setupBlockListConnections();
		
		Vector<InputPort> inputPortList=add.getInputPortList();
		for(int i=0;i<inputPortList.size();i++) {
			InputPort input=inputPortList.get(i);
			OutputPort output=null;
			BlockVoltage voltage=getVoltageList().get(i);
			output=voltage.getCircuitBlock().getOutputPort();
			
			Vector<Block> blocks=new Vector<Block>();
			blocks.add(input.getBLock());
			blocks.add(output.getBLock());
			
			createLine(output.getBLock().getBlockName(), 1, input.getBLock().getBlockName(), i+1,blocks);
		}
		
		this.getCircuitModel().addTerminalBlocks(add);
	}

}
