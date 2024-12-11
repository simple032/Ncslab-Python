package com.ncslab.code.c.windows;

import com.ncslab.WindowsTest;
import com.ncslab.code.c.windows.pc.CodeModelCWindowsPC;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import utils.ResourceReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(WindowsTest.class)
public class CodeModelCWindowsTest {
    private CodeModelCWindowsPC codeModelC;

    @Before
    public void setUp() {
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        try{
            codeModelC = CodeModelCWindowsPC.createFromJSON(jsonIn, ModelMode.Compilation);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @Test
    public void testCodeModelCWindows() {
//        assertEquals("User id should be 35", 35,codeModelC.getUserId());
//        assertEquals("Model id should be 8078", 8078, codeModelC.getModelId());
//        assertEquals("Model name should be s376320", "s376320", codeModelC.getModelName());
//        assertEquals("Model solver should be ode5", Solver.ode5, codeModelC.getSolver());
    }

    @After
    public void tearDown() throws Exception {

    }
}

