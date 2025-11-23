package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.continuous.Integrator;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.circuit.block.io.PortCurrent;
import com.ncslab.dto.communication.CircuitBlockDto;

import com.ncslab.circuit.block.electblock.*;

/**
 * Capacitor circuit element block for electrical circuit simulation.
 * Can only operate in Branch mode (stores voltage, produces current).
 */
public class Capacitor extends CircuitBlock {

	private Integrator integrator;
	private GainElect gain;
	private AddElect add;

	/**
	 * Legacy JSON Constructor - Creates Capacitor block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing capacitor configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public Capacitor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}

	/**
	 * DTO-Native Constructor - Creates Capacitor block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing capacitor configuration
	 * @param model Parent model reference
	 */
	public Capacitor(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		System.out.println("DTO-NATIVE: Capacitor block created successfully - " + dto.getBlockName());
	}

	// === Static Factory Methods for Programmatic Creation ===

	/**
	 * Create a capacitor with specified capacitance value
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param capacitance Capacitance in Farads
	 * @param model Parent model
	 * @return Capacitor instance
	 */
	public static Capacitor create(String blockName, String blockPath, double capacitance, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.CapacitorDto dto =
			com.ncslab.dto.block.specialized.circuit.element.CapacitorDto.create(blockName, blockPath, capacitance);
		return new Capacitor(dto, model);
	}

	/**
	 * Create a capacitor with specified capacitance and initial voltage
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param capacitance Capacitance in Farads
	 * @param initialVoltage Initial voltage in Volts
	 * @param model Parent model
	 * @return Capacitor instance
	 */
	public static Capacitor create(String blockName, String blockPath, double capacitance, double initialVoltage, NCSLabModel model) {
		com.ncslab.dto.block.specialized.circuit.element.CapacitorDto dto =
			com.ncslab.dto.block.specialized.circuit.element.CapacitorDto.create(blockName, blockPath, capacitance, initialVoltage);
		return new Capacitor(dto, model);
	}

	/**
	 * Create a 1μF capacitor with default parameters
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return Capacitor instance with 1μF capacitance
	 */
	public static Capacitor createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath, 1e-6, model);
	}
	
	//电容只能时树枝
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.BranchOnly;
	}
	
	//设置add,gain和integrator
	protected void setupBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		
		String c=paramValues.getString("c");
		JSONObject gainJSON=new JSONObject();
		gainJSON.put("blockType", "Gain");
		gainJSON.put("blockName", this.blockName+"_c");
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
		
		//根据自己获得自己Block的电流
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
		
		//连接Block内部的线路
		createLine(gain.getBlockName(), 1, integrator.getBlockName(), 1);
		createLine(add.getBlockName(), 1, gain.getBlockName(), 1);
		
		this.setOutputBlock(integrator);
		this.setupInputBlock(add);
	}
}
