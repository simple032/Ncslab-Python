package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class EtherCATservo extends com.ncslab.block.Block {
    private static final String PARAM_SLAVE_ID = "SlaveID";
    private static final String PARAM_INTERFACE = "Interface";
    private static final String PARAM_TIME_SAMPLE = "timeSample";
    private static final String PARAM_OPERATION_MODE = "operationMode";
    
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put(PARAM_SLAVE_ID, "1");
        PARAMETER_DEFAULTS.put(PARAM_INTERFACE, "eth0");
        PARAMETER_DEFAULTS.put(PARAM_TIME_SAMPLE, "0.001");
        PARAMETER_DEFAULTS.put(PARAM_OPERATION_MODE, "1"); // Position mode
    }

    private enum ServoParam {
        SLAVE_ID(1, "slave_id", PARAM_SLAVE_ID),
        INTERFACE(2, "interface_name", PARAM_INTERFACE),
        TIME_SAMPLE(3, "time_sample", PARAM_TIME_SAMPLE),
        OPERATION_MODE(4, "operation_mode", PARAM_OPERATION_MODE);

        final int id;
        final String codeName;
        final String jsonKey;

        ServoParam(int id, String codeName, String jsonKey) {
            this.id = id;
            this.codeName = codeName;
            this.jsonKey = jsonKey;
        }
    }

    Parameter slaveId;
    Parameter interface_name;
    Parameter timeSample;
    Parameter operationMode;

    public EtherCATservo(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // 创建参数
        slaveId = new Parameter(
            this,
            ServoParam.SLAVE_ID.id,
            ServoParam.SLAVE_ID.codeName,
            String.valueOf(paramValues.getInt(PARAM_SLAVE_ID))
        );

        String interfaceValue = paramValues.getString(PARAM_INTERFACE);
        interface_name = new Parameter(
            this,
            ServoParam.INTERFACE.id,
            ServoParam.INTERFACE.codeName,
            "\"" + interfaceValue + "\""
        );

        timeSample = new Parameter(
            this,
            ServoParam.TIME_SAMPLE.id,
            ServoParam.TIME_SAMPLE.codeName,
            String.valueOf(paramValues.getDouble(PARAM_TIME_SAMPLE))
        );

        operationMode = new Parameter(
            this,
            ServoParam.OPERATION_MODE.id,
            ServoParam.OPERATION_MODE.codeName,
            String.valueOf(paramValues.getInt(PARAM_OPERATION_MODE))
        );
        // 根据模式设置输入输出端口
        setupPortsForMode(paramValues.getInt(PARAM_OPERATION_MODE));
    }

    private void setupPortsForMode(int mode) {
        inputPortList.clear();
        outputPortList.clear();

        switch (mode) {
            case 1: // Position Mode
                inputPortList.add(new InputPort(this, 1));  // 期望位置输入
                outputPortList.add(new OutputPort(this, 1)); // 实际位置输出
                break;
            case 3: // Velocity Mode
                inputPortList.add(new InputPort(this, 1));  // 期望速度输入
                outputPortList.add(new OutputPort(this, 1)); // 实际速度输出
                break;
            case 4: // Torque Mode
                inputPortList.add(new InputPort(this, 1));  // 期望转矩输入
                outputPortList.add(new OutputPort(this, 1)); // 实际转矩输出
                break;
        }
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // 获取参数值
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        double sampleTime = paramValues.getDouble(PARAM_TIME_SAMPLE);
        int operationModeValue = paramValues.getInt(PARAM_OPERATION_MODE);

        // 定义接口名称
        code.addGlobalVariable(String.format("char %s_interface[] = \"%s\";\n",
            getBlockName(), paramValues.getString(PARAM_INTERFACE)));

        context.put("block", this);
        context.put("slaveId", slaveIdValue);
        context.put("sampleTime", sampleTime);
        context.put("operationMode", operationModeValue);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/EtherCATservo/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        int operationModeValue = paramValues.getInt(PARAM_OPERATION_MODE);

        // 获取输入输出信号名
        String targetValue = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        String actualValue = outputPortList.get(0).getOutputSignalC().getName();

        context.put("block", this);
        context.put("slaveId", slaveIdValue);
        context.put("operationMode", operationModeValue);
        context.put("targetValue", targetValue);
        context.put("actualValue", actualValue);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/EtherCATservo/output.vm", context);
        code.addOutputCode(codeStr);
    }
}
