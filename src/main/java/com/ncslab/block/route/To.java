package com.ncslab.block.route;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.NCSLabModel;

public class To extends Block {
	private String tagName;
	public To(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		tagName=paramValues.getString("GotoTag");
		//System.out.println(tagName);
	}

	public String getTagName() {
		return this.tagName;
	}
}
