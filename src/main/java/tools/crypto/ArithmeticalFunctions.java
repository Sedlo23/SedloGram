package tools.crypto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.Map;

public final class ArithmeticalFunctions {

    private static final Logger LOG = LogManager.getLogger(ArithmeticalFunctions.class);

    static String[] lookupTable = {"000", "001", "010", "011", "100", "101", "110", "111"};

    /**
     * Returns a string containing a repeated character "c" "x" times.
     *
     * @param x the number of times to repeat the character
     * @param c the character to be repeated
     * @return a string containing "x" instances of the character "c"
     */
    public static String uniString(int x, char c) {
        return String.valueOf(c).repeat(x);
    }

    /**
     * Inverts the binary string by replacing each 0 with 1 and each 1 with 0.
     *
     * @param bs the binary string to be inverted
     * @return the inverted binary string
     */
    public static String invertBin(String bs) {
        StringBuilder text = new StringBuilder();
        for (char a : bs.toCharArray()) {
            text.append(a == '0' ? '1' : '0');
        }
        return text.toString();
    }

    /**
     * Converts a binary string to its hexadecimal equivalent.
     *
     * @param bs a binary string to be converted
     * @return the hexadecimal equivalent of the binary string
     */
    public static String bin2Hex(String bs) {
        
        StringBuilder result = new StringBuilder();
        // Pad the binary string to make its length a multiple of 4
        String paddedBinary = bs + "0".repeat((4 - bs.length() % 4) % 4);

        // Process each 4-bit segment
        for (int i = 0; i < paddedBinary.length(); i += 4) {
            String fourBits = paddedBinary.substring(i, i + 4);
            int decimalValue = bin2Dec(fourBits);
            String hexValue = dec2Hex(decimalValue);
            result.append(hexValue);
        }
        return result.toString();
    }

    public static int bin2Dec(String binary) {
        
        return Integer.parseInt(binary, 2);
    }

    public static String dec2Hex(int decimal) {
        
        return Integer.toHexString(decimal).toUpperCase();
    }

    /**
     * Converts a decimal number to its hexadecimal equivalent.
     *
     * @param d a decimal number to be converted
     * @return the hexadecimal equivalent of the decimal number
     */
    public static String dec2Hex(long d) {
        
        if (d <= 0) return "0";
        int base = 16; // flexible to change in any base under 16
        String hex = "";
        while (d > 0) {
            long digit = d % base; // rightmost digit
            hex = "0123456789ABCDEF".charAt((int) digit) + hex; // String concatenation
            d = d / base;
        }
        return hex;
    }

    // Mapping of hex characters to their binary representations
    private static final String[] hexToBinMap = {
            "0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111",
            "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111"
    };

    /**
     * Converts a hexadecimal string to its binary equivalent.
     *
     * @param hex a hexadecimal string to be converted
     * @return the binary equivalent of the hexadecimal string
     */
    public static String hex2Bin(String hex) {
        
        StringBuilder binary = new StringBuilder();
        for (char c : hex.toCharArray()) {
            // Convert the hex character to its binary string representation
            if (c >= '0' && c <= '9') {
                binary.append(hexToBinMap[c - '0']);
            } else if (c >= 'A' && c <= 'F') {
                binary.append(hexToBinMap[10 + c - 'A']);
            } else if (c >= 'a' && c <= 'f') {
                binary.append(hexToBinMap[10 + c - 'a']);
            } else {
                
                throw new IllegalArgumentException("Input contains non-hex character");
            }
        }
        return binary.toString();
    }

    /**
     * Converts an octal string to its binary equivalent.
     *
     * @param hs an octal string to be converted
     * @return the binary equivalent of the octal string
     */
    public static String oct2Bin(String hs) {
        
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < hs.length(); i++) {
            int index = hs.charAt(i) - '0';
            strBuilder.append(lookupTable[index]);
        }
        return strBuilder.toString();
    }

    /**
     * Converts a decimal number to a binary string with a length of x.
     *
     * @param Dec a decimal number to be converted
     * @param x   the length of the resulting binary string
     * @return a binary string with a length of x
     */
    public static String dec2XBin(String Dec, int x) {
        
        long decimalValue = Long.parseLong(Dec);
        String binaryString = Long.toBinaryString(decimalValue);

        // Calculate the number of zeros needed to pad
        int paddingLength = x - binaryString.length();

        // Use StringBuilder for efficient padding
        StringBuilder sb = new StringBuilder(x);
        for (int i = 0; i < paddingLength; i++) {
            sb.append('0');
        }
        sb.append(binaryString);

        // If the binary string is longer than x, truncate it to the rightmost x characters
        if (sb.length() > x) {
            return sb.substring(sb.length() - x);
        }
        return sb.toString();
    }

    public static String padToEnd(BigInteger num, int length) {
        
        String numStr = num.toString(2);
        while (numStr.length() < length) {
            numStr = "0" + numStr;
        }
        return numStr;
    }

    /**
     * Returns the result of XOR operation between two characters.
     *
     * @param A the first character
     * @param B the second character
     * @return the result of the XOR operation
     */
    private static final Map<String, Character> xorLookup = Map.of(
            "00", '0',
            "01", '1',
            "10", '1',
            "11", '0'
    );

    public static char textXOR(char A, char B) {
        
        return A == B ? '0' : '1';
    }

    /**
     * Divides two binary strings using polynomial division.
     *
     * @param numerator   the numerator of the division operation
     * @param denominator the denominator of the division operation
     * @return the result of the division operation
     */
    public static String polynomialDivision(String numerator, String denominator) {
        

        StringBuilder text = new StringBuilder(numerator);
        final int lenDiff = numerator.length() - denominator.length();
        final int denomLen = denominator.length();

        for (int i = 0; i <= lenDiff; i++) {
            StringBuilder text2 = new StringBuilder();
            String text3 = (text.charAt(0) != '1') ? uniString(denomLen, '0') : denominator;
            char[] xorResult = new char[denomLen];

            for (int j = 0; j < denomLen; j++) {
                xorResult[j] = textXOR(text.charAt(j), text3.charAt(j));
            }
            text2.append(xorResult);

            if (text2.length() > 0) {
                text = new StringBuilder(text2.substring(1)).append(text.substring(denomLen));
            }
        }
        return text.toString();
    }

    public static String polynomialDivision2(String numerator, BigInteger denom) {
        

        BigInteger num = new BigInteger(numerator, 2);
        int numLength = num.bitLength();
        int denomLength = denom.bitLength();

        int lenDiff = numLength - denomLength;

        while (lenDiff >= 0 && !num.equals(BigInteger.ZERO)) {
            // XOR the numerator with the denominator shifted left by the length difference
            num = num.xor(denom.shiftLeft(lenDiff));
            numLength = num.bitLength();
            lenDiff = numLength - denomLength;
        }

        String result = num.toString(2);
        int resultLength = result.length();
        int expectedLength = denom.bitLength() - 1;

        if (resultLength < expectedLength) {
            StringBuilder sb = new StringBuilder(expectedLength);
            for (int i = 0; i < expectedLength - resultLength; i++) {
                sb.append('0');
            }
            sb.append(result);
            result = sb.toString();
        }

        return result;
    }

    /**
     * Adds two binary strings A and B by performing a bitwise XOR operation on each bit of the two strings.
     *
     * @param A the first binary string
     * @param B the second binary string
     * @return the sum of the two binary strings in binary form
     */
    public static String polynomialAddition(String A, String B) {
        
        int aLength = A.length();
        int bLength = B.length();
        int maxLength = Math.max(aLength, bLength);
        char[] result = new char[maxLength];
        char[] aChars = A.toCharArray();
        char[] bChars = B.toCharArray();

        for (int i = 0; i < maxLength; i++) {
            char aBit = i < aLength ? aChars[aLength - 1 - i] : '0';
            char bBit = i < bLength ? bChars[bLength - 1 - i] : '0';
            result[maxLength - 1 - i] = aBit == bBit ? '0' : '1';
        }

        return new String(result);
    }

    /**
     * Converts a hexadecimal string to its ASCII equivalent.
     *
     * @param hs a hexadecimal string to be converted
     * @return the ASCII equivalent of the hexadecimal string
     */
    public static String hex2Ascii(String hs) {
        
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hs.length(); i += 2) {
            String str = hs.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    /**
     * Decodes a hexadecimal string into a byte array.
     *
     * @param hexString a hexadecimal string to be decoded
     * @return a byte array representation of the hexadecimal string
     * @throws IllegalArgumentException if the length of the hexadecimal string is odd
     */
    public static byte[] decodeHexString(String hexString) {
        
        if (hexString.length() % 2 == 1) {
            
            throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    /**
     * Converts a hexadecimal string to a byte.
     *
     * @param hexString a hexadecimal string to be converted
     * @return the byte representation of the hexadecimal string
     */
    public static byte hexToByte(String hexString) {
        
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    /**
     * Converts a hexadecimal character to a digit.
     *
     * @param hexChar a hexadecimal character to be converted
     * @return the digit representation of the hexadecimal character
     * @throws IllegalArgumentException if the character is not a valid hexadecimal character
     */
    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if (digit == -1) {
            
            throw new IllegalArgumentException("Invalid Hexadecimal Character: " + hexChar);
        }
        return digit;
    }
}
