package code.c.windows;

import com.ncslab.WindowsTest;
import com.ncslab.code.Solver;
import com.ncslab.code.c.windows.CodeModelCWindows;
import com.ncslab.ncslablink.ErrorMessage;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import utils.ResourceReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(WindowsTest.class)
public class CodeModelCWindowsTest {
    private CodeModelCWindows codeModelC;

    @Before
    public void setUp() throws Exception {
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        codeModelC = CodeModelCWindows.createFromJSON(jsonIn, ModelMode.Compilation);
    }

    @Test
    public void testCodeModelCWindows() {
        assertEquals("User id should be 35", 35,codeModelC.getUserId());
        assertEquals("Model id should be 8078", 8078, codeModelC.getModelId());
        assertEquals("Model name should be s376320", "s376320", codeModelC.getModelName());
        assertEquals("Model solver should be ode5", Solver.ode5, codeModelC.getSolver());
    }

    @After
    public void tearDown() throws Exception {

    }
}

