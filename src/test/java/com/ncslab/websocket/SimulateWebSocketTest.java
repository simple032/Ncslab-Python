package com.ncslab.websocket;

import com.ncslab.database.MdlBlock;
import com.utils.MdlBlockMapper;
import com.utils.Mybatis1Utils;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import utils.ResourceReader;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.fail;

public class SimulateWebSocketTest {

    String filePath = "simulateWebsocket.json";//"websocketCompile.json"; // 替换为实际文件路径
    List<MdlBlock> mdlBlockList = null;
    @Before
    public void setUp(){
        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
        MdlBlockMapper mapper = sqlSession.getMapper( MdlBlockMapper.class);
        mdlBlockList =  mapper.selectAll();
        sqlSession.commit();
        sqlSession.close();
    }

    JSONObject wrapperObject(MdlBlock mdlBlock){
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

    @Ignore
    public void simulateOnce(){
        String filePath = "com/DE.json";//"websocketCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        SimulateWebSocket ws = new SimulateWebSocket();
        String msgString = jsonIn.toString();
        ws.onMessage(null, msgString);
    }

    @Test
    public void simulate(){
        for(MdlBlock mdlBlock : mdlBlockList){
            if(Objects.equals(mdlBlock.getLibraryId(), 1)){ //Device
                continue;
            }
            if(Objects.equals(mdlBlock.getLibraryId(), 10)){ //Electrical
                continue;
            }
            String blockType = mdlBlock.getType();
            System.out.println("Generating project for " + blockType);
            try{
                String[] continueList = { "DemuxBlock", "S-FunctionBlock","UDPRecvBlock","UDPSendBlock", };
                String[] allowList = { "TransferFcnBlock"};
                boolean found = Arrays.asList(continueList).contains(blockType);

                if (found) {
                    continue;
                }

//                found = Arrays.asList(allowList).contains(mdlBlock.getType());
//                if (!found) {
//                    continue;
//                }

                JSONObject jsonIn = wrapperObject(mdlBlock);

                SimulateWebSocket ws = new SimulateWebSocket();
                String msgString = jsonIn.toString();
                ws.onMessage(null, msgString);
            }
            catch(Exception e){
//                fail("Can not generate project for " + blockType);
                System.err.println("Can not generate project for " + blockType);
                e.printStackTrace();
            }
        }
    }



    @After
    public void finish(){

    }
}
