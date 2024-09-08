package com.ncslab.code.plc;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;

public class CodeModelPLCRockwell extends CodeModelPLC{

    protected CodeModelPLCRockwell(JSONObject jsonIn, ModelMode mode) throws ModelException {
        super(jsonIn, mode);
    }
}
