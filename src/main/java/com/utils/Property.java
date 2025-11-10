package com.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * This class is used to read the config.properties file.
 * You can get the value of the property based on the key.
 */
public class Property {

	public static Property instance=new Property();

//	public String getProperty(String key) {
//		Properties prop = new Properties();
//		String filePath= "com/ncslab/utils/config.properties";
//		String value = null;
//		try {
//			// read config from file
//			InputStream InputStream = this.getClass().getResourceAsStream(filePath);
//			// load the properties
//			prop.load(InputStream);
//			// get the value of the property based on the key
//			// 根据关键字获取value值
//			value = prop.getProperty(key);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		//System.out.println(value);
//
//		return value;
//	}

    public String getProperty(String key) {
        String value = null;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find config.properties");
                return "";
            }

            // Load a properties file from the class path with ISO-8859-1 encoding
            Properties prop = new Properties();
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.ISO_8859_1)) {
                prop.load(reader);
            }

            // Get the property value and print it
            value = prop.getProperty(key);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        String value = null;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find config.properties");
                return "";
            }

            // Load a properties file from the class path with ISO-8859-1 encoding
            Properties prop = new Properties();
            try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.ISO_8859_1)) {
                prop.load(reader);
            }

            // Get the property value and print it
            value = prop.getProperty(key, defaultValue);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return value;
    }
}
