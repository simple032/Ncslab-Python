package com.ncslab.block.source;

import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import lombok.Getter;
import org.json.JSONObject;

import java.util.Vector;

public class BandLimitedWhiteNoise extends com.ncslab.block.Block{

    Parameter seed;
    Parameter cov;
    Parameter samplePeriod;

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();

    static {
        // 由于构造函数中没有添加参数，所以parameterNames保持空
        outputNames.add("out1"); // 假设输出端口的名称为"out1"，因为构造函数中没有提供输出端口的名称

        parameterNames.add("seed");
        parameterNames.add("cov");
        parameterNames.add("samplePeriod");
    }

    public BandLimitedWhiteNoise(JSONObject blockJSON, NCSLabModel model) {
		super(blockJSON,model);

        seed = new Parameter(this, 1, "seed", new String(String.valueOf(paramValues.getInt("Seed"))));
        cov = new Parameter(this, 1, "cov", new String(String.valueOf(paramValues.getDouble("Cov"))));
        samplePeriod = new Parameter(this, 1, "samplePeriod", new String(String.valueOf(paramValues.getDouble("Ts"))));

		outputPortList.add(new OutputPort(this,1,false));

        outputPortList.get(0).setHeight(seed.getHeight());
        outputPortList.get(0).setWidth(seed.getWidth());
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        String initCode="/*Code for initialization of block Band-Limited White Noise:("+getBlockId()+")"+getBlockName()+"*/\n";
        initCode+=seed.getInitCodeC();
        initCode+=cov.getInitCodeC();
        initCode+=samplePeriod.getInitCodeC();
        initCode+="srand(+"+ seed.getDataString() +");";
        code.addInitCode(initCode);
    }

	public void generateOutputCodeC(CodeStructC code) {
		String outputCode="/*Code for output of block Band-Limited White Noise::("+getBlockId()+")"+getBlockName()+"*/\n";

        switch(cov.getDataType()) {
            case REAL:
                outputCode +=
                // 1.生成白噪声 2.带限处理
                outputPortList.get(0).getOutputSignalC().getName() + "= lowPassFilter(generateGaussianNoise(0.0, 1.0), 2.0 * M_PI *0.01);\n";
                break;
            case MATRIX:
                break;
        }
        code.addOutputCode(outputCode);
	}
    public void updateDimension() throws MatDimException{
        if(seed.getWidth()!=cov.getWidth()
            ||seed.getHeight()!=cov.getHeight()){
            MatDimException e=new MatDimException("Block "+this.blockName+" input dimensions don't match!All input dimensions should be same!");
            throw(e);
        }
    }
	public void checkDimension() throws MatDimException{

	}
}
