package by.bsu.famcs.notepad.client.security;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {

    private static final int BIT_LENGTH = 1024;

    public static BigInteger n, e, d;

    public static void generatePublicKey() {
        SecureRandom r = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(BIT_LENGTH, r);
        BigInteger q = BigInteger.probablePrime(BIT_LENGTH, r);

        n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        e = BigInteger.probablePrime(BIT_LENGTH / 2, r);
        while (phi.gcd(e).compareTo(BigInteger.ONE) > 0 && e.compareTo(phi) < 0) {
            e = e.add(BigInteger.ONE);
        }
        d = e.modInverse(phi);
    }

    public static byte[] decrypt(byte[] message) {
        return (new BigInteger(message)).modPow(d, n).toByteArray();
    }
}
