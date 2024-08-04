package servlet;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import code.c.CodeModelC;
import ncslablink.ModelException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class compileTest {

    @InjectMocks
    private compile compileServlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDoPostSuccess() throws Exception {
        // Setup
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Assuming JSON input related to the platform 'raspberry'
        String inputJson = "{\"platform\":\"raspberry\"}";
        when(request.getInputStream()).thenReturn(new ByteArrayInputStream(inputJson.getBytes("UTF-8")));

        // Execution
        compileServlet.doPost(request, response);

        // Assertion
        assertTrue("Response should contain success message", responseWriter.toString().contains("make exe success"));
    }

    @Test
    public void testDoPostWithModelException() throws Exception {
        // Setup
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Assuming JSON input that will cause a ModelException
        String inputJson = "{\"invalid\":\"json\"}";
        when(request.getInputStream()).thenReturn(new ByteArrayInputStream(inputJson.getBytes("UTF-8")));

        // Execution & Assertion
        try {
            compileServlet.doPost(request, response);
            fail("Should have thrown ModelException");
        } catch (ModelException e) {
            assertTrue("Response should contain error message", responseWriter.toString().contains(e.getMessage()));
        }
    }

    @Test
    public void testDoPostErrorResponse() throws IOException, ServletException {
        // Setup
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Assuming JSON input that will cause some error
        String inputJson = "{\"error\":\"true\"}";
        when(request.getInputStream()).thenReturn(new ByteArrayInputStream(inputJson.getBytes("UTF-8")));

        // Mocking CodeModelC creation and behavior since it's not directly testable without instantiation.
        // This assumes some modifications are made to allow mocking or partial mocking of the CodeModelC behavior.

        // Execution & Assertion
        try {
            compileServlet.doPost(request, response);
            assertTrue("Response should contain error code and message", responseWriter.toString().contains("\"code\":400"));
        } catch (Exception e) {
            fail("Test should not throw exceptions other than ModelException");
        }
    }
}