package com.ncslab.block.matrix;

import org.json.JSONObject;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.block.specialized.matrix.IsHermitianDto;

import com.ncslab.block.Block;
import com.ncslab.block.data.DataType;
import com.ncslab.block.io.OutputPort;
import com.ncslab.block.io.OutputSignal;
import com.ncslab.code.c.CodeStructC;
import com.ncslab.ncslablink.MatDimException;
import com.ncslab.ncslablink.NCSLabModel;
import com.ncslab.block.io.InputPort;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * no support for complex matrix
 */
public class IsHermitian extends Block {
    
    
    /**
     * DTO-NATIVE Constructor - Creates IsHermitian block directly from BlockDto DTO
     */
    public IsHermitian(BlockDto blockDto, NCSLabModel model) {
        super(blockDto, model);
        System.out.println("DTO-NATIVE: IsHermitian block created successfully - " + blockDto.getBlockName());
    }


    public static final List<String> outputNames = new ArrayList<>();
    public static final List<String> inputNames = new ArrayList<>();

    public static final Map<String, String> PARAMETER_DEFAULTS;
    static {
        PARAMETER_DEFAULTS = new HashMap<>();
        // No parameters for IsHermitian
        
        outputNames.add("out1");
        inputNames.add("in1");
    }
    
    // TODO
    public IsHermitian(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
    }
}
