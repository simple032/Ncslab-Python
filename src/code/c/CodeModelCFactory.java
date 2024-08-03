package code.c;


import code.c.linux.loong.CodeModelCLinuxLoong;
import code.c.linux.raspberry.CodeModelCLinuxRaspberry;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import org.json.JSONObject;

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
        registry.put("raspberry", (jsonIn, mode)->{
          try{
              return CodeModelCLinuxRaspberry.createFromJSON(jsonIn, mode);
          }catch (ModelException e) {
              throw e;
          }
        });
        registry.put("loong", (jsonIn, mode)->{
            try{
                return CodeModelCLinuxLoong.createFromJSON(jsonIn, mode);
            }catch (ModelException e) {
                throw e;
            }
        });
    }

    public static CodeModelC createInstance(String type, JSONObject jsonIn, ModelMode mode) throws ModelException {
        BiFunctionWithException
                <JSONObject, ModelMode, CodeModelC> function = registry.get(type);
        if(function != null){
            return function.apply(jsonIn, mode);
        }
        throw new IllegalArgumentException("Invalid type: " + type);
    }
}