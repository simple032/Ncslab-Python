package com.ncslab.block.discrete;

import org.apache.velocity.VelocityContext;
import com.ncslab.util.TemplateManager;
import com.ncslab.block.data.DataType;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.ncslablink.MatDimException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ncslab.block.io.Parameter;
import com.ncslab.block.io.State;
import com.ncslab.block.math.Matrix;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;

public class DiscreteStateSpace extends DiscreteBlock {
    private String name = "Discrete State Space";
    
    private boolean feedThrough = false;    

    private Parameter A;
    private Parameter B;
    private Parameter C;
    private Parameter D;
    private Parameter X0;

    private State xState;

    private JSONArray initialConditions;
    
    private Vector<State> xStateList = new Vector<State>();

    public DiscreteStateSpace(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);
        
        parseVector();
        
        for (int i = 0; i < initialConditions.length(); i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);        
        }
    }

    private void parseVector() {
        String aStr = paramValues.getString("A");
        String bStr = paramValues.getString("B");
        String cStr = paramValues.getString("C");
        String dStr = paramValues.getString("D");
        String initCond = paramValues.getString("InitialCondition");
        
        A = new Parameter(this, 1, "A", paramValues.getString("A"));
        B = new Parameter(this, 2, "B", paramValues.getString("B"));
        C = new Parameter(this, 3, "C", paramValues.getString("C"));
        D = new Parameter(this, 4, "D", paramValues.getString("D"));
        X0 = new Parameter(this, 5, "X0", paramValues.getString("X0"));

        if (D.isZero()) {
            feedThrough = false;
        } else {
            feedThrough = true;
        }

        parameterList.add(A);
        parameterList.add(B);
        parameterList.add(C);
        parameterList.add(D);
        parameterList.add(X0);

        xState = new State(this, 1, "x", A.getWidth(), 1);
        xStateList.add(xState);
        stateList.add(xState);
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("A", A);
        context.put("B", B);
        context.put("C", C);
        context.put("D", D);
        context.put("X0", X0);
        context.put("xState", xState);

        String codeStr = TemplateManager.renderTemplate("m/discrete/DiscreteStateSpace/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("C", C);
        context.put("D", D);
        context.put("xState", xState);
        context.put("feedThrough", feedThrough);

        String codeStr = TemplateManager.renderTemplate("m/discrete/DiscreteStateSpace/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        String derivativeCode = xState.getDerivativeName() + "=" + A.getName() + "*" + xState.getName() + "+" + B.getName() + "*" + this.getInputPortVariable(0) + ";\n";

        code.addDerivativeCode(derivativeCode);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        
        String initCode = "/*Code for initialization of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        
        for (int i = 0; i < xStateList.size(); i++) {
            initCode += xStateList.elementAt(i).getName() + "=" + initialConditions.getDouble(i) + ";\n";
        }   
        
        code.addInitCode(initCode);
    }

    public void generateOutputCodeC(CodeStructC code) {
        String outputCode = "/*Code for output of block " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        // y(k) = Cx(k) + Du(k)
        
        if (feedThrough) {
            outputCode += "+" + D + "*" + getInputPortVariable(0);
        }
        
        outputCode += ";\n";
        
        code.addOutputCode(outputCode);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        String derivativeCode = "/*Code for Derivative of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";
        
        code.addDerivativeCode(derivativeCode);
    }

    public void generateUpdateCodeC(CodeStructC code) {
        String updateCode = "/*Code for Update of " + name + ":(" + getBlockId() + ")" + getBlockName() + "*/\n";

        code.addUpdateCode(updateCode);  
    }

    public void updateDimension() throws MatDimException {
        OutputPort out = outputPortList.get(0);
        InputPort in = inputPortList.get(0);
        OutputSignal signal = in.getLinkedLine().getLinkedOutputPort().getOutputSignalC();

        if (this.feedThrough) {
            if (A.getWidth() != A.getHeight() // A是否是方阵
                    || A.getHeight() != B.getHeight() // A和B是否匹配
                    || A.getWidth() != C.getWidth() // A和CB是否匹配
                    || A.getWidth() != xState.getHeight() // A和状态是否匹配
                    || D.getWidth() != B.getWidth() // B和D是否匹配
                    || D.getHeight() != C.getHeight() // D和C是否匹配
                    || B.getWidth() != in.getHeight() // 输入和B是否匹配
                    || in.getWidth() != 1 // 输入必须是列向量
                    || X0.getHeight() != A.getHeight()
                    || X0.getWidth() != 1) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match!");
                throw e;
            }
        } else {
            // 如果没有D，检查的时候就不用考虑D向量
            if (A.getWidth() != A.getHeight()
                    || A.getHeight() != B.getHeight()
                    || A.getWidth() != C.getWidth()
                    || A.getWidth() != xState.getHeight()
                    || in.getWidth() != 1
                    || X0.getHeight() != A.getHeight()
                    || X0.getWidth() != 1) {
                MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match!");
                throw e;
            }
        }

        // 根据参数，设置输出的宽度
        out.setHeight(C.getHeight());
        out.setWidth(1);
        out.getOutputSignalC().setHeight(C.getHeight());
        out.getOutputSignalC().setWidth(1);
        if (D.getHeight() > 1) {
            out.getOutputSignalC().setDataType(DataType.MATRIX);
        } else {
            // TODO: 如果没有D但C的高度 > 1，输出应该是一个矩阵
            out.getOutputSignalC().setDataType(DataType.REAL);
        }
    }

    public void checkDimension() throws MatDimException {
        InputPort in = inputPortList.get(0);
        if (B.getWidth() != in.getHeight()) { // 输入和B是否匹配
            MatDimException e = new MatDimException("Block " + this.blockName + " input dimensions don't match!");
            throw e;
        }
    }
}
