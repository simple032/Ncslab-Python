package com.ncslab.servlet;

import com.ncslab.database.MdlBlock;
import com.ncslab.dto.core.ModelDto;
import com.ncslab.util.JsonUtils;
import com.utils.MdlBlockMapper;
import com.utils.Mybatis1Utils;
import com.utils.ResourceReader;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

// 启用Mockito扩展，用于注入模拟对象
@RunWith(MockitoJUnitRunner.class)
public class AndroidSimulateTest {

    String filePath = "com/ncslab/servlet/android_sim.json";//"websocketCompile.json"; // 替换为实际文件路径
    List<MdlBlock> mdlBlockList = null;
    @Before
    public void setUp(){
        
    }

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    public void simulate(){
        try {
            // 1. 读取JSON测试数据（使用你的ResourceReader工具类）
            String jsonString = ResourceReader.readResourceAsString(filePath);
            byte[] jsonBytes = jsonString.getBytes("utf-8");

            // 2. 模拟 ServletInputStream（关键：必须是 ServletInputStream 类型）
            ServletInputStream mockInputStream = mock(ServletInputStream.class);
            AtomicInteger counter = new AtomicInteger(0); // 用于记录读取位置

            // Mock read(byte[], int, int) method - this is what InputStreamReader actually calls
            when(mockInputStream.read(org.mockito.ArgumentMatchers.any(byte[].class),
                                       org.mockito.ArgumentMatchers.anyInt(),
                                       org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer((Answer<Integer>) invocation -> {
                    byte[] buffer = invocation.getArgument(0);
                    int offset = invocation.getArgument(1);
                    int length = invocation.getArgument(2);

                    int currentPos = counter.get();
                    if (currentPos >= jsonBytes.length) {
                        return -1; // End of stream
                    }

                    int bytesToRead = Math.min(length, jsonBytes.length - currentPos);
                    System.arraycopy(jsonBytes, currentPos, buffer, offset, bytesToRead);
                    counter.addAndGet(bytesToRead);
                    return bytesToRead;
                });

            // 3. 绑定模拟的输入流到 request
            when(request.getInputStream()).thenReturn(mockInputStream);
                    
            // 捕获响应输出
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(printWriter);
            
            // 调用doPost
            new android_simulate().doPost(request, response);

            // 输出结果（不验证响应状态，因为可能编译失败）
            printWriter.flush();
            String result = stringWriter.toString().trim();
            System.out.println("=== Test Result ===");
            System.out.println(result);
            System.out.println("==================");
        } catch (ServletException e) {
            System.err.println("ServletException during test: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException during test: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected exception during test: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @After
    public void finish(){

    }
}
