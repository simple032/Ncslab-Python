package com.ncslab.block.route;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class To extends Block {
	@Getter
    private String tagName;


    @Getter
    public static final Vector<String> inputNames = new Vector<>();
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    static {


        inputNames.add("in1");
        parameterNames.add("GotoTag");
    }
	public To(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		inputPortList.add(new InputPort(this,1));
		tagName=paramValues.getString("GotoTag");
		//System.out.println(tagName);
	}

}
