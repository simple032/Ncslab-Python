package com.ncslab.circuit.block.electblock;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.elect.DiodeCurrent;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.circuit.loop.CircuitLoopException;
import com.ncslab.dto.block.specialized.elect.DiodeCurrentElectDto;
import com.ncslab.dto.block.specialized.elect.DiodeCurrentDto;

/**
 * DiodeCurrentElect block extends DiodeCurrent to support electrical circuit algebraic loop resolution.
 *
 * This block provides the same functionality as DiodeCurrent but includes support for
 * algebraic loop detection and resolution in electrical circuit simulation.
 */
public class DiodeCurrentElect extends DiodeCurrent implements ElectBlock {

	private String electLoopString;

	private List<Block> relatedBlockList = new ArrayList<>();

	/**
	 * DTO-Native Constructor - Creates DiodeCurrentElect block directly from DiodeCurrentElectDto.
	 * Provides type-safe construction with comprehensive validation.
	 *
	 * @param dto DiodeCurrentElectDto containing block configuration
	 * @param model Parent model reference
	 */
	public DiodeCurrentElect(DiodeCurrentElectDto dto, NCSLabModel model) {
		super((DiodeCurrentDto) dto, model);  // Call parent DTO constructor

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
	public DiodeCurrentElect(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);
	}
	
	public String getGainBlock() throws CircuitLoopException {
		//String gainString="(("+this.getOutputPortVariable(0)+"!=0)?("+this.getOutputPortVariable(0)+"/"+this.getInputPortVariable(0)+"):0)\n";
		
		throw(new CircuitLoopException(""));
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
