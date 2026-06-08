package com.ncslab.block;

import com.ncslab.test.category.NativeTest;
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
import org.junit.experimental.categories.Category;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ncslab.websocket.SimulateWebSocket;

@Category(NativeTest.class)
public class CompilationOutputTest {

    String filePath = "com/ncslab/websocket/simulateWebsocket.json";
    List<MdlBlock> mdlBlockList = null;
    
    // Thread-safe collections to capture output
    private final ConcurrentLinkedQueue<String> compilationOutput = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> compilationErrors = new ConcurrentLinkedQueue<>();
    
    // Original streams
    private PrintStream originalOut;
    private PrintStream originalErr;
    
    @Before
    public void setUp(){
        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
        MdlBlockMapper mapper = sqlSession.getMapper(MdlBlockMapper.class);
        mdlBlockList = mapper.selectAll();
        sqlSession.commit();
        sqlSession.close();
        
        // Capture original streams
        originalOut = System.out;
        originalErr = System.err;
        
        // Set up custom output streams to capture compilation output
        System.setOut(new PrintStream(new OutputStream() {
            private StringBuilder buffer = new StringBuilder();
            
            @Override
            public void write(int b) throws IOException {
                buffer.append((char) b);
                if (b == '\n') {
                    String line = buffer.toString();
                    compilationOutput.add(line);
                    originalOut.print(line); // Still print to console
                    buffer.setLength(0);
                }
            }
        }));
        
        System.setErr(new PrintStream(new OutputStream() {
            private StringBuilder buffer = new StringBuilder();
            
            @Override
            public void write(int b) throws IOException {
                buffer.append((char) b);
                if (b == '\n') {
                    String line = buffer.toString();
                    compilationErrors.add(line);
                    originalErr.print(line); // Still print to console
                    buffer.setLength(0);
                }
            }
        }));
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
    
    private CompilationResult analyzeCompilationOutput(String blockType) {
        CompilationResult result = new CompilationResult(blockType);
        
        // Analyze output for key compilation stages
        for (String line : compilationOutput) {
            if (line.contains("Making exe file ncslab")) {
                result.compilationStarted = true;
            }
            if (line.contains("Exe file ncslab created!")) {
                result.compilationSuccessful = true;
            }
            if (line.contains("Cannot create exe file ncslab!")) {
                result.compilationFailed = true;
            }
            if (line.contains("generating")) {
                result.codeGenerationStarted = true;
            }
            if (line.contains("generated")) {
                result.codeGenerationCompleted = true;
            }
            if (line.contains("compiling")) {
                result.makeFileExecutionStarted = true;
            }
            if (line.contains("compiled")) {
                result.makeFileExecutionCompleted = true;
            }
        }
        
        // Analyze errors
        for (String line : compilationErrors) {
            result.errorMessages.add(line.trim());
            if (line.contains("error:") || line.contains("Error:")) {
                result.hasCompilationErrors = true;
            }
            if (line.contains("warning:") || line.contains("Warning:")) {
                result.hasWarnings = true;
            }
        }
        
        return result;
    }

    @Test
    public void testCompilationOutput(){
        int totalTested = 0;
        int successful = 0;
        int failed = 0;
        
        System.out.println("=== COMPILATION OUTPUT TEST STARTED ===");
        
        for(MdlBlock mdlBlock : mdlBlockList){
            if(Objects.equals(mdlBlock.getLibraryId(), 10)){ // Skip electrical blocks
                continue;
            }
            
            String blockType = mdlBlock.getType();
            
            // Test a representative sample of blocks
            String[] testBlocks = {
                "ConstantBlock", "AddBlock", "GainBlock", "IntegratorBlock", 
                "ScopeBlock", "SineWaveBlock", "StepBlock", "SumBlock",
                "ProductBlock", "AbsBlock", "SaturationBlock", "RelayBlock"
            };
            
            if (!Arrays.asList(testBlocks).contains(blockType)) {
                continue;
            }
            
            totalTested++;
            
            // Clear previous output
            compilationOutput.clear();
            compilationErrors.clear();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("TESTING BLOCK: " + blockType);
            System.out.println("=".repeat(60));
            
            try{
                String[] continueList = { "DemuxBlock", "S-FunctionBlock","UDPRecvBlock","UDPSendBlock", "FcnBlock"};
                boolean found = Arrays.asList(continueList).contains(blockType);

                if (found) {
                    System.out.println("SKIPPED: " + blockType + " (in skip list)");
                    continue;
                }

                JSONObject jsonIn = wrapperObject(mdlBlock);

                SimulateWebSocket ws = new SimulateWebSocket();
                String msgString = jsonIn.toString();
                
                // Execute the simulation/compilation
                ws.onMessage(null, msgString);
                
                // Wait a moment for output to be captured
                Thread.sleep(1000);
                
                // Analyze the compilation output
                CompilationResult result = analyzeCompilationOutput(blockType);
                
                System.out.println("\n--- COMPILATION ANALYSIS FOR " + blockType + " ---");
                System.out.println("Code Generation Started: " + result.codeGenerationStarted);
                System.out.println("Code Generation Completed: " + result.codeGenerationCompleted);
                System.out.println("Compilation Started: " + result.compilationStarted);
                System.out.println("MakeFile Execution Started: " + result.makeFileExecutionStarted);
                System.out.println("MakeFile Execution Completed: " + result.makeFileExecutionCompleted);
                System.out.println("Compilation Successful: " + result.compilationSuccessful);
                System.out.println("Compilation Failed: " + result.compilationFailed);
                System.out.println("Has Compilation Errors: " + result.hasCompilationErrors);
                System.out.println("Has Warnings: " + result.hasWarnings);
                System.out.println("Error Count: " + result.errorMessages.size());
                
                if (!result.errorMessages.isEmpty()) {
                    System.out.println("\nERROR MESSAGES:");
                    for (String error : result.errorMessages) {
                        if (!error.trim().isEmpty()) {
                            System.out.println("  " + error);
                        }
                    }
                }
                
                // Determine overall success
                boolean overallSuccess = result.codeGenerationCompleted && 
                                       !result.compilationFailed && 
                                       !result.hasCompilationErrors;
                
                if (overallSuccess) {
                    successful++;
                    System.out.println("\n✅ OVERALL RESULT: SUCCESS");
                } else {
                    failed++;
                    System.out.println("\n❌ OVERALL RESULT: FAILED");
                }
                
            } catch(Exception e){
                failed++;
                System.out.println("\n❌ EXCEPTION OCCURRED: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Final summary
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FINAL COMPILATION TEST SUMMARY");
        System.out.println("=".repeat(80));
        System.out.println("Total Blocks Tested: " + totalTested);
        System.out.println("Successful Compilations: " + successful);
        System.out.println("Failed Compilations: " + failed);
        System.out.println("Success Rate: " + (totalTested > 0 ? (successful * 100.0 / totalTested) : 0) + "%");
        System.out.println("=".repeat(80));
    }

    @After
    public void tearDown(){
        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    
    // Helper class to store compilation results
    private static class CompilationResult {
        String blockType;
        boolean codeGenerationStarted = false;
        boolean codeGenerationCompleted = false;
        boolean compilationStarted = false;
        boolean makeFileExecutionStarted = false;
        boolean makeFileExecutionCompleted = false;
        boolean compilationSuccessful = false;
        boolean compilationFailed = false;
        boolean hasCompilationErrors = false;
        boolean hasWarnings = false;
        List<String> errorMessages = new java.util.ArrayList<>();
        
        CompilationResult(String blockType) {
            this.blockType = blockType;
        }
    }
}
