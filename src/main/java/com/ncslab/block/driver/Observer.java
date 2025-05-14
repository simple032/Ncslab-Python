package com.ncslab.block.driver;

import lombok.Getter;
import org.json.JSONObject;
import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.*;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import java.util.Vector;

public class Observer extends Block {
    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();
    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    private String observerName;

    static {
        inputNames.add("i1");
        inputNames.add("i2");
        outputNames.add("o1");
        outputNames.add("o2");
        outputNames.add("o3");
    }

    public Observer(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);

        for(int i = 0; i < inputNames.size(); i++) {
            inputPortList.add(new InputPort(this, i+1));
        }

        for(int i = 0; i < outputNames.size(); i++) {
            outputPortList.add(new OutputPort(this, i+1, true));
        }

        observerName = getBlockName();
    }

    protected void paraseParamValues() {

    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        StringBuilder initCode = new StringBuilder();

        initCode.append("/*Code for initialization of block Observer:(" + getBlockId() + ")" + observerName + "*/\n");

        // 定义固定的系统矩阵
        initCode.append("static const double A_" + observerName + "[9] = {1.2998,-0.4341,0.1343,1,0,0,0,1,0};\n");
        initCode.append("static const double B_" + observerName + "[3] = {1,0,0};\n");
        initCode.append("static const double C_" + observerName + "[3] = {3.5629,2.7739,1.0121};\n");
        initCode.append("static const double L_" + observerName + "[3] = {0.0363,0.0439,0.1470};\n");

        // 初始化状态为0
        initCode.append(outputPortList.get(0).getOutputSignalC().getName() + " = 0.0;\n");
        initCode.append(outputPortList.get(1).getOutputSignalC().getName() + " = 0.0;\n");
        initCode.append(outputPortList.get(2).getOutputSignalC().getName() + " = 0.0;\n");

        code.addInitCode(initCode.toString());
    }

    public void generateOutputCodeC(CodeStructC code) {
        StringBuilder outputCode = new StringBuilder();
        outputCode.append("/*Code for output of block Observer:(" + getBlockId() + ")" + observerName + "*/\n");

        // 系统矩阵声明 - 在输出代码中也需要声明一次
        outputCode.append("static const double A_" + observerName + "[9] = {1.2998,-0.4341,0.1343,1,0,0,0,1,0};\n");
        outputCode.append("static const double B_" + observerName + "[3] = {1,0,0};\n");
        outputCode.append("static const double C_" + observerName + "[3] = {3.5629,2.7739,1.0121};\n");
        outputCode.append("static const double L_" + observerName + "[3] = {0.0363,0.0439,0.1470};\n\n");

        // 获取信号名称
        String x1Name = outputPortList.get(0).getOutputSignalC().getName();
        String x2Name = outputPortList.get(1).getOutputSignalC().getName();
        String x3Name = outputPortList.get(2).getOutputSignalC().getName();
        String yName = inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();
        String uName = inputPortList.get(1).getLinkedLine().getLinkedOutputPort().getOutputSignalC().getName();

        // 计算创新项 (y - Cx)
        outputCode.append("// Calculate innovation term\n");
        outputCode.append("double innovation_" + observerName + " = " + yName + " - (C_" + observerName +
            "[0] * " + x1Name + " + C_" + observerName + "[1] * " + x2Name + " + C_" +
            observerName + "[2] * " + x3Name + ");\n\n");

        // 计算新的状态估计
        outputCode.append("// Calculate new state estimates\n");
        outputCode.append("double x1_next = " + x1Name + " * A_" + observerName + "[0] + " +
            x2Name + " * A_" + observerName + "[1] + " +
            x3Name + " * A_" + observerName + "[2] + " +
            "B_" + observerName + "[0] * " + uName + " + " +
            "L_" + observerName + "[0] * innovation_" + observerName + ";\n");

        outputCode.append("double x2_next = " + x1Name + " * A_" + observerName + "[3] + " +
            x2Name + " * A_" + observerName + "[4] + " +
            x3Name + " * A_" + observerName + "[5] + " +
            "B_" + observerName + "[1] * " + uName + " + " +
            "L_" + observerName + "[1] * innovation_" + observerName + ";\n");

        outputCode.append("double x3_next = " + x1Name + " * A_" + observerName + "[6] + " +
            x2Name + " * A_" + observerName + "[7] + " +
            x3Name + " * A_" + observerName + "[8] + " +
            "B_" + observerName + "[2] * " + uName + " + " +
            "L_" + observerName + "[2] * innovation_" + observerName + ";\n\n");

        // 更新状态
        outputCode.append("// Update states\n");
        outputCode.append(x1Name + " = x1_next;\n");
        outputCode.append(x2Name + " = x2_next;\n");
        outputCode.append(x3Name + " = x3_next;\n");

        code.addOutputCode(outputCode.toString());
    }

    public void updateDimension() throws MatDimException {
        // 设置输出维度，都是标量输出
        for(OutputPort out : outputPortList) {
            out.setHeight(1);
            out.setWidth(1);
            out.getOutputSignalC().setHeight(1);
            out.getOutputSignalC().setWidth(1);
            out.getOutputSignalC().setDataType(DataType.REAL);
        }
    }

    public void checkDimension() throws MatDimException {
        // 检查输入信号维度，应该都是标量
        for(InputPort in : inputPortList) {
            if(in.getLinkedLine().getLinkedOutputPort().getHeight() != 1 ||
                in.getLinkedLine().getLinkedOutputPort().getWidth() != 1) {
                throw new MatDimException("Block " + blockName + ": Input dimension mismatch, expect scalar input");
            }
        }
    }
}
