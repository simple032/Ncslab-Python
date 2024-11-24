package com.ncslab.block.route;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class To extends Block {
	private String tagName;


    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {


        inputNames.add("in1");
    }
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
