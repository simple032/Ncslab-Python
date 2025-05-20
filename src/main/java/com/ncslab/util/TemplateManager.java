package com.ncslab.util;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;  // Add this import
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;  // Add this import

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.concurrent.Executors;


/*
* ERROR [org.apache.velocity.parser] - templates/c/discrete/Zero_Order_Hold/output.vm: Encountered "-1" at line 10, column 31.
ERROR [org.apache.velocity.parser] - templates/c/discontinuous/DeadZone/output.vm: Encountered "-1" at line 12, column 35.
* ERROR [org.apache.velocity.parser] - templates/c/discontinuous/Relay/init.vm: Encountered "-1" at line 9, column 34.
* ERROR [org.apache.velocity.parser] - templates/c/discontinuous/Saturation/output.vm: Encountered "-1" at line 12, column 35.
*  ERROR [org.apache.velocity.parser] - templates/c/source/SineWave/output.vm: Encountered "-1" at line 6, column 37.
* ERROR [org.apache.velocity.parser] - templates/c/source/Step/output.vm: Encountered "-1" at line 10, column 32.
* ERROR [org.apache.velocity.parser] - templates/c/discrete/UnitDelay/output.vm: Encountered "-1" at line 25, column 34.
* */

public class TemplateManager {
    private static VelocityEngine ve;
//    private static final String TEMPLATE_PATH = "src/main/resources/templates/";

    static {
        initializeEngine();
        startFileWatcher();
    }

    private static void initializeEngine() {
        ve = new VelocityEngine();

        // Use updated property keys to avoid deprecation warnings
        ve.setProperty("resource.loaders", "classpath");
        ve.setProperty("resource.loader.classpath.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        // Optional: Configure logging (more detailed for debugging)
        ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        ve.setProperty("runtime.log.logsystem.log4j.category", "velocity");

        ve.init();
    }

    public static String renderTemplate(String templateName, VelocityContext context) {
        try {
//            debugTemplateLocations(templateName);
            Template t = ve.getTemplate("templates/"+templateName);
            StringWriter writer = new StringWriter();
            t.merge(context, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Template rendering failed: " + templateName, e);
        }
    }

    private static void startFileWatcher() {
        try {
            // Try to get the resource URL from the classpath
            URL templateResourceUrl = TemplateManager.class.getClassLoader().getResource("templates");

            // Only watch if the resource is on the file system
            if (templateResourceUrl != null && templateResourceUrl.getProtocol().equals("file")) {
                Path templatePath;
                try {
                    templatePath = Paths.get(templateResourceUrl.toURI());
                } catch (URISyntaxException e) {
                    System.err.println("Cannot convert URL to Path: " + e.getMessage());
                    return;  // Cannot watch this path
                }

                // Start file watcher in a separate thread
                Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        WatchService watchService = FileSystems.getDefault().newWatchService();
                        templatePath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                        System.out.println("Watching template directory: " + templatePath);

                        while (true) {
                            WatchKey key = watchService.take();
                            for (WatchEvent<?> event : key.pollEvents()) {
                                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                    @SuppressWarnings("unchecked")
                                    Path changedFile = ((WatchEvent<Path>) event).context();
                                    System.out.println("Template modified: " + changedFile);

                                    // Reinitialize engine on template change
                                    initializeEngine();
                                }
                            }
                            key.reset();
                        }
                    } catch (Exception e) {
                        System.err.println("Error in template file watcher: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("Template resources are not on file system, file watching disabled");
            }
        } catch (Exception e) {
            System.err.println("Error setting up template file watcher: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void debugTemplateLocations(String templateName) {
        // Get the classloader that loaded this class
        ClassLoader classLoader = TemplateManager.class.getClassLoader();

        // Check different possible template paths
        String[] possiblePaths = {
            templateName,
            "templates/" + templateName,
            "resources/templates/" + templateName,
            "main/resources/templates/" + templateName,
            "src/main/resources/templates/" + templateName,
        };

        System.out.println("===== TEMPLATE DEBUG INFO =====");
        for (String path : possiblePaths) {
            URL url = classLoader.getResource(path);
            System.out.println("Checking path: " + path);
            System.out.println("  Result: " + (url != null ? "FOUND at " + url : "NOT FOUND"));
        }

        // Try to list all .vm files in the classpath
        try {
            Enumeration<URL> resources = classLoader.getResources("");
            System.out.println("Base classpath locations:");
            while (resources.hasMoreElements()) {
                System.out.println("  " + resources.nextElement());
            }
        } catch (IOException e) {
            System.out.println("Error listing classpath resources: " + e.getMessage());
        }

        System.out.println("=============================");
    }
}
