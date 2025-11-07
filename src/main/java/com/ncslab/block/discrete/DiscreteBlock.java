package com.ncslab.block.discrete;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.dto.block.discrete.DiscreteBlockDto;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.util.TemplateManager;
import org.apache.velocity.VelocityContext;

abstract public class DiscreteBlock extends Block {

    @Getter
    protected double sampleTime=-1;
    protected boolean feedthrough = false;
    
    // === Timing Precision Constants ===
    private static final double TIMING_EPSILON = 1e-9;
    private static final long TIMING_SCALE = 1000000000L; // nanosecond precision

    public DiscreteBlock(JSONObject blockIn,NCSLabModel model) {
        super(blockIn,model);
    }

    /**
     * DTO-NATIVE Constructor - Creates DiscreteBlock block directly from BlockDto DTO
     */
    public DiscreteBlock(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: DiscreteBlock block created successfully - " + blockDto.getBlockName());
    }

    public void setSampleTime(Parameter sampleTime) {
        this.sampleTime=sampleTime.getData().getInitValue();
    }
    
    // === Timing Precision Helper Methods ===
    protected boolean isTimeForUpdate(double currentTime, double nextDiscreteTime) {
        return nextDiscreteTime <= currentTime || 
               Math.abs(nextDiscreteTime - currentTime) < TIMING_EPSILON;
    }
    
    protected boolean isSampleTimeMultiple(double sampleTime, double fixedStep) {
        double ratio = sampleTime / fixedStep;
        double roundedRatio = Math.round(ratio);
        return Math.abs(ratio - roundedRatio) < TIMING_EPSILON;
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

        discreteUpdateCode+="while(block"+this.getBlockId()+".discreteTime<=mp->time||"+"fabs(block"+this.getBlockId()+".discreteTime-mp->time)<"+TIMING_EPSILON+"){\n";
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
                // Add defensive null checks for input port list
                if (inputPortList != null && !inputPortList.isEmpty() && 
                    inputPortList.get(0) != null && 
                    inputPortList.get(0).getLinkedLine() != null &&
                    inputPortList.get(0).getLinkedLine().getLinkedOutputPort() != null) {
                    
                    Block linkedBlock = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getBlock();
                    if (linkedBlock instanceof DiscreteBlock) {
                        sampleTime = ((DiscreteBlock) linkedBlock).getSampleTime();
                    } else {
                        sampleTime = model.getConfig().getFixedStep();
                    }
                } else {
                    // If no input connection, use fixed step as default
                    sampleTime = model.getConfig().getFixedStep();
                }
            }else {
                throw new MatDimException("Block "+this.blockName+"sample time error.");
            }
        }else if(sampleTime == 0) {
        	sampleTime = model.getConfig().getFixedStep();
        }
        
        // Add null check for parameter list
        if (parameterList != null) {
            for(Parameter parameter:parameterList) {
                if(parameter != null && parameter.getLocalName().equals("sampleTime")) {
                    parameter.getData().setInitValue(sampleTime);
                }
            }
        }
        
        // Call subclass-specific dimension update logic
        updateDimensionInside();
    }

   abstract public void calculateOutput(double t);
   abstract public void calculateUpdate(double t);
}
