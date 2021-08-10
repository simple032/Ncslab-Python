package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.io.*;


public class Property {
	
	public static Property instance=new Property();
	
	public String getProperty(String key) {
		Properties prop = new Properties();
		String filePath="config.properties";
		String value = null;
		try {

			
			// 通过输入缓冲流进行读取配置文件
			InputStream InputStream = this.getClass().getResourceAsStream(filePath); 
			// 加载输入流
			prop.load(InputStream);
			// 根据关键字获取value值
			value = prop.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println(value);
		
		return value;

	}
	
}
