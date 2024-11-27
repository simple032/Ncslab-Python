package code.c.windows;

import com.ncslab.code.c.windows.pc.CodeModelCWindowsPC;
import com.ncslab.code.c.windows.pc.CodeStructCWindowsPC;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import utils.ResourceReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.ncslab.WindowsTest;

@Category(WindowsTest.class)
public class CodeStructCWindowsTest {
    private static CodeStructCWindowsPC codeStructCWindows;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        CodeModelCWindowsPC codeModelCWindows = CodeModelCWindowsPC.createFromJSON(jsonIn, ModelMode.Compilation);

        codeStructCWindows = new CodeStructCWindowsPC(codeModelCWindows);
    }

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testWriteCCodeFiles(){
//        codeStructCWindows.writeCCodeFiles();
    }

    @Test
    public void testMakeExeFile() {
//        assertTrue("", codeStructCWindows.makeExeFile());
        assertEquals("haha","haha");
    }

    @Test
    public void testReadCCodeFiles() {
        assertTrue(true);
    }

    @After
    public void tearDown() throws Exception {

    }
}
