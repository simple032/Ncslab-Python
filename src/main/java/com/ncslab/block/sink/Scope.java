package com.ncslab.block.sink;

import lombok.Getter;
import org.json.JSONObject;

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

import java.util.Vector;

public class Scope extends SinkBlock{

	ScopeStruct scopeStruct;

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        inputNames.add("in1");
    }

	public Scope(JSONObject scopeIn,NCSLabModel model) {
		super(scopeIn,model);

		//һ������
		inputPortList.add(new InputPort(this,1));
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);
		String outputCode="";
		outputCode+="if storeEnable>0\n";
		outputCode+=getBlockName()+"=["+getBlockName()
				+" Block"+getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()
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

			String initCode="/*Code for initialization of block Scope:("+getBlockId()+")"+getBlockName()+"*/\n";

			initCode+=scopeStruct.getName()+".cursor=0;\n";
			initCode+=scopeStruct.getName()+".isFull=0;\n";

			code.addInitCode(initCode);
		}
	}

	public void generateOutputCodeC(CodeStructC code) {

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
					outputCode+="if(block"+out.getBLock().getBlockId()+".discreteUpdated){\n";
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
					outputCode+="if(block"+out.getBLock().getBlockId()+".discreteUpdated){\n";
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

            outputCode+=scopeStruct.getName()+".cursor++;\n";
            outputCode+="if("+scopeStruct.getName()+".cursor>="+scopeStruct.getName()+".maxDataLength){\n";
            outputCode+=scopeStruct.getName()+".cursor-="+scopeStruct.getName()+".maxDataLength;\n";
            outputCode+=scopeStruct.getName()+".isFull=1;\n";
            outputCode+="}\n";

			code.addOutputCode(outputCode);
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
					outputCode+="if(block"+out.getBLock().getBlockId()+".discreteUpdated){\n";
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
			sinkStatusClearCode+="block"+this.getInputPortList().get(0).getLinkedLine().getLinkedOutputPort().getBLock().getBlockId()+".discreteUpdated=0;\n";
			code.addSinkStatusClearCode(sinkStatusClearCode);
		}
	}

	public void generateTerminateCodeC(CodeStructC code) {

		if(model.getModelMode()==ModelMode.Simulation) {

			String terminateCode="/*Code for terminate code of block Scope:("+getBlockId()+")"+getBlockName()+"*/\n";

			//terminateCode+="printf(\"%d\\n\","+scopeStruct.getName()+".cursor);\n";

			code.addTerminateCode(terminateCode);
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
