package circuit.block.element;

import org.json.JSONObject;

import block.continuous.Integrator;
import block.math.Add;
import block.math.Gain;
import circuit.block.BlockModeType;
import circuit.block.CircuitBlock;
import circuit.block.io.BlockVoltage;
import circuit.block.io.CircuitPort;
import ncslablink.NCSLabModel;

public class Inductor extends CircuitBlock {
	private Integrator integrator;
	private Gain gain;
	private Add add;
	
	public Inductor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
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
		
		gain=new Gain(gainJSON,this.model);
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
		
		add=new Add(addJSON,this.model);
		this.blockList.add(add);
		
		createLine(gain.getBlockName(), 1, integrator.getBlockName(), 1);
		createLine(add.getBlockName(), 1, gain.getBlockName(), 1);
		
		this.setOutputBlock(integrator);
		this.setupInputBlock(add);
	}
}
