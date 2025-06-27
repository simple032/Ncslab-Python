package com.ncslab.code.c.windows.simulation;

import com.ncslab.WindowsTest;
//import com.ncslab.code.c.windows.CodeModelCWindows;
import com.ncslab.database.MdlBlock;
import com.ncslab.ncslablink.ModelException;
import com.ncslab.ncslablink.ModelMode;
import com.utils.MdlBlockMapper;
import com.utils.Mybatis1Utils;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import utils.ResourceReader;


import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@Category(WindowsTest.class)
public class CodeModelCWindowsSimulationTest {
    private CodeModelCWindowsSimulation codeModelC;
    String filePath = "simulate.json"; // 替换为实际文件路径

    void retrieveFromDatabase(){
        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
        MdlBlockMapper mapper = sqlSession.getMapper( MdlBlockMapper.class);
        List<MdlBlock> mdlBlockList =  mapper.selectAll();
        sqlSession.commit();
        sqlSession.close();
    }

    @Before
    public void setUp() {




    }

    @Test
    public void testCodeModelCWindows() {
        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
        MdlBlockMapper mapper = sqlSession.getMapper( MdlBlockMapper.class);
        List<MdlBlock> mdlBlockList =  mapper.selectAll();
        sqlSession.commit();
        sqlSession.close();
        for(MdlBlock mdlBlock : mdlBlockList){
            try{
                String[] continueList = { "DemuxBlock", "S-FunctionBlock" };
                boolean found = Arrays.stream(continueList).anyMatch(mdlBlock.getType()::equals);
                if (found) {
                    continue;
                }
                JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
                JSONArray jsonBlock = jsonIn.getJSONArray("blocks");
                JSONObject jo = new JSONObject();
                String blockData = mdlBlock.getData();
                JSONObject jsonData = new JSONObject(blockData);
                JSONObject defaults = jsonData.getJSONObject("defaults");
                JSONObject props = defaults.getJSONObject("props");

                jo.put("blockType", props.getString("blockType"));
                jo.put("srcBlock", props.getString("srcBlock"));
                jo.put("blockName", props.getString("title"));

                jo.put("paramValues", props.optJSONObject("paramValues", new JSONObject()));
                jo.put("blockPath", "s138880");
                jsonBlock.put(jo);
                codeModelC = CodeModelCWindowsSimulation.createFromJSON(jsonIn, ModelMode.Simulation);
            }
            catch (ModelException e){
                System.err.println("Block:" + mdlBlock.getType() + ":" + e.getMessage());
            }
            catch(Exception e){
                fail("Can not generate project for " + mdlBlock.getType());
                e.printStackTrace();
            }

        }
    }

    @After
    public void tearDown() throws Exception {

    }
}

