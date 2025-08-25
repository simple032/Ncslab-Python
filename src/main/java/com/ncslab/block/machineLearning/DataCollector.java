package com.ncslab.block.machineLearning;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.machineLearning.DataCollectorDto;

import com.ncslab.block.io.GlobalVariable;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.Block;
import com.ncslab.block.machineLearning.MLBlock;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class DataCollector extends MLBlock{
    // path
    private String path;
    private Parameter inputFeatures, outputFeatures;
    private InputPort _inputs, _outputs;
    private DataCollectorVariable dataCollectorVariable;


    
    
    /**
     * DTO-NATIVE Constructor - Creates DataCollector block directly from BlockDto DTO
     */
    public DataCollector(DataCollectorDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: DataCollector block created successfully - " + blockDto.getBlockName());
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("inputFeatures", "1");
        PARAMETER_DEFAULTS.put("outputFeatures", "1");
        PARAMETER_DEFAULTS.put("savePath", "data.csv");
    }

    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }

    public DataCollector(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);
        this.inputFeatures = new Parameter(this, 1, "inputFeatures", paramValues.getString("inputFeatures"));
        this.outputFeatures = new Parameter(this, 2, "outputFeatures", paramValues.getString("outputFeatures"));
        this.path = paramValues.getString("savePath");

        this._inputs = new InputPort(this, 1);
        this._outputs = new InputPort(this, 2);
        this.inputPortList.add(this._inputs);
        this.inputPortList.add(this._outputs);
        this.dataCollectorVariable = new DataCollectorVariable(this, 1, "data_collector");

        this.globalVariableList.add(this.dataCollectorVariable);
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        code.addIncludeCode("#include\"DataCollector.hpp\"");
        code.addWrittenFile("../../ml/DataCollector.hpp", "DataCollector.hpp");
        
        context.put("inputFeatures", this.inputFeatures);
        context.put("outputFeatures", this.outputFeatures);
        context.put("dataCollectorVariable", this.dataCollectorVariable);

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/DataCollector/init.vm", context);
        code.addInitCode(codeStr);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code){
        super.generateOutputCodeC(code);
        
        context.put("dataCollectorVariable", this.dataCollectorVariable);
        context.put("inputSignal", this._inputs.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        context.put("outputSignal", this._outputs.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());

        String codeStr = com.ncslab.util.TemplateManager.renderTemplate("c/machineLearning/DataCollector/output.vm", context);
        code.addOutputCode(codeStr);
    }

    /**
     * Check the dimension of the input data
     * Default, the machine learning toolbox support the matrix input.
     */
    @Override
    public void checkDimension() throws MatDimException{
        return;
    }

    class DataCollectorVariable extends GlobalVariable{

        public DataCollectorVariable(Block block,int id,String localName){
            super(block, id, localName);
        }

        @Override
        public String getDefineCodeC() {
            return String.format("DataCollector* %s;\n", this.getName());
        }

        @Override
        public String getInitCodeC() {
            return String.format("%s = new DataCollector(int(%s), int(%s));\n",
                this.getName(),
                DataCollector.this.inputFeatures.getName(),
                DataCollector.this.outputFeatures.getName()
            );
        }

        @Override
        public String getEndCodeC(){
            return String.format("%s->save(\"%s\");\n",
                this.getName(),
                DataCollector.this.path
                // this.getName()
            );
        }
    }

}
