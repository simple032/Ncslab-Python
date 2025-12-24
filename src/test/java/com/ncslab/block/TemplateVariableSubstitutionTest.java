package com.ncslab.block;

import com.ncslab.database.MdlBlock;
import com.utils.MdlBlockMapper;
import com.utils.Mybatis1Utils;
import com.utils.ResourceReader;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.ncslab.websocket.SimulateWebSocket;

/**
 * Test that verifies template variable substitution is working correctly.
 * This test focuses on the specific issues that were fixed:
 * 1. Template variables like $stateName, $signalName, $sampleTimeName not being substituted
 * 2. Missing discreteUpdate.vm template for UnitDelay  
 * 3. Variables like $output1, $stateX0, $stateX1 not being provided to testrig block templates
 * 4. Hardware testrig blocks missing context variables like $AD1, $outputAD1, etc.
 * 
 * WHAT THIS TEST DOES:
 * ====================
 * - Creates simple models with individual block types (Derivative, UnitDelay, InvertedPendulum)
 * - Triggers the full code generation pipeline using the existing WebSocket infrastructure
 * - Captures all generated output from the code generation process
 * - Scans the output for unsubstituted template variables (any string starting with $)
 * - Reports success/failure with detailed information about any unsubstituted variables found
 * 
 * HOW TO USE THIS TEST:
 * ====================
 * - Run individual tests: mvn test -Dtest=TemplateVariableSubstitutionTest#testDerivativeBlockTemplateSubstitution
 * - Run all template tests: mvn test -Dtest=TemplateVariableSubstitutionTest
 * - The test will show "SUCCESS" for properly working templates and "FAILED" with variable names for broken ones
 * 
 * WHEN TO RUN THIS TEST:
 * ======================
 * - After making template changes to verify variables are properly substituted
 * - When adding new block types to ensure template context is properly populated
 * - During CI/CD to catch template variable substitution regressions
 * - When debugging template rendering issues
 * 
 * @author Template Variable Substitution Verification System
 * @version 1.0 - Initial implementation for template fixes validation
 */
public class TemplateVariableSubstitutionTest {

    private static final Pattern TEMPLATE_VARIABLE_PATTERN = Pattern.compile("\\$\\w+");
    private String simulateFilePath = "com/ncslab/websocket/simulateWebsocket.json";
    
    @Before
    public void setUp() {
        // Setup test environment if needed
    }

    /**
     * Test template variable substitution for a simple continuous block (Derivative)
     */
    @Test
    public void testDerivativeBlockTemplateSubstitution() {
        System.out.println("Testing Derivative block template variable substitution...");
        testBlockTemplateSubstitution("DerivativeBlock", "Derivative");
    }

    /**
     * Test template variable substitution for a discrete block (UnitDelay)
     */
    @Test
    public void testUnitDelayBlockTemplateSubstitution() {
        System.out.println("Testing UnitDelay block template variable substitution...");
        testBlockTemplateSubstitution("UnitDelayBlock", "UnitDelay");
    }

    /**
     * Test template variable substitution for a testrig block (InvertedPendulum)
     */
    @Test
    public void testInvertedPendulumTemplateSubstitution() {
        System.out.println("Testing InvertedPendulum block template variable substitution...");
        testBlockTemplateSubstitution("Inverted PendulumBlock", "InvertedPendulum");
    }

    /**
     * Demonstrate that the test can detect unsubstituted variables by testing a string with 
     * template variables directly.
     */
    @Test
    public void testTemplateVariableDetection() {
        System.out.println("Testing template variable detection capability...");
        
        // Test string with unsubstituted template variables
        String testCode = "This is a test with $unsubstitutedVar and $anotherVar in the code.";
        
        List<String> unsubstitutedVars = findUnsubstitutedVariables(testCode);
        
        // This test should find the unsubstituted variables
        assertFalse("Template variable detection should find unsubstituted variables", unsubstitutedVars.isEmpty());
        assertTrue("Should detect $unsubstitutedVar", unsubstitutedVars.contains("$unsubstitutedVar"));
        assertTrue("Should detect $anotherVar", unsubstitutedVars.contains("$anotherVar"));
        assertEquals("Should find exactly 2 unsubstituted variables", 2, unsubstitutedVars.size());
        
        System.out.println("SUCCESS: Template variable detection is working correctly");
        System.out.println("Found variables: " + unsubstitutedVars);
    }

    /**
     * Helper method to test template variable substitution for a specific block type.
     * This follows the same pattern as existing tests but captures output to check for 
     * unsubstituted template variables.
     */
    private void testBlockTemplateSubstitution(String blockType, String testName) {
        try {
            // Get block data from database
            SqlSession sqlSession = Mybatis1Utils.getSqlSession();
            MdlBlockMapper mapper = sqlSession.getMapper(MdlBlockMapper.class);
            MdlBlock mdlBlock = mapper.selectByType(blockType);
            sqlSession.commit();
            sqlSession.close();

            if (mdlBlock == null) {
                System.err.println("WARNING: Block type " + blockType + " not found in database, skipping test");
                return;
            }

            // Create wrapper JSON object like existing tests do
            JSONObject jsonIn = wrapperObject(mdlBlock, simulateFilePath);
            
            // Capture system output to monitor for template variable issues
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            PrintStream ps = new PrintStream(baos);
            
            // Redirect output to capture generated code and any errors
            System.setOut(ps);
            System.setErr(ps);
            
            try {
                // Run the simulation WebSocket to generate C code
                SimulateWebSocket ws = new SimulateWebSocket();
                String msgString = jsonIn.toString();
                ws.onMessage(null, msgString);
                
                // Restore original output streams
                System.setOut(originalOut);
                System.setErr(originalErr);
                
                // Get captured output
                String output = baos.toString();
                
                // Check for unsubstituted template variables in the output
                List<String> unsubstitutedVars = findUnsubstitutedVariables(output);
                
                // Report results
                if (unsubstitutedVars.isEmpty()) {
                    System.out.println("SUCCESS: No unsubstituted template variables found in " + testName + " block");
                } else {
                    System.err.println("FAILED: Found unsubstituted template variables in " + testName + " block:");
                    for (String var : unsubstitutedVars) {
                        System.err.println("  - " + var);
                    }
                    fail("Found " + unsubstitutedVars.size() + " unsubstituted template variables in " + testName + " block: " + unsubstitutedVars);
                }
                
            } catch (Exception e) {
                // Restore original output streams even if exception occurs
                System.setOut(originalOut);
                System.setErr(originalErr);
                throw e;
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Exception during " + testName + " block test: " + e.getMessage());
            e.printStackTrace();
            fail("Exception during " + testName + " block template substitution test: " + e.getMessage());
        }
    }

    /**
     * Create wrapper object like existing tests do
     */
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

    /**
     * Find unsubstituted template variables in the generated code
     */
    private List<String> findUnsubstitutedVariables(String code) {
        List<String> unsubstituted = new ArrayList<>();
        
        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(code);
        while (matcher.find()) {
            String var = matcher.group();
            if (!unsubstituted.contains(var)) {
                unsubstituted.add(var);
            }
        }
        
        return unsubstituted;
    }
}