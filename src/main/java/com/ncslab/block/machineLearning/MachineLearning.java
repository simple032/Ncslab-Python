package com.ncslab.block.machineLearning;

import java.util.Vector;

import org.json.JSONObject;

import com.ncslab.block.io.GlobalVariable;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.greenpineyu.fel.parser.FelParser.program_return;
import com.ncslab.block.Block;
import com.ncslab.ncslablink.NCSLabModel;

public abstract class MachineLearning extends Block{
    // protected MLVariable modelVariable;
    // private Parameter layersNumber, lossFunction, learningRate, inputFeatures, activationFunction, dataset;
    public MachineLearning(JSONObject jsonObject, NCSLabModel model){
        super(jsonObject, model);
    }

    @Override
    public void generateInitCodeC(CodeStructC code){
        super.generateInitCodeC(code);
        String initCode = "/*Code for initialization of block MLTest:("+getBlockId()+")"+getBlockName()+"*/\n";
        // initCode += "Py_Initialize();\n";
        // initCode += "PyRun_SimpleString(\"import sys\");\n";
        // initCode += "PyRun_SimpleString(\"sys.path.append('./')\");\n";
        // initCode += this.inputFeatures.getInitCodeC();
        code.addInitCode(initCode);
    }

    public abstract String getVariableName();

    public abstract String getVariableParameters();

    protected class MLVariable extends GlobalVariable{
        public MLVariable(Block block, int id, String localName, String dataString) {
            super(block, id, localName, dataString);
        }
    
        @Override
        public String getInitCodeC() {
            // StringBuilder parameters = new StringBuilder();
            // Vector<String> variableParameters = getVariableParameters();
            // for(String s:variableParameters){
            //     parameters.append(s);
            //     parameters.append(",");
            // }
            // if(parameters.length()>0){
            //     parameters.deleteCharAt(parameters.length()-1);
            // }
            return String.format("%s = %s;\n", 
                        MLVariable.this.getName(), 
                        MachineLearning.this.getVariableParameters());   
        }
    
        @Override
        public String getDefineCodeC() {
            return String.format("%s %s;\n", 
            MachineLearning.this.getVariableName(), 
            MLVariable.this.getName());
        }
    }
}
