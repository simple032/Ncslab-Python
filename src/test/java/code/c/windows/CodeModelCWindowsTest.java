package test.java.code.c.windows;

import code.Solver;
import code.c.windows.CodeModelCWindows;
import ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import test.java.ResourceReader;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class CodeModelCWindowsTest {
    private CodeModelCWindows codeModelCWindows;

    @Before
    public void setUp() throws Exception {
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        codeModelCWindows = CodeModelCWindows.createFromJSON(jsonIn, ModelMode.Compilation);
    }

    @Test
    public void testCodeModelCWindows() {
        assertEquals("User id should be 35", codeModelCWindows.getUserId(), 35);
        assertEquals("Model id should be 8078", codeModelCWindows.getModelId(), 8078);
        assertEquals("Model name should be s376320", codeModelCWindows.getModelName(), "s376320");
        assertEquals("Model solver should be ode4", codeModelCWindows.getSolver(), Solver.ode4);
    }

    @After
    public void tearDown() throws Exception {

    }
}

