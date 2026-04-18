package com.ncslab.servlet;

import com.utils.Property;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;

/**
 * Servlet for serving simulation result files with proper Content-Length header.
 * This ensures the frontend can display download progress accurately.
 *
 * Maps URL pattern /CCode/* to the actual filesystem path configured by CCodePath.
 *
 * Example:
 *   URL: /CCode/31/123/results.bin
 *   Maps to: CCodePath/31/123/results.bin
 */
@WebServlet("/CCode/*")
public class ResultFileDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Resolve the requested file path
        String requestPath = request.getPathInfo(); // e.g. /31/123/results.bin
        if (requestPath == null || requestPath.isEmpty() || "/".equals(requestPath)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file path");
            return;
        }

        // Remove leading slash
        String relativePath = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;

        // 2. Build absolute path from configured CCodePath
        String codePathBase = Property.instance.getProperty("CCodePath");
        if (codePathBase == null || codePathBase.isEmpty()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CCodePath not configured");
            return;
        }

        // Normalize path separators (handle both Windows and Linux)
        codePathBase = codePathBase.replace("\\", "/");
        if (!codePathBase.endsWith("/")) {
            codePathBase += "/";
        }

        File file = new File(codePathBase + relativePath);

        // 3. Security check: ensure file is within CCodePath directory
        try {
            String canonicalBase = new File(codePathBase).getCanonicalPath();
            String canonicalFile = file.getCanonicalPath();
            if (!canonicalFile.startsWith(canonicalBase)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid path");
            return;
        }

        // 4. Check file existence and readability
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + relativePath);
            return;
        }

        // 5. Determine content type based on file extension
        String fileName = file.getName().toLowerCase();
        String contentType;
        if (fileName.endsWith(".bin")) {
            contentType = "application/octet-stream";
        } else if (fileName.endsWith(".json")) {
            contentType = "application/json";
        } else {
            contentType = "application/octet-stream";
        }

        // 6. Set response headers with Content-Length (critical for download progress)
        response.setContentType(contentType);
        response.setContentLengthLong(file.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 7. Stream file content
        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // HEAD request: return headers only (including Content-Length)
        String requestPath = request.getPathInfo();
        if (requestPath == null || requestPath.isEmpty() || "/".equals(requestPath)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String relativePath = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
        String codePathBase = Property.instance.getProperty("CCodePath");
        if (codePathBase == null || codePathBase.isEmpty()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        codePathBase = codePathBase.replace("\\", "/");
        if (!codePathBase.endsWith("/")) {
            codePathBase += "/";
        }

        File file = new File(codePathBase + relativePath);

        // Security check
        try {
            String canonicalBase = new File(codePathBase).getCanonicalPath();
            String canonicalFile = file.getCanonicalPath();
            if (!canonicalFile.startsWith(canonicalBase)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!file.exists() || !file.isFile() || !file.canRead()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = file.getName().toLowerCase();
        String contentType = fileName.endsWith(".json") ? "application/json" : "application/octet-stream";

        response.setContentType(contentType);
        response.setContentLengthLong(file.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
    }
}
