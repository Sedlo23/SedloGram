package tools.crypto;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;

public class ArithmeticalFunctionsTest {

    @Test
    public void testUniString() {
        assertEquals("aaaaa", ArithmeticalFunctions.uniString(5, 'a'));
        assertEquals("", ArithmeticalFunctions.uniString(0, 'x'));
        assertEquals("00000", ArithmeticalFunctions.uniString(5, '0'));
    }

    @Test
    public void testInvertBin() {
        assertEquals("10101", ArithmeticalFunctions.invertBin("01010"));
        assertEquals("111", ArithmeticalFunctions.invertBin("000"));
        assertEquals("000", ArithmeticalFunctions.invertBin("111"));
        assertEquals("", ArithmeticalFunctions.invertBin(""));
    }

    @Test
    public void testBin2Hex() {
        assertEquals("A", ArithmeticalFunctions.bin2Hex("1010"));
        assertEquals("F", ArithmeticalFunctions.bin2Hex("1111"));
        assertEquals("0", ArithmeticalFunctions.bin2Hex("0000"));
        assertEquals("42", ArithmeticalFunctions.bin2Hex("01000010"));
        assertEquals("2A", ArithmeticalFunctions.bin2Hex("00101010"));
    }

    @Test
    public void testBin2Dec() {
        assertEquals(10, ArithmeticalFunctions.bin2Dec("1010"));
        assertEquals(15, ArithmeticalFunctions.bin2Dec("1111"));
        assertEquals(0, ArithmeticalFunctions.bin2Dec("0000"));
        assertEquals(42, ArithmeticalFunctions.bin2Dec("101010"));
    }

    @Test
    public void testDec2Hex() {
        assertEquals("A", ArithmeticalFunctions.dec2Hex(10));
        assertEquals("F", ArithmeticalFunctions.dec2Hex(15));
        assertEquals("0", ArithmeticalFunctions.dec2Hex(0));
        assertEquals("2A", ArithmeticalFunctions.dec2Hex(42));

        // Test the long version
        assertEquals("A", ArithmeticalFunctions.dec2Hex(10L));
        assertEquals("F", ArithmeticalFunctions.dec2Hex(15L));
        assertEquals("0", ArithmeticalFunctions.dec2Hex(0L));
        assertEquals("2A", ArithmeticalFunctions.dec2Hex(42L));
        assertEquals("3E8", ArithmeticalFunctions.dec2Hex(1000L));
    }

    @Test
    public void testHex2Bin() {
        assertEquals("1010", ArithmeticalFunctions.hex2Bin("A"));
        assertEquals("1111", ArithmeticalFunctions.hex2Bin("F"));
        assertEquals("0000", ArithmeticalFunctions.hex2Bin("0"));
        assertEquals("01000010", ArithmeticalFunctions.hex2Bin("42"));
        assertEquals("00101010", ArithmeticalFunctions.hex2Bin("2a"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHex2BinWithInvalidInput() {
        ArithmeticalFunctions.hex2Bin("G");
    }

    @Test
    public void testOct2Bin() {
        assertEquals("001010011", ArithmeticalFunctions.oct2Bin("123"));
        assertEquals("000", ArithmeticalFunctions.oct2Bin("0"));
        assertEquals("111", ArithmeticalFunctions.oct2Bin("7"));
        assertEquals("001010111", ArithmeticalFunctions.oct2Bin("127"));
    }

    @Test
    public void testDec2XBin() {
        assertEquals("00010", ArithmeticalFunctions.dec2XBin("2", 5));
        assertEquals("1010", ArithmeticalFunctions.dec2XBin("10", 4));
        assertEquals("010", ArithmeticalFunctions.dec2XBin("2", 3));
        assertEquals("00000", ArithmeticalFunctions.dec2XBin("0", 5));
        assertEquals("01111", ArithmeticalFunctions.dec2XBin("15", 5));
    }

    @Test
    public void testPadToEnd() {
        assertEquals("0101", ArithmeticalFunctions.padToEnd(new BigInteger("5"), 4));
        assertEquals("00101", ArithmeticalFunctions.padToEnd(new BigInteger("5"), 5));
        assertEquals("1010", ArithmeticalFunctions.padToEnd(new BigInteger("10"), 4));
        assertEquals("0000", ArithmeticalFunctions.padToEnd(new BigInteger("0"), 4));
    }

    @Test
    public void testTextXOR() {
        assertEquals('0', ArithmeticalFunctions.textXOR('0', '0'));
        assertEquals('1', ArithmeticalFunctions.textXOR('0', '1'));
        assertEquals('1', ArithmeticalFunctions.textXOR('1', '0'));
        assertEquals('0', ArithmeticalFunctions.textXOR('1', '1'));
    }

    @Test
    public void testPolynomialDivision() {
        assertEquals("100", ArithmeticalFunctions.polynomialDivision("110101", "1011"));
        assertEquals("01", ArithmeticalFunctions.polynomialDivision("11001", "100"));
        assertEquals("11", ArithmeticalFunctions.polynomialDivision("100100", "101"));
    }

    @Test
    public void testPolynomialDivision2() {
        assertEquals("100", ArithmeticalFunctions.polynomialDivision2("110101", new BigInteger("1011", 2)));
        assertEquals("01", ArithmeticalFunctions.polynomialDivision2("11001", new BigInteger("100", 2)));
        assertEquals("11", ArithmeticalFunctions.polynomialDivision2("100100", new BigInteger("101", 2)));
    }

    @Test
    public void testPolynomialAddition() {
        assertEquals("111", ArithmeticalFunctions.polynomialAddition("101", "010"));
        assertEquals("1100", ArithmeticalFunctions.polynomialAddition("1010", "0110"));
        assertEquals("0", ArithmeticalFunctions.polynomialAddition("0", "0"));
        assertEquals("1010", ArithmeticalFunctions.polynomialAddition("1010", "0000"));
        assertEquals("1111", ArithmeticalFunctions.polynomialAddition("1100", "0011"));
    }

    @Test
    public void testHex2Ascii() {
        assertEquals("Hello", ArithmeticalFunctions.hex2Ascii("48656C6C6F"));
        assertEquals("ABC", ArithmeticalFunctions.hex2Ascii("414243"));
        assertEquals("123", ArithmeticalFunctions.hex2Ascii("313233"));
    }

    @Test
    public void testDecodeHexString() {
        byte[] expected1 = {(byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F};
        assertArrayEquals(expected1, ArithmeticalFunctions.decodeHexString("48656C6C6F"));

        byte[] expected2 = {(byte) 0x41, (byte) 0x42, (byte) 0x43};
        assertArrayEquals(expected2, ArithmeticalFunctions.decodeHexString("414243"));

        byte[] expected3 = {(byte) 0xFF, (byte) 0x00};
        assertArrayEquals(expected3, ArithmeticalFunctions.decodeHexString("FF00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeHexStringWithInvalidLength() {
        ArithmeticalFunctions.decodeHexString("ABC");
    }

    @Test
    public void testHexToByte() {
        assertEquals((byte) 0x0A, ArithmeticalFunctions.hexToByte("0A"));
        assertEquals((byte) 0xFF, ArithmeticalFunctions.hexToByte("FF"));
        assertEquals((byte) 0x00, ArithmeticalFunctions.hexToByte("00"));
        assertEquals((byte) 0xAB, ArithmeticalFunctions.hexToByte("AB"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexToByteWithInvalidChar() {
        ArithmeticalFunctions.hexToByte("GH");
    }
}