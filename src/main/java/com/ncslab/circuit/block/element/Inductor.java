package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.continuous.Integrator;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.io.BlockVoltage;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.communication.CircuitBlockDto;

import com.ncslab.circuit.block.electblock.*;

/**
 * Inductor circuit element block for electrical circuit simulation.
 * Can only operate in Link mode (stores current, produces voltage).
 */
public class Inductor extends CircuitBlock {
	private Integrator integrator;
	private GainElect gain;
	private AddElect add;

	/**
	 * Legacy JSON Constructor - Creates Inductor block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing inductor configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public Inductor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}

	/**
	 * DTO-Native Constructor - Creates Inductor block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing inductor configuration
	 * @param model Parent model reference
	 */
	public Inductor(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		System.out.println("DTO-NATIVE: Inductor block created successfully - " + dto.getBlockName());
	}

	// === Static Factory Methods for Programmatic Creation ===

	/**
	 * Create an inductor with specified inductance value
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param inductance Inductance in Henries
	 * @param model Parent model
	 * @return Inductor instance
	 */
	public static Inductor create(String blockName, String blockPath, double inductance, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.InductorDto dto =
			com.ncslab.dto.block.specialized.circuit.element.InductorDto.create(blockName, blockPath, inductance);
		return new Inductor(dto, model);
	}

	/**
	 * Create an inductor with specified inductance and initial current
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param inductance Inductance in Henries
	 * @param initialCurrent Initial current in Amperes
	 * @param model Parent model
	 * @return Inductor instance
	 */
	public static Inductor create(String blockName, String blockPath, double inductance, double initialCurrent, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.InductorDto dto =
			com.ncslab.dto.block.specialized.circuit.element.InductorDto.create(blockName, blockPath, inductance, initialCurrent);
		return new Inductor(dto, model);
	}

	/**
	 * Create a 1mH inductor with default parameters
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return Inductor instance with 1mH inductance
	 */
	public static Inductor createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath, 1e-3, model);
	}
	
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.LinkOnly;
	}
	
	protected void setupBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		
		String c=paramValues.getString("l");
		JSONObject gainJSON=new JSONObject();
		gainJSON.put("blockType", "Gain");
		gainJSON.put("blockName", this.blockName+"_l");
		gainJSON.put("blockPath", this.blockPath);
		JSONObject gainParamValues=new JSONObject();
		gainParamValues.put("Gain", "(1.0/"+c+")");
		gainParamValues.put("Multiplication", "Element-wise(K.*u)");
		gainJSON.put("paramValues", gainParamValues);
		
		gain=new GainElect(gainJSON,this.model);
		this.blockList.add(gain);
		
		JSONObject integratorJSON=new JSONObject();
		integratorJSON.put("blockType", "Integrator");
		integratorJSON.put("blockName", this.blockName+"_Integrator");
		integratorJSON.put("blockPath", this.blockPath);
		JSONObject integratorParamValues=new JSONObject();
		integratorParamValues.put("InitialCondition", "0");
		integratorJSON.put("paramValues", integratorParamValues);
		
		integrator=new Integrator(integratorJSON,this.model);
		this.blockList.add(integrator);
		
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
		
		createLine(gain.getBlockName(), 1, integrator.getBlockName(), 1);
		createLine(add.getBlockName(), 1, gain.getBlockName(), 1);
		
		this.setOutputBlock(integrator);
		this.setupInputBlock(add);
	}
}
