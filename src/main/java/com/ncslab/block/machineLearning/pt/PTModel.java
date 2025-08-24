package com.ncslab.block.machineLearning.pt;

import com.ncslab.block.machineLearning.MachineLearning;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.machineLearning.pt.PTModelDto;

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

    /**
     * DTO-NATIVE Constructor - Creates A2C block directly from BlockDto DTO
     */
    public PTModel(PTModelDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: PTModel block created successfully - " + blockDto.getBlockName());
    }


    @Override
    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        
        // Use template-based generation instead of string concatenation
        com.ncslab.util.TemplateUtils.populateAllContext(context, this);
        context.put("pyInit", PY_INIT);
        context.put("pyInclude", PY_INCLUDE);
        
        String initCode = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/pt/PTModel/init.vm", context);
        
        code.addGlobalInitCode(PY_INIT);
        code.addGlobalInitCode(PY_INCLUDE);
        code.addIncludeCode("#include \"PTModel.hpp\"\n");
        code.addWrittenFile("../../ml/pt/PTModel.hpp", "PTModel.hpp");
        code.addInitCode(initCode);
    }
}
