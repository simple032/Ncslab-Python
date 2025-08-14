package com.ncslab.block.sink;

import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import com.ncslab.block.Block;
import com.ncslab.ncslablink.NCSLabModel;

public class SinkBlock extends Block {
	public SinkBlock(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);
	}
	
	/**
	 * DTO-NATIVE Constructor for SinkBlock - Uses DTO directly without conversion
	 */
	public SinkBlock(BlockJson blockDto, NCSLabModel model) {
		super(blockDto, model);
	}
}
