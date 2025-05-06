package com.ncslab.block.math;
import lombok.Getter;
import org.json.JSONObject;

import com.ncslab.block.Block;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.Parameter;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.block.io.InputPort;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;

import java.util.Objects;
import java.util.Vector;

public class MathFunction extends Block{

	private String seq;


    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {

        outputNames.add("out1");
        inputNames.add("in1");
    }

	public MathFunction(JSONObject blockJSON,NCSLabModel model) {
		super(blockJSON,model);
		//����һ�����
		outputPortList.add(new OutputPort(this,1,true));
		//����һ������
		inputPortList.add(new InputPort(this,1));

		seq=paramValues.optString("Operator", paramValues.getString("MathFunctionOperator"));
        if(Objects.equals(seq, "pow")){
            inputPortList.add(new InputPort(this,2));
        }
	}

	public void generateOutputCodeM(CodeStructM code) {
		super.generateOutputCodeM(code);

		String outputCode="j=" + inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName()+";\n";
		outputCode+="Block" + this.getBlockId()+"_Output1="+seq+"(j);\n";
		code.addOutputCode(outputCode);
	}

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode="/*Code for output of block MathFunction:("+getBlockId()+")"+getBlockName()+"*/\n";

        OutputSignal signal = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        //outputCode+=outputPortList.get(0).getOutputSignalC().getName()+"="+seq+"("+signal.getName()+");\n";

        switch (seq) {
            case "transpose":
                switch (signal.getDataType()) {
                    case REAL:
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=" + signal.getName() + ";\n";
                        break;
                    case MATRIX:
                        int width = signal.getWidth();
                        int height = signal.getHeight();

                        outputCode += "for(int i=0;i<" + width + ";i++){\n";
                        outputCode += "for(int j=0;j<" + height + ";j++){\n";
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(i,j)=" + signal.getName() + "(j,i);\n";
                        outputCode += "}\n";
                        outputCode += "}\n";
                        break;
                }
                break;
            case "reciprocal":
                switch (signal.getDataType()) {
                    case REAL:
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=1.0/(" + signal.getName() + ");\n";
                        break;
                    case MATRIX:
                        int width = signal.getWidth();
                        int height = signal.getHeight();

                        outputCode += "for(int i=0;i<" + height + ";i++){\n";
                        outputCode += "for(int j=0;j<" + width + ";j++){\n";
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(i,j)=1.0/(" + signal.getName() + "(i,j));\n";
                        outputCode += "}\n";
                        outputCode += "}\n";

                        break;
                }
                break;
            case "square":
                switch (signal.getDataType()) {
                    case REAL:
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=(" + signal.getName() + ")*(" + signal.getName() + ");\n";
                        break;
                    case MATRIX:
                        int width = signal.getWidth();
                        int height = signal.getHeight();

                        outputCode += "for(int i=0;i<" + height + ";i++){\n";
                        outputCode += "for(int j=0;j<" + width + ";j++){\n";
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(i,j)=(" + signal.getName() + ")*(" + signal.getName() + "(i,j));\n";
                        outputCode += "}\n";
                        outputCode += "}\n";

                        break;
                }
                break;
            case "exp":
            case "log":
            case "log10":
                switch (signal.getDataType()) {
                    case REAL:
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=" + seq + "(" + signal.getName() + ");\n";
                        break;
                    case MATRIX:
                        int width = signal.getWidth();
                        int height = signal.getHeight();

                        outputCode += "for(int i=0;i<" + height + ";i++){\n";
                        outputCode += "for(int j=0;j<" + width + ";j++){\n";
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(i,j)=" + seq + "(" + signal.getName() + "(i,j));\n";
                        outputCode += "}\n";
                        outputCode += "}\n";

                        break;
                }
                break;
            case "pow":
                switch (signal.getDataType()) {
                    case REAL:
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "=pow(" + signal.getName() + "," + inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ");\n";
                        break;
                    case MATRIX:
                        int width = signal.getWidth();
                        int height = signal.getHeight();
                        outputCode += "for(int i=0;i<" + height + ";i++){\n";
                        outputCode += "for(int j=0;j<" + width + ";j++){\n";
                        outputCode += outputPortList.get(0).getOutputSignalC().getName() + "(i,j)=pow(" + signal.getName() + "(i,j)," + inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName() + ");\n";
                        outputCode += "}\n";
                        outputCode += "}\n";
                        break;
                }
                break;
        }

        code.addOutputCode(outputCode);
    }

	public void updateDimension() throws MatDimException{
		OutputPort out  = outputPortList.get(0);
		InputPort in  = inputPortList.get(0);
		OutputSignal signal=in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();
		if(seq.equals("transpose")) {
			out.setHeight(signal.getWidth());
			out.setWidth(signal.getHeight());
			out.getOutputSignalC().setHeight(signal.getWidth());
			out.getOutputSignalC().setWidth(signal.getHeight());
			out.getOutputSignalC().setDataType(signal.getDataType());
		}
		else {
			out.setHeight(signal.getHeight());
			out.setWidth(signal.getWidth());
			out.getOutputSignalC().setHeight(signal.getHeight());
			out.getOutputSignalC().setWidth(signal.getWidth());
			out.getOutputSignalC().setDataType(signal.getDataType());
		}
	}

	public void checkDimension() throws MatDimException{
	}
}
