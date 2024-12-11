package com.ncslab.code.c.linux.loong;

import com.ncslab.LinuxLoongarchTest;
import com.ncslab.code.Solver;
import com.ncslab.ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import utils.ResourceReader;

import static org.junit.Assert.assertEquals;

@Category(LinuxLoongarchTest.class)
public class CodeModelCLinuxLoongTest {
    private CodeModelCLinuxLoong codeModelC;

    @Before
    public void setUp() throws Exception {
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        codeModelC = CodeModelCLinuxLoong.createFromJSON(jsonIn, ModelMode.Compilation);
    }

    @Test
    public void testCodeModelCLinuxLoongTest() {
        assertEquals("User id should be 35", 35,codeModelC.getUserId());
        assertEquals("Model id should be 8078", 8078, codeModelC.getModelId());
        assertEquals("Model name should be s376320", "s376320", codeModelC.getModelName());
        assertEquals("Model solver should be ode5", Solver.ode5, codeModelC.getSolver());
    }

    @After
    public void tearDown() throws Exception {

    }
}

