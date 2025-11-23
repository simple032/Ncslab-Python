package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.source.Constant;
import com.ncslab.dto.communication.CircuitBlockDto;

/**
 * DC Voltage Source circuit element block for electrical circuit simulation.
 * Provides constant DC voltage in Branch mode only.
 */
public class DCVoltageSource extends CircuitBlock {

	private Constant dcVoltageSource;

	/**
	 * Legacy JSON Constructor - Creates DCVoltageSource block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing DC voltage source configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public DCVoltageSource(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
	}

	/**
	 * DTO-Native Constructor - Creates DCVoltageSource block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing DC voltage source configuration
	 * @param model Parent model reference
	 */
	public DCVoltageSource(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		System.out.println("DTO-NATIVE: DCVoltageSource block created successfully - " + dto.getBlockName());
	}

	// === Static Factory Methods for Programmatic Creation ===

	/**
	 * Create a DC voltage source with specified voltage
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param voltage Voltage in Volts
	 * @param model Parent model
	 * @return DCVoltageSource instance
	 */
	public static DCVoltageSource create(String blockName, String blockPath, double voltage, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.DCVoltageSourceDto dto =
			com.ncslab.dto.block.specialized.circuit.element.DCVoltageSourceDto.create(blockName, blockPath, voltage);
		return new DCVoltageSource(dto, model);
	}

	/**
	 * Create a 5V DC voltage source with default parameters
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return DCVoltageSource instance with 5V output
	 */
	public static DCVoltageSource createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath, 5.0, model);
	}
	
	protected void setupBlockList() {
		setupEquivilentBlockModels();
	}
	
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.BranchOnly;
	}
	
	private void setupEquivilentBlockModels() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		String v0=paramValues.getString("v0");
		
		JSONObject dcVoltageSourceJSON=new JSONObject();
		dcVoltageSourceJSON.put("blockType", "Constant");
		dcVoltageSourceJSON.put("blockName", this.blockName.replaceAll(" ", "_")+"_v0");
		dcVoltageSourceJSON.put("blockPath", this.blockPath);
		JSONObject dcVoltageSourceParamValues=new JSONObject();
		dcVoltageSourceParamValues.put("Value", v0);
		dcVoltageSourceJSON.put("paramValues", dcVoltageSourceParamValues);
		
		dcVoltageSource=new Constant(dcVoltageSourceJSON,this.model);
		this.blockList.add(dcVoltageSource);
		
		//System.out.println(dcVoltageSourceJSON);
		
		this.setOutputBlock(dcVoltageSource);
		this.setupInputBlock(null);
	}
}
