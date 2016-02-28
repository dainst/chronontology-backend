package org.dainst.chronontology.handler;

import org.dainst.chronontology.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds rules on which datasets exist and which users
 * have which rights to perform certain operations on these
 * datasets.
 *
 * @author Daniel M. de Oliveira
 */
public class RightsValidator {

    private static final String EDITOR = "editor";
    private static final String READER = "reader";

    public enum Operation {
        EDIT,
        READ
    }


    private Map<String,Map<String,String>> rules = new HashMap<String,Map<String,String>>();

    /**
     * @param rules [dataset[user[permission_level]]]
     */
    public void setRules(Map<String,Map<String,String>> rules) {
        this.rules = rules;
    }

    /**
     * Answers the question if a certain user has sufficient rights to perform
     * a certain operation on a given dataset. The user must at least have the
     * minimum right to do so. For example, if it should be determinded if a user
     * is allowed perform a read operation a certain dataset, then he must have
     * been granted at least read level permissions. If he has been granted edit
     * level permissions, then he is also allowed to read.
     *
     * @param userName not null
     * @param dataset
     * @param operation not null
     * @return <code>true</code> if user has permission to perform operation on dataset.
     *   <code>false</code> otherwise.
     * @throws IllegalArgumentException if not null constraint violated.
     */
    public boolean hasPermission(String userName, String dataset, Operation operation) {

        if (userName==null)  throw new IllegalArgumentException(Constants.MSG_USER_NAME_NOT_NULL);
        if (operation==null) throw new IllegalArgumentException(Constants.MSG_OPERATION_NOT_NULL);

        if (operation.equals(Operation.EDIT))
            return (userName.equals(Constants.USER_NAME_ADMIN)
                    || has(userName,dataset, EDITOR));

        return (operation.equals(Operation.READ) &&
                ((userName.equals(Constants.USER_NAME_ADMIN)
                || has(Constants.USER_NAME_ANONYMOUS,dataset,READER)
                || has(userName,dataset, READER)
                || has(userName,dataset, EDITOR))));
    }


    private boolean has(String userName, String dataset, String right) {
        return ((rules.get(dataset)!=null) &&
                (rules.get(dataset).get(userName)!=null) &&
                (rules.get(dataset).get(userName).equals(right)));
    }
}
