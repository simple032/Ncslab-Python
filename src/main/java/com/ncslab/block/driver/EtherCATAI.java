package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class EtherCATAI extends com.ncslab.block.Block {
    private static final String PARAM_SLAVE_ID = "SlaveID";
    private static final String PARAM_INTERFACE = "Interface";
    private static final String PARAM_TIME_SAMPLE = "timeSample";

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

        parameterList.add(slaveId);
        parameterList.add(interface_name);
        parameterList.add(timeSample);

    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        StringBuilder initCode = new StringBuilder();

        // 获取参数值
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        double sampleTime = paramValues.getDouble(PARAM_TIME_SAMPLE);

        // 定义接口名称
        code.addGlobalVariable(String.format("char %s_interface[] = \"%s\";\n",
            getBlockName(), paramValues.getString(PARAM_INTERFACE)));

        initCode.append(String.format("/*Code for initialization of block EtherCAT_AI:(%d)%s*/\n",
            getBlockId(), getBlockName()));

        // EtherCAT初始化
        initCode.append("if(ECAT_init_Flag==0){\n")
            //.append(String.format("  printf(\"Initializing EtherCAT with interface: %%s\\n\", %s_interface);\n", getBlockName()))
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

        // 使用 AnalogIOManager 创建和初始化输入设备
        initCode.append(String.format("AnalogIOManager::createAnalogInput(%d);\n", slaveIdValue))
            .append(String.format("auto* analogInput = AnalogIOManager::getAnalogInput(%d);\n", slaveIdValue))
            .append("if (!analogInput || !analogInput->init()) {\n")
            .append("    printf(\"Failed to initialize EtherCAT Analog Input\\n\");\n")
            .append("    exit(1);\n")
            .append("} else {\n")
            .append("    printf(\"EtherCAT Analog Input initialized successfully\\n\");\n")
            .append("}\n");

        code.addInitCode(initCode.toString());
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);

        StringBuilder outputCode = new StringBuilder();
        outputCode.append(String.format("/*Code for output of block EtherCAT_AI:(%d)%s*/\n",
            getBlockId(), getBlockName()));

        outputCode.append("if(model.majorStep==1){\n")
            .append("    double value = 0.0f;\n")
            .append(String.format("    auto* analogInput = AnalogIOManager::getAnalogInput(%d);\n", slaveIdValue))
            .append("    if (analogInput && analogInput->read(&value)) {\n")
            .append(String.format("        %s = value;\n", outputPortList.get(0).getOutputSignalC().getName()))
            .append("    }\n")
            .append("}\n");

        code.addOutputCode(outputCode.toString());
    }
}
