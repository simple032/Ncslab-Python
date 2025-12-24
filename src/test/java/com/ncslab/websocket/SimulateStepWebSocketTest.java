package com.ncslab.websocket;

import com.ncslab.database.MdlBlock;
import com.utils.MdlBlockMapper;
import com.utils.Mybatis1Utils;
import com.utils.ResourceReader;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.websocket.Session;
import jakarta.websocket.RemoteEndpoint;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

/**
 * SimulateStepWebSocket Test using Mockito
 *
 * This test suite validates the step-by-step simulation functionality of SimulateStepWebSocket
 * by mocking the WebSocket Session and testing various block types.
 *
 * @author NCSLab Team
 * @version 2.0 - Refactored with Mockito support
 */
@RunWith(MockitoJUnitRunner.class)
public class SimulateStepWebSocketTest {

    String filePath = "simulateWebsocket.json";
    List<MdlBlock> mdlBlockList = null;

    @Mock
    private Session session;

    @Mock
    private RemoteEndpoint.Basic basicRemote;

    @Before
    public void setUp() {
        // Load all blocks from database
        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
        MdlBlockMapper mapper = sqlSession.getMapper(MdlBlockMapper.class);
        mdlBlockList = mapper.selectAll();
        sqlSession.commit();
        sqlSession.close();

        // Setup mock Session behavior
        setupMockSession();
    }

    /**
     * Configure mock Session with typical WebSocket behavior
     */
    private void setupMockSession() {
        try {
            // Mock session ID
            when(session.getId()).thenReturn("test-step-session-" + UUID.randomUUID().toString());

            // Mock session state
            when(session.isOpen()).thenReturn(true);

            // Mock request URI
            when(session.getRequestURI()).thenReturn(URI.create("ws://localhost:8080/websocketsimulatestep"));

            // Mock remote endpoint for sending messages
            when(session.getBasicRemote()).thenReturn(basicRemote);

            // Mock sendText to do nothing (or capture for assertions)
            doNothing().when(basicRemote).sendText(anyString());

            // Mock buffer sizes
            doNothing().when(session).setMaxTextMessageBufferSize(1024 * 1024);
            doNothing().when(session).setMaxBinaryMessageBufferSize(1024 * 1024);
            doNothing().when(session).setMaxIdleTimeout(300000L);

        } catch (IOException e) {
            System.err.println("Error setting up mock session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Wrap a single MdlBlock into a complete model JSON for testing
     *
     * @param mdlBlock The block to test
     * @return JSONObject ready for WebSocket message
     */
    JSONObject wrapperObject(MdlBlock mdlBlock) {
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

    /**
     * Test step-by-step simulation with a predefined test model
     * Uses Mockito-based session mocking
     */
    @Test
    public void simulate() {
        SimulateStepWebSocket ws = new SimulateStepWebSocket();
        String msgString;

        try {
            // Read test JSON file
            msgString = ResourceReader.readResourceAsString("com/ncslab/websocket/stepWebsocket.json");

            System.out.println("=== Step-by-Step Simulation Test ===");
            System.out.println("Testing with stepWebsocket.json");
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute step-by-step simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("✅ Step-by-step simulation test completed successfully");
            System.out.println("=====================================\n");

        } catch (IOException e) {
            System.err.println("❌ IOException during step simulation test: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected exception during step simulation test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @After
    public void finish() {
        // Cleanup if needed
        System.out.println("\nStep simulation test cleanup completed");
    }
}
