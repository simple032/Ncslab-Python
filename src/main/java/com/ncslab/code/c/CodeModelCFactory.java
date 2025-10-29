package com.ncslab.code.c;


import com.ncslab.code.c.linux.loong.CodeModelCLinuxLoong;
import com.ncslab.code.c.linux.raspberry.CodeModelCLinuxRaspberry;
import com.ncslab.code.c.windows.android.CodeModelCWindowsAndroid;
import com.ncslab.code.c.windows.pc.CodeModelCWindowsPC;
import com.ncslab.code.c.linux.pc.simulation.CodeModelCLinuxPCSimulation;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import org.json.JSONArray;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.dto.core.BlockDto;
import com.ncslab.dto.model.LineDto;
import com.ncslab.util.JsonUtils;

import java.util.HashMap;

@FunctionalInterface
interface BiFunctionWithException<T,U,R>{
    R apply(T t, U u) throws ModelException;
}

public class CodeModelCFactory {
    private static final HashMap<String, BiFunctionWithException
            <JSONObject, ModelMode, CodeModelC>> registry =
            new HashMap<>();
    static {
        registry.put("raspberry", CodeModelCLinuxRaspberry::createFromJSON);
        registry.put("loong", CodeModelCLinuxLoong::createFromJSON);
        registry.put("windows", CodeModelCWindowsPC::createFromJSON);
    }

    public static CodeModelC createInstance(String type, JSONObject jsonIn, ModelMode mode) throws ModelException {
        BiFunctionWithException
                <JSONObject, ModelMode, CodeModelC> function = registry.get(type);
        if(function != null){
            return function.apply(jsonIn, mode);
        }
        throw new IllegalArgumentException("Invalid type: " + type);
    }
    
    // New DTO-based factory method
    public static CodeModelC createInstanceFromDto(String type, ModelDto modelDto, ModelMode mode) throws ModelException {
        System.out.println("Creating CodeModelC from DTO for platform: " + type);
        
        switch (type.toLowerCase()) {
            case "raspberry":
                // Use native DTO method
                return CodeModelCLinuxRaspberry.createFromDto(modelDto, mode);
            case "loong":
                // Use native DTO method
                return CodeModelCLinuxLoong.createFromDto(modelDto, mode);
            case "android":
                return CodeModelCWindowsAndroid.createFromDto(modelDto, mode);
            case "windows":
                // Use native DTO method
                return CodeModelCWindowsPC.createFromDto(modelDto, mode);
            case "linux":
            case "pc":
                // Use native DTO method
                return CodeModelCLinuxPCSimulation.createFromDto(modelDto, mode);
            default:
                throw new IllegalArgumentException("Invalid type for DTO creation: " + type);
        }
    }

}
