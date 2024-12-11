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

public class EnergySwingUpInvertedPendulumSUST extends Block {


	private String name = "EnergySwingUpInvertedPendulumSUST";
	Parameter InnerFactor,InitSpeed;

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
        inputNames.add("in6");
        parameterNames.add("InnerFactor");
        parameterNames.add("InitSpeed");
    }


	public EnergySwingUpInvertedPendulumSUST(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);

		//һ�����룬�������
		inputPortList.add(new InputPort(this,1));//	Exe Flag
		inputPortList.add(new InputPort(this,2));//Angle (degree)
		inputPortList.add(new InputPort(this,3));//dif-Angle
		inputPortList.add(new InputPort(this,4));//xishu
		inputPortList.add(new InputPort(this,5));//Invel
		inputPortList.add(new InputPort(this,6));//xishu
		outputPortList.add(new OutputPort(this,"AccOutput",1,false));
		outputPortList.add(new OutputPort(this,"SpeedOutput",2,false));


		InnerFactor=new Parameter(this,parameterList.size()+1,"InnerFactor",paramValues.getString("InnerFactor"));
		parameterList.add(InnerFactor);

		InitSpeed=new Parameter(this,parameterList.size()+1,"InitSpeed",paramValues.getString("InitSpeed"));
		parameterList.add(InitSpeed);

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

		outputCode+="float Energy=0,FlagSign=0,v=0,vel=0,acc=0;\n"
				+ "float Angle = "+this.getInputPortVariable(1)+";\n"
				+ "float Dif_Angle = "+this.getInputPortVariable(2)+"*3.1415926/180.0;\n"
				+ "Energy = 0.5*0.0004961*Dif_Angle*Dif_Angle + "+this.getInputPortVariable(5)+"*0.482*9.81*0.11*(cos(Angle)-1.0);\n"//0.382->0.082
				+ "FlagSign= Energy* Dif_Angle * cos(Angle);\n"
				+ "if(FlagSign<0){\n"
//				+ "v="+paramValues.getDouble("InnerFactor")+"*fabs(Energy);\n"
//				+ "vel="+paramValues.getDouble("InitSpeed")+";\n"
						+ "v="+this.getInputPortVariable(3)+"*fabs(Energy);\n"
						+ "vel="+this.getInputPortVariable(4)+";\n"
				+ "}else if(FlagSign > 0){\n"
//				+ "v = -1*"+paramValues.getDouble("InnerFactor")+" *fabs(Energy);\n"
//				+ "vel=-1*"+paramValues.getDouble("InitSpeed")+";\n"
						+ "v=-1*"+this.getInputPortVariable(3)+"*fabs(Energy);\n"
						+ "vel=-1*"+this.getInputPortVariable(4)+";\n"
				+ "}else{\n"
				+ "v=0.001;\n"
//				+ "vel="+paramValues.getDouble("InitSpeed")+";\n"
						+ "vel=0*"+this.getInputPortVariable(4)+";\n"
				+ "}\n"

				+ "acc = fabs(v /0.09424776);\n"

				+ "int acc1=(int)acc;\n"
				+ "int vel1=(int)vel;\n"
				;


//		outputCode+="if("+this.getInputPortVariable(0)+"==1){\n";
//
//		outputCode+="char cmdswingup[50]={0},cmdswingup2[50]={0};\n"
//				+ "sprintf((char*)cmdswingup,\"GZ100A%d\\r\\n\",acc1);\n"
//				+ "Serialport_Send(hCommIPSUST,cmdswingup,strlen(cmdswingup));"
//				+ "usleep(2500);\n"
//				+ "sprintf((char*)cmdswingup2,\"GZ000V%d\\r\\n\",vel1);\n"
//				+ "Serialport_Send(hCommIPSUST,cmdswingup2,strlen(cmdswingup2));"
//				;
//
//		outputCode+="}\n";

		outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=v*100;\n";//v*100
		outputCode+=outputPortList.get(1).getOutputSignalC().getName()+"=vel1;\n";

		code.addOutputCode(outputCode);
	}

	public void  generateDerivativeCodeC(CodeStructC code) {
		String derivativeCode="/*Code for Derivative of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";


		code.addDerivativeCode(derivativeCode);
	}

	public void generateStatementCodeC(CodeStructC code) {
		String statementCode = "/*Code for statement of " + name + ":("+getBlockId()+")"+getBlockName()+"*/\n";
		code.addStatementCode(statementCode);
	}
}
