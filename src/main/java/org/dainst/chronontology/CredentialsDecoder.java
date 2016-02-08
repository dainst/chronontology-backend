package org.dainst.chronontology;

/**
 * @author Daniel M. de Oliveira
 */
public class CredentialsDecoder {

    /**
     * @param toDecode the value of request header "Authorization".
     * @return
     */
    public static String decode(String toDecode) {
        return new String(
                java.util.Base64.getDecoder().decode(
                toDecode.substring("Basic".length()).trim()));
    }
}
