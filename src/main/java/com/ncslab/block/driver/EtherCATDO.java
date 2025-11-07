package com.ncslab.block.driver;

import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.driver.EtherCATDODto;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import java.util.HashMap;
import java.util.Map;

public class EtherCATDO extends com.ncslab.block.Block {
    private static final String PARAM_SLAVE_ID = "SlaveID";
    private static final String PARAM_CHANNEL = "Channel";
    private static final String PARAM_INTERFACE = "Interface";
    private static final String PARAM_TIME_SAMPLE = "timeSample";
    
    
    
    /**
     * DTO-NATIVE Constructor - Creates EtherCATDO block directly from BlockDto DTO
     */
    public EtherCATDO(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: EtherCATDO block created successfully - " + blockDto.getBlockName());
    }


    
    public static final Map<String, String> PARAMETER_DEFAULTS;
    
    static {
        
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put(PARAM_SLAVE_ID, "1");
        PARAMETER_DEFAULTS.put(PARAM_CHANNEL, "0");
        PARAMETER_DEFAULTS.put(PARAM_INTERFACE, "eth0");
        PARAMETER_DEFAULTS.put(PARAM_TIME_SAMPLE, "0.001");
    }

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
        inputPortList.add(new InputPort(this, 1));
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);

        // 获取参数值
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        int channelValue = paramValues.getInt(PARAM_CHANNEL);
        double sampleTime = paramValues.getDouble(PARAM_TIME_SAMPLE);

        // 定义接口名称
        code.addGlobalVariable(String.format("char %s_interface[] = \"%s\";\n",
            getBlockName(), paramValues.getString(PARAM_INTERFACE)));

        context.put("block", this);
        context.put("slaveId", slaveIdValue);
        context.put("channel", channelValue);
        context.put("sampleTime", sampleTime);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/EtherCATDO/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code) {
        super.generateOutputCodeC(code);
        
        int slaveIdValue = paramValues.getInt(PARAM_SLAVE_ID);
        int channelValue = paramValues.getInt(PARAM_CHANNEL);

        // 获取输入信号
        String inputSignal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();

        context.put("block", this);
        context.put("slaveId", slaveIdValue);
        context.put("channel", channelValue);
        context.put("inputSignal", inputSignal);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/driver/EtherCATDO/output.vm", context);
        code.addOutputCode(codeStr);
    }
}
