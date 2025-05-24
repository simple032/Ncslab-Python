package com.ncslab.block.math;

import com.ncslab.block.Block;
import com.ncslab.block.data.Data;
import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputPort;
import com.ncslab.code.c.CodeStructC;
import java.util.Vector;
import com.ncslab.util.TemplateManager;
import com.ncslab.ncslablink.NCSLabModel;
import org.json.JSONObject;

public class Matrix extends Block {

    public double elements[][];
    public int row;
    public int column;
    private boolean scalar = false;

    public Matrix(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
    }

    public void initialize(int row, int column) {
        this.row = row;
        this.column = column;
        this.elements = new double[row][column];
    }

    // Convert MATLAB-string to matrix
    public void initialize(String str) {
        if ("".equals(str)) {
            this.row = 0;
            this.column = 0;
            this.elements = null;
        } else {
            try {
                this.row = 1;
                this.column = 1;
                double value = Double.parseDouble(str);
                this.elements = new double[1][];
                this.elements[0] = new double[1];
                this.elements[0][0] = value;
                this.scalar = true;
            } catch (NumberFormatException nfe) {
                String newstr = str.replaceAll(" ", "").trim();
                newstr = newstr.substring(1, newstr.length() - 1);
                String[] strs = newstr.split(";");
                this.row = strs.length;
                elements = new double[this.row][];
                for (int i = 0; i < elements.length; i++) {
                    String[] substrs = strs[i].split(",");
                    if (i == 0) {
                        this.column = substrs.length;
                    } else if (this.column != substrs.length) {
                    }
                    elements[i] = new double[this.column];
                    for (int j = 0; j < substrs.length; j++) {
                        elements[i][j] = Double.parseDouble(substrs[j]);
                    }
                }
                this.scalar = this.row * this.column == 1;
            }
        }
    }

    @Override
    public void calculateInit() {
        // Initialization logic for Matrix block
    }

    @Override
    public void calculateOutput(double t) {
        OutputPort out = outputPortList.get(0);
        String matrixString = arrayToString(this.elements);
        Data data = new Data(matrixString);
        out.setData(data);
    }

    private String arrayToString(double[][] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(";");
            }
            sb.append("[");
            for (int j = 0; j < array[i].length; j++) {
                if (j > 0) {
                    sb.append(",");
                }
                sb.append(array[i][j]);
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    public void generateOutputCodeC(CodeStructC code) {
        context.put("blockId", getBlockId());
        context.put("blockName", getBlockName());
        context.put("inputPortList", getInputPortList());
        context.put("outputPortList", getOutputPortList());
        context.put("matrix", getMatrix());

        String codeStr = TemplateManager.renderTemplate("c/math/Matrix/output.vm", context);
        code.addOutputCode(codeStr);
    }

    private Matrix getMatrix() {
        return this;
    }

    public void add(Matrix mat) {
        if (this.row != mat.row || this.column != mat.column) {
        } else {
        }
    }

    public void product(Matrix mat) {
        if (this.column != mat.row) {
        } else {
        }
    }

    public void product(Vector vec) {
        if (this.column != vec.size()) {
        } else {
        }
    }

    public boolean isScalar() {
        return this.scalar;
    }

    public int length() {
        return this.row * this.column;
    }

    public void print() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                System.out.print(" a[" + i + "][" + j + "]=" + elements[i][j]);
            }
        }
    }
}
