package com.ncslab.block.route;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class From extends Block {
	@Getter
    private String tagName;



    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();


    static {

        outputNames.add("out1");
        parameterNames.add("GotoTag");
    }


	public From(JSONObject blockIn,NCSLabModel model) {
		super(blockIn,model);
		outputPortList.add(new OutputPort(this,1,false));
		tagName=paramValues.getString("GotoTag");
	}

}
