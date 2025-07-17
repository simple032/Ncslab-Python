package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class EtherCATAO extends com.ncslab.block.Block {
    private static final String PARAM_SLAVE_ID = "SlaveID";
    private static final String PARAM_INTERFACE = "Interface";
    private static final String PARAM_TIME_SAMPLE = "timeSample";
    
    public static final Vector<String> parameterNames = new Vector<>();
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        parameterNames.add(PARAM_SLAVE_ID);
        parameterNames.add(PARAM_INTERFACE);
        parameterNames.add(PARAM_TIME_SAMPLE);
        
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

    public EtherCATAO(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // 创建从站ID参数
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
            "\"" + interfaceValue + "\""  // 给字符串加上引号
        );

        // 创建采样时间参数
        timeSample = new Parameter(
            this,
            EtherCATParam.TIME_SAMPLE.id,
            EtherCATParam.TIME_SAMPLE.codeName,
            String.valueOf(paramValues.getDouble(PARAM_TIME_SAMPLE))
        );
        inputPortList.add(new InputPort(this, 1));

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

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/EtherCATAO/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        String inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();

        context.put("block", this);
        context.put("slaveId", slaveIdValue);
        context.put("inputSignal", inputSignal);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/EtherCATAO/output.vm", context);
        code.addOutputCode(codeStr);
    }
}
