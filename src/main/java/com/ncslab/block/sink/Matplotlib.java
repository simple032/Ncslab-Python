package com.ncslab.block.sink;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;

import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import com.ncslab.block.discrete.DiscreteBlock;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.OutputPort;

import com.ncslab.block.io.terminal.ScopeStruct;

import com.ncslab.ncslablink.ModelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Matplotlib extends SinkBlock{

	ScopeStruct scopeStruct;
    
    
    
    /**
     * DTO-NATIVE Constructor - Creates Matplotlib block directly from BlockDto DTO
     */
    public Matplotlib(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: Matplotlib block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> inputNames = new ArrayList<>();

    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("SaveName", "MatplotlibData");
        PARAMETER_DEFAULTS.put("SaveFormat", "Array");
        PARAMETER_DEFAULTS.put("BufferSize", "100000");
    }

    static {
        inputNames.add("in1");
    }

	public Matplotlib(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);

		//һ������
		inputPortList.add(new InputPort(this,1));
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if storeEnable>0\n";
		outputCode+=getBlockName()+"=["+getBlockName()
				+" Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId()
				+"_Output"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getNumber()
				+"]"
				+";\n";
		outputCode+="end\n";
		code.addOutputCode(outputCode);
	}

	public void generateInitCodeM(CodeStructM code) {
		super.generateInitCodeM(code);
		String initCode="";
		code.addGlobalDefineCode("global "+getBlockName()+";\n");
		initCode+=getBlockName()+"=[];\n";
		initCode+="ScopeNum=ScopeNum+1;\n";
		initCode+="ScopeList=[ScopeList; '"+getBlockName()+"'];\n";
		code.addInitCode(initCode);
	}

	public void generateInitCodeC(CodeStructC code) {
		super.generateInitCodeC(code);

		if(model.getModelMode()==ModelMode.Simulation) {
			context.put("block", this);
			context.put("scopeStruct", scopeStruct);

			String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/sink/Matplotlib/init.vm", context);
			code.addInitCode(codeStr);
		}
	}

	public void generateOutputCodeC(CodeStructC code) {

		if(model.getModelMode()==ModelMode.Simulation) {

			OutputSignal signal=this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
			OutputPort out=this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort();

			double sampleTime=-1;
			if(signal.getBlock() instanceof DiscreteBlock) {
				DiscreteBlock block=(DiscreteBlock)signal.getBlock();
				sampleTime=block.getSampleTime();
			}

			context.put("block", this);
			context.put("signal", signal);
			context.put("outputPort", out);
			context.put("sampleTime", sampleTime);
			context.put("scopeStruct", scopeStruct);

			String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/sink/Matplotlib/output.vm", context);
			code.addOutputCode(codeStr);
		}
	}

	public void generateOutputSinkCodeC(CodeStructC code) {

		if(model.getModelMode()==ModelMode.Simulation) {

			OutputSignal signal=this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

			OutputPort out=this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort();

			double sampleTime=-1;

			//如果连接的是离散模块,就读出采样周期
			if(signal.getBlock() instanceof DiscreteBlock) {
				DiscreteBlock block=(DiscreteBlock)signal.getBlock();
				sampleTime=block.getSampleTime();
			}

			String outputCode="/*Code for output of block Scope:("+getBlockId()+")"+getBlockName()+"*/\n";

			outputCode+="if(sfcnIsMajorStep()){\n";
			switch(signal.getDataType()) {
			case REAL:
				//如果是离散模块,就画出阶梯图
				if(sampleTime>0) {
					outputCode+="if(block"+out.getBlock().getBlockId()+".discreteUpdated){\n";
					//画当前时间的点
					outputCode+=scopeStruct.getName()+".timeList.push_back(sfcnGetT());\n";
					outputCode+=scopeStruct.getName()+".dataList.push_back("+signal.getName()+");\n";
					//保持一个采样周期sampleTime,画下一个周期的点
					outputCode+=scopeStruct.getName()+".timeList.push_back(sfcnGetT()+"+sampleTime+");\n";
					outputCode+=scopeStruct.getName()+".dataList.push_back("+signal.getName()+");\n";
					outputCode+="}\n";
				}
				//否则就一般的画法
				else {
					outputCode+=scopeStruct.getName()+".timeList.push_back(sfcnGetT());\n";
					outputCode+=scopeStruct.getName()+".dataList.push_back("+signal.getName()+");\n";
				}

				break;
			case MATRIX:
				//如果是离散模块,就画出阶梯图
				if(sampleTime>0) {
					outputCode+="double dist=distance(mp->time,"+sampleTime+");\n";
					outputCode+="if(fabs(dist)<0.000000001||fabs(dist-"+sampleTime+")<0.000000001){\n";
					//画当前时间的点
					outputCode+=scopeStruct.getName()+".timeList.push_back(sfcnGetT());\n";
					outputCode+="for(int i=0;i<"+signal.getHeight()+";i++){\n";
					outputCode+="for(int j=0;j<"+signal.getWidth()+";j++){\n";
					outputCode+=scopeStruct.getName()+".dataList.push_back("+signal.getName()+"(i,j));\n";
					outputCode+="}\n";
					outputCode+="}\n";
					//保持一个采样周期sampleTime,画下一个周期的点
					outputCode+=scopeStruct.getName()+".timeList.push_back(sfcnGetT()+"+sampleTime+");\n";
					outputCode+="for(int i=0;i<"+signal.getHeight()+";i++){\n";
					outputCode+="for(int j=0;j<"+signal.getWidth()+";j++){\n";
					outputCode+=scopeStruct.getName()+".dataList.push_back("+signal.getName()+"(i,j));\n";
					outputCode+="}\n";
					outputCode+="}\n";

					outputCode+="}\n";
				}
				//否则就一般的画法
				else {
					outputCode+=scopeStruct.getName()+".timeList.push_back(sfcnGetT());\n";
					outputCode+="for(int i=0;i<"+signal.getHeight()+";i++){\n";
					outputCode+="for(int j=0;j<"+signal.getWidth()+";j++){\n";
					outputCode+=scopeStruct.getName()+".dataList.push_back("+signal.getName()+"(i,j));\n";
					outputCode+="}\n";
					outputCode+="}\n";
				}
				break;
			}

			outputCode+="}\n";
			code.addSinkOutputCode(outputCode);

			String sinkStatusClearCode="";
			sinkStatusClearCode+="block"+this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBlock().getBlockId()+".discreteUpdated=0;\n";
			code.addSinkStatusClearCode(sinkStatusClearCode);
		}
	}

	public void generateTerminateCodeC(CodeStructC code) {

		if(model.getModelMode()==ModelMode.Simulation) {
			context.put("block", this);
			context.put("scopeStruct", scopeStruct);

			String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/sink/Matplotlib/terminate.vm", context);
			code.addTerminateCode(codeStr);
		}
	}

	public void updateDimension() throws MatDimException{
	}

	public void checkDimension() throws MatDimException{
		scopeStruct=new ScopeStruct(this,1,this.blockName);

		OutputSignal signal=this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		scopeStruct.setDimension(signal.getWidth(), signal.getHeight());
		scopeStruct.setMaxDataLength(3000);

		model.addTerminal(scopeStruct);
	}
}
