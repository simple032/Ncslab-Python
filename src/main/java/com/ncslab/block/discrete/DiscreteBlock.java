package com.ncslab.block.discrete;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;

public class DiscreteBlock extends Block {

    @Getter
    @Setter
    private double sampleTime=-1;

    public DiscreteBlock(JSONObject blockIn,NCSLabModel model) {
        super(blockIn,model);
    }

    public void setSampleTime(Parameter sampleTime) {
        this.sampleTime=sampleTime.getData().getInitValue();
    }

    public void generateDiscreteUpdateCodeCInside(CodeStructC code) throws MatDimException{

    }

    public void generateDiscreteUpdateCodeC(CodeStructC code) throws MatDimException{
        String discreteUpdateCode="/*Code for update of discrete block "+getBlockType()+":("+getBlockId()+")"+getBlockName()+"*/\n";
		/*
		discreteUpdateCode+="dist=distance(mp->time,"+sampleTime+");\n";
		discreteUpdateCode+="if(fabs(floor(mp->time/"+sampleTime+"+0.5)-mp->time/"+sampleTime+")<0.000001) {\n";
		code.addDiscreteUpdateCode(discreteUpdateCode);

		generateDiscreteUpdateCodeCInside(code);

		discreteUpdateCode="}\n";
		code.addDiscreteUpdateCode(discreteUpdateCode);*/

        //discreteUpdateCode+="if(block"+this.getBlockId()+".discreteTime>mp->time){\n";
        //discreteUpdateCode+="block"+this.getBlockId()+".discreteUpdated=0;\n";
        //discreteUpdateCode+="}\n";

        //discreteUpdateCode+="while(block"+this.getBlockId()+".discreteTime<=mp->time){\n";
        
        discreteUpdateCode+="while(block"+this.getBlockId()+".discreteTime<=mp->time||"+"block"+this.getBlockId()+".discreteTime-mp->time<0.0000001){\n";
        code.addDiscreteUpdateCode(discreteUpdateCode);

        generateDiscreteUpdateCodeCInside(code);

        discreteUpdateCode="block"+this.getBlockId()+".discreteTime+="+this.sampleTime+";\n";
        discreteUpdateCode+="block"+this.getBlockId()+".discreteUpdated=1;\n";
        discreteUpdateCode+="}\n";
        code.addDiscreteUpdateCode(discreteUpdateCode);
    }


    public void generateUpdateCodeC(CodeStructC code) throws MatDimException {

    }

    public void updateDimensionInside() throws MatDimException{

    }

    //离散模块都需更新sampleTime,有额外的维度要更新需继承该函数
    public void updateDimension() throws MatDimException{
        if (sampleTime < 0) {
            if (sampleTime == -1) {
                Block linkedBlock = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getBLock();
                if (linkedBlock instanceof DiscreteBlock) {
                    sampleTime = ((DiscreteBlock) linkedBlock).getSampleTime();
                } else {
                    sampleTime = model.getConfig().getFixedStep();
                }
            }else {
                throw new MatDimException("Block "+this.blockName+"sample time error.");
            }
        }else if(sampleTime == 0) {
        	sampleTime = model.getConfig().getFixedStep();
        }
        for(Parameter parameter:parameterList) {
            if(parameter.getLocalName().equals("sampleTime")) {
                parameter.getData().setInitValue(sampleTime);
            }
        }
    }

}
