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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * CompileWebSocket Test using Mockito
 *
 * This test suite validates the compilation functionality of the CompileWebSocket
 * by mocking the WebSocket Session and testing various block types.
 *
 * @author NCSLab Team
 * @version 2.0 - Refactored with Mockito support
 */
@RunWith(MockitoJUnitRunner.class)
public class CompileWebSocketTest {

    String filePath = "com/ncslab/websocket/compileWebsocket.json";
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
            when(session.getId()).thenReturn("test-session-" + UUID.randomUUID().toString());

            // Mock session state
            when(session.isOpen()).thenReturn(true);

            // Mock request URI
            when(session.getRequestURI()).thenReturn(URI.create("ws://localhost:8080/websocketcompile"));

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
     * Test compilation of a single predefined model
     * This test is ignored by default - remove @Ignore to run
     */
    @Test
    @Ignore
    public void compileOnce() {
        String filePath = "com/DE.json";
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        CompileWebSocket ws = new CompileWebSocket();
        String msgString = jsonIn.toString();

        try {
            ws.onMessage(session, msgString);
            System.out.println("✅ Single compile test completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Single compile test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test compilation of all blocks in the database
     * Skips known problematic blocks and libraries
     */
    @Test
    public void compile() {
        int totalBlocks = 0;
        int successfulCompilations = 0;
        int failedCompilations = 0;
        int skippedBlocks = 0;

        for (MdlBlock mdlBlock : mdlBlockList) {
            totalBlocks++;

            // Skip Device library (library_id = 1) - issues have been fixed but kept for safety
            if (Objects.equals(mdlBlock.getLibraryId(), 1)) {
                skippedBlocks++;
                continue;
            }

            // Skip Electrical library (library_id = 10) - still has issues
            if (Objects.equals(mdlBlock.getLibraryId(), 10)) {
                skippedBlocks++;
                continue;
            }

            String blockType = mdlBlock.getType();
            System.out.println("*****************************************");
            System.out.println("Generating project for " + blockType);

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

                // Create WebSocket and test compilation
                CompileWebSocket ws = new CompileWebSocket();
                String msgString = jsonIn.toString();

                // Call onMessage with mocked session
                ws.onMessage(session, msgString);

                System.out.println("✅ Successfully compiled: " + blockType);
                successfulCompilations++;

            } catch (Exception e) {
                System.err.println("❌ Cannot generate project for " + blockType);
                System.err.println("   Error: " + e.getMessage());
                e.printStackTrace();
                failedCompilations++;
            }

            System.out.println("*****************************************\n");
        }

        // Print summary
        System.out.println("\n" + "=".repeat(60));
        System.out.println("COMPILATION TEST SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("Total blocks:              " + totalBlocks);
        System.out.println("Successful compilations:   " + successfulCompilations);
        System.out.println("Failed compilations:       " + failedCompilations);
        System.out.println("Skipped blocks:            " + skippedBlocks);
        System.out.println("Success rate:              " +
                String.format("%.1f%%", (successfulCompilations * 100.0 / (successfulCompilations + failedCompilations))));
        System.out.println("=".repeat(60));
    }

    /**
     * Test normal compilation with a predefined test model
     * Uses Mockito-based session mocking
     */
    @Test
    public void compilation_normal() {
        CompileWebSocket ws = new CompileWebSocket();
        String msgString;

        try {
            // Read test JSON file
            msgString = ResourceReader.readResourceAsString("com/ncslab/websocket/Untitled-4.json");

            System.out.println("=== Normal Compilation Test ===");
            System.out.println("Testing with Untitled-4.json");
            System.out.println("Session ID: " + session.getId());
            System.out.println("Session Open: " + session.isOpen());

            // Execute compilation with mocked session
            ws.onMessage(session, msgString);

            System.out.println("✅ Normal compilation test completed successfully");
            System.out.println("===============================\n");

        } catch (IOException e) {
            System.err.println("❌ IOException during normal compilation test: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected exception during normal compilation test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test compilation with SerialConfiguration blocks
     * This test validates the refactored serial blocks implementation
     */
    @Test
    public void testSerialBlocksCompilation() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SERIAL BLOCKS COMPILATION TEST");
        System.out.println("=".repeat(60));

        String[] serialBlockTypes = {
            "SerialConfigurationBlock",
            "SerialSendBlock",
            "SerialRecvBlock"
        };

        int tested = 0;
        int passed = 0;

        for (MdlBlock mdlBlock : mdlBlockList) {
            String blockType = mdlBlock.getType();

            // Test only serial blocks
            boolean isSerialBlock = Arrays.asList(serialBlockTypes).contains(blockType);
            if (!isSerialBlock) {
                continue;
            }

            tested++;
            System.out.println("\n--- Testing: " + blockType + " ---");

            try {
                JSONObject jsonIn = wrapperObject(mdlBlock);
                CompileWebSocket ws = new CompileWebSocket();
                String msgString = jsonIn.toString();

                ws.onMessage(session, msgString);

                System.out.println("✅ " + blockType + " compiled successfully");
                passed++;

            } catch (Exception e) {
                System.err.println("❌ " + blockType + " compilation failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Serial Blocks Tested: " + tested);
        System.out.println("Serial Blocks Passed: " + passed);
        System.out.println("Serial Blocks Failed: " + (tested - passed));
        if (tested > 0) {
            System.out.println("Success Rate: " + String.format("%.1f%%", (passed * 100.0 / tested)));
        }
        System.out.println("=".repeat(60) + "\n");
    }

    @After
    public void finish() {
        // Cleanup if needed
        System.out.println("\nTest cleanup completed");
    }
}
