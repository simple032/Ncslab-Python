package com.ncslab.block.route;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.ncslablink.NCSLabModel;

public class From extends Block {
	private String tagName;


	public From(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		outputPortList.add(new OutputPort(this,1,false));
		tagName=paramValues.getString("GotoTag");
	}

	public String getTagName() {
		return this.tagName;
	}
}
