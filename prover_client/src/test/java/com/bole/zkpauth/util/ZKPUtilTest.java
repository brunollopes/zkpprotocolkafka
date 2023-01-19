package com.bole.zkpauth.util;

import com.bole.zkpauth.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test class to cover possible scenarios for the
 * @com.bole.zkpauth.util.ZKPUtil
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ZKPUtilTest {


    @Test
    public void givenAZeroInput_ComputeKeys_GotErrorMessage() {
        BigInteger number = BigInteger.ZERO;

        try {
            ZKPUtil.computePGHQX(number);
            fail("Shouldn't be able to compute the keys");
        } catch (BadRequestException e) {

        }

    }

    @Test
    public void givenANegativeInput_ComputeKeys_GotErrorMessage() {
        BigInteger number = BigInteger.valueOf(-1);

        try {
            ZKPUtil.computePGHQX(number);
            fail("Shouldn't be able to compute the keys");
        } catch (BadRequestException e) {

        }

    }

    @Test
    public void givenASmallInput_ComputeKeys_GotKeys() {
        BigInteger number = BigInteger.valueOf(9);

        try {

            ZKPUtil.computePGHQX(number);
            fail("Shouldn't be able to compute the keys");
        } catch (BadRequestException e) {

        }

    }

    @Test
    public void givenABigInput_ComputeKeys_GotKeys() {
        BigInteger number = BigInteger.valueOf(Integer.MAX_VALUE);

        try {

            BigInteger[] keys = ZKPUtil.computePGHQX(number);
            Assertions.assertEquals(5,keys.length, "Expected 5 keys");
        } catch (BadRequestException e) {
            fail("Should be able to compute the keys");
        }

    }


    @Test
    public void givenAHugeInput_ComputeKeys_GotKeys() {
        BigInteger number = BigInteger.valueOf(Long.MAX_VALUE);

        try {
            Instant start = Instant.now();
            BigInteger[] keys = ZKPUtil.computePGHQX(number);
            Instant end = Instant.now();
            long elapsed = Duration.between(start,end).toMillis();
            System.out.println("elapsed time (ms): " + elapsed);
            Assertions.assertEquals(5,keys.length, "Expected 5 keys");
        } catch (BadRequestException e) {
            fail("Should be able to compute the keys");
        }

    }

    @Test
    public void givenAHugeInput_IterVersion_ComputeKeys_GotKeys() {
        BigInteger number = BigInteger.valueOf(Long.MAX_VALUE);

        try {
            Instant start = Instant.now();
            BigInteger[] keys = ZKPUtil.computeIterPGHQX(number);
            Instant end = Instant.now();
            long elapsed = Duration.between(start,end).toMillis();
            System.out.println("elapsed time (ms): " + elapsed);
            Assertions.assertEquals(5,keys.length, "Expected 5 keys");
        } catch (BadRequestException e) {
            fail("Should be able to compute the keys");
        }

    }

    @Test
    public void givenABigRandomInput_ComputeKeys_GotKeys() {
        BigInteger rnum = BigInteger.valueOf((int)Math.floor(Math.random()*(Integer.MAX_VALUE-1000+1)+1000));

        try {

            BigInteger[] keys = ZKPUtil.computePGHQX(rnum);
            Assertions.assertEquals(5,keys.length, "Expected 5 keys");
        } catch (BadRequestException e) {
            fail("Should be able to compute the keys");
        }

    }

    @Test
    public void givenAHugeRandomInput_ComputeKeys_GotKeys() {
        BigInteger rnum = BigInteger.valueOf((long)Math.floor(Math.random()*(Integer.MAX_VALUE-1000+1)+1000));

        try {

            BigInteger[] keys = ZKPUtil.computePGHQX(rnum);
            Assertions.assertEquals(5,keys.length, "Expected 5 keys");
        } catch (BadRequestException e) {
            fail("Should be able to compute the keys");
        }

    }

    @Test
    public void givenABigHugeInput_ComputeKeys_GotErrorMessage() {
        BigInteger rnum = new BigInteger("999999999999999999999999999999999");

        try {
            ZKPUtil.computePGHQX(rnum);
            fail("Shouldn't be able to compute the keys");
        } catch (BadRequestException e) {

        }

    }

    @Test
    public void givenValidKeys_ComputeR1R2_GotR1R2() {
        BigInteger k = BigInteger.valueOf(9);
        BigInteger p = BigInteger.valueOf(9);
        BigInteger g = BigInteger.valueOf(9);
        BigInteger h = BigInteger.valueOf(9);

        BigInteger[] keys = ZKPUtil.computeR1R2(k,p,g,h);

        Assertions.assertEquals(2,keys.length,"Fail to compute exactly 2 values");
    }

    @Test
    public void givenHugeKeys_ComputeR1R2_GotR1R2() {
        BigInteger k = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger p = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger g = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger h = BigInteger.valueOf(Long.MAX_VALUE);

        try {
            BigInteger[] keys = ZKPUtil.computeR1R2(k, p, g, h);
            fail("Shouldn't be able to compute R1 and R2 keys");
        } catch (ArithmeticException ex) {

        }

    }

    @Test
    public void givenSmallK_ComputeR1R2_GotR1R2() {
        BigInteger k = BigInteger.valueOf(1000);
        BigInteger p = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger g = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger h = BigInteger.valueOf(Integer.MAX_VALUE);

        try {
            BigInteger[] keys = ZKPUtil.computeR1R2(k, p, g, h);
            Assertions.assertEquals(2,keys.length,"Fail to compute exactly 2 keys");
        } catch (ArithmeticException ex) {
            fail("Should be able to compute R1 and R2 keys");

        }

    }


    @Test
    public void givenLargeK_ComputeR1R2_GotR1R2() {
        int rnum = (int)Math.floor(Math.random()*(Integer.MAX_VALUE-1000+1)+1000);
        BigInteger k = BigInteger.valueOf(rnum/1000);
        BigInteger p = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger g = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger h = BigInteger.valueOf(Integer.MAX_VALUE);

        try {
            BigInteger[] keys = ZKPUtil.computeR1R2(k, p, g, h);
            Assertions.assertEquals(2,keys.length,"Fail to compute exactly 2 keys");
        } catch (ArithmeticException ex) {
            fail("Should be able to compute R1 and R2 keys");

        }

    }

    /**
     * Need optimization for Bigger Ks (>=1000000)
     * takes 10+ seconds
     */
    @Test
    public void givenBigK_ComputeR1R2_GotR1R2() {
        BigInteger k = BigInteger.valueOf(1000000);
        BigInteger p = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger g = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger h = BigInteger.valueOf(Integer.MAX_VALUE);

        try {
            BigInteger[] keys = ZKPUtil.computeR1R2(k, p, g, h);
            Assertions.assertEquals(2,keys.length,"Fail to compute exactly 2 keys");
        } catch (ArithmeticException ex) {
            fail("Should be able to compute R1 and R2 keys");

        }

    }


    @Test
    public void givenNonSenseInput_ComputeS_GotWrongS() {
        BigInteger k = BigInteger.valueOf(123);
        BigInteger c = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger x = BigInteger.valueOf(Integer.MAX_VALUE);
        BigInteger q = BigInteger.valueOf(Integer.MAX_VALUE);

        try {
            BigInteger s = ZKPUtil.computeS(k, c, x, q);
            Assertions.assertTrue(s.intValue()==k.intValue(),"Fail to compute s");
        } catch (ArithmeticException ex) {
            fail("Should be able to compute s");

        }

    }

    @Test
    public void givenValidInput_ComputeS_GotValidS() {
        BigInteger k = BigInteger.valueOf((long)Math.floor(Math.random()*(Integer.MAX_VALUE-1000+1)+1000));
        BigInteger c = BigInteger.valueOf((long)Math.floor(Math.random()*(Integer.MAX_VALUE-1000+1)+1000));
        BigInteger x = BigInteger.valueOf(3);
        BigInteger q = BigInteger.valueOf(54);

        try {
            BigInteger s = ZKPUtil.computeS(k, c, x, q);
            Assertions.assertTrue(s.intValue()>0&s.intValue()!=k.intValue(),"Fail to compute s");
        } catch (ArithmeticException ex) {
            fail("Should be able to compute s");
        }

    }

}
