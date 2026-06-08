package com.ncslab.websocket;

import jakarta.websocket.Session;
import org.json.JSONException;
import org.json.JSONObject;
import com.ncslab.dto.communication.WebSocketMessageDto;
import com.ncslab.util.JsonUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class WebSocketSecurity {
    private static final Logger logger = Logger.getLogger(WebSocketSecurity.class.getName());
    
    // Rate limiting: max 10 requests per session
    private static final int MAX_CONCURRENT_OPERATIONS = 10;
    private static final Semaphore globalSemaphore = new Semaphore(MAX_CONCURRENT_OPERATIONS);
    
    // Track sessions for rate limiting
    private static final ConcurrentHashMap<String, AtomicLong> sessionRequestCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> sessionLastActivity = new ConcurrentHashMap<>();
    
    // Allowed commands - whitelist approach
    private static final Set<String> ALLOWED_COMMANDS = Set.of("start", "stop", "pause", "resume");
    
    // Input validation patterns
    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-\\.\\s]+$");
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    
    // Security limits
    public static final int MAX_MESSAGE_SIZE = resolveMaxMessageSize();
    private static final int MAX_JSON_DEPTH = 10;
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    
    private WebSocketSecurity() {
        // Utility class
    }

    private static int resolveMaxMessageSize() {
        String configured = System.getProperty("ncslab.websocket.maxMessageBytes");
        if (configured == null || configured.trim().isEmpty()) {
            configured = System.getenv("NCSLAB_WEBSOCKET_MAX_MESSAGE_BYTES");
        }
        if (configured != null && !configured.trim().isEmpty()) {
            try {
                return Math.max(1024 * 1024, Integer.parseInt(configured.trim()));
            } catch (NumberFormatException e) {
                logger.warning("Invalid WebSocket max message size: " + configured + ", using default 64MB");
            }
        }
        return 64 * 1024 * 1024;
    }
    
    /**
     * Validates and sanitizes incoming WebSocket message
     * @param session WebSocket session
     * @param messageString Raw message string
     * @return Validated WebSocketMessageDto or null if invalid
     * @throws SecurityException if message is malicious
     */
    public static WebSocketMessageDto validateAndParseMessage(Session session, String messageString) 
            throws SecurityException {
        
        // Basic security checks
        if (session == null) {
            throw new SecurityException("Invalid session");
        }
        
        if (messageString == null || messageString.trim().isEmpty()) {
            throw new SecurityException("Empty message not allowed");
        }
        
        // Size validation
        if (messageString.length() > MAX_MESSAGE_SIZE) {
            throw new SecurityException("Message size exceeds limit: " + messageString.length());
        }
        
        // Rate limiting check
        if (!checkRateLimit(session)) {
            throw new SecurityException("Rate limit exceeded for session: " + session.getId());
        }
        
        try {
            // Parse and validate JSON structure
            JSONObject tempJson = new JSONObject(messageString);
            
            // Validate JSON depth to prevent stack overflow attacks
            if (getJsonDepth(tempJson) > MAX_JSON_DEPTH) {
                throw new SecurityException("JSON structure too deep");
            }
            
            // Parse to DTO with Jackson validation
            WebSocketMessageDto wsMessage;
            try {
                wsMessage = JsonUtils.getObjectMapper().readValue(messageString, WebSocketMessageDto.class);
            } catch (Exception e) {
                throw new SecurityException("Failed to parse message as valid DTO: " + e.getMessage());
            }
            
            if (wsMessage == null) {
                throw new SecurityException("Parsed message is null");
            }
            
            // Validate command
            String command = wsMessage.getCom();
            if (command == null || !ALLOWED_COMMANDS.contains(command.toLowerCase())) {
                throw new SecurityException("Invalid or disallowed command: " + command);
            }
            
            // Validate model data if present
            if (wsMessage.getMdlData() != null) {
                validateModelData(wsMessage.getMdlData());
            }
            
            // Update activity tracking
            updateSessionActivity(session);
            
            logger.info("Message validated successfully for session: " + session.getId() + ", command: " + command);
            return wsMessage;
            
        } catch (JSONException e) {
            throw new SecurityException("Invalid JSON format: " + e.getMessage());
        }
    }
    
    /**
     * Acquires a permit for processing (rate limiting)
     * @return true if permit acquired, false if should be rejected
     */
    public static boolean acquireProcessingPermit() {
        return globalSemaphore.tryAcquire();
    }
    
    /**
     * Releases processing permit
     */
    public static void releaseProcessingPermit() {
        globalSemaphore.release();
    }
    
    /**
     * Validates model data for potential security issues
     */
    private static void validateModelData(Object mdlData) throws SecurityException {
        if (mdlData instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) mdlData;
            
            // Check for jsonData
            Object jsonDataObj = dataMap.get("jsonData");
            if (jsonDataObj instanceof String) {
                String jsonData = (String) jsonDataObj;
                
                // Validate JSON data structure
                String validationError = JsonUtils.validateJsonStructure(jsonData);
                if (validationError != null) {
                    throw new SecurityException("Invalid model JSON: " + validationError);
                }
                
                // Additional checks for malicious content
                if (containsMaliciousContent(jsonData)) {
                    throw new SecurityException("Potentially malicious content detected in model data");
                }
            }
        }
    }
    
    /**
     * Check for potentially malicious content in JSON data
     */
    private static boolean containsMaliciousContent(String jsonData) {
        // Check for common injection patterns
        String lowerJson = jsonData.toLowerCase();
        
        // Check for script injection attempts
        if (lowerJson.contains("<script") || lowerJson.contains("javascript:") || 
            lowerJson.contains("eval(") || lowerJson.contains("exec(")) {
            return true;
        }
        
        // Check for path traversal attempts
        if (jsonData.contains("../") || jsonData.contains("..\\") || 
            jsonData.contains("%2e%2e") || jsonData.contains("~")) {
            return true;
        }
        
        // Check for command injection attempts
        if (lowerJson.contains("system(") || lowerJson.contains("runtime.exec") ||
            lowerJson.contains("processbuilder") || lowerJson.contains("cmd.exe")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check rate limiting for session
     */
    private static boolean checkRateLimit(Session session) {
        String sessionId = session.getId();
        long currentTime = System.currentTimeMillis();
        
        // Get or create request counter for session
        AtomicLong requestCount = sessionRequestCounts.computeIfAbsent(sessionId, k -> new AtomicLong(0));
        Long lastActivity = sessionLastActivity.get(sessionId);
        
        // Reset counter if more than a minute has passed
        if (lastActivity == null || (currentTime - lastActivity) > 60000) {
            requestCount.set(1);
            sessionLastActivity.put(sessionId, currentTime);
            return true;
        }
        
        // Check if under rate limit
        long currentCount = requestCount.incrementAndGet();
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            logger.warning("Rate limit exceeded for session: " + sessionId + ", count: " + currentCount);
            return false;
        }
        
        sessionLastActivity.put(sessionId, currentTime);
        return true;
    }
    
    /**
     * Update session activity tracking
     */
    private static void updateSessionActivity(Session session) {
        sessionLastActivity.put(session.getId(), System.currentTimeMillis());
    }
    
    /**
     * Calculate JSON object depth to prevent stack overflow attacks
     */
    private static int getJsonDepth(JSONObject json) {
        return getJsonDepthRecursive(json, 0);
    }
    
    private static int getJsonDepthRecursive(Object obj, int currentDepth) {
        if (currentDepth > MAX_JSON_DEPTH) {
            return currentDepth;
        }
        
        int maxDepth = currentDepth;
        
        if (obj instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) obj;
            for (String key : jsonObj.keySet()) {
                Object value = jsonObj.get(key);
                int depth = getJsonDepthRecursive(value, currentDepth + 1);
                maxDepth = Math.max(maxDepth, depth);
            }
        } else if (obj instanceof org.json.JSONArray) {
            org.json.JSONArray jsonArray = (org.json.JSONArray) obj;
            for (int i = 0; i < jsonArray.length(); i++) {
                Object value = jsonArray.get(i);
                int depth = getJsonDepthRecursive(value, currentDepth + 1);
                maxDepth = Math.max(maxDepth, depth);
            }
        }
        
        return maxDepth;
    }
    
    /**
     * Clean up session data when session closes
     */
    public static void cleanupSession(Session session) {
        if (session != null) {
            String sessionId = session.getId();
            sessionRequestCounts.remove(sessionId);
            sessionLastActivity.remove(sessionId);
            logger.info("Cleaned up session data for: " + sessionId);
        }
    }
}
