package by.bsu.famcs.notepad.server.security;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {

    public static byte[] encrypt(byte[] message, BigInteger e, BigInteger n) {
        return (new BigInteger(message)).modPow(e, n).toByteArray();
    }
}
