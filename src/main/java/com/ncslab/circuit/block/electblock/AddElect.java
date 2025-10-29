package com.ncslab.circuit.block.electblock;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.Block;
import com.ncslab.block.math.Add;
import com.ncslab.dto.block.specialized.elect.AddElectDto;
import com.ncslab.dto.block.specialized.math.AddDto;

/**
 * AddElect block extends Add to support electrical circuit algebraic loop resolution.
 *
 * This block provides the same functionality as Add but includes support for
 * algebraic loop detection and resolution in electrical circuit simulation.
 */
public class AddElect extends Add implements ElectBlock {

	private String electLoopString;

	private List<Block> relatedBlockList = new ArrayList<>();

	/**
	 * DTO-Native Constructor - Creates AddElect block directly from AddElectDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto AddElectDto containing block configuration
	 * @param model Parent model reference
	 */
	public AddElect(AddElectDto dto, NCSLabModel model) {
		super((AddDto) dto, model);  // Call parent DTO constructor

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
	public AddElect(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() {
		return "1";
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

	@Override
	public void clearLoop() {
		// TODO Auto-generated method stub
		relatedBlockList=new ArrayList<>();
		electLoopString=null;
	}
}
