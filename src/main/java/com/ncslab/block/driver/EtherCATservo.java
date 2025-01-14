package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class EtherCATservo extends com.ncslab.block.Block {
    private static final String PARAM_SLAVE_ID = "SlaveID";
    private static final String PARAM_INTERFACE = "Interface";
    private static final String PARAM_TIME_SAMPLE = "timeSample";
    private static final String PARAM_OPERATION_MODE = "operationMode";

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

        parameterList.add(slaveId);
        parameterList.add(interface_name);
        parameterList.add(timeSample);
        parameterList.add(operationMode);

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
        StringBuilder initCode = new StringBuilder();

        // 获取参数值
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        double sampleTime = paramValues.getDouble(PARAM_TIME_SAMPLE);
        int operationModeValue = paramValues.getInt(PARAM_OPERATION_MODE);

        // 定义接口名称
        code.addGlobalVariable(String.format("char %s_interface[] = \"%s\";\n",
            getBlockName(), paramValues.getString(PARAM_INTERFACE)));

        // EtherCAT初始化
        initCode.append("if(ECAT_init_Flag==0){\n")
            .append("    auto* ethercat = EtherCATSE::getInstance();\n")
            .append(String.format("    ethercat->setSamplingPeriod(%f);\n", sampleTime))
            //.append(String.format("    if (!ethercat->init(%s_interface)) {\n", getBlockName()))
            .append(String.format("    if (!ethercat->init(%s_interface, %d)) {\n", getBlockName(), operationModeValue))
            .append("        printf(\"Failed to initialize EtherCAT\\n\");\n")
            .append("        exit(1);\n")
            .append("    } else {\n")
            .append("        printf(\"EtherCAT initialized successfully\\n\");\n")
            .append("    }\n")
            .append("    ECAT_init_Flag=1;\n")
            .append("}\n");

        // 创建并初始化伺服对象
        initCode.append(String.format("ServoManager::createServo(%d);\n", slaveIdValue))
            .append(String.format("auto* servo = ServoManager::getServo(%d);\n", slaveIdValue))
            .append("if (!servo || !servo->init()) {\n")
            .append("    printf(\"Failed to initialize EtherCAT Servo\\n\");\n")
            .append("    exit(1);\n")
            .append("} else {\n")
            .append("    servo->enableServo();\n")
            .append("    printf(\"EtherCAT Servo initialized and enabled\\n\");\n")
            .append("}\n");

        code.addInitCode(initCode.toString());
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        int operationModeValue = paramValues.getInt(PARAM_OPERATION_MODE);

        // 获取输入输出信号名
        String targetValue = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        String actualValue = outputPortList.get(0).getOutputSignalC().getName();

        // 生成运行代码
        outputCode.append("if(model.majorStep==1){\n")
            .append(String.format("    auto* servo = ServoManager::getServo(%d);\n", slaveIdValue));

        // 根据不同模式生成相应的代码
        switch (operationModeValue) {
            case 1: // Position Mode
                outputCode.append(String.format("    servo->setPosition(%s);\n", targetValue))
                    .append("    int32_t current_position = 0;\n")
                    .append("    servo->readActualPosition(&current_position);\n")
                    .append(String.format("    %s = current_position;\n", actualValue));
                break;
            case 3: // Velocity Mode
                outputCode.append(String.format("    servo->setVelocity(%s);\n", targetValue))
                    .append("    int32_t current_velocity = 0;\n")
                    .append("    servo->readActualVelocity(&current_velocity);\n")
                    .append(String.format("    %s = current_velocity;\n", actualValue));
                break;
            case 4: // Torque Mode
                outputCode.append(String.format("    servo->setTorque(%s);\n", targetValue))
                    .append("    int32_t current_torque = 0;\n")
                    .append("    servo->readActualTorque(&current_torque);\n")
                    .append(String.format("    %s = current_torque;\n", actualValue));
                break;
        }

        outputCode.append("}\n");
        code.addOutputCode(outputCode.toString());
    }
}
