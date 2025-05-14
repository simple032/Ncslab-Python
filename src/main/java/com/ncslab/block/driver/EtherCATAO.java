package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.NCSLabModel;

public class EtherCATAO extends com.ncslab.block.Block {
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

        parameterList.add(slaveId);
        parameterList.add(interface_name);
        parameterList.add(timeSample);

        inputPortList.add(new InputPort(this, 1));

    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        StringBuilder initCode = new StringBuilder();

        initCode.append(String.format("/*Code for initialization of block EtherCAT_AO:(%d)%s*/\n",
            getBlockId(), getBlockName()));

        // 获取参数值
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        double sampleTime = paramValues.getDouble(PARAM_TIME_SAMPLE);

        // 定义接口名称
        code.addGlobalVariable(String.format("char %s_interface[] = \"%s\";\n",
            getBlockName(), paramValues.getString(PARAM_INTERFACE)));

        initCode.append("if(ECAT_init_Flag==0){\n")
            //.append(String.format("    printf(\"Initializing EtherCAT with interface: %%s\\n\", %s_interface);\n", getBlockName()))
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


        // 添加设备初始化代码
        initCode.append(String.format("AnalogIOManager::createAnalogOutput(%d);\n", slaveIdValue))
            .append(String.format("auto* analogOutput = AnalogIOManager::getAnalogOutput(%d);\n", slaveIdValue))
            .append("if (!analogOutput || !analogOutput->init()) {\n")
            .append("    printf(\"Failed to initialize EtherCAT Analog Output\\n\");\n")
            .append("    exit(1);\n")
            .append("} else {\n")
            .append("    printf(\"EtherCAT Analog Output initialized successfully\\n\");\n")
            .append("}\n");



        code.addInitCode(initCode.toString());
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);

        outputCode.append(String.format("/*Code for output of block EtherCAT_AO:(%d)%s*/\n",
            getBlockId(), getBlockName()));

        // 获取输入信号
        String inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();

        outputCode.append("if(model.majorStep==1){\n")
            .append(String.format("    double output_value = %s;\n", inputSignal))
            .append(String.format("    auto* analogOutput = AnalogIOManager::getAnalogOutput(%d);\n", slaveIdValue))
            .append("    analogOutput->write(output_value);\n")
            .append("}\n");

        code.addOutputCode(outputCode.toString());
    }


}
