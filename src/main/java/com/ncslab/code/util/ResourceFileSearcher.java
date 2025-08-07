package com.ncslab.code.util;

import java.io.InputStream;

public class ResourceFileSearcher {

    public static InputStream findResourceFile(String fileName, String subDirectory) {
        // Construct the full path
        String fullPath = (subDirectory != null ? subDirectory + "/" : "") + fileName;
        
        // Add the base path for ncslab code resources
        if (!fullPath.startsWith("/com/ncslab/code/")) {
            fullPath = "/com/ncslab/code/" + fullPath;
        }

        // Try to find the resource
        InputStream inputStream = ResourceFileSearcher.class.getResourceAsStream(fullPath);
        System.out.println("ResourceFileSearcher: Looking for '" + fullPath + "' - " + (inputStream != null ? "FOUND" : "NOT FOUND"));

        return inputStream;
    }
}
