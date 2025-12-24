package com.ncslab.block;

import com.ncslab.database.MdlBlock;
import com.ncslab.websocket.CompileWebSocket;
import com.ncslab.websocket.SimulateWebSocket;
import com.ncslab.websocket.SimulateRTWebSocket;
import com.utils.MdlBlockMapper;
import com.utils.Mybatis1Utils;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.utils.ResourceReader;

import java.util.UUID;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.fail;

public class BlockTest {
    private String simulateFilePath = "com/ncslab/websocket/simulateWebsocket.json";
    private String compileFilePath = "com/ncslab/websocket/compileWebsocket.json";
    
    private String blockType;
    private MdlBlock mdlBlock;
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;
    
    // Error classification patterns
    private static final Pattern TEMPLATE_ERROR_PATTERN = Pattern.compile(
        "(TemplateNotFoundException|VelocityException|template.*not found|template.*error)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DTO_ERROR_PATTERN = Pattern.compile(
        "(NullPointerException.*dto|dto.*null|parameter.*null|BlockDto.*null|validation.*failed)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMPILATION_ERROR_PATTERN = Pattern.compile(
        "(compilation.*failed|compiler.*error|make.*error|build.*failed)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BLOCK_CREATION_ERROR_PATTERN = Pattern.compile(
        "(BlockCreationException|createBlock.*failed|unknown.*block.*type)", Pattern.CASE_INSENSITIVE);
    
    @Before
    public void setUp(){
        SqlSession sqlSession = Mybatis1Utils.getSqlSession();
        MdlBlockMapper mapper = sqlSession.getMapper(MdlBlockMapper.class);
        
        // Get block type from system property, environment variable, or default
        blockType = System.getProperty("blockType");
        if (blockType == null) {
            blockType = System.getenv("BLOCK_TYPE");
        }
        if (blockType == null) {
            blockType = "OutBlock";
        }
        
        if (blockType == null || blockType.trim().isEmpty()) {
            blockType = "PIDControllerBlock";
        }
        
        System.out.println("[" + getCurrentTimestamp() + "] Setting up test for block type: " + blockType);
        
        try {
            mdlBlock = mapper.selectByType(blockType);
            if (mdlBlock == null) {
                System.err.println("[" + getCurrentTimestamp() + "] ERROR: Block type '" + blockType + "' not found in database");
                throw new RuntimeException("Block type not found: " + blockType);
            }
            System.out.println("[" + getCurrentTimestamp() + "] Successfully loaded block data for: " + blockType);
        } catch (Exception e) {
            System.err.println("[" + getCurrentTimestamp() + "] ERROR: Failed to load block data for " + blockType + ": " + e.getMessage());
            throw e;
        } finally {
            sqlSession.commit();
            sqlSession.close();
        }
    }
    
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
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

    /**
     * Categorizes errors based on stack trace and error message patterns
     */
    private String categorizeError(String stackTrace) {
        if (stackTrace == null) return "UNKNOWN";
        
        if (TEMPLATE_ERROR_PATTERN.matcher(stackTrace).find()) {
            return "TEMPLATE_ERROR";
        }
        if (DTO_ERROR_PATTERN.matcher(stackTrace).find()) {
            return "DTO_ERROR";
        }
        if (COMPILATION_ERROR_PATTERN.matcher(stackTrace).find()) {
            return "COMPILATION_ERROR";
        }
        if (BLOCK_CREATION_ERROR_PATTERN.matcher(stackTrace).find()) {
            return "BLOCK_CREATION_ERROR";
        }
        if (stackTrace.contains("NullPointerException")) {
            return "NULL_POINTER_ERROR";
        }
        if (stackTrace.contains("IllegalArgumentException")) {
            return "INVALID_PARAMETER_ERROR";
        }
        if (stackTrace.contains("ClassNotFoundException") || stackTrace.contains("NoClassDefFoundError")) {
            return "CLASS_NOT_FOUND_ERROR";
        }
        if (stackTrace.contains("NoSuchMethodException") || stackTrace.contains("NoSuchMethodError")) {
            return "METHOD_NOT_FOUND_ERROR";
        }
        
        return "RUNTIME_ERROR";
    }
    
    /**
     * Gets the full stack trace as a string
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    @After
    public void finish(){
        System.out.println("[" + getCurrentTimestamp() + "] Test cleanup completed for " + blockType);
    }
}
