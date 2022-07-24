package circuit.block.element;

import org.json.JSONObject;

import circuit.block.BlockModeType;
import circuit.block.CircuitBlock;
import ncslablink.NCSLabModel;
import block.source.Constant;

public class DCVoltageSource extends CircuitBlock {
	
	private Constant dcVoltageSource;
	
	public DCVoltageSource(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//this.blockName=this.blockName.replaceAll(" ", "_");
		//setupEquivilentBlockModels();
		
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
