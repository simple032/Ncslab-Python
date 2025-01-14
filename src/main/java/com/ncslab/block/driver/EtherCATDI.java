package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import java.util.Vector;

public class EtherCATDI extends com.ncslab.block.Block {
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

    public EtherCATDI(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        // 添加一个值输出端口
        outputPortList.add(new OutputPort(this, 1, false));

        // 使用前端传入的参数
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

        initCode.append(String.format("/*Code for initialization of block EtherCAT_DI:(%d)%s*/\n",
            getBlockId(), getBlockName()));

        // EtherCAT初始化
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

        // 使用 DigitalIOManager 创建和初始化输入设备
        initCode.append(String.format("DigitalIOManager::createDigitalInput(%d, %d);\n",
            slaveIdValue, channelValue))
            .append(String.format("auto* digitalInput = DigitalIOManager::getDigitalInput(%d, %d);\n",
                slaveIdValue, channelValue))
            .append("if (!digitalInput || !digitalInput->init()) {\n")
            .append("    printf(\"Failed to initialize EtherCAT Digital Input\\n\");\n")
            .append("    exit(1);\n")
            .append("} else {\n")
            .append("    printf(\"EtherCAT Digital Input initialized successfully\\n\");\n")
            .append("}\n");

        code.addInitCode(initCode.toString());
    }

    @Override
//    public void generateOutputCodeC(CodeStructC code) {
//        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
//        int channelValue = paramValues.getInt(PARAM_CHANNEL);
//
//        StringBuilder outputCode = new StringBuilder();
//        outputCode.append(String.format("/*Code for output of block EtherCAT_DI:(%d)%s*/\n",
//            getBlockId(), getBlockName()));
//
//        outputCode.append("if(model.majorStep==1){\n")
//            .append("    bool value = false;\n")
//            .append(String.format("    auto* digitalInput = DigitalIOManager::getDigitalInput(%d, %d);\n",
//                slaveIdValue, channelValue))
//            .append("    if (digitalInput && digitalInput->read(&value)) {\n")
//            .append(String.format("        %s = value ? 1.0 : 0.0;\n",
//                outputPortList.get(0).getOutputSignalC().getName()))
//            .append("    }\n")
//            .append("}\n");
//
//        code.addOutputCode(outputCode.toString());
//    }

    public void generateOutputCodeC(CodeStructC code) {
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        int channelValue = paramValues.getInt(PARAM_CHANNEL);

        StringBuilder outputCode = new StringBuilder();
        outputCode.append(String.format("/*Code for output of block EtherCAT_DI:(%d)%s*/\n",
            getBlockId(), getBlockName()));

        outputCode.append("if(model.majorStep==1){\n")
            .append("    bool value = false;\n")
            .append(String.format("    auto* digitalInput = DigitalIOManager::getDigitalInput(%d, %d);\n",
                slaveIdValue, channelValue))
            .append("    if (!digitalInput) {\n")  // 添加检查 digitalInput 是否有效
            .append("        printf(\"Failed to get digital input\\n\");\n")
            .append("    }\n")
            .append("    else {\n")  // 如果 digitalInput 存在，则进行 read 操作
            //.append("        printf(\"Calling read function...\\n\");\n")  // 在调用 read 前打印
            .append("        if (digitalInput->read(&value)) {\n")
            //.append("            printf(\"Read successful, value: %d\\n\", value);  // 打印读取到的值\n")
            .append(String.format("            %s = value ? 1.0 : 0.0;\n",
                outputPortList.get(0).getOutputSignalC().getName()))
            .append("        } else {\n")
            .append("            printf(\"Read failed\\n\");\n")
            .append("        }\n")
            .append("    }\n")
            .append("}\n");

        code.addOutputCode(outputCode.toString());
    }


}
