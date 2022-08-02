package circuit.block.element;

import org.json.JSONObject;

import circuit.block.BlockModeType;
import circuit.block.CircuitBlock;
import circuit.block.electblock.AddElect;
import circuit.block.electblock.DiodeElect;
import circuit.block.electblock.DiodeCurrentElect;
import circuit.block.io.BlockVoltage;
import circuit.block.io.PortCurrent;
import ncslablink.NCSLabModel;

public class Diode extends CircuitBlock {
	
	private DiodeElect diode;
	private DiodeCurrentElect diodeCurrent;
	private AddElect add;
	
	public Diode(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		
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
		blockModeType=BlockModeType.LinkOnly;
	}
	
	private void setupBranchBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"' as a branch");
		
		JSONObject diodeJSON=new JSONObject();
		diodeJSON.put("blockType", "Diode");
		diodeJSON.put("blockName", this.blockName+"_DiodeCurrent");
		diodeJSON.put("blockPath", this.blockPath);
		diodeJSON.put("paramValues", this.paramValues);
		
		diodeCurrent=new DiodeCurrentElect(diodeJSON,this.model);
		this.blockList.add(diodeCurrent);
		
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
		
		createLine(add.getBlockName(), 1, diodeCurrent.getBlockName(), 1);
		
		this.setOutputBlock(diodeCurrent);
		this.setupInputBlock(add);
	}
	
	private void setupLinkBlockList() {
		System.out.println("Setup equivilent blocks for '"+this.blockName+"' as a link");
		
		JSONObject diodeJSON=new JSONObject();
		diodeJSON.put("blockType", "Diode");
		diodeJSON.put("blockName", this.blockName+"_Diode");
		diodeJSON.put("blockPath", this.blockPath);
		diodeJSON.put("paramValues", this.paramValues);
		
		diode=new DiodeElect(diodeJSON,this.model);
		this.blockList.add(diode);
		
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
		
		createLine(add.getBlockName(), 1, diode.getBlockName(), 1);
		
		this.setOutputBlock(diode);
		this.setupInputBlock(add);
	}

}
