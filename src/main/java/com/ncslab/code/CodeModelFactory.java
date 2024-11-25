package com.ncslab.code;

import com.ncslab.code.c.CodeModelC;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class CodeModelFactory {

    public static CodeModelC createModel(String host, String target, JSONObject jsonData, ModelMode mode) {
        try {
            String className = "com.ncslab.code.c." + host.toLowerCase() + "." + target.toLowerCase() + ".CodeModelC" + host + target;
            Class<?> clazz = Class.forName(className);
            return (CodeModelC) clazz.getMethod("createFromJSON", JSONObject.class, ModelMode.class).invoke(null, jsonData, mode);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
