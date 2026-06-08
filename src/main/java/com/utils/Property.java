package com.utils;

import com.ncslab.config.NCSLabConfig;

/**
 * Compatibility facade for runtime configuration.
 * You can get the value of the property based on the key.
 */
public class Property {

	public static Property instance=new Property();

    public String getProperty(String key) {
        return NCSLabConfig.get().getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return NCSLabConfig.get().getProperty(key, defaultValue);
    }
}
