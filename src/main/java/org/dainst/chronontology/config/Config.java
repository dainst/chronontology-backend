package org.dainst.chronontology.config;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public abstract class Config {

    final static Logger logger = Logger.getLogger(Config.class);

    protected String prefix= "";


    abstract public boolean validate(Properties props);

    boolean _validate(final Properties props, final String name) {
        return _validate(props,name,false);
    }


    boolean _validate(final Properties props, final String name, final boolean optional) {

        if (props.get(prefix+name)==null) {
            if (!optional){
                logger.error("Property \""+prefix+name+"\" does not exist.");
                return false;
            }
            return true;
        }

        return invokeSetter(name,(String)props.get(prefix+name));
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
            if (e.getCause().getClass().equals(ConfigException.class))
                logger.error(e.getCause().getMessage());
            else
                e.printStackTrace();
            return false;
        }


        return true;
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

}
