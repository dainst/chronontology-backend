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
public class AppConfig {

    final static Logger logger = Logger.getLogger(AppConfig.class);

    private String serverPort = Constants.SERVER_PORT;
    private String esIndexName = Constants.ES_INDEX_NAME;
    private String dataStorePath = Constants.DATASTORE_PATH;
    private String esUrl = Constants.EMBEDDED_ES_URL;
    private String[] credentials = null;
    private String typeNames = null;
    private boolean useEmbeddedES = false;

    /**
     * @param propertiesFilePath
     * @return true if all the properties for running the application
     *   could be loaded properly. false otherwise.
     */
    public boolean loadConfiguration(String propertiesFilePath) {
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

    private boolean _validate(final Properties props,final String name) {
        return _validate(props,name,false);
    }


    private boolean _validate(final Properties props,final String name, final boolean optional) {

        if (props.get(name)==null) {
            if (!optional){
                logger.error("Property "+name+" does not exist");
                return false;
            }
            return true;
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
    private boolean invokeSetter(String name, String value) {

        Method method= null;
        try {
            method = this.getClass().
                    getDeclaredMethod("set"+capitalize(name), String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        try {
            method.invoke(this,value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private boolean validate(Properties props) {
        return (
            _validate(props,"serverPort",true) &&
            _validate(props,"esIndexName",true) &&
            _validate(props,"datastorePath",true) &&
            _validate(props,"useEmbeddedES", true) &&
            _validate(props,"esUrl",(useEmbeddedES)) &&  // must come after useEmbeddedES
            _validate(props,"credentials") &&
            _validate(props,"typeNames")
            );
    }

    public String getServerPort() {
        return serverPort;
    }

    private void setServerPort(String serverPort) {
        this.serverPort= serverPort;
    }

    public String getTypeNames() {
        return this.typeNames;
    }

    private void setTypeNames(String typeNames) {
        this.typeNames= typeNames;
    }

    public String[] getCredentials() {
        return credentials;
    }

    private void setCredentials(String credentials) {
        this.credentials= credentials.split(",");
    }

    public String getEsUrl() {
        return esUrl;
    }

    private void setEsUrl(String esUrl) {
        this.esUrl = esUrl;

        // override it
        if (useEmbeddedES) this.esUrl= Constants.EMBEDDED_ES_URL;
    }

    public String getEsIndexName() {
        return esIndexName;
    }

    private void setEsIndexName(String esIndexName) {
        this.esIndexName= esIndexName;
    }

    public String getDataStorePath() {
        return dataStorePath;
    }

    private void setDatastorePath(String dataStorePath) {
        this.dataStorePath= dataStorePath;
    }

    public boolean isUseEmbeddedES() {
        return useEmbeddedES;
    }

    private void setUseEmbeddedES(String useIt) {
        if (useIt.equals("true")) {
            useEmbeddedES= true;
        }
    }
}
