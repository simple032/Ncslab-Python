package block.source;

import org.json.JSONObject;

import block.io.OutputPort;
import block.io.Parameter;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class SineWave extends block.Block{
	   block.io.Parameter amplitude;
	    block.io.Parameter bias;
	    block.io.Parameter frequency;
	    block.io.Parameter phase;
	    block.io.Parameter sampleTime;
	public SineWave(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//构建一个输出
		outputPortList.add(new OutputPort(this,1,false));
        amplitude=new Parameter(this,1,"amplitude");
		bias=new Parameter(this,1,"bias");
		frequency=new Parameter(this,1,"frequency");
		phase=new Parameter(this,1,"phase");
		sampleTime=new Parameter(this,1,"sampleTime");
		parameterList.add(amplitude);
		parameterList.add(bias);
		parameterList.add(frequency);
		parameterList.add(phase);
		parameterList.add(sampleTime);
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=amplitude.getName()+"="+paramValues.getDouble("Amplitude")+";\n";
		initCode+=bias.getName()+"="+paramValues.getDouble("Bias")+";\n";
		initCode+=frequency.getName()+"="+paramValues.getDouble("Frequency")+";\n";
		initCode+=phase.getName()+"="+paramValues.getDouble("Phase")+";\n";
		initCode+=sampleTime.getName()+"="+paramValues.getDouble("SampleTime")+";\n";
		//initCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
	   // outputCode+="if "+sampleTime.getName()+"==0\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+initial_output.getName()+"+"+slope.getName()+"*(t-"+start.getName()+");\n";
        outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+bias.getName()+"+"+amplitude.getName()+"*sin("+frequency.getName()+"*(t+offset-"+phase.getName()+"));\n";
     //调试
       // outputCode+="else\n";
        //outputCode+="if mod(t,"+sampleTime.getName()+")==0\n";
       // outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+bias.getName()+"+"+amplitude.getName()+"*sin("+frequency.getName()+"*t-"+phase.getName()+");\n";
       // outputCode+="end\n";
       // outputCode+="end\n";
		code.addOutputCode(outputCode);
	}

}
