package com.ncslab.code.util;

import java.io.InputStream;

public class ResourceUtils {
    public static InputStream getResourceAsStream(String resourceName, String subDirectory) {

        // Construct the full path
        String fullPath = (subDirectory != null ? subDirectory + "/" : "") + resourceName;
        if (!fullPath.startsWith("/")) {
            fullPath = "/com/ncslab/code/" + fullPath;
        }
        InputStream stream = ResourceUtils.class.getClassLoader().getResourceAsStream(fullPath);
        //            System.out.println("Resource not found: " + fullPath);
        return stream;
    }
}
