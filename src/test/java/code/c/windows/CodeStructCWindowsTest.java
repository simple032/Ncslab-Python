package test.java.code.c.windows;

import code.c.windows.CodeModelCWindows;
import code.c.windows.CodeStructCWindows;
import ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import test.java.ResourceReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class CodeStructCWindowsTest {
    private static CodeStructCWindows codeStructCWindows;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        CodeModelCWindows codeModelCWindows = CodeModelCWindows.createFromJSON(jsonIn, ModelMode.Compilation);

        codeStructCWindows = new CodeStructCWindows(codeModelCWindows);
    }

    @Before
    public void setUp() throws Exception {

    }

    @Ignore
    public void testWriteCCodeFiles(){
        codeStructCWindows.writeCCodeFiles();
    }

    @Ignore
    public void testMakeExeFile() {
        assertTrue("", codeStructCWindows.makeExeFile());
    }

    @After
    public void tearDown() throws Exception {

    }
}
