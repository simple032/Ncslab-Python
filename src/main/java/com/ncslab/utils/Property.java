package com.ncslab.utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * This class is used to read the config.properties file.
 * You can get the value of the property based on the key.
 */
public class Property {
	
	public static Property instance=new Property();
	
	public String getProperty(String key) {
		Properties prop = new Properties();
		String filePath="config.properties";
		String value = null;
		try {

			
			// read config from file
			InputStream InputStream = this.getClass().getResourceAsStream(filePath); 
			// load the properties
			prop.load(InputStream);
			// get the value of the property based on the key
			value = prop.getProperty(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println(value);
		
		return value;
	}
	
}
