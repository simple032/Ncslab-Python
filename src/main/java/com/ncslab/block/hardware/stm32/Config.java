package com.ncslab.block.hardware.stm32;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.BlockJson;

import java.util.Map;
import java.util.HashMap;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.ArrayList;
import java.util.List;

public class Config extends com.ncslab.block.Block{

	Parameter ip;
	Parameter netmask;
	Parameter gateway;
	Parameter port;


    
    
    /**
     * DTO-NATIVE Constructor - Creates Config block directly from BlockJson DTO
     */
    public Config(BlockJson blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Config block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    
    public static final Map<String, String> PARAMETER_DEFAULTS = new HashMap<>();
    
    static {
        outputNames.add("out1");
        
        PARAMETER_DEFAULTS.put("ip_Stm32", "192.168.1.100");
        PARAMETER_DEFAULTS.put("netmask_Stm32", "255.255.255.0");
        PARAMETER_DEFAULTS.put("gateway_Stm32", "192.168.1.1");
        PARAMETER_DEFAULTS.put("monitorPort_Stm32", "8080");
    }
	public Config(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		outputPortList.add(new OutputPort(this,1,false));

		ip=new Parameter(this,1,"ip",paramValues.getString("ip_Stm32"));
		netmask=new Parameter(this,2,"netmask",paramValues.getString("netmask_Stm32"));
		gateway=new Parameter(this,3,"gateway",paramValues.getString("gateway_Stm32"));
		port=new Parameter(this,4,"port",paramValues.getString("monitorPort_Stm32"));
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		// No initialization code needed for STM32 Config block
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		// No output code needed for STM32 Config block
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);
		// No initialization code needed for STM32 Config block
	}

	public void generateOutputCodeC(CodeStructC code) {
		// No output code needed for STM32 Config block
	}
}
