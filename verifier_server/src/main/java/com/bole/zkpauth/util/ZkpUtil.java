package com.bole.zkpauth.util;

import java.math.BigInteger;

/**
 * Utility computations for the ZKP Protocol
 */
public class ZkpUtil {


    /**
     * Compute the values of R1 and R2
     * r1 = g^s.y1^c mod password
     * r2 = h^s.y2^c mod password
     *
     * @param g order q or where g^q mode p = 1
     * @param h order q or where g^q mode p = 1
     * @param p  A prime number
     * @param s  computed key to answer to the challenge
     * @param c  random key
     * @param y1 Prover shared key
     * @param y2 Prover shared key
     * @return
     */
    public static BigInteger[] computeR1R2(
            BigInteger p,
            BigInteger g,
            BigInteger h,
            BigInteger s,
            BigInteger c,
            BigInteger y1,
            BigInteger y2) {
        BigInteger[] rS = new BigInteger[2];


        rS[0] = g.pow(s.intValue()).multiply(y1.pow(c.intValue())).mod(p);
        rS[1] = h.pow(s.intValue()).multiply(y2.pow(c.intValue())).mod(p);

        return  rS;
    }

    /**
     *  Find the largest prime factor - the one that can no longer be
     *  divided by any other number except itself and 1
     *
     * @param n java.math.BigInteger
     * @return java.math.BigInteger
     */
    public static BigInteger lpf(BigInteger n)
    {
        if(n.intValue() < 3 || n.longValue() < 3)
            return n;
        // Initialize the maximum prime
        // factor variable with the
        // lowest one
        BigInteger lpf = BigInteger.valueOf(-1);

        // Print the number of 2s
        // that divide n
        while (n.mod(BigInteger.valueOf(2)).intValue() == 0
                || n.mod(BigInteger.valueOf(2)).longValue() == 0) {
            lpf = BigInteger.valueOf(2);

            n = n.divide(lpf);
        }
        // n must be odd at this point
        while (n.mod(BigInteger.valueOf(3)).intValue() == 0
                || n.mod(BigInteger.valueOf(3)).longValue() == 0) {
            lpf = BigInteger.valueOf(3);
            n = n.divide(lpf);
        }

        // now we have to iterate only for integers
        // who does not have prime factor 2 and 3
        for (int i = 5; i <= n.sqrt().intValue(); i += 6) {
            while (n.mod(BigInteger.valueOf(i)).intValue() == 0) {
                lpf = BigInteger.valueOf(i);
                n = n.divide(lpf);
            }
            while (n.mod(BigInteger.valueOf(i+2)).intValue() == 0) {
                lpf = BigInteger.valueOf(i+2);
                n = n.divide(lpf);
            }
        }

        // This condition is to handle the case
        // when n is a prime number greater than 4
        if (n.intValue() > 4)
            lpf = n;

        return lpf;
    }
}
