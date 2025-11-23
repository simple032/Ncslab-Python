package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.source.SineWave;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.communication.CircuitBlockDto;

/**
 * AC Voltage Source circuit element block for electrical circuit simulation.
 * Provides sinusoidal AC voltage in Branch mode only.
 */
public class ACVoltageSource extends CircuitBlock {
    private SineWave acVoltageSource;

	/**
	 * Legacy JSON Constructor - Creates ACVoltageSource block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing AC voltage source configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public ACVoltageSource(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
	}

	/**
	 * DTO-Native Constructor - Creates ACVoltageSource block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing AC voltage source configuration
	 * @param model Parent model reference
	 */
	public ACVoltageSource(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		System.out.println("DTO-NATIVE: ACVoltageSource block created successfully - " + dto.getBlockName());
	}

	// === Static Factory Methods for Programmatic Creation ===

	/**
	 * Create an AC voltage source with specified amplitude and frequency
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param amplitude Peak amplitude in Volts
	 * @param frequency Frequency in Hz
	 * @param model Parent model
	 * @return ACVoltageSource instance
	 */
	public static ACVoltageSource create(String blockName, String blockPath, double amplitude, double frequency, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.ACVoltageSourceDto dto =
			com.ncslab.dto.block.specialized.circuit.element.ACVoltageSourceDto.create(blockName, blockPath, amplitude, frequency);
		return new ACVoltageSource(dto, model);
	}

	/**
	 * Create an AC voltage source with amplitude, frequency, and phase shift
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param amplitude Peak amplitude in Volts
	 * @param frequency Frequency in Hz
	 * @param phaseShift Phase shift in degrees
	 * @param model Parent model
	 * @return ACVoltageSource instance
	 */
	public static ACVoltageSource create(String blockName, String blockPath, double amplitude, double frequency, double phaseShift, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.ACVoltageSourceDto dto =
			com.ncslab.dto.block.specialized.circuit.element.ACVoltageSourceDto.create(blockName, blockPath, amplitude, frequency, phaseShift);
		return new ACVoltageSource(dto, model);
	}

	/**
	 * Create a 10V 50Hz AC voltage source with default parameters
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return ACVoltageSource instance with 10V @ 50Hz
	 */
	public static ACVoltageSource createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath, 10.0, 50.0, model);
	}
	
	protected void setupBlockList() {
		setupEquivilentBlockModels();
	}
	
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.BranchOnly;
	}
	
	private void setupEquivilentBlockModels() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		String v0=paramValues.getString("amp");
		String s=paramValues.getString("shift");
		String f=paramValues.getString("frequency");
		
		JSONObject acVoltageSourceJSON=new JSONObject();
		acVoltageSourceJSON.put("blockType", "SineWave");
		acVoltageSourceJSON.put("blockName", this.blockName.replaceAll(" ", "_")+"_v0");
		acVoltageSourceJSON.put("blockPath", this.blockPath);
		JSONObject acVoltageSourceParamValues=new JSONObject();
		acVoltageSourceParamValues.put("Amplitude", v0);
		acVoltageSourceParamValues.put("Bias", "0");
		acVoltageSourceParamValues.put("Frequency", "6.2831852*("+f+")");
		acVoltageSourceParamValues.put("Phase", "-1*("+s+")/("+f+")");
		acVoltageSourceJSON.put("paramValues", acVoltageSourceParamValues);
		acVoltageSource=new SineWave(acVoltageSourceJSON,this.model);
		this.blockList.add(acVoltageSource);
		
		//System.out.println(dcVoltageSourceJSON);
		
		this.setOutputBlock(acVoltageSource);
		this.setupInputBlock(null);
	}
}
