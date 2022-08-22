package circuit.block.element;

import org.json.JSONObject;

import block.elect.Limiting;
import block.elect.LimitingLink;
import block.source.SineWave;
import circuit.block.BlockModeType;
import circuit.block.CircuitBlock;
import circuit.block.electblock.AddElect;
import circuit.block.electblock.DiodeElect;
import circuit.block.electblock.GainElect;
import circuit.block.electblock.ProductElect;
import circuit.block.io.BlockVoltage;
import circuit.block.io.PortCurrent;
import ncslablink.NCSLabModel;

public class VariableResistor extends CircuitBlock {
	
	private ProductElect product;
	private AddElect add;
	private Limiting limiting;
	private LimitingLink limitingLink;
	
	public VariableResistor(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		circuitPortList.get(0).setName("LConn2");
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
		//blockModeType=BlockModeType.Anything;
		blockModeType=BlockModeType.LinkOnly;
		//blockModeType=BlockModeType.BranchOnly;
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
