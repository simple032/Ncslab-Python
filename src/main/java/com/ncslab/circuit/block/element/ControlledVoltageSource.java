package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.source.Constant;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.electblock.GainElect;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.communication.CircuitBlockDto;
import com.ncslab.dto.block.specialized.circuit.element.ControlledVoltageSourceDto;

/**
 * Controlled Voltage Source circuit element block for electrical circuit simulation.
 * Voltage is controlled by an external input signal.
 */
public class ControlledVoltageSource extends CircuitBlock {

	private GainElect controlledVoltageSource;

	/**
	 * Legacy JSON Constructor - Creates ControlledVoltageSource block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing controlled voltage source configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public ControlledVoltageSource(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		circuitPortList.get(1).setName("RConn2");
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
	}

	/**
	 * DTO-Native Constructor - Creates ControlledVoltageSource block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing controlled voltage source configuration
	 * @param model Parent model reference
	 */
	public ControlledVoltageSource(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		circuitPortList.get(1).setName("RConn2");
		System.out.println("DTO-NATIVE: ControlledVoltageSource block created successfully - " + dto.getBlockName());
	}

	/**
	 * Factory method: Create controlled voltage source with specified gain
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param gain Gain factor
	 * @param model Parent model
	 * @return ControlledVoltageSource instance
	 */
	public static ControlledVoltageSource create(String blockName, String blockPath,
	                                             double gain, NCSLabModel model) {
		ControlledVoltageSourceDto dto = ControlledVoltageSourceDto.create(blockName, blockPath, gain);
		return new ControlledVoltageSource(dto, model);
	}

	/**
	 * Factory method: Create controlled voltage source with default gain (1.0)
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return ControlledVoltageSource instance
	 */
	public static ControlledVoltageSource createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath, ControlledVoltageSourceDto.DEFAULT_GAIN, model);
	}

	@Override
	protected void setupBlockList() {
		// TODO Auto-generated method stub
		setupEquivilentBlockModels();
	}

	@Override
	protected void setupBlockModeType() {
		// TODO Auto-generated method stub
		blockModeType=BlockModeType.BranchOnly;
	}
	
	private void setupEquivilentBlockModels() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		
		JSONObject controlledVoltageSourceJSON=new JSONObject();
		controlledVoltageSourceJSON.put("blockType", "Gain");
		controlledVoltageSourceJSON.put("blockName", this.blockName.replaceAll(" ", "_")+"_v0");
		controlledVoltageSourceJSON.put("blockPath", this.blockPath);
		JSONObject controlledVoltageSourceParamValues=new JSONObject();
		controlledVoltageSourceParamValues.put("Gain", "1.0");
		controlledVoltageSourceParamValues.put("Multiplication", "Element-wise(K.*u)");
		controlledVoltageSourceJSON.put("paramValues", controlledVoltageSourceParamValues);
		
		controlledVoltageSource=new GainElect(controlledVoltageSourceJSON,this.model);
		this.blockList.add(controlledVoltageSource);
		
		this.setOutputBlock(controlledVoltageSource);
		
		//controlledVoltageSource模块的输出作为外界的输入,加入到列表中
		inputPortList.add(controlledVoltageSource.getInputPortList().get(0));
	}

}
