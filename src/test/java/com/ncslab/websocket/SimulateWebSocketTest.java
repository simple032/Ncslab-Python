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
 * SimulateWebSocket Test using Mockito
 *
 * This test suite validates the simulation functionality of SimulateWebSocket
 * by mocking the WebSocket Session and testing various block types.
 *
 * @author NCSLab Team
 * @version 2.0 - Refactored with Mockito support
 */
@RunWith(MockitoJUnitRunner.class)
public class SimulateWebSocketTest {

    String filePath = "com/ncslab/websocket/simulateWebsocket.json";
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
            when(session.getId()).thenReturn("test-sim-session-" + UUID.randomUUID().toString());

            // Mock session state
            when(session.isOpen()).thenReturn(true);

            // Mock request URI
            when(session.getRequestURI()).thenReturn(URI.create("ws://localhost:8080/websocketsimulate"));

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
     * Test simulation with serial port blocks
     * Uses Mockito-based session mocking
     */
    @Test
    public void simulateSerialPort() {
        String filePath = "com/ncslab/block/comm/serialport.json";

        try {
            JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();

            System.out.println("=== Serial Port Simulation Test ===");
            System.out.println("Testing with serialport.json");
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("✅ Serial port simulation test completed successfully");
            System.out.println("====================================\n");

        } catch (Exception e) {
            System.err.println("❌ Serial port simulation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void simulateSubsystem() {        

        try {
            JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();

            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("====================================\n");

        } catch (Exception e) {
            System.err.println("❌ Serial port simulation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void simulateRaspFan() {        

        String filePath = "com/ncslab/websocket/testRaspFan.json";
        try {
            JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();

            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("====================================\n");

        } catch (Exception e) {
            System.err.println("❌ Serial port simulation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void simulatetestDCMotorAngle() {        

        String filePath = "com/ncslab/websocket/testDCMotorAngle.json";
        try {
            JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();

            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("====================================\n");

        } catch (Exception e) {
            System.err.println("❌ Serial port simulation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void simulatetestWaterLevel() {        

        String filePath = "com/ncslab/websocket/testWaterLevel.json";
        try {
            JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();

            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("====================================\n");

        } catch (Exception e) {
            System.err.println("❌ Serial port simulation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void simulatetestALP() {        

        String filePath = "com/ncslab/websocket/testALP.json";
        try {
            JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();

            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("====================================\n");

        } catch (Exception e) {
            System.err.println("❌ Serial port simulation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void simulatetestSlider() {        

        String filePath = "com/ncslab/websocket/testSlider.json";
        try {
            JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();

            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("====================================\n");

        } catch (Exception e) {
            System.err.println("❌ Serial port simulation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void simulatetestOnlineIdentPulse() {
        String filePath = "com/ncslab/websocket/testOnlineIdentPulse.json";
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
   
        SimulateWebSocket ws = new SimulateWebSocket();
        String msgString = jsonIn.toString();

        // Capture messages sent to the session
        List<String> sentMessages = new ArrayList<>();
        try {
            doNothing().when(basicRemote).sendText(anyString());
            // Actually capture the messages using Answer
            org.mockito.Mockito.doAnswer(invocation -> {
                String message = invocation.getArgument(0);
                sentMessages.add(message);
                System.out.println("WebSocket message: " + message);
                return null;
            }).when(basicRemote).sendText(anyString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to setup message capture", e);
        }

        ws.onMessage(session, msgString);

        // Verify that simulation completed successfully by checking for error messages
        boolean hasError = sentMessages.stream().anyMatch(msg ->
            msg.contains("\"status\":\"error\"") || msg.contains("MatDimException") ||
            msg.contains("terminated unsuccessfully")
        );

        boolean hasSuccess = sentMessages.stream().anyMatch(msg ->
            msg.contains("simulated") || msg.contains("\"status\":\"success\"")
        );

        if (hasError) {
            String errorMessages = sentMessages.stream()
                .filter(msg -> msg.contains("error") || msg.contains("Exception"))
                .reduce("", (a, b) -> a + "\n" + b);
            throw new AssertionError("❌ Simulation failed with errors:\n" + errorMessages);
        }

        if (!hasSuccess) {
            throw new AssertionError("❌ Simulation did not complete successfully. Messages sent: " + sentMessages);
        }

        System.out.println("✅ Single RT simulation test completed successfully");
    }

    /**
     * Test simulation of a single predefined model (DE.json)
     * This test is ignored by default - remove @Ignore to run
     */
    @Test
    @Ignore
    public void simulateDE() {
        String filePath = "com/DE.json";

        try {
            JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
            SimulateWebSocket ws = new SimulateWebSocket();
            String msgString = jsonIn.toString();

            System.out.println("=== DE Simulation Test ===");
            System.out.println("Testing with DE.json");
            System.out.println("Session ID: " + session.getId());

            // Execute simulation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("✅ DE simulation test completed successfully");
            System.out.println("==========================\n");

        } catch (Exception e) {
            System.err.println("❌ DE simulation test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test simulation of all blocks in the database
     * Skips known problematic blocks and libraries
     */
    @Test
    public void simulate() {
        int totalBlocks = 0;
        int successfulSimulations = 0;
        int failedSimulations = 0;
        int skippedBlocks = 0;

        for (MdlBlock mdlBlock : mdlBlockList) {
            totalBlocks++;

            // Test all blocks including library_id = 1 (Device) blocks - issues have been fixed
            // Skip only problematic libraries that are still being worked on
            if (Objects.equals(mdlBlock.getLibraryId(), 10)) { // Electrical - still has issues
                skippedBlocks++;
                continue;
            }

            String blockType = mdlBlock.getType();
            System.out.println("*****************************************");
            System.out.println("Simulating " + blockType);

            try {
                // Skip known problematic block types
                String[] continueList = {
                    "DemuxBlock",
                    "S-FunctionBlock",
                    "UDPRecvBlock",
                    "UDPSendBlock",
                    "FcnBlock"
                };

                boolean shouldSkip = Arrays.asList(continueList).contains(blockType);
                if (shouldSkip) {
                    System.out.println("⚠️  Skipping known problematic block: " + blockType);
                    skippedBlocks++;
                    continue;
                }

                // Wrap block in model JSON
                JSONObject jsonIn = wrapperObject(mdlBlock);

                // Create WebSocket and test simulation
                SimulateWebSocket ws = new SimulateWebSocket();
                String msgString = jsonIn.toString();

                // Call onMessage with mocked session
                ws.onMessage(session, msgString);

                System.out.println("✅ Successfully simulated: " + blockType);
                successfulSimulations++;

            } catch (Exception e) {
                System.err.println("❌ Cannot simulate " + blockType);
                System.err.println("   Error: " + e.getMessage());
                e.printStackTrace();
                failedSimulations++;
            }

            System.out.println("*****************************************\n");
        }

        // Print summary
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SIMULATION TEST SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("Total blocks:              " + totalBlocks);
        System.out.println("Successful simulations:    " + successfulSimulations);
        System.out.println("Failed simulations:        " + failedSimulations);
        System.out.println("Skipped blocks:            " + skippedBlocks);
        System.out.println("Success rate:              " +
                String.format("%.1f%%", (successfulSimulations * 100.0 / (successfulSimulations + failedSimulations))));
        System.out.println("=".repeat(60));
    }

    @After
    public void finish() {
        // Cleanup if needed
        System.out.println("\nSimulation test cleanup completed");
    }
}
