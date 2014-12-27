package org.rril.bungeelogin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 basic encryption
 * Used to encrypt passwords
 * 
 * @author Stakzz
 * @version 0.9.0
 */
public class MD5 {
    
	/** */
    private static MessageDigest digester;

    static {
        try {
            digester = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to encrypt a string in md5
     * 
     * @param string String to encrypt
     * @return string String encrypted in MD5
     */
    public static String crypt(String string) {
        if (string == null || string.length() == 0) {
            throw new IllegalArgumentException("String to encript cannot be null or zero length");
        }

        digester.update(string.getBytes());
        byte[] hash = digester.digest();
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
            }
            else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        return hexString.toString();
    }
}