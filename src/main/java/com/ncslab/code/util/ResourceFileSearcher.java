package com.ncslab.code.util;

import java.io.InputStream;

public class ResourceFileSearcher {

    public static InputStream findResourceFile(String fileName, String subDirectory) {
        // Construct the full path
        String fullPath = (subDirectory != null ? subDirectory + "/" : "") + fileName;

        // Try to find the resource
        InputStream inputStream = ResourceFileSearcher.class.getResourceAsStream(fullPath);

        return inputStream;
    }
}
