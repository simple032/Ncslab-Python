package utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class ResourceReader {
    public static String readResource(String resourcePath) {
        // 获取当前类的类加载器
        ClassLoader classLoader = ResourceReader.class.getClassLoader();
        // 通过类加载器获取资源文件的输入流
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject readJsonResource(String resourcePath) {
        return new JSONObject(Objects.requireNonNull(ResourceReader.readResource(resourcePath)));
    }
}