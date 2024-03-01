package com.ncslab.block.matrix;

import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

/**
 * no support for complex matrix
 */
public class IsHermitian extends Block {
    // TODO
    public IsHermitian(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
    }
}
