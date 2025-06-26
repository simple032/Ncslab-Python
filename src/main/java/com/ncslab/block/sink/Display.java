package com.ncslab.block.sink;

import com.ncslab.block.io.InputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.block.io.terminal.ScopeStruct;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.code.m.CodeStructM;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.ModelMode;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.util.TemplateManager;
import lombok.Getter;
import org.json.JSONObject;

import java.util.Vector;

public class Display extends Scope {

    @Getter
    public static final Vector<String> inputNames = new Vector<>();

    static {
        inputNames.add("in1");
    }

    public Display(JSONObject scopeIn, NCSLabModel model) {
        super(scopeIn, model);

        scopeStructs[0].setMaxDataLength(1);
    }

    @Override
    public void checkDimension() throws MatDimException {

        OutputSignal signal = this.inputPortList.get(0).getLinkedLine().getLinkedOutputPort().getOutputSignalC();
        for(int i = 0; i < inportNum; i++) {
            scopeStructs[i] = new ScopeStruct(this, 1, this.blockName);
            scopeStructs[i].setDimension(signal.getWidth(), signal.getHeight());
            scopeStructs[i].setMaxDataLength(1);

            model.addTerminal(scopeStructs[i]);
        }
    }
}
