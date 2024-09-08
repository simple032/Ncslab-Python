package code.c.linux.loong;

import com.ncslab.code.Solver;
import com.ncslab.code.c.linux.loong.CodeModelCLinuxLoong;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import test.java.ResourceReader;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CodeModelCLinuxLoongTest {
    private CodeModelCLinuxLoong codeModelC;

    @Before
    public void setUp() throws Exception {
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        codeModelC = CodeModelCLinuxLoong.createFromJSON(jsonIn, ModelMode.Compilation);
    }

    @Test
    public void testCodeModelCWindows() {
        assertEquals("User id should be 35", codeModelC.getUserId(), 35);
        assertEquals("Model id should be 8078", codeModelC.getModelId(), 8078);
        assertEquals("Model name should be s376320", codeModelC.getModelName(), "s376320");
        assertEquals("Model solver should be ode4", codeModelC.getSolver(), Solver.ode4);
    }

    @After
    public void tearDown() throws Exception {

    }
}

