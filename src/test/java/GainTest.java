package test.java;

import block.math.Gain;
import code.m.CodeStructM;
import ncslablink.NCSLabModel;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class GainTest extends Gain {

    public GainTest(JSONObject blockJSON, NCSLabModel model) {
        super(blockJSON, model);
    }

    @Test
    public void generateInitCodeM() {
        CodeStructM code = null;
        super.generateInitCodeM(code);
    }
}
