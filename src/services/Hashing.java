package services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

public class Hashing {

    public static String getHash(String input){
        String hashedString = "";
        byte[] inputBytes = input.getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-224");
            messageDigest.update(inputBytes);
            byte[] digestedBytes = messageDigest.digest();
            hashedString = DatatypeConverter.printHexBinary(digestedBytes).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedString;
    }

}