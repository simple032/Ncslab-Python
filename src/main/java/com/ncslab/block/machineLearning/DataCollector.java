package com.ncslab.block.machineLearning;

import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.io.GlobalVariable;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.block.Block;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Vector;

public class DataCollector extends Block{
    // path
    private String path;
    private Parameter inputFeatures, outputFeatures;
    private InputPort _inputs, _outputs;
    private DataCollectorVariable dataCollectorVariable;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
        parameterNames.add("inputFeatures");
        parameterNames.add("outputFeatures");
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

        parameterList.add(this.inputFeatures);
        parameterList.add(this.outputFeatures);
        this.globalVariableList.add(this.dataCollectorVariable);
    }

    @Override
    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        code.addIncludeCode("#include\"DataCollector.hpp\"");
        code.addWrittenFile("../../ml/DataCollector.hpp", "DataCollector.hpp");
        String initCode="/*Code for initialization of block DataCollector:("+getBlockId()+")"+getBlockName()+"*/\n";
        initCode += this.inputFeatures.getInitCodeC();
        initCode += this.outputFeatures.getInitCodeC();

        initCode += this.dataCollectorVariable.getInitCodeC();
        code.addInitCode(initCode);
    }

    @Override
    public void generateOutputCodeC(CodeStructC code){
        super.generateOutputCodeC(code);
        String outputCode = "";
        outputCode += String.format("%s->collect(%s, %s);\n",
            this.dataCollectorVariable.getName(),
            this._inputs.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName(),
            this._outputs.getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName());
        code.addOutputCode(outputCode);
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
