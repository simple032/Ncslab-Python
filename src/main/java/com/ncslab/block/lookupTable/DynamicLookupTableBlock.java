package com.ncslab.block.lookupTable;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DynamicLookupTableBlock extends LookupTableBlock{

    // === Static Parameter Definitions ===
    public static final Vector<String> parameterNames = new Vector<>();
    
    // Parameter defaults matching database format
    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");
        PARAMETER_DEFAULTS.put("OutDataTypeStr", "Inherit: Same as first input");
    }

    static {
        // Parameter names for dynamic lookup table
        parameterNames.add("SampleTime");
        parameterNames.add("OutDataTypeStr");
    }

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
        super.generateOutputCodeC(code);
        
        context.put("block", this);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/lookupTable/DynamicLookupTableBlock/output.vm", context);
        code.addOutputCode(codeStr);
    }

    @Override
    public void generateDerivativeCodeC(CodeStructC code){
        super.generateDerivativeCodeC(code);
        
        context.put("block", this);
        
        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/lookupTable/DynamicLookupTableBlock/derivative.vm", context);
        code.addDerivativeCode(codeStr);
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
