package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.dto.BlockJson;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class EtherCATAI extends com.ncslab.block.Block {
    private static final String PARAM_SLAVE_ID = "SlaveID";
    private static final String PARAM_INTERFACE = "Interface";
    private static final String PARAM_TIME_SAMPLE = "timeSample";
    
    
    
    /**
     * DTO-NATIVE Constructor - Creates EtherCATAI block directly from BlockJson DTO
     */
    public EtherCATAI(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: EtherCATAI block created successfully - " + blockDto.getBlockName());
    }


    
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put(PARAM_SLAVE_ID, "1");
        PARAMETER_DEFAULTS.put(PARAM_INTERFACE, "eth0");
        PARAMETER_DEFAULTS.put(PARAM_TIME_SAMPLE, "0.001");
    }

    private enum EtherCATParam {
        SLAVE_ID(1, "slave_id", PARAM_SLAVE_ID),
        INTERFACE(2, "interface_name", PARAM_INTERFACE),
        TIME_SAMPLE(3, "time_sample", PARAM_TIME_SAMPLE);

        final int id;
        final String codeName;
        final String jsonKey;

        EtherCATParam(int id, String codeName, String jsonKey) {
            this.id = id;
            this.codeName = codeName;
            this.jsonKey = jsonKey;
        }
    }

    Parameter slaveId;
    Parameter interface_name;
    Parameter timeSample;

    public EtherCATAI(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // 只添加一个值输出端口
        outputPortList.add(new OutputPort(this, 1, false));

        // 使用前端传入的参数
        slaveId = new Parameter(
            this,
            EtherCATParam.SLAVE_ID.id,
            EtherCATParam.SLAVE_ID.codeName,
            String.valueOf(paramValues.getInt(PARAM_SLAVE_ID))
        );

        // 创建接口名称参数
        String interfaceValue = paramValues.getString(PARAM_INTERFACE);
        interface_name = new Parameter(
            this,
            EtherCATParam.INTERFACE.id,
            EtherCATParam.INTERFACE.codeName,
            "\"" + interfaceValue + "\""
        );

        timeSample = new Parameter(
            this,
            EtherCATParam.TIME_SAMPLE.id,
            EtherCATParam.TIME_SAMPLE.codeName,
            String.valueOf(paramValues.getDouble(PARAM_TIME_SAMPLE))
        );
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // 获取参数值
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        double sampleTime = paramValues.getDouble(PARAM_TIME_SAMPLE);

        // 定义接口名称
        code.addGlobalVariable(String.format("char %s_interface[] = \"%s\";\n",
            getBlockName(), paramValues.getString(PARAM_INTERFACE)));

        context.put("block", this);
        context.put("slaveId", slaveIdValue);
        context.put("sampleTime", sampleTime);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/EtherCATAI/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);

        context.put("block", this);
        context.put("slaveId", slaveIdValue);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/EtherCATAI/output.vm", context);
        code.addOutputCode(codeStr);
    }
}
