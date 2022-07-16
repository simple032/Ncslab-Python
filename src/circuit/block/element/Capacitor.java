package circuit.block.element;

import org.json.JSONObject;

import block.continuous.Integrator;
import block.math.Gain;
import block.source.Constant;
import block.math.Add;
import circuit.block.BlockModeType;
import circuit.block.CircuitBlock;
import ncslablink.NCSLabModel;

import line.Line;

public class Capacitor extends CircuitBlock {
	
	private Integrator integrator;
	private Gain gain;
	private Add add;
	
	public Capacitor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	
		
	}
	
	protected void setupBlockList() {
		setupEquivilentBlockModels();
	}
	
	protected void setupBlockModeType() {
		blockModeType=BlockModeType.BranchOnly;
	}
	
	private void setupEquivilentBlockModels() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"'");
		
		String c=paramValues.getString("c");
		JSONObject gainJSON=new JSONObject();
		gainJSON.put("blockType", "Gain");
		gainJSON.put("blockName", this.blockName+"_c");
		gainJSON.put("blockPath", this.blockPath);
		JSONObject gainParamValues=new JSONObject();
		gainParamValues.put("Gain", c);
		gainParamValues.put("Multiplication", "Element-wise(K.*u)");
		gainJSON.put("paramValues", gainParamValues);
		
		gain=new Gain(gainJSON,this.model);
		this.blockList.add(gain);
		
		JSONObject integratorJSON=new JSONObject();
		integratorJSON.put("blockType", "Integrator");
		integratorJSON.put("blockName", this.blockName+"_integrator");
		integratorJSON.put("blockPath", this.blockPath);
		JSONObject integratorParamValues=new JSONObject();
		integratorParamValues.put("InitialCondition", "0");
		integratorJSON.put("paramValues", integratorParamValues);
		
		integrator=new Integrator(integratorJSON,this.model);
		this.blockList.add(integrator);
		
		createLine(gain.getBlockName(), 1, integrator.getBlockName(), 1);
	}
}
