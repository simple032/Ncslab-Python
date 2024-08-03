package code.plc;

import ncslablink.ModelException;
import ncslablink.ModelMode;
import org.json.JSONObject;

public class CodeModelPLCRockwell extends CodeModelPLC{

    protected CodeModelPLCRockwell(JSONObject jsonIn, ModelMode mode) throws ModelException {
        super(jsonIn, mode);
    }
}
