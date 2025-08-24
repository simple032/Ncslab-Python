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
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.sink.DisplayDto;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Display extends Scope {

    // Parameter defaults matching database format
    
    
    /**
     * DTO-NATIVE Constructor - Creates Display block directly from DisplayDto DTO
     */
    public Display(com.ncslab.dto.block.specialized.sink.DisplayDto displayDto, NCSLabModel model) {
        super(createDisplayJSON(displayDto), model);
        scopeStructs[0].setMaxDataLength(1);
        System.out.println("DTO-NATIVE: Display block created successfully from DisplayDto - " + displayDto.getBlockName());
    }


    // === DTO Helper Methods ===
    private static JSONObject createDisplayJSON(com.ncslab.dto.block.specialized.sink.DisplayDto displayDto) {
        JSONObject json = new JSONObject();
        json.put("blockType", "Display");
        json.put("blockName", displayDto.getBlockName());
        json.put("blockPath", displayDto.getBlockPath());
        json.put("blockUUID", displayDto.getBlockUUID() != null ? displayDto.getBlockUUID() : "null");
        
        JSONObject paramValues = new JSONObject();
        paramValues.put("SampleTime", displayDto.getSampleTimeValue());        
        json.put("paramValues", paramValues);
        
        return json;
    }


    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        PARAMETER_DEFAULTS.put("SampleTime", "-1");  // Inherited
    }

    public static final List<String> inputNames = new ArrayList<>();

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
