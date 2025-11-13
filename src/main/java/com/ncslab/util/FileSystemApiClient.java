package com.ncslab.util;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client for interacting with the Filesystem API.
 *
 * Provides methods for:
 * - Getting current directory
 * - Reading files
 * - Writing files
 *
 * API Endpoints (Unauthorized - userId in URL):
 * - GET /api/filesystem/user/{userId}/current-directory
 * - GET /api/filesystem/user/{userId}/file/{path}
 * - PUT /api/filesystem/user/{userId}/file/{path}
 *
 * @author NCSLab Team
 * @version 2025
 */
public class FileSystemApiClient {

    private static final String API_BASE_URL = getApiBaseUrl();
    private static final int TIMEOUT_MS = 5000; // 5 second timeout

    /**
     * Get the API base URL from system properties or default to localhost
     */
    private static String getApiBaseUrl() {
        String baseUrl = System.getProperty("filesystem.api.url");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "http://localhost:8080"; // Default for development
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * Get the current directory from the filesystem API (unauthorized endpoint).
     *
     * @param userId User ID for the request
     * @return Current directory path (e.g., "/", "/projects")
     * @throws Exception if the API call fails
     */
    public static String getCurrentDirectory(String userId) throws Exception {
        String endpoint = API_BASE_URL + "/api/filesystem/user/" + userId + "/current-directory";

        HttpURLConnection conn = null;
        try {
            URL url = new URI(endpoint).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(conn);
                JSONObject json = new JSONObject(response);
                return json.optString("currentDirectory", "/");
            } else {
                throw new Exception("Failed to get current directory. HTTP " + responseCode);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Read a file from the filesystem API (unauthorized endpoint).
     *
     * @param userId User ID for the request
     * @param filePath Full path to the file (e.g., "/projects/data.mat" or "/data.mat")
     * @return File content as string
     * @throws Exception if the API call fails or file not found
     */
    public static String readFile(String userId, String filePath) throws Exception {
        // Remove leading slash for URL path
        String urlPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String endpoint = API_BASE_URL + "/api/filesystem/user/" + userId + "/file/" + urlPath;

        HttpURLConnection conn = null;
        try {
            URL url = new URI(endpoint).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(conn);
                JSONObject json = new JSONObject(response);

                // Response format: {"data": {"name": "...", "path": "...", "content": "...", "size": ..., "lastModified": ...}}
                if (json.has("data")) {
                    JSONObject data = json.getJSONObject("data");
                    return data.optString("content", "");
                } else {
                    // Fallback for legacy format
                    return json.optString("content", "");
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new Exception("File not found: " + filePath);
            } else {
                throw new Exception("Failed to read file. HTTP " + responseCode);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Write a file to the filesystem API (unauthorized endpoint).
     *
     * @param userId User ID for the request
     * @param filePath Full path to the file (e.g., "/projects/output.mat" or "/output.mat")
     * @param content File content to write
     * @return JSON response with file metadata
     * @throws Exception if the API call fails
     */
    public static JSONObject writeFile(String userId, String filePath, String content) throws Exception {
        // Remove leading slash for URL path
        String urlPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String endpoint = API_BASE_URL + "/api/filesystem/user/" + userId + "/file/" + urlPath;

        HttpURLConnection conn = null;
        try {
            URL url = new URI(endpoint).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            // Create request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("content", content);

            // Write request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                String response = readResponse(conn);
                return new JSONObject(response);
            } else {
                throw new Exception("Failed to write file. HTTP " + responseCode);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Construct full file path from current directory and filename.
     * Handles root directory case properly.
     *
     * @param currentDir Current directory (e.g., "/", "/projects")
     * @param fileName File name (e.g., "data.mat")
     * @return Full path (e.g., "/data.mat", "/projects/data.mat")
     */
    public static String constructFilePath(String currentDir, String fileName) {
        if (currentDir == null || currentDir.isEmpty()) {
            currentDir = "/";
        }

        // Handle root directory case
        if (currentDir.equals("/")) {
            return "/" + fileName;
        }

        // Handle subdirectory case
        return currentDir + "/" + fileName;
    }

    /**
     * Read HTTP response body as string.
     */
    private static String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
}
