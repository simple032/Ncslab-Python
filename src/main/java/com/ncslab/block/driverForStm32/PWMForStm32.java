package com.ncslab.block.driverForStm32;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class PWMForStm32 extends com.ncslab.block.Block{

	Parameter channel;
	Parameter timx;
	Parameter frequency;


    @Getter
    public static final Vector<String> parameterNames = new Vector<>();


    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        parameterNames.add("channel");
        parameterNames.add("timx");
        parameterNames.add("frequency");
        inputNames.add("in1");
    }
	public PWMForStm32(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ������
		inputPortList.add(new InputPort(this,1));

		channel=new Parameter(this,1,"channel",paramValues.getString("PWMForStm32Channel"));
		parameterList.add(channel);
		timx=new Parameter(this,2,"timx",paramValues.getString("PWMForStm32TIM"));
		parameterList.add(timx);
		frequency=new Parameter(this,3,"frequency",Integer.toString(paramValues.getInt("PWMForStm32Frequency")));
		parameterList.add(frequency);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);

		String initCode="";

//		initCode+=channel.getName()+"="+paramValues.getInt("PWMForStm32Channel")+";\n";
//
//		initCode+="TIM3_PWM_Init(2*1000000*STEP_SIZE-1,100-1,"+paramValues.getInt("PWMForStm32Channel")+");";

		code.addInitCode(initCode);
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";

		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+channel.getName()+";\n";

		code.addOutputCode(outputCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		String initCode="/*Code for initialization of block PWMForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";

		initCode+=timx.getName()+"="+paramValues.getInt("PWMForStm32TIM")+";\n";
		initCode+=channel.getName()+"="+paramValues.getInt("PWMForStm32Channel")+";\n";
		initCode+=frequency.getName()+"="+paramValues.getInt("PWMForStm32Frequency")+";\n";

		initCode+="if("+frequency.getName()+">=16&&"+frequency.getName()+"<=1000000){\n"
				+"TIM"+paramValues.getInt("PWMForStm32TIM")+"_PWM_Init(200-1,(int)(1000000.0/"+frequency.getName()+")-1,"+paramValues.getInt("PWMForStm32Channel")+");\n}else{\n";
		initCode+=""
				+ "if(STEP_SIZE<=0.0032768) TIM"+paramValues.getInt("PWMForStm32TIM")+"_PWM_Init(2*10000000*STEP_SIZE-1,10-1,"+paramValues.getInt("PWMForStm32Channel")+");\n"
				+ "else if(STEP_SIZE<=0.032768) TIM"+paramValues.getInt("PWMForStm32TIM")+"_PWM_Init(2*1000000*STEP_SIZE-1,100-1,"+paramValues.getInt("PWMForStm32Channel")+");\n"
				+ "else if(STEP_SIZE<=0.32768) TIM"+paramValues.getInt("PWMForStm32TIM")+"_PWM_Init(2*100000*STEP_SIZE-1,1000-1,"+paramValues.getInt("PWMForStm32Channel")+");\n"
				+ "else if(STEP_SIZE<=3.2768) TIM"+paramValues.getInt("PWMForStm32TIM")+"_PWM_Init(2*10000*STEP_SIZE-1,10000-1,"+paramValues.getInt("PWMForStm32Channel")+");\n"
				+ "else TIM"+paramValues.getInt("PWMForStm32TIM")+"_PWM_Init(2*10000-1,10000-1,"+paramValues.getInt("PWMForStm32Channel")+");\n";
		initCode+="}\n";
		code.addInitCode(initCode);
	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block PWMForStm32:("+getBlockId()+")"+getBlockName()+"*/\n";

		outputCode+="if(model.majorStep==1){\n"
				  +"int pwmduty = 0;\n"
				  +"pwmduty = " + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + "<=100?" + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ":100;\n"
				  +"pwmduty = pwmduty>=0?pwmduty:0;\n"
				  +"pwm_set("+ paramValues.getInt("PWMForStm32TIM") +","+ paramValues.getInt("PWMForStm32Channel") +",pwmduty,"+frequency.getName()+");\n"
				  //+"pwmWrite("+ paramValues.getDouble("port") +","+ inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() +");\n"
				  +"}\n";

		code.addOutputCode(outputCode);
	}
}
