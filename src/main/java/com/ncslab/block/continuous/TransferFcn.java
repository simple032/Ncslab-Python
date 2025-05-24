package com.ncslab.block.continuous;

import com.ncslab.block.data.Data;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.Parameter;
import lombok.Getter;
import org.json.JSONObject;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.ncslab.block.Block;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.State;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;

public class TransferFcn extends Block {
    private double D = 0;
    private boolean feedThrough = false;

    private Parameter numParam;
    private Parameter denParam;

    private double[] num;
    private double[] den;

    private Vector<State> xStateList = new Vector<>();

    @Getter
    public static final Vector<String> parameterNames = new Vector<>();

    @Getter
    public static final Vector<String> outputNames = new Vector<>();
    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        parameterNames.add("Numerator");
        parameterNames.add("Denominator");

        outputNames.add("out1");
        inputNames.add("in1");
    }

    public TransferFcn(JSONObject blockIn, NCSLabModel model) {
        super(blockIn, model);

        parseVector();

        for (int i = 0; i < num.length; i++) {
            State xState = new State(this, i + 1, "x" + (i + 1));
            xStateList.add(xState);
            stateList.add(xState);
        }

        // One input, one output
        inputPortList.add(new InputPort(this, 1));
        outputPortList.add(new OutputPort(this, 1, feedThrough));
    }

    private void parseVector() {
        numParam = new Parameter(this, 1, "Numerator", paramValues.getString("Numerator"));
        parameterList.add(numParam);
        denParam = new Parameter(this, 2, "Denominator", paramValues.getString("Denominator"));
        parameterList.add(denParam);

        num = numParam.getDoubleArray();
        den = denParam.getDoubleArray();

        // Normalize
        double unit = den[0];
        for (int i = 0; i < den.length; i++) {
            den[i] = den[i] / unit;
        }

        for (int i = 0; i < num.length; i++) {
            num[i] = num[i] / unit;
        }

        if (num.length == den.length) {
            feedThrough = true;
            D = num[0] / den[0];

            for (int i = 0; i < num.length; i++) {
                num[i] = num[i] - D * den[i];
            }

            double[] numShort = new double[num.length - 1];
            System.arraycopy(num, 1, numShort, 0, num.length - 1);
            num = numShort;
        }

        double[] denShort = new double[den.length - 1];
        System.arraycopy(den, 1, denShort, 0, den.length - 1);
        den = denShort;

        double[] numShort = new double[den.length];
        for (int i = 0; i < den.length; i++) {
            if (i < den.length - num.length) {
                numShort[i] = 0;
            } else {
                numShort[i] = num[i - (den.length - num.length)];
            }
        }
        num = numShort;
    }

    @Override
    public void calculateInit() {
        OutputPort out = outputPortList.get(0);
        Data data = new Data(num.length > 0 ? num[0] : 0);
        for (int i = 0; i < xStateList.size(); i++) {
            xStateList.get(i).setData(data);
        }
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        Data currentState = new Data();
        Data inputSignal = inputPortList.get(0).getData();

        if (feedThrough) {
            currentState = new Data(D * inputSignal.getInitValue());
        }

        for (int i = 0; i < xStateList.size(); i++) {
            currentState = currentState.plus(xStateList.get(i).getData().times(new Data(num[i])));
        }

        out.setData(currentState);
    }

    @Override
    public void calculateDerivative(double t) {
        Data derivativeData;
        Data inputSignal = inputPortList.get(0).getData();

        for (int i = xStateList.size() - 1; i >= 0; i--) {
            if (i == 0) {
                derivativeData = inputSignal.minus(xStateList.get(i).getData().times(new Data(den[i])));
            } else {
                derivativeData = xStateList.get(i - 1).getData().minus(xStateList.get(i).getData().times(new Data(den[i])));
            }
            xStateList.get(i).setDerivateData(derivativeData);
        }
    }

    public void generateInitCodeM(CodeStructM code) {
        super.generateInitCodeM(code);
        context.put("block", this);
        context.put("states", xStateList);

        String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeM(CodeStructM code) {
        super.generateOutputCodeM(code);
        context.put("block", this);
        context.put("states", xStateList);
        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
        context.put("feedThrough", feedThrough);
        context.put("D", D);
        context.put("inputs", getInputPortVariables());
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeM(CodeStructM code) {
        super.generateDerivativeCodeM(code);
        context.put("block", this);
        context.put("states", xStateList);
        context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));
        context.put("inputs", getInputPortVariables());

        String codeStr = TemplateManager.renderTemplate("m/continuous/TransferFcn/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }

    public void generateInitCodeC(CodeStructC code) {
        super.generateInitCodeC(code);
        context.put("block", this);
        context.put("states", xStateList);

        String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/init.vm", context);
        code.addInitCode(codeStr);
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", xStateList);
        context.put("num", Arrays.stream(num).boxed().collect(Collectors.toList()));
        context.put("feedThrough", feedThrough);
        context.put("D", D);
        context.put("inputs", getInputPortVariables());
        context.put("outputs", getOutputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/output.vm", context);
        code.addOutputCode(codeStr);
    }

    public void generateDerivativeCodeC(CodeStructC code) {
        context.put("block", this);
        context.put("states", xStateList);
        context.put("den", Arrays.stream(den).boxed().collect(Collectors.toList()));
        context.put("inputs", getInputPortVariables());

        String codeStr = TemplateManager.renderTemplate("c/continuous/TransferFcn/derivative.vm", context);
        code.addDerivativeCode(codeStr);
    }
}
