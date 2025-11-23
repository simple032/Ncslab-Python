package com.ncslab.circuit.block.element;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.ncslab.block.source.Constant;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.io.BlockVoltage;
import com.ncslab.circuit.block.io.PortCurrent;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.math.Add;
import com.ncslab.dto.communication.CircuitBlockDto;
import com.ncslab.dto.block.specialized.circuit.element.VoltageSensorDto;

/**
 * Voltage Sensor circuit element block for electrical circuit simulation.
 * Measures voltage across its terminals and outputs the measured value.
 */
public class VoltageSensor extends CircuitBlock {

	private Constant currentSource;

	private Add add;

	/**
	 * Legacy JSON Constructor - Creates VoltageSensor block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing voltage sensor configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public VoltageSensor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		circuitPortList.get(1).setName("RConn2");
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
	}

	/**
	 * DTO-Native Constructor - Creates VoltageSensor block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing voltage sensor configuration
	 * @param model Parent model reference
	 */
	public VoltageSensor(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		circuitPortList.get(1).setName("RConn2");
		System.out.println("DTO-NATIVE: VoltageSensor block created successfully - " + dto.getBlockName());
	}

	/**
	 * Factory method: Create voltage sensor with specified scale and offset
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param scale Scale factor
	 * @param offset Voltage offset in Volts
	 * @param model Parent model
	 * @return VoltageSensor instance
	 */
	public static VoltageSensor create(String blockName, String blockPath,
	                                   double scale, double offset, NCSLabModel model) {
		VoltageSensorDto dto = VoltageSensorDto.create(blockName, blockPath, scale, offset);
		return new VoltageSensor(dto, model);
	}

	/**
	 * Factory method: Create voltage sensor with scale factor only
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param scale Scale factor
	 * @param model Parent model
	 * @return VoltageSensor instance
	 */
	public static VoltageSensor create(String blockName, String blockPath,
	                                   double scale, NCSLabModel model) {
		return create(blockName, blockPath, scale, VoltageSensorDto.DEFAULT_OFFSET, model);
	}

	/**
	 * Factory method: Create ideal voltage sensor (scale=1.0, offset=0.0)
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return VoltageSensor instance
	 */
	public static VoltageSensor createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath,
			VoltageSensorDto.DEFAULT_SCALE, VoltageSensorDto.DEFAULT_OFFSET, model);
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
		
		List<InputPort> inputPortList=add.getInputPortList();
		for(int i=0;i<inputPortList.size();i++) {
			InputPort input=inputPortList.get(i);
			OutputPort output=null;
			BlockVoltage voltage=getVoltageList().get(i);
			output=voltage.getCircuitBlock().getOutputPort();
			
			List<Block> blocks = new ArrayList<>();
			blocks.add(input.getBlock());
			blocks.add(output.getBlock());
			
			createLine(output.getBlock().getBlockName(), 1, input.getBlock().getBlockName(), i+1,blocks);
		}
		
		this.getCircuitModel().addTerminalBlocks(add);
	}

}
