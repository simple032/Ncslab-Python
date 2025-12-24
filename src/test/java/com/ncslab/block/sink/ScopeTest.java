package com.ncslab.block.sink;

import com.ncslab.database.MdlBlock;
import com.ncslab.websocket.CompileWebSocket;
import com.ncslab.websocket.SimulateRTWebSocket;
import com.ncslab.websocket.SimulateStepWebSocket;
import com.ncslab.websocket.SimulateWebSocket;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ScopeTest {

    private String simulateFilePath = "com/ncslab/websocket/simulateWebsocket.json";
    private String compileFilePath = "com/ncslab/websocket/compileWebsocket.json";
    
    private String blockType = "ScopeBlock";
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

    @Test
    public void simulate(){        
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
            fail("Simulation failed for " + blockType + ": " + e.getMessage());
        }
        
        System.out.println("*****************************************");
    }

    @Test
    public void rt_simulate(){        
        System.out.println("*****************************************");
        System.out.println("Generating project for " + blockType);
        
        try {
            JSONObject jsonIn = wrapperObject(mdlBlock, simulateFilePath);
            SimulateRTWebSocket ws = new SimulateRTWebSocket();
            String msgString = jsonIn.toString();
            ws.onMessage(null, msgString);
        }
        catch(Exception e) {
            System.err.println("Can not generate project for " + blockType);
            e.printStackTrace();
            fail("Simulation failed for " + blockType + ": " + e.getMessage());
        }
        
        System.out.println("*****************************************");
    }

    @Test
    public void step_simulate(){        
        System.out.println("*****************************************");
        System.out.println("Step Control Simulation for " + blockType);
        
        try {
            JSONObject jsonIn = wrapperObject(mdlBlock, simulateFilePath);
            SimulateStepWebSocket ws = new SimulateStepWebSocket();
            String msgString = jsonIn.toString();
            
            // Step 1: Initialize simulation with start command
            System.out.println("Step 1: Sending start command...");
            ws.onMessage(null, msgString);
            
            // Step 2: Execute multiple step forward commands to collect scope data
            System.out.println("Step 2: Executing step forward commands...");
            for (int i = 1; i <= 5; i++) {
                String stepForwardString = createStepForwardMessage(i, 0.01);
                System.out.println("  Executing step " + i + "...");
                ws.onMessage(null, stepForwardString);
            }
            
            // Step 3: Test additional step control commands
            System.out.println("Step 3: Testing additional step control commands...");
            
            // Test get_results command
            String getResultsString = createGetResultsMessage();
            ws.onMessage(null, getResultsString);
            
            // Test stream_current_state command
            String streamStateString = createStreamCurrentStateMessage();
            ws.onMessage(null, streamStateString);
    
            
            // Execute a few more steps after resume
            for (int i = 6; i <= 8; i++) {
                String stepForwardString = createStepForwardMessage(1, 0.01);
                System.out.println("  Executing step " + i + " after resume...");
                ws.onMessage(null, stepForwardString);
            }
            
            System.out.println("Step control simulation completed successfully for " + blockType);
            
        }
        catch(Exception e) {
            System.err.println("Can not execute step simulation for " + blockType);
            e.printStackTrace();
            fail("Step simulation failed for " + blockType + ": " + e.getMessage());
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
            System.err.println("Can not compile project for " + blockType);
            e.printStackTrace();
            fail("Compilation failed for " + blockType + ": " + e.getMessage());
        }
        
        System.out.println("*****************************************");
    }
    
    /**
     * Create step forward message based on SimulateStepWebSocket.handleStepForwardCommand logic
     * @param steps Number of steps to execute
     * @param customStepSize Custom step size (can be null for default)
     * @return JSON string for step_forward command
     */
    private String createStepForwardMessage(int steps, double customStepSize) {
        JSONObject stepMessage = new JSONObject();
        stepMessage.put("com", "step_forward");
        stepMessage.put("steps", steps);
        stepMessage.put("customStepSize", customStepSize);
        stepMessage.put("saveCheckpoint", false);
        stepMessage.put("timestamp", System.currentTimeMillis());
        return stepMessage.toString();
    }
    
    /**
     * Create get_results message
     * @return JSON string for get_results command
     */
    private String createGetResultsMessage() {
        JSONObject message = new JSONObject();
        message.put("com", "get_results");
        message.put("timestamp", System.currentTimeMillis());
        return message.toString();
    }
    
    /**
     * Create stream_current_state message
     * @return JSON string for stream_current_state command
     */
    private String createStreamCurrentStateMessage() {
        JSONObject message = new JSONObject();
        message.put("com", "stream_current_state");
        message.put("timestamp", System.currentTimeMillis());
        return message.toString();
    }
    
    /**
     * Create pause message
     * @return JSON string for pause command
     */
    private String createPauseMessage() {
        JSONObject message = new JSONObject();
        message.put("com", "pause");
        message.put("timestamp", System.currentTimeMillis());
        return message.toString();
    }
    
    /**
     * Create resume message
     * @return JSON string for resume command
     */
    private String createResumeMessage() {
        JSONObject message = new JSONObject();
        message.put("com", "resume");
        message.put("timestamp", System.currentTimeMillis());
        return message.toString();
    }
    
    /**
     * Create ping message for connectivity testing
     * @return JSON string for ping command
     */
    private String createPingMessage() {
        JSONObject message = new JSONObject();
        message.put("com", "ping");
        message.put("timestamp", System.currentTimeMillis());
        return message.toString();
    }
    
    /**
     * Create goto_time message
     * @param targetTime Target simulation time to jump to
     * @return JSON string for goto_time command
     */
    private String createGotoTimeMessage(double targetTime) {
        JSONObject message = new JSONObject();
        message.put("com", "goto_time");
        message.put("targetTime", targetTime);
        message.put("timestamp", System.currentTimeMillis());
        return message.toString();
    }
    
    /**
     * Create step_backward message
     * @param steps Number of steps to go backward
     * @param targetTime Optional target time (can be null)
     * @return JSON string for step_backward command
     */
    private String createStepBackwardMessage(int steps, Double targetTime) {
        JSONObject message = new JSONObject();
        message.put("com", "step_backward");
        message.put("steps", steps);
        if (targetTime != null) {
            message.put("targetTime", targetTime);
        }
        message.put("timestamp", System.currentTimeMillis());
        return message.toString();
    }
    
    /**
     * Create run_to_end message
     * @param endTime Optional end time (can be null to use model's stop time)
     * @param stepSize Optional step size (can be null to use current step size)
     * @param enableStreaming Whether to enable streaming updates
     * @return JSON string for run_to_end command
     */
    private String createRunToEndMessage(Double endTime, Double stepSize, boolean enableStreaming) {
        JSONObject message = new JSONObject();
        message.put("com", "run_to_end");
        if (endTime != null) {
            message.put("endTime", endTime);
        }
        if (stepSize != null) {
            message.put("stepSize", stepSize);
        }
        message.put("enableStreaming", enableStreaming);
        message.put("timestamp", System.currentTimeMillis());
        return message.toString();
    }

    @After
    public void finish(){
    }
}