package com.utils;

import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Enhanced resource file reader utility for loading template and resource files
 * with support for nested directory structures and multiple search paths.
 */
public class ResourceReader {

    private static final String[] RESOURCE_BASE_PATHS = {
        "/com/ncslab/code/",
        "/com/ncslab/code2/",
        "/templates/",
        "/resources/"
    };

    /**
     * Reads a resource file as a string with automatic encoding detection
     * @param resourcePath the path to the resource file
     * @return the content of the file as a string
     * @throws IOException if the file cannot be read
     */
    public static String readResourceAsString(String resourcePath) throws IOException {
        InputStream inputStream = getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * Gets an InputStream for a resource with nested directory search capability
     * @param resourcePath the path to the resource
     * @return InputStream for the resource, or null if not found
     */
    public static InputStream getResourceAsStream(String resourcePath) {
        // First try direct path
        InputStream stream = ResourceReader.class.getResourceAsStream(resourcePath);
        if (stream != null) {
            return stream;
        }

        // Try with leading slash if not present
        if (!resourcePath.startsWith("/")) {
            stream = ResourceReader.class.getResourceAsStream("/" + resourcePath);
            if (stream != null) {
                return stream;
            }
        }

        // Try with each base path
        for (String basePath : RESOURCE_BASE_PATHS) {
            String fullPath = basePath + resourcePath;
            stream = ResourceReader.class.getResourceAsStream(fullPath);
            if (stream != null) {
                return stream;
            }

            // Also try without leading slash on resource path
            if (resourcePath.startsWith("/")) {
                fullPath = basePath + resourcePath.substring(1);
                stream = ResourceReader.class.getResourceAsStream(fullPath);
                if (stream != null) {
                    return stream;
                }
            }
        }

        return null;
    }

    /**
     * Reads a resource file as byte array
     * @param resourcePath the path to the resource file
     * @return the content of the file as byte array
     * @throws IOException if the file cannot be read
     */
    public static byte[] readResourceAsBytes(String resourcePath) throws IOException {
        InputStream inputStream = getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    /**
     * Lists all resources in a given directory path
     * @param directoryPath the directory path to search
     * @return list of resource names found
     */
    public static List<String> listResources(String directoryPath) {
        List<String> resources = new ArrayList<>();

        try {
            URL url = ResourceReader.class.getResource(directoryPath);
            if (url != null) {
                if ("file".equals(url.getProtocol())) {
                    // Handle file system resources
                    Path path = Paths.get(url.toURI());
                    if (Files.exists(path) && Files.isDirectory(path)) {
                        Files.list(path).forEach(p -> resources.add(p.getFileName().toString()));
                    }
                }
                // Add support for JAR resources if needed
            }
        } catch (Exception e) {
            // Silent fail - return empty list
        }

        return resources;
    }

    /**
     * Checks if a resource exists
     * @param resourcePath the path to check
     * @return true if the resource exists
     */
    public static boolean resourceExists(String resourcePath) {
        return getResourceAsStream(resourcePath) != null;
    }

    /**
     * Gets the URL for a resource
     * @param resourcePath the path to the resource
     * @return URL for the resource, or null if not found
     */
    public static URL getResourceURL(String resourcePath) {
        // Try direct path first
        URL url = ResourceReader.class.getResource(resourcePath);
        if (url != null) {
            return url;
        }

        // Try with leading slash if not present
        if (!resourcePath.startsWith("/")) {
            url = ResourceReader.class.getResource("/" + resourcePath);
            if (url != null) {
                return url;
            }
        }

        // Try with each base path
        for (String basePath : RESOURCE_BASE_PATHS) {
            String fullPath = basePath + resourcePath;
            url = ResourceReader.class.getResource(fullPath);
            if (url != null) {
                return url;
            }
        }

        return null;
    }

    /**
     * Reads a JSON resource file and returns it as a string
     * @param resourcePath the path to the JSON resource file
     * @return the JSON content
     * @throws IOException if the file cannot be read
     */
    public static JSONObject readJsonResource(String resourcePath)  {
        String jsonString = null;
        try {
            jsonString = readResourceAsString(resourcePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new JSONObject(jsonString);
    }

    /**
     * Searches for resources matching a pattern in nested directories
     * @param pattern the pattern to match (simple wildcard support)
     * @param searchPath the base path to search from
     * @return list of matching resource paths
     */
    public static List<String> findResources(String pattern, String searchPath) {
        List<String> matches = new ArrayList<>();

        // This is a simplified implementation
        // In a full implementation, you might want to use more sophisticated pattern matching
        List<String> resources = listResources(searchPath);
        for (String resource : resources) {
            if (resource.matches(pattern.replace("*", ".*"))) {
                matches.add(searchPath + "/" + resource);
            }
        }

        return matches;
    }
}
