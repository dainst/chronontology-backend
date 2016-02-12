package org.dainst.chronontology.config;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Config objects are designed to validate, extract and arrange the properties
 * required for the creation of specific application components.
 * Config objects are designed for consumption by their
 * corresponding {@link Configurator} objects.
 *
 * <pre>
 *
 * Application component name: Component
 * Config object: ComponentConfig
 * Configurator object: ComponentConfigurator&lt;Component,ComponentConfig&gt;
 * </pre>
 *
 * @author Daniel M. de Oliveira
 */
public abstract class Config {

    protected String prefix= "";

    protected ArrayList<String> constraintViolations = new ArrayList<String>();

    /**
     * Used to prepare the Config object for consumption
     * by its corresponding {@link Configurator} object.
     *
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
     * @return true if the Config object can be used for configuring a component.
     */
    abstract public boolean validate(Properties props);

    /**
     * Validates the properties objects property with the name specified
     * by the "name" parameter. For doing this, it will call a setter generated from
     * the "name" parameter, and taking the value from the property as its only string argument,
     * following the pattern
     * <pre>
     *
     * Property: myProp=abc
     * Setter name: setMyProp(abc)
     * </pre>
     *
     * It is the responsibility of implementors of Config subclasses to create these
     * setters for each property to validate. It is also the responsibility of implementors
     * to signal each error encountered by throwing {@link ConfigValidationException}.
     *
     * @param props
     * @param name
     * @return false if the setter does not exist or cannot be accessed.
     *   false if a {@link ConfigValidationException}
     *   got thrown from the setter. false if
     */
    boolean _validate(final Properties props, final String name) {
        return _validate(props,name,null);
    }


    /**
     * Works the same as {@link Config#_validate(Properties, String)}, with the exception
     * that if the property does not exist in props, a defaultval is used to pass it
     * to the setter.
     *
     * @param props
     * @param name
     * @param defaultval if set to null, it behaves as specified in {@link Config#_validate(Properties, String)}.
     *   However, it is recommended to use that method instead from {@link Config#validate(Properties)}.
     * @return
     */
    boolean _validate(final Properties props, final String name, final String defaultval) {

        String value= null;
        if (props.get(prefix+name)==null) {

            if (defaultval==null){
                constraintViolations.add("Property \""+prefix+name+"\" does not exist.");
                return false;
            }
            value= defaultval;
        } else
            value= (String)props.get(prefix+name);

        return invokeSetter(name,value);
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
            constraintViolations.add(e.getStackTrace().toString());
            return false;
        }
        return invokeMethod(method,value);

    }

    /**
     * Invokes a method. In any but one case for every exception
     * an error message gets added to {@link Config#constraintViolations}
     *
     * For exceptions of type {@link ConfigValidationException} with no messages
     * nothing gets added to {@link Config#constraintViolations}. This is as designed because
     * it is expected that the constructor {@link ConfigValidationException#ConfigValidationException()}
     * gets used only after validation of a child component in which case the
     * child componenents Config subtyped object adds its encountered error messages
     * to this objects own {@link Config#constraintViolations}. It is the responsibility of
     * the implementor of {@link Config#getConstraintViolations()} then to collect all the messages
     * and pass them to the caller.
     *
     * @param method
     * @param value
     * @return
     */
    private boolean invokeMethod(Method method,String value) {

        String constraintViolation= null;
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
