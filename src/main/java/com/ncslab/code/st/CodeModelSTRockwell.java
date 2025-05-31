package com.ncslab.code.plc;

import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;

public class CodeModelSTRockwell extends CodeModelST {

    protected CodeModelSTRockwell(JSONObject jsonIn, ModelMode mode) throws ModelException {
        super(jsonIn, mode);
    }
}
