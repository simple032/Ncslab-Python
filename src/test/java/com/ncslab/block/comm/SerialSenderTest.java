package com.ncslab.block.comm;

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

import java.util.Arrays;
import java.util.UUID;

import com.ncslab.websocket.CompileWebSocket;
import com.ncslab.websocket.SimulateWebSocket;
import com.ncslab.websocket.SimulateRTWebSocket;

/**
 * Test class for SerialSender block.
 * Tests serial communication sender functionality with various configurations.
 */
public class SerialSenderTest {

    private String simulateFilePath = "com/ncslab/websocket/simulateWebsocket.json";
    private String compileFilePath = "com/ncslab/websocket/compileWebsocket.json";

    private String blockType = "SerialSender";
    MdlBlock mdlBlock;

    @Before
    public void setUp(){
        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
        MdlBlockMapper mapper = sqlSession.getMapper(MdlBlockMapper.class);
        mdlBlock = mapper.selectByType(blockType);
        sqlSession.commit();
        sqlSession.close();
    }

    private JSONObject wrapperObject(MdlBlock mdlBlock, String filePath){
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
        jo.put("blockName", "SerialSender1");

        jo.put("paramValues", props.optJSONObject("paramValues", new JSONObject()));
        jo.put("blockPath", "s" + UUID.randomUUID().toString().substring(0, 6));
        jo.put("blockUUID", UUID.randomUUID().toString());
        jsonBlocks.put(jo);

        jsonData.put("blocks", jsonBlocks);
        jsonData.put("lines", new JSONArray());

        mdlData.put("jsonData", jsonData.toString());
        return jsonIn;
    }

    @Test
    public void simulate(){
        System.out.println("*****************************************");
        System.out.println("Generating simulation project for " + blockType);

        try {
            JSONObject jsonIn = wrapperObject(mdlBlock, simulateFilePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();
            ws.onMessage(null, msgString);
        }
        catch(Exception e) {
            System.err.println("Cannot generate simulation project for " + blockType);
            e.printStackTrace();
            fail("Simulation failed for " + blockType + ": " + e.getMessage());
        }

        System.out.println("*****************************************");
    }

    @Test
    public void rt_simulate(){
        System.out.println("*****************************************");
        System.out.println("Generating real-time simulation project for " + blockType);

        try {
            JSONObject jsonIn = wrapperObject(mdlBlock, simulateFilePath);
            SimulateRTWebSocket ws = new SimulateRTWebSocket();
            String msgString = jsonIn.toString();
            ws.onMessage(null, msgString);
        }
        catch(Exception e) {
            System.err.println("Cannot generate real-time simulation project for " + blockType);
            e.printStackTrace();
            fail("Real-time simulation failed for " + blockType + ": " + e.getMessage());
        }

        System.out.println("*****************************************");
    }

    @Test
    public void compile(){
        System.out.println("*****************************************");
        System.out.println("Compiling project for " + blockType);

        try {
            JSONObject jsonIn = wrapperObject(mdlBlock, compileFilePath);
            CompileWebSocket ws = new CompileWebSocket();
            String msgString = jsonIn.toString();
            ws.onMessage(null, msgString);
        }
        catch(Exception e) {
            System.err.println("Cannot compile project for " + blockType);
            e.printStackTrace();
            fail("Compilation failed for " + blockType + ": " + e.getMessage());
        }

        System.out.println("*****************************************");
    }

    @After
    public void finish(){
        // Cleanup if needed
    }
}
