package org.dainst.chronontology.config;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class PropertiesLoader {

    final static Logger logger = Logger.getLogger(AppConfig.class);

    public static Properties loadConfiguration(String propertiesFilePath) {
        Properties props = new Properties();
        try (
                FileInputStream is =new FileInputStream(new File(propertiesFilePath)))
        {
            props.load(is);
        } catch (IOException e) {
            return null;
        }
        return props;
    }
}
