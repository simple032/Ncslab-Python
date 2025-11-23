package com.ncslab.circuit.block.element;

import org.json.JSONObject;

import com.ncslab.block.elect.Limiting;
import com.ncslab.block.elect.LimitingLink;
import com.ncslab.circuit.block.BlockModeType;
import com.ncslab.circuit.block.CircuitBlock;
import com.ncslab.circuit.block.electblock.AddElect;
import com.ncslab.circuit.block.electblock.ProductElect;
import com.ncslab.circuit.block.io.BlockVoltage;
import com.ncslab.circuit.block.io.PortCurrent;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.communication.CircuitBlockDto;
import com.ncslab.dto.block.specialized.circuit.element.VariableResistorDto;

/**
 * Variable Resistor circuit element block for electrical circuit simulation.
 * Resistance is controlled by an external input signal.
 * Can operate in both Branch mode and Link mode.
 */
public class VariableResistor extends CircuitBlock {

	private ProductElect product;
	private AddElect add;
	private Limiting limiting;
	private LimitingLink limitingLink;

	/**
	 * Legacy JSON Constructor - Creates VariableResistor block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing variable resistor configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public VariableResistor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		circuitPortList.get(0).setName("LConn2");
	}

	/**
	 * DTO-Native Constructor - Creates VariableResistor block directly from CircuitBlockDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto CircuitBlockDto containing variable resistor configuration
	 * @param model Parent model reference
	 */
	public VariableResistor(CircuitBlockDto dto, NCSLabModel model) {
		super(dto, model);
		circuitPortList.get(0).setName("LConn2");
		System.out.println("DTO-NATIVE: VariableResistor block created successfully - " + dto.getBlockName());
	}

	/**
	 * Factory method: Create variable resistor with specified minimum resistance
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param minimumResistance Minimum resistance in Ohms
	 * @param model Parent model
	 * @return VariableResistor instance
	 */
	public static VariableResistor create(String blockName, String blockPath,
	                                      double minimumResistance, NCSLabModel model) {
		VariableResistorDto dto = VariableResistorDto.create(blockName, blockPath, minimumResistance);
		return new VariableResistor(dto, model);
	}

	/**
	 * Factory method: Create variable resistor with default minimum resistance (1mΩ)
	 * @param blockName Block name
	 * @param blockPath Block path
	 * @param model Parent model
	 * @return VariableResistor instance
	 */
	public static VariableResistor createDefault(String blockName, String blockPath, NCSLabModel model) {
		return create(blockName, blockPath, VariableResistorDto.DEFAULT_MIN_RESISTANCE, model);
	}

	@Override
	protected void setupBlockList() {
		// TODO Auto-generated method stub
		switch(this.blockMode) {
		case Branch:
			setupBranchBlockList();
			break;
		case Link:
			setupLinkBlockList();
			break;
		}
	}

	@Override
	protected void setupBlockModeType() {
		// TODO Auto-generated method stub
		blockModeType=BlockModeType.Anything;
	}
	
	private void setupBranchBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"' as a branch");
		
		JSONObject ProcuctJSON=new JSONObject();
		ProcuctJSON.put("blockType", "Product");
		ProcuctJSON.put("blockName", this.blockName+"_R");
		ProcuctJSON.put("blockPath", this.blockPath);
		JSONObject productParamValues=new JSONObject();
		productParamValues.put("Inputs", "**");
		productParamValues.put("Multiplication", "Element-wise(.*)");
		ProcuctJSON.put("paramValues", productParamValues);
		
		product=new ProductElect(ProcuctJSON,this.model);
		this.blockList.add(product);
		
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
		
		JSONObject limitingJSON=new JSONObject();
		limitingJSON.put("blockType", "Limiting");
		limitingJSON.put("blockName", this.blockName+"_Limiting");
		limitingJSON.put("blockPath", this.blockPath);
		limitingJSON.put("paramValues", this.paramValues);
		
		limiting=new Limiting(limitingJSON,this.model);
		this.blockList.add(limiting);
		
		createLine(add.getBlockName(), 1, product.getBlockName(), 1);
		createLine(limiting.getBlockName(),1,product.getBlockName(),2);
		
		this.setOutputBlock(product);
		this.setupInputBlock(add);
		
		this.inputPortList.add(limiting.getInputPortList().get(0));
	}
	
	private void setupLinkBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"' as a link");
		
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
		JSONObject limitingJSON=new JSONObject();
		limitingJSON.put("blockType", "Limiting");
		limitingJSON.put("blockName", this.blockName+"_Limiting");
		limitingJSON.put("blockPath", this.blockPath);
		limitingJSON.put("paramValues", this.paramValues);
		
		limitingLink=new LimitingLink(limitingJSON,this.model);
		this.blockList.add(limitingLink);
		
		createLine(add.getBlockName(), 1, product.getBlockName(), 1);
		createLine(limitingLink.getBlockName(),1,product.getBlockName(),2);
		
		this.setOutputBlock(product);
		this.setupInputBlock(add);
		
		this.inputPortList.add(limitingLink.getInputPortList().get(0));
	}

}
