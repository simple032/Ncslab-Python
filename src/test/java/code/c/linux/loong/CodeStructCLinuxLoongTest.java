package code.c.linux.loong;

import com.ncslab.LinuxLoongarchTest;
import com.ncslab.code.c.linux.loong.CodeModelCLinuxLoong;
import com.ncslab.code.c.linux.loong.CodeStructCLinuxLoong;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import utils.ResourceReader;

import static org.junit.Assert.assertTrue;

@Category(LinuxLoongarchTest.class)
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
