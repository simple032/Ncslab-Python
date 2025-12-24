package com.ncslab.block.testrig;

import com.ncslab.database.MdlBlock;
import com.utils.MdlBlockMapper;
import com.utils.Mybatis1Utils;
import com.utils.ResourceReader;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;
import java.util.List;
import java.util.UUID;
import com.ncslab.websocket.CompileWebSocket;
import com.ncslab.websocket.SimulateWebSocket;

public class ServoMotorSliderTest {
    
        private String simulateFilePath = "com/ncslab/websocket/simulateWebsocket.json";
    private String compileFilePath = "com/ncslab/websocket/compileWebsocket.json";
    
    private String blockType = "ServoMotorSliderBlock";
    MdlBlock mdlBlock;
    
    @Before
    public void setUp() {
        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
        MdlBlockMapper mapper = sqlSession.getMapper(MdlBlockMapper.class);
        mdlBlock = mapper.selectByType(blockType);
        sqlSession.commit();
        sqlSession.close();
    }
    
    @Test
    public void simulate() {
        if(mdlBlock == null) {
            fail("No block found for type: " + blockType);
            return;
        }
        
        System.out.println("*****************************************");
        System.out.println("Generating project for " + blockType);
        
        try {
            JSONObject jsonIn = wrapperObject(mdlBlock, simulateFilePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();
            ws.onMessage(null, msgString);
        }
        catch(Exception e) {
            System.err.println("Can not generate project for " + blockType);
            e.printStackTrace();
            fail("Compilation failed for " + blockType + ": " + e.getMessage());
        }
        
        System.out.println("*****************************************");
    }
    
    private JSONObject wrapperObject(MdlBlock mdlBlock, String filePath) {
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        JSONObject mdlData = jsonIn.getJSONObject("mdlData");
        String jsonDataString = mdlData.getString("jsonData");
        JSONObject jsonData = new JSONObject(jsonDataString);
        
        JSONArray jsonBlocks = new JSONArray();
        JSONObject jo = new JSONObject();
        String blockDataString = mdlBlock.getData();
        JSONObject blockData = new JSONObject(blockDataString);
        JSONObject defaults = blockData.getJSONObject("defaults");
        JSONObject props = defaults.getJSONObject("props");
        
        jo.put("blockType", props.getString("blockType").replace("\n", ""));
        jo.put("srcBlock", props.getString("srcBlock"));
        jo.put("blockName", "block1");
        jo.put("paramValues", props.optJSONObject("paramValues", new JSONObject()));
        jo.put("blockPath", "s138880");
        jo.put("blockUUID", UUID.randomUUID().toString());
        jsonBlocks.put(jo);
        
        jsonData.put("blocks", jsonBlocks);
        jsonData.put("lines", new JSONArray());
        mdlData.put("jsonData", jsonData.toString());
        
        return jsonIn;
    }
    
    @After
    public void finish() {
        // Cleanup if needed
    }
}