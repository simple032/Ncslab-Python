package block.source;

import org.json.JSONObject;

import block.io.OutputPort;
import block.io.Parameter;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;

public class Pulse extends block.Block{
	    block.io.Parameter amplitude;
	    block.io.Parameter period;
	    block.io.Parameter pulseWidth;
	    block.io.Parameter phaseDelay;
	public Pulse(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON, model);
		//构建一个输出
		outputPortList.add(new OutputPort(this,1,false));
        amplitude=new Parameter(this,1,"amplitude");
		period=new Parameter(this,1,"period");
		pulseWidth=new Parameter(this,1,"pulseWidth");
		phaseDelay=new Parameter(this,1,"phaseDelay");
		parameterList.add(amplitude);
		parameterList.add(period);
		parameterList.add(pulseWidth);
		parameterList.add(phaseDelay);
	}
	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		initCode+=amplitude.getName()+"="+paramValues.getDouble("Amplitude")+";\n";
		initCode+=period.getName()+"="+paramValues.getDouble("Period")+";\n";
		initCode+=pulseWidth.getName()+"="+paramValues.getDouble("PulseWidth")+";\n";
		initCode+=phaseDelay.getName()+"="+paramValues.getDouble("PhaseDelay")+";\n";
		code.addInitCode(initCode);
	}
	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
	    outputCode+="if (t+offset)<"+phaseDelay.getName()+"\n";
	    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
	    outputCode+="elseif mod(t+offset-"+phaseDelay.getName()+","+period.getName()+")<"+period.getName()+"*"+pulseWidth.getName()+"/100\n";
	    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+amplitude.getName()+";\n";
	    outputCode+="else\n";
	    outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=0;\n";
	    outputCode+="end\n";
		//outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=pulseFunction(t+offset,"+phaseDelay.getName()+","+period.getName()+","+pulseWidth.getName()+","+amplitude.getName()+");\n";
       // outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"=up(t,"+phaseDelay.getName()+","+period.getName()+","+pulseWidth.getName()+","+amplitude.getName()+");\n";
		code.addOutputCode(outputCode);
	}

}
