package com.ncslab.block.config;

import com.ncslab.block.Block;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.ncslablink.MatDimException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Block (组态模块)
 * 
 * 这是一个纯前端功能块，不参与仿真计算。
 * 用于在画布中显示组态界面，绑定信号/参数进行可视化。
 */
public class Configuration extends Block {

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("ConfigurationData", "");
        PARAMETER_DEFAULTS = defaults;
    }

    public Configuration(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
    }

    public Configuration(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    public Configuration(com.ncslab.dto.block.specialized.config.ConfigurationDto blockDto, NCSLabModel model) {
        super(blockDto, model);
    }

    /**
     * Configuration 不参与 C 代码生成
     */
    @Override
    public void generateOutputCodeC(CodeStructC code) {
        // no-op: configuration block does not participate in simulation
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        // no-op
    }

    @Override
    public void generateUpdateCodeC(CodeStructC code) throws MatDimException {
        // no-op
    }

    @Override
    public void generateDerivativeCodeC(CodeStructC code) {
        // no-op
    }

    @Override
    public void generateDiscreteUpdateCodeC(CodeStructC code) throws MatDimException {
        // no-op
    }
}
