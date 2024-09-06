package test.java.code.c.loong;

import code.c.linux.loong.CodeModelCLinuxLoong;
import code.c.linux.loong.CodeStructCLinuxLoong;
import ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import test.java.ResourceReader;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class CodeStructCLinuxLoongTest {
    private static CodeStructCLinuxLoong codeStructCLinuxLoong;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        CodeModelCLinuxLoong codeModelCLinuxLoong = CodeModelCLinuxLoong.createFromJSON(jsonIn, ModelMode.Compilation);

        codeStructCLinuxLoong = new CodeStructCLinuxLoong(codeModelCLinuxLoong);
    }

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void hello(){
        assertTrue(true);
    }

    @Ignore
    public void testWriteCCodeFiles(){
        codeStructCLinuxLoong.writeCCodeFiles();
    }

    @Ignore
    public void testMakeExeFile() {
        assertTrue("", codeStructCLinuxLoong.makeExeFile());
    }

    @After
    public void tearDown() throws Exception {

    }
}
