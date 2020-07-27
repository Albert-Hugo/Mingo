package com.ido.mingo.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 读取配置文件 默认的config.properties 和自定义都支持
 */
public class Config {

    private static final String DEFAULT_CONF = "config.properties";

    private static Map<String, Config> instances = new ConcurrentHashMap<String, Config>();

    private Properties configuration = new Properties();

    private Config() {
        initConfig(DEFAULT_CONF);
    }

    private Config(String configFile) {
        initConfig(configFile);
    }

    private void initConfig(String configFile) {
        InputStream is = Config.class.getClassLoader().getResourceAsStream(configFile);
        try {
            configuration.load(is);
            is.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 获得Configuration实例。 默认为config.property
     *
     * @return Configuration实例
     */
    public static Config getInstance() {
        return getInstance(DEFAULT_CONF);
    }

    /**
     * 自定义文件解析**.property
     *
     * @param configFile
     * @return
     */
    public static Config getInstance(String configFile) {
        Config config = instances.get(configFile);
        if (config == null) {
            synchronized (instances) {
                config = instances.get(configFile);
                if (config == null) {
                    config = new Config(configFile);
                    instances.put(configFile, config);
                }
            }
        }
        return config;
    }

    /**
     * 获得配置项。
     *
     * @param key 配置关键字
     * @return 配置项
     */
    public String getStringValue(String key) {
        return System.getProperty(key, configuration.getProperty(key));
    }


    public int getIntValue(String key) {
        return Integer.getInteger(key, LangUtil.parseInt(configuration.getProperty(key)));
    }


    public double getDoubleValue(String key) {
        return LangUtil.parseDouble(configuration.getProperty(key));
    }


    public double getLongValue(String key) {
        return LangUtil.parseLong(configuration.getProperty(key));
    }


    public Boolean getBooleanValue(String key) {
        return LangUtil.parseBoolean(configuration.getProperty(key));
    }

}