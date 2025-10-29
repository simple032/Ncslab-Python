package com.ncslab.circuit.block.electblock;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.math.Product;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.dto.block.specialized.elect.ProductElectDto;
import com.ncslab.dto.block.specialized.math.ProductDto;

/**
 * ProductElect block extends Product to support electrical circuit algebraic loop resolution.
 *
 * This block provides the same functionality as Product but includes support for
 * algebraic loop detection and resolution in electrical circuit simulation.
 */
public class ProductElect extends Product implements ElectBlock {

	private String electLoopString;

	private List<Block> relatedBlockList = new ArrayList<>();

	/**
	 * DTO-Native Constructor - Creates ProductElect block directly from ProductElectDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto ProductElectDto containing block configuration
	 * @param model Parent model reference
	 */
	public ProductElect(ProductElectDto dto, NCSLabModel model) {
		super((ProductDto) dto, model);  // Call parent DTO constructor

		// ElectBlock-specific initialization
		if (dto.getElectLoopString() != null) {
			this.electLoopString = dto.getElectLoopString().getAsString();
		}
		// relatedBlockList will be populated during circuit analysis
	}

	/**
	 * Legacy JSON Constructor - Creates block from JSONObject.
	 * Maintained for backward compatibility with existing JSON-based workflows.
	 *
	 * @param blockJSON JSON object containing block configuration
	 * @param model Parent model reference
	 * @deprecated Use DTO-native constructor for new development
	 */
	@Deprecated
	public ProductElect(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		
		//return gain.getName();
		
		return inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
	}
	
	public void setElecLoopString(String electLoopString) {
		this.electLoopString=electLoopString;
	}
	
	public void setRelatedBlockList(List<Block> relatedBlockList) {
		this.relatedBlockList=relatedBlockList;
	}
	
	public List<Block> getRelatedBlockList(){
		return this.relatedBlockList;
	}
	
	public boolean isLoopPoint() {
		if(relatedBlockList.size()>0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void generateOutputCodeC(CodeStructC code) {
		
		if(isLoopPoint()) {
			String outputCode="/*Code for output of block Product:("+getBlockId()+")"+getBlockName()+" (Circuit Algebraic Loop Point) */\n";
			
			outputCode+=electLoopString+";\n";
			
			code.addOutputCode(outputCode);
		}
		else {
			super.generateOutputCodeC(code);
		}
		
		
	}
	
	@Override
	public void clearLoop() {
		// TODO Auto-generated method stub
		relatedBlockList=new ArrayList<>();
		electLoopString=null;
	}
}

