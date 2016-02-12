package org.dainst.chronontology.config;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Daniel M. de Oliveira
 */
public abstract class Config {

    protected String prefix= "";

    protected ArrayList<String> constraintViolations = new ArrayList<String>();

    /**
     * It is recommended that implementations
     * follow this pattern for validating the properties considered to be
     * part of the configuration of the component Config belongs to:
     *
     * <pre>
     *
     * return (
     *   _validate(props,"propA") &
     *   _validate(props,"propB") &
     * );
     * </pre>
     *
     * Using the & makes sure that all validations get executed and
     * all validation messages get collected so that a caller can
     * retrieve them via {@link #getConstraintViolations()}.
     *
     * @param props
     * @return
     */
    abstract public boolean validate(Properties props);

    boolean _validate(final Properties props, final String name) {
        return _validate(props,name,false);
    }


    boolean _validate(final Properties props, final String name, final boolean optional) {

        if (props.get(prefix+name)==null) {
            if (!optional){
                constraintViolations.add("Property \""+prefix+name+"\" does not exist.");
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
        String constraintViolation= null;
        try {
            method = this.getClass().
                    getDeclaredMethod("set"+capitalize(name), String.class);
        } catch (NoSuchMethodException e) {
            constraintViolation= e.getStackTrace().toString();
        }
        try {
            method.invoke(this,value);
        } catch (IllegalAccessException e) {
            constraintViolation= e.getStackTrace().toString();
        } catch (InvocationTargetException e) {
            if (e.getCause().getClass().equals(ConfigValidationException.class)) {

                if (e.getCause().getMessage()!=null)
                    constraintViolation= e.getCause().getMessage();
            }
            else
                constraintViolation= e.getStackTrace().toString();
        }

        if (constraintViolation!=null) {
            constraintViolations.add(constraintViolation);
            return false;
        }
        return true;
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    /**
     * To collect all error messages from the actual Config
     * object as well as all other messages from the Config hierarchy,
     * it is recommended that implementors follow this pattern
     *
     * <pre>
     *
     *   ArrayList<String> allViolations= new ArrayList<String>();
     *   allViolations.addAll(constraintViolations);
     *   allViolations.addAll(childConfig.getConstraintViolations());
     *   return allViolations;
     * </pre>
     *
     * where childConfig denotes some field of the actual object, which also
     * is of a subtype of Config.
     *
     * @return all the validation violation error messages from a hierarchy
     *   of subtypes of Config.
     */
    abstract public ArrayList<String> getConstraintViolations();
}
