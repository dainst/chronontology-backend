package org.dainst.chronontology;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public class PropertiesHolder {

    final static Logger logger = Logger.getLogger(PropertiesHolder.class);

    private static String serverPort = null;
    private static String esIndexName = null;
    private static String dataStorePath = null;
    private static String esUrl = null;
    private static String[] credentials = null;
    private static String typeNames = null;

    /**
     * @param propertiesFilePath
     * @return true if all the properties for running the application
     *   could be loaded properly. false otherwise.
     */
    public static boolean loadConfiguration(String propertiesFilePath) {
        Properties props = new Properties();
        try (
                FileInputStream is =new FileInputStream(new File(propertiesFilePath)))
        {
            props.load(is);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
        return validate(props);
    }

    private static boolean _validate(final Properties props,final String name) {
        if (props.get(name)==null) {
            logger.error("Property "+name+" does not exist");
            return false;
        }

        return invokeSetter(name,(String)props.get(name));
    }

    /**
     * Invokes the static method with the name "set"+name,
     * with the value as its param, where
     * the first letter of name gets capitalized automatically.
     *
     * @param name
     * @param value
     * @return
     */
    private static boolean invokeSetter(String name, String value) {

        Method method= null;
        try {
            method = Class.forName("org.dainst.chronontology.PropertiesHolder").
                    getDeclaredMethod("set"+capitalize(name), String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            method.invoke(null,value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private static boolean validate(Properties props) {
        return (
            _validate(props,"serverPort") &&
            _validate(props,"esIndexName") &&
            _validate(props,"datastorePath") &&
            _validate(props,"esUrl") &&
            _validate(props,"credentials") &&
            _validate(props,"typeNames")
            );
    }

    public static String getServerPort() {
        return serverPort;
    }

    private static void setServerPort(String serverPort) {
        PropertiesHolder.serverPort= serverPort;
    }

    public static String getTypeNames() {
        return typeNames;
    }

    private static void setTypeNames(String typeNames) {
        PropertiesHolder.typeNames= typeNames;
    }

    public static String[] getCredentials() {
        return credentials;
    }

    private static void setCredentials(String credentials) {
    }

    public static String getEsUrl() {
        return esUrl;
    }

    private static void setEsUrl(String esUrl) {
        PropertiesHolder.esUrl = esUrl;
    }

    public static String getEsIndexName() {
        return esIndexName;
    }

    private static void setEsIndexName(String esIndexName) {
        PropertiesHolder.esIndexName= esIndexName;
    }

    public static String getDataStorePath() {
        return dataStorePath;
    }

    private static void setDatastorePath(String dataStorePath) {
        PropertiesHolder.dataStorePath= dataStorePath;
    }
}
