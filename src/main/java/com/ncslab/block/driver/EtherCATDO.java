package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class EtherCATDO extends com.ncslab.block.Block {
    private static final String PARAM_SLAVE_ID = "SlaveID";
    private static final String PARAM_CHANNEL = "Channel";
    private static final String PARAM_INTERFACE = "Interface";
    private static final String PARAM_TIME_SAMPLE = "timeSample";

    private enum EtherCATParam {
        SLAVE_ID(1, "slave_id", PARAM_SLAVE_ID),
        CHANNEL(2, "channel", PARAM_CHANNEL),
        INTERFACE(3, "interface_name", PARAM_INTERFACE),
        TIME_SAMPLE(4, "time_sample", PARAM_TIME_SAMPLE);

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
    Parameter channel;
    Parameter interface_name;
    Parameter timeSample;

    public EtherCATDO(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // 创建从站ID参数
        slaveId = new Parameter(
            this,
            EtherCATParam.SLAVE_ID.id,
            EtherCATParam.SLAVE_ID.codeName,
            String.valueOf(paramValues.getInt(PARAM_SLAVE_ID))
        );

        // 创建通道参数
        channel = new Parameter(
            this,
            EtherCATParam.CHANNEL.id,
            EtherCATParam.CHANNEL.codeName,
            String.valueOf(paramValues.getInt(PARAM_CHANNEL))
        );

        // 创建接口名称参数
        String interfaceValue = paramValues.getString(PARAM_INTERFACE);
        interface_name = new Parameter(
            this,
            EtherCATParam.INTERFACE.id,
            EtherCATParam.INTERFACE.codeName,
            "\"" + interfaceValue + "\""
        );

        // 创建采样时间参数
        timeSample = new Parameter(
            this,
            EtherCATParam.TIME_SAMPLE.id,
            EtherCATParam.TIME_SAMPLE.codeName,
            String.valueOf(paramValues.getDouble(PARAM_TIME_SAMPLE))
        );

        parameterList.add(slaveId);
        parameterList.add(channel);
        parameterList.add(interface_name);
        parameterList.add(timeSample);

        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        StringBuilder initCode = new StringBuilder();

        // 获取参数值
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        int channelValue = paramValues.getInt(PARAM_CHANNEL);
        double sampleTime = paramValues.getDouble(PARAM_TIME_SAMPLE);

        // 定义接口名称
        code.addGlobalVariable(String.format("char %s_interface[] = \"%s\";\n",
            getBlockName(), paramValues.getString(PARAM_INTERFACE)));

        initCode.append("if(ECAT_init_Flag==0){\n")
            .append("    auto* ethercat = EtherCAT::getInstance();\n")
            .append(String.format("    ethercat->setSamplingPeriod(%f);\n", sampleTime))
            .append(String.format("    if (!ethercat->init(%s_interface)) {\n", getBlockName()))
            .append("        printf(\"Failed to initialize EtherCAT\\n\");\n")
            .append("        exit(1);\n")
            .append("    } else {\n")
            .append("        printf(\"EtherCAT initialized successfully\\n\");\n")
            .append("    }\n")
            .append("    ECAT_init_Flag=1;\n")
            .append("}\n");

        // 添加数字量输出设备初始化代码
        initCode.append(String.format("DigitalIOManager::createDigitalOutput(%d, %d);\n",
                slaveIdValue, channelValue))
            .append(String.format("auto* digitalOutput = DigitalIOManager::getDigitalOutput(%d, %d);\n",
                slaveIdValue, channelValue))
            .append("if (!digitalOutput || !digitalOutput->init()) {\n")
            .append("    printf(\"Failed to initialize EtherCAT Digital Output\\n\");\n")
            .append("    exit(1);\n")
            .append("} else {\n")
            .append("    printf(\"EtherCAT Digital Output initialized successfully\\n\");\n")
            .append("}\n");

        code.addInitCode(initCode.toString());
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        int channelValue = paramValues.getInt(PARAM_CHANNEL);

        // 获取输入信号
        String inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();

        outputCode.append("if(model.majorStep==1){\n")
            .append(String.format("    bool output_value = (%s > 0.5);\n", inputSignal))
            .append(String.format("    auto* digitalOutput = DigitalIOManager::getDigitalOutput(%d, %d);\n", slaveIdValue, channelValue))
            .append("    digitalOutput->write(output_value);\n")
            .append("}\n");

        code.addOutputCode(outputCode.toString());
    }
}
