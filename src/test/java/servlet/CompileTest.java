package test.java.servlet;


import code.c.CodeModelC;
import code.c.CodeModelCFactory;
import ncslablink.ModelException;
import ncslablink.ModelMode;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
//
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import servlet.compile;
import test.java.ResourceReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class CompileTest{

    @InjectMocks
    private compile compileServlet;

    public static class compilesubclass extends compile {
        public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            super.doPost(request, response);
        }
    }

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Before
    public void setUp() {
//        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCompileWithJSONWindows(){
        String filePath = "mlsCompile.json"; // 替换为实际文件路径
        JSONObject jsonIn = ResourceReader.readJsonResource(filePath);
        CodeModelC modelC = null;
        try {
            modelC = CodeModelCFactory.createInstance("windows", jsonIn, ModelMode.Compilation);
        } catch (ModelException e) {
            throw new RuntimeException(e);
        }
        assertEquals("Fail to make exe.", "make exe success.", compile.compileWithJSON(modelC, jsonIn));

    }

    @Ignore
    public void testDoPostSuccess() throws Exception {
        // Setup
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Assuming JSON input related to the platform 'raspberry'
        String inputJson = "{\"platform\":\"raspberry\"}";
//        when(request.getInputStream()).thenReturn(new ByteArrayInputStream(inputJson.getBytes("UTF-8")));

        compilesubclass spy = Mockito.spy(compilesubclass.class);
        // Execution
        Mockito.doReturn("mocked private method").when(spy).doPost(request, response);

        // Assertion
        assertTrue("Response should contain success message", responseWriter.toString().contains("make exe success"));
    }

    @Ignore
    public void testDoPostWithModelException() throws Exception {
        // Setup
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Assuming JSON input that will cause a ModelException
        String inputJson = "{\"invalid\":\"json\"}";
//        when(request.getInputStream()).thenReturn(new ByteArrayInputStream(inputJson.getBytes("UTF-8")));

        // Execution & Assertion
//        super.doPost(request, response);
        fail("Should have thrown ModelException");
    }

    @Ignore
    public void testDoPostErrorResponse() throws IOException, ServletException {
        // Setup
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Assuming JSON input that will cause some error
        String inputJson = "{\"error\":\"true\"}";
        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        });

        // Mocking CodeModelC creation and behavior since it's not directly testable without instantiation.
        // This assumes some modifications are made to allow mocking or partial mocking of the CodeModelC behavior.

        // Execution & Assertion
        try {
//            super.doPost(request, response);
            assertTrue("Response should contain error code and message", responseWriter.toString().contains("\"code\":400"));
        } catch (Exception e) {
            fail("Test should not throw exceptions other than ModelException");
        }
    }
}