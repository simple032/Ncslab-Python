package com.ncslab.circuit.block.electblock;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.math.Gain;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.Block;
import com.ncslab.dto.block.specialized.elect.GainElectDto;
import com.ncslab.dto.block.specialized.math.GainDto;

/**
 * GainElect block extends Gain to support electrical circuit algebraic loop resolution.
 *
 * This block provides the same functionality as Gain but includes support for
 * algebraic loop detection and resolution in electrical circuit simulation.
 */
public class GainElect extends Gain implements ElectBlock {
	private String electLoopString;

	private List<Block> relatedBlockList = new ArrayList<>();

	/**
	 * DTO-Native Constructor - Creates GainElect block directly from GainElectDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto GainElectDto containing block configuration
	 * @param model Parent model reference
	 */
	public GainElect(GainElectDto dto, NCSLabModel model) {
		super((GainDto) dto, model);  // Call parent DTO constructor

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
	public GainElect(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		return gain.getName();
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
			String outputCode="/*Code for output of block Constant:("+getBlockId()+")"+getBlockName()+" (Circuit Algebraic Loop Point) */\n";
			
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
