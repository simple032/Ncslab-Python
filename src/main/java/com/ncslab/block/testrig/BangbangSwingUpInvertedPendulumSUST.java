package com.ncslab.block.testrig;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class BangbangSwingUpInvertedPendulumSUST extends Block {


	private String name = "BangbangSwingUpInvertedPendulumSUST";
	Parameter v,vel;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("AccOutput");
        outputNames.add("SpeedOutput");
        inputNames.add("in1");
        inputNames.add("in2");
        inputNames.add("in3");
        inputNames.add("in4");
        inputNames.add("in5");
        parameterNames.add("v");
        parameterNames.add("vel");
    }


	public BangbangSwingUpInvertedPendulumSUST(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));//	Exe Flag
		inputPortList.add(new InputPort(this,2));//Angle (degree)
		inputPortList.add(new InputPort(this,3));//dif-Angle
		inputPortList.add(new InputPort(this,4));//v
		inputPortList.add(new InputPort(this,5));//vel
		outputPortList.add(new OutputPort(this,"AccOutput",1,false));
		outputPortList.add(new OutputPort(this,"SpeedOutput",2,false));


		v=new Parameter(this,parameterList.size()+1,"v",paramValues.getString("v"));
		parameterList.add(v);

		vel=new Parameter(this,parameterList.size()+1,"vel",paramValues.getString("vel"));
		parameterList.add(vel);

		//pumpState=new State(this,1,"pumpState");
		//stateList.add(pumpState);
		//levelState=new State(this,2,"levelState");
		//stateList.add(levelState);
	}




	public void generateIncludeCodeC(CodeStructC code) {
		String includeCode="/*Code for include files of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
//		code.addIncludeCode(includeCode);
	}

	public void addLine(String originCode, String newLine) {

	}

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";

		outputCode+="float v=0,acc=0;\n"
				+ "float Angle = "+this.getInputPortVariable(1)+"*3.1415926/180.0;\n"
				+ "float Dif_Angle = "+this.getInputPortVariable(2)+"*3.1415926/180.0;\n"

				+ "if(Angle==0&&Dif_Angle==0){v=1;velbangbang"+getBlockId()+"=200;}\n"
				+ "if(Angle*Dif_Angle<=0&&Angle<0){v=-"+this.getInputPortVariable(3)+";velbangbang"+getBlockId()+"=-"+this.getInputPortVariable(4)+";}\n"
				+ "if(Angle*Dif_Angle<=0&&Angle>0){v="+this.getInputPortVariable(3)+";velbangbang"+getBlockId()+"="+this.getInputPortVariable(4)+";}\n"
				+ "if(Angle*Dif_Angle>=0&&Angle>0){v=0.01;}\n"
				+ "if(Angle*Dif_Angle>=0&&Angle<0){v=0.01;}\n"


				+ "acc = fabs(v /0.09424776);\n"

				+ "int acc1=(int)acc;\n"
				+ "int vel1=(int)velbangbang"+getBlockId()+";\n"
				;



		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=v*100;\n";
		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=vel1;\n";

		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";


		code.addDerivativeCode(derivativeCode);
	}

	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		statementCode +="float velbangbang"+getBlockId()+"=0;\n";
		code.addStatementCode(statementCode);
	}
}
