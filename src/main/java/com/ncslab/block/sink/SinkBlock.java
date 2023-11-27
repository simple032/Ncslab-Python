package com.ncslab.block.sink;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.ncslablink.NCSLabModel;

public class SinkBlock extends Block {
	public SinkBlock(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);
	}
}
