package com.bole.zkpauth.util;

import com.bole.zkpauth.exception.BadRequestException;

import java.math.BigInteger;
import java.util.Vector;

/**
 * Utility computations for the ZKP Protocol
 */
public class ZKPUtil {

    /**
     * Obtain all the Prime factors for the given number
     *
     * @param n BigInteger
     * @return java.util.Vector containing all the Prime factors found
     */
    public static Vector primeFactors(BigInteger n) {
        Vector res = new Vector();
        BigInteger c = BigInteger.valueOf(2);

        while (n.intValue() > 1 || n.longValue() > 1) {
            if (n.mod(c).intValue() == 0 | n.mod(c).longValue() == 0) {
                n = n.divide(c);
                res.add(n);
            } else {
                c = c.add(BigInteger.ONE);
            }

        }

        return res;
    }

    /**
     * Find the largest prime factor - the one that can no longer be
     * divided by any other number except itself and 1
     *
     * @param n java.math.BigInteger
     * @return java.math.BigInteger
     */
    public static BigInteger lpf(BigInteger n) {
        if (n.intValue() < 3 || n.longValue() < 3)
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
            while (n.mod(BigInteger.valueOf(i + 2)).intValue() == 0) {
                lpf = BigInteger.valueOf(i + 2);
                n = n.divide(lpf);
            }
        }

        // This condition is to handle the case
        // when n is a prime number greater than 4
        if (n.intValue() > 4)
            lpf = n;

        return lpf;
    }

    /**
     * Verifies is the given number is a Prime number
     *
     * @param n java.math.BigInteger
     * @return boolean
     */
    public static boolean isPrime(final BigInteger n) {
        return n.isProbablePrime(1);

    }

    /**
     * Converts a String into a BigInteger
     *
     * @param number string to be converted
     * @return java.math.BigInteger
     * @throws NumberFormatException if not possible to convert the String into a BigInteger
     */
    public static BigInteger getBigInteger(String number) throws NumberFormatException {
        return new BigInteger(number);
    }

    /**
     * 1. Finds the value of P, verifying if the given input is a prime number
     * 2. If not finds the next prime number backs to 1.
     * 3. Finds the value of X (the greatest prime factor)
     * 4. Find all the prime factors of P-1
     * 5. Checks if there is at least 3 prime factors found
     * 6. In case of not able to find all the values throws an exception
     *
     * @param number java.math.BigInteger
     * @return an array of 5 positions with the values for P, G, H, Q and X
     * @throws BadRequestException if not able to compute all the required keys
     */
    public static BigInteger[] computePGHQX(BigInteger number) throws BadRequestException {
        BigInteger[] pghqx = new BigInteger[5];

        if (isPrime(number)) {
            pghqx[0] = number;

            BigInteger x = lpf(number.subtract(BigInteger.ONE));
            pghqx[4] = x;
            Vector<BigInteger> res = primeFactors(number.subtract(BigInteger.ONE));
            if (res.size() < 3) {
                throw new BadRequestException("Unable to compute all the keys, please chose a different input");
            } else {
                pghqx[3] = res.get(0);
                pghqx[2] = res.get(1);
                pghqx[1] = res.get(2);
            }
            return pghqx;
        } else {
            BigInteger p = lpf(number);
            if (p.intValue() > 3 || p.longValue() > 3) {
                if (p.toString().length() > String.valueOf(Integer.MAX_VALUE).length()) {
                    return computePGHQX(p.subtract(BigInteger.TEN));
                } else {
                    return computePGHQX(p);
                }

            } else {
                throw new BadRequestException("Unable to compute all the keys, please chose a different input");
            }
        }
    }

    public static BigInteger[] computeIterPGHQX(BigInteger number) throws BadRequestException {
        final BigInteger[] pghqx = new BigInteger[5];

        BigInteger nPrime = null;

        while (!isPrime(number)) {
            nPrime = lpf(number);
            if (nPrime.intValue() > 3 || nPrime.longValue() > 3) {
                if (nPrime.toString().length() > String.valueOf(Integer.MAX_VALUE).length()) {
                    nPrime = nPrime.subtract(BigInteger.TEN);
                }
            } else {
                throw new BadRequestException("Unable to compute all the keys, please chose a different input");
            }
        }

        BigInteger p;

        if (nPrime==null) {
            p = number;
        } else {
            p = nPrime;
        }

        pghqx[0] = p;
        BigInteger x = lpf(p.subtract(BigInteger.ONE));

        pghqx[4] = x;
        Vector<BigInteger> res = primeFactors(p.subtract(BigInteger.ONE));
        if (res.size() < 3) {
            throw new BadRequestException("Unable to compute all the keys, please chose a different input");
        } else {
            pghqx[3] = res.get(0);
            pghqx[2] = res.get(1);
            pghqx[1] = res.get(2);
        }
        return pghqx;

    }

    /**
     * Build array of BigIntegers
     * @param keyP java.math.BigInteger
     * @param keyG java.math.BigInteger
     * @param keyH java.math.BigInteger
     * @param keyQ java.math.BigInteger
     * @param keyX java.math.BigInteger
     * @return java.math.BigInteger[]
     */
    public static BigInteger[] buildPGHQX(
            BigInteger keyP,BigInteger keyG,BigInteger keyH,BigInteger keyQ,BigInteger keyX
    ) {
        BigInteger[] pghqx = new BigInteger[]{keyP,keyG,keyH,keyQ,keyX};
        return  pghqx;
    }
    /**
     * Perform the calculation to obtain Y1 and Y2 from password X, G and H
     * @param g java.math.BigInteger
     * @param h java.math.BigInteger
     * @param x java.math.BigInteger
     * @param p java.math.BigInteger
     * @return java.math.BigInteger[]
     */
    public static BigInteger[] computeY1Y2(BigInteger g, BigInteger h, BigInteger x, BigInteger p){
        BigInteger[] yS = new BigInteger[2];

        yS[0] = g.pow(x.intValue()).mod(p);
        yS[1] = h.pow(x.intValue()).mod(p);

        return yS;
    }

    /**
     * Compute R1 and R2
     * r1 = g^k mod p
     * r2 = h^k mod p
     *
     * @param k java.math.BigInteger
     * @param p java.math.BigInteger
     * @param g java.math.BigInteger
     * @param h java.math.BigInteger
     * @return java.math.BigInteger[]
     */
    public static BigInteger[] computeR1R2(BigInteger k, BigInteger p, BigInteger g, BigInteger h) {
        BigInteger[] rS = new BigInteger[2];

        rS[0] = g.pow(k.intValue()).mod(p);
        rS[1] = h.pow(k.intValue()).mod(p);

        return rS;
    }

    /**
     Compute the value of the math form
     s = k - c.x (mod q)
     *
     * @param k java.math.BigInteger
     * @param c java.math.BigInteger
     * @param x java.math.BigInteger
     * @param q java.math.BigInteger
     * @return java.math.BigInteger
     */
    public static BigInteger computeS(BigInteger k, BigInteger c, BigInteger x, BigInteger q) {
        return k.subtract(c.multiply(x)).mod(q);

    }
}
