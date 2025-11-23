package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.continuous.Integrator;
import com.ncslab.block.elect.LimitingLink;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.electblock.AddElect;
import com.ncslab.circuit.block.electblock.ProductElect;
import com.ncslab.circuit.block.io.PortCurrent;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.communication.CircuitBlockDto;
import com.ncslab.dto.block.specialized.circuit.element.VariableCapacitorDto;

/**
 * Variable Capacitor circuit element block for electrical circuit simulation.
 * Capacitance is controlled by an external input signal.
 * Can only operate in Branch mode.
 */
public class VariableCapacitor extends CircuitBlock{

	private Integrator integrator;
	private LimitingLink limiting;
	private ProductElect product;
	private AddElect add;

	/**
	 * Legacy JSON Constructor - Creates VariableCapacitor block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing variable capacitor configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public VariableCapacitor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		circuitPortList.get(0).setName("LConn2");
	}

	/**
	 * DTO-Native Constructor - Creates VariableCapacitor block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing variable capacitor configuration
	 * @param model Parent model reference
	 */
	public VariableCapacitor(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		circuitPortList.get(0).setName("LConn2");
		System.out.println("DTO-NATIVE: VariableCapacitor block created successfully - " + dto.getBlockName());
	}

	/**
	 * Factory method: Create variable capacitor with specified parameters
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param minimumCapacitance Minimum capacitance in Farads
	 * @param initialVoltage Initial voltage in Volts
	 * @param model Parent model
	 * @return VariableCapacitor instance
	 */
	public static VariableCapacitor create(String blockName, String blockPath,
	                                       double minimumCapacitance, double initialVoltage,
	                                       NCSLabModel model) {
		VariableCapacitorDto dto = VariableCapacitorDto.create(
			blockName, blockPath, minimumCapacitance, initialVoltage);
		return new VariableCapacitor(dto, model);
	}

	/**
	 * Factory method: Create variable capacitor with default parameters (1pF min, 0V initial)
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return VariableCapacitor instance
	 */
	public static VariableCapacitor createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath,
			VariableCapacitorDto.DEFAULT_MIN_CAPACITANCE, 0.0, model);
	}

	//可变电容只能作为树枝存在
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.BranchOnly;
	}
	
	protected void setupBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
	
		String v0=paramValues.getString("v0");
		JSONObject integratorJSON=new JSONObject();
		integratorJSON.put("blockType", "Integrator");
		integratorJSON.put("blockName", this.blockName+"_Integrator");
		integratorJSON.put("blockPath", this.blockPath);
		JSONObject integratorParamValues=new JSONObject();
		integratorParamValues.put("InitialCondition", v0);
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
		
		//构建product
		JSONObject ProcuctJSON=new JSONObject();
		ProcuctJSON.put("blockType", "ProductElect");
		ProcuctJSON.put("blockName", this.blockName+"_Product");
		ProcuctJSON.put("blockPath", this.blockPath);
		JSONObject productParamValues=new JSONObject();
		productParamValues.put("Inputs", "**");
		productParamValues.put("Multiplication", "Element-wise(.*)");
		ProcuctJSON.put("paramValues", productParamValues);
		product=new ProductElect(ProcuctJSON,this.model);
		this.blockList.add(product);
		
		//构建电容限幅
		String cmin = paramValues.getString("Cmin");
		JSONObject limitingJSON=new JSONObject();
		limitingJSON.put("blockType", "LimitingLink");
		limitingJSON.put("blockName", this.blockName+"_Limiting");
		limitingJSON.put("blockPath", this.blockPath);
		JSONObject limitingParamValues=new JSONObject();
		limitingParamValues.put("Rmin", cmin);
		limitingJSON.put("paramValues", limitingParamValues);
		limiting=new LimitingLink(limitingJSON,this.model);
		this.blockList.add(limiting);
		
		//连接Block内部的线路
		createLine(product.getBlockName(), 1, integrator.getBlockName(), 1);
		createLine(add.getBlockName(), 1, product.getBlockName(), 1);
		createLine(limiting.getBlockName(), 1, product.getBlockName(), 2);
		
		this.setOutputBlock(integrator);
		this.setupInputBlock(add);
		this.inputPortList.add(limiting.getInputPortList().get(0));
	}
}
