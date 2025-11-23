package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.math.Add;
import com.ncslab.block.math.Gain;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.io.BlockVoltage;
import com.ncslab.circuit.block.io.PortCurrent;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.communication.CircuitBlockDto;

import com.ncslab.circuit.block.electblock.*;

/**
 * Resistor circuit element block for electrical circuit simulation.
 * Can operate in both Branch mode (voltage-to-current) and Link mode (current-to-voltage).
 */
public class Resistor extends CircuitBlock {

	private GainElect gain;
	private AddElect add;

	/**
	 * Legacy JSON Constructor - Creates Resistor block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing resistor configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public Resistor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}

	/**
	 * DTO-Native Constructor - Creates Resistor block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing resistor configuration
	 * @param model Parent model reference
	 */
	public Resistor(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		System.out.println("DTO-NATIVE: Resistor block created successfully - " + dto.getBlockName());
	}

	// === Static Factory Methods for Programmatic Creation ===

	/**
	 * Create a resistor with specified resistance value
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param resistance Resistance in Ohms
	 * @param model Parent model
	 * @return Resistor instance
	 */
	public static Resistor create(String blockName, String blockPath, double resistance, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.ResistorDto dto =
			com.ncslab.dto.block.specialized.circuit.element.ResistorDto.create(blockName, blockPath, resistance);
		return new Resistor(dto, model);
	}

	/**
	 * Create a resistor with specified resistance value (string expression)
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param resistance Resistance expression (e.g., "1000", "1e3", "R1")
	 * @param model Parent model
	 * @return Resistor instance
	 */
	public static Resistor create(String blockName, String blockPath, String resistance, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.ResistorDto dto =
			com.ncslab.dto.block.specialized.circuit.element.ResistorDto.create(blockName, blockPath, resistance);
		return new Resistor(dto, model);
	}

	/**
	 * Create a 1kΩ resistor with default parameters
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return Resistor instance with 1kΩ resistance
	 */
	public static Resistor createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath, 1000.0, model);
	}
	
	protected void setupBlockList() {
		switch(this.blockMode) {
		case Branch:
			setupBranchBlockList();
			break;
		case Link:
			setupLinkBlockList();
			break;
		}
	}
	
	private void setupBranchBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"' as a branch");
		
		String R=paramValues.getString("R");
		JSONObject gainJSON=new JSONObject();
		gainJSON.put("blockType", "Gain");
		gainJSON.put("blockName", this.blockName+"_R");
		gainJSON.put("blockPath", this.blockPath);
		JSONObject gainParamValues=new JSONObject();
		gainParamValues.put("Gain", R);
		gainParamValues.put("Multiplication", "Element-wise(K.*u)");
		gainJSON.put("paramValues", gainParamValues);
		
		gain=new GainElect(gainJSON,this.model);
		this.blockList.add(gain);
		
		JSONObject addJSON=new JSONObject();
		addJSON.put("blockType", "Add");
		addJSON.put("blockName", this.blockName+"_Add");
		addJSON.put("blockPath", this.blockPath);
		JSONObject addParamValues=new JSONObject();
		String inputs="";
		for(PortCurrent current:this.getCurcuitPortList().get(0).getCurrentList()) {
			if(current.getSign()) {
				inputs+="+";
			}
			else {
				inputs+="-";
			}
		}
		addParamValues.put("Inputs", inputs);
		addJSON.put("paramValues", addParamValues);
		
		add=new AddElect(addJSON,this.model);
		this.blockList.add(add);
		
		createLine(add.getBlockName(), 1, gain.getBlockName(), 1);
		
		this.setOutputBlock(gain);
		this.setupInputBlock(add);
	}
	
	private void setupLinkBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"' as a link");
		
		String R=paramValues.getString("R");
		JSONObject gainJSON=new JSONObject();
		gainJSON.put("blockType", "Gain");
		gainJSON.put("blockName", this.blockName+"_R");
		gainJSON.put("blockPath", this.blockPath);
		JSONObject gainParamValues=new JSONObject();
		gainParamValues.put("Gain", "1.0/("+R+")");
		gainParamValues.put("Multiplication", "Element-wise(K.*u)");
		gainJSON.put("paramValues", gainParamValues);
		
		gain=new GainElect(gainJSON,this.model);
		this.blockList.add(gain);
		
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
		
		add=new AddElect(addJSON,this.model);
		this.blockList.add(add);
		
		createLine(add.getBlockName(), 1, gain.getBlockName(), 1);
		
		this.setOutputBlock(gain);
		this.setupInputBlock(add);
	}
	
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.Anything;
	}
}
