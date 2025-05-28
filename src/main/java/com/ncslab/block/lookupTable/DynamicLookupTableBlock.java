package com.ncslab.block.lookupTable;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

public class DynamicLookupTableBlock extends LookupTableBlock{

    protected DynamicLookupTableBlock(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        inputPortList.add(new InputPort(this,1));
        inputPortList.add(new InputPort(this,2));
        inputPortList.add(new InputPort(this,3));
        outputPortList.add(new OutputPort(this,1,true));

        parseParameters();
    }

    @Override
    protected void parseParameters() {

    }


    @Override
    public void generateOutputCodeC(CodeStructC code){
        String outputCode="/*Code for output of block Logical operator:("+getBlockId()+")"+getBlockName()+"*/\n";
        //TODO: to be implemented
        code.addOutputCode(outputCode);
    }

    @Override
    public void generateDerivativeCodeC(CodeStructC code){
        String derivativeCode="/*Code for derivative of block Logical operator:("+getBlockId()+")"+getBlockName()+"*/\n";
        //TODO: to be implemented
        code.addOutputCode(derivativeCode);
    }

    @Override
    public void checkDimension() throws MatDimException {
        OutputSignal xdatSignal =
            inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        OutputSignal ydatSignal =
            inputPortList.get(2).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        if(xdatSignal.getWidth()!=ydatSignal.getWidth()){
            MatDimException e=new MatDimException("Block "+this.blockName+ " the width of xdat and ydat should be equal!\n \n");
            throw(e);
        }
    }
}
