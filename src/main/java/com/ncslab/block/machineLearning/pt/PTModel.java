package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

/**
 * pytorch-based models.
 */
public abstract class PTModel extends MachineLearning {
    protected static String PY_INIT = "Py_Initialize();";
    protected static String PY_INCLUDE = "\nPyRun_SimpleString(\"import sys\");\nPyRun_SimpleString(\"sys.path.append('./')\");\n";
    protected static String PY_FINALIZE = "Py_Finalize();\n";

    public PTModel(JSONObject jsonObject, NCSLabModel model) {
        super(jsonObject, model);
    }

    @Override
    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of pytorch based block:("+getBlockId()+")"+getBlockName()+"*/\n";
        code.addGlobalInitCode(PY_INIT);
        code.addGlobalInitCode(PY_INCLUDE);
        code.addIncludeCode("#include \"PTModel.hpp\"\n");
        code.addWrittenFile("../../ml/pt/PTModel.hpp", "PTModel.hpp");
        code.addInitCode(initCode);
    }
}
