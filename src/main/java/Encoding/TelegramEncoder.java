package Encoding;

import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tools.crypto.ArithmeticalFunctions;

import java.math.BigInteger;

import static Encoding.EncryptionHelperList.*;

/**
 * TelegramEncoder is responsible for encoding an input string into a telegram
 * representation using various transformations and error-checking conditions.
 * <p>
 * The encoding process involves iterative scrambling and transformation of
 * the input telegram's binary data until it meets a set of conditions.
 * </p>
 */
public class TelegramEncoder {

    private static final Logger LOG = LogManager.getLogger(TelegramEncoder.class);

    // Global state variables used during encoding
    static BigInteger SB;
    static BigInteger ESB;
    static BigInteger adderESB = BigInteger.ONE;
    static final BigInteger STATIC_VALUE = new BigInteger("001", 2).shiftLeft(12);

    /**
     * Encodes the provided input string into a telegram using the given SB, ESB, and adder values.
     *
     * @param input      the input telegram as a binary string
     * @param sb         initial SB value (12 bits)
     * @param esb        initial ESB value (10 bits)
     * @param adderESB1  additional value to be added to ESB during processing
     * @return the encoded telegram in hexadecimal form, or null if encoding fails
     */
    public static String encode(String input, int sb, int esb, BigInteger adderESB1) {

        LOG.info("Začátek kodování - ["+ArithmeticalFunctions.bin2Hex(input)+"]");

        Telegram telegram = new Telegram(input);

        LOG.info("Délka telegramu - ("+(telegram.isLongTelegram?"Dlouhý":"Krátký")+")");

        // Initialize global state variables
        SB = BigInteger.valueOf(sb);
        ESB = BigInteger.valueOf(esb);
        adderESB = adderESB1;

        LOG.info("SB="+SB.toString(16)+" - ESB="+ESB.toString(16)+" - krok="+adderESB.toString(16));

        BigInteger s = BigInteger.ZERO;
        BigInteger Utick = BigInteger.ZERO;
        BigInteger scrambledData = BigInteger.ZERO;
        BigInteger transformData = BigInteger.ZERO;
        
        int currentSB = -1;

        // Iterate up to a maximum number of iterations (here 4194303) to find valid encoding.
        for (int i = 0; i < 4194303; i++) {
            int tmpSB = setNextSbEsb(telegram);
            if (tmpSB != currentSB) {
                
                s = calculateS(telegram.getSB());
                Utick = determineUTick(telegram.userdata, telegram.isLongTelegram ? 830 : 210);
                scrambledData = scrambleUserData(s, CURRENT_INPUT, Utick, telegram.isLongTelegram ? 830 : 210);
                transformData = transform10to11(scrambledData, telegram.isLongTelegram ? 830 : 210);
                currentSB = tmpSB;
            }
            // Update the telegram's transformation data.
            telegram.transformData = telegram.transformData.or(transformData);

            BigInteger CB = computeCheckBits(telegram.transformData, telegram.isLongTelegram);
            if (CB == null) {
                continue;
            }
            telegram.transformData = telegram.transformData.or(CB);

            // Check several conditions to validate the transformation.
            if (!check_alphabet_condition(telegram)) {
                LOG.debug("Neprošla abeceda");
                continue;
            }
            if (!check_aperiodicity_condition(telegram)) {
                LOG.debug("Neprošla aperiodicity");
                continue;
            }
            if (!check_off_synch_parsing_condition(telegram)) {
                LOG.debug("Neprošla synch parsing");
                continue;
            }
            if (!checkUndersamplingCondition(telegram)) {
                LOG.debug("Neprošla undersampling");
                continue;
            }

            // Prepare binary string transformation by padding appropriately.
            String binaryTransform = telegram.transformData.toString(2);
            int shiftAmount = telegram.isLongTelegram ? 1023 : 341;
            int shiftAmount2 = telegram.isLongTelegram ? 1024 : 344;
            binaryTransform = padStart(binaryTransform, shiftAmount, '0');
            binaryTransform = padEnd(binaryTransform, shiftAmount2, '0');

            // Convert the binary string to hexadecimal.
            String result = ArithmeticalFunctions.bin2Hex(binaryTransform);
            
            LOG.info("Výsledek - ["+ result+"]");
            
            return result;
        }

        return null;
    }

    /**
     * Encodes the input using default SB, ESB, and adder values.
     *
     * @param input the input telegram as a binary string
     * @return the encoded telegram as a hexadecimal string, or null if encoding fails
     */
    public static String encode(String input) {
        
        return encode(input, 0, 0, BigInteger.ONE);
    }

    /**
     * Encodes the input using a specified adder value.
     *
     * @param input  the input telegram as a binary string
     * @param adder  the adder value for ESB adjustments
     * @return the encoded telegram as a hexadecimal string, or null if encoding fails
     */
    public static String encode(String input, BigInteger adder) {
        
        adderESB = adder;
        return encode(input, 0, 0, adderESB);
    }

    /**
     * Checks the "off-synch parsing" condition on the telegram.
     *
     * @param telegram the telegram to check
     * @return true if the condition is satisfied; false otherwise
     */
    public static boolean check_off_synch_parsing_condition(Telegram telegram) {
        
        BigInteger tested = telegram.transformData.shiftLeft(telegram.getSize()).or(telegram.transformData);
        int telegramSize = telegram.getSize();
        int[] maxCvw = telegram.isLongTelegram
                ? new int[]{0, 2, 10, 10, 10, 10, 10, 10, 10, 10, 2}
                : new int[]{0, 2, 6, 6, 6, 6, 6, 6, 6, 6, 2};

        for (int offset = 1; offset < 11; offset++) {
            int err = 0;
            for (int i = offset; i < telegramSize + ((offset + 1) * 11); i += 11) {
                if (EncryptionHelperList.WORDS_11_REVERSE[getIntFromBigIntAtIndex(tested, i, 11).intValue()] != -1) {
                    err += 1;
                } else {
                    err = 0;
                }
                if (err > maxCvw[offset]) {
                    
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks the "alphabet condition" for the telegram.
     *
     * @param telegram the telegram to check
     * @return true if all 11-bit words are valid; false otherwise
     */
    public static boolean check_alphabet_condition(Telegram telegram) {
        
        int sizeInWords = telegram.getSize() / 11;
        for (int i = 0; i < sizeInWords; i++) {
            int bit11 = getIntFromBigIntAtIndex(telegram.transformData, i * 11, 11).intValue();
            if (EncryptionHelperList.WORDS_11_REVERSE[bit11] == -1) {
                
                return false;
            }
        }
        return true;
    }

    /**
     * Undersamples the telegram's user data.
     *
     * @param telegram the telegram to undersample
     * @param k        the sampling factor
     * @param offset   the sampling offset
     * @return the undersampled BigInteger value
     */
    public static BigInteger undersampleTelegram(Telegram telegram, int k, int offset) {
        
        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < telegram.getSize(); i++) {
            int bitVal = telegram.transformData.testBit((i * k + offset) % telegram.getSize()) ? 1 : 0;
            result = setBit(result, i, bitVal);
        }
        return result;
    }

    /**
     * Determines the maximum run of valid 11-bit words within a BigInteger.
     *
     * @param bigInteger the BigInteger containing data words
     * @param telegram   the telegram (for size reference)
     * @return the maximum consecutive count of valid words
     */
    public static int getMaxRunValidWords(BigInteger bigInteger, Telegram telegram) {
        int telegramSize = telegram.getSize();
        int maxRun = 0;
        int[][] cache = new int[11][];
        for (int offset = 0; offset < 11; offset++) {
            cache[offset] = new int[telegramSize + 30 * 11];
            for (int i = 0, index = 0; i < telegramSize + 30 * 11; i += 11, index++) {
                cache[offset][index] = getIntFromBigIntAtIndex(bigInteger, i + offset, 11).intValue();
            }
        }
        for (int offset = 0; offset < 11; offset++) {
            int consecutive = 0;
            for (int value : cache[offset]) {
                if (EncryptionHelperList.WORDS_11_REVERSE[value] != -1) {
                    consecutive++;
                } else {
                    maxRun = Math.max(maxRun, consecutive);
                    consecutive = 0;
                }
            }
            maxRun = Math.max(maxRun, consecutive);
        }
        return maxRun;
    }

    /**
     * Checks the undersampling condition for the telegram.
     *
     * @param telegram the telegram to check
     * @return true if undersampling condition is met; false otherwise
     */
    public static boolean checkUndersamplingCondition(Telegram telegram) {
        
        BigInteger v;
        for (int k = 2; k <= 16; k *= 2) {
            for (int offset = 0; offset < 11 * k; offset++) {
                v = undersampleTelegram(telegram, k, offset);
                int maxRunValidWords = getMaxRunValidWords(v, telegram);
                if (maxRunValidWords > 30) {
                    
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calculates S using the provided B value.
     *
     * @param B the B value (SB, 12 bits)
     * @return the computed S
     */
    private static BigInteger calculateS(BigInteger B) {
        
        return MULTIPLIER.multiply(B).mod(MODULO);
    }

    /**
     * Sets a bit in a BigInteger at a specific index to the specified value.
     *
     * @param number the original BigInteger
     * @param index  the bit index
     * @param value  the bit value (0 or 1)
     * @return the updated BigInteger
     */
    private static BigInteger setBit(BigInteger number, int index, int value) {
        return (value == 1) ? number.setBit(index) : number.clearBit(index);
    }

    /**
     * Scrambles the user data using S and a polynomial from currentInput.
     *
     * @param S             the computed S value
     * @param currentInput  a polynomial value (CURRENT_INPUT)
     * @param userDataOrig  the original user data as BigInteger
     * @param m             the length parameter (830 for long, 210 for short)
     * @return the scrambled user data
     */
    public static BigInteger scrambleUserData(BigInteger S, BigInteger currentInput, BigInteger userDataOrig, int m) {
        
        BigInteger scrambled = BigInteger.ZERO;
        for (int i = m - 1; i >= 0; i--) {
            boolean userBit = userDataOrig.testBit(i);
            boolean t = S.testBit(31);
            int sb = (t ^ userBit) ? 1 : 0;
            if (sb == 1) {
                scrambled = scrambled.setBit(i);
            }
            S = S.shiftLeft(1);
            if (sb == 1) {
                S = S.xor(currentInput);
            }
        }
        return scrambled;
    }

    /**
     * Determines UTick by summing 10-bit words in U and restricting the result to 10 bits.
     *
     * @param U the BigInteger representing U
     * @param m the length parameter (830 for long, 210 for short)
     * @return the computed UTick as a BigInteger
     */
    public static BigInteger determineUTick(BigInteger U, int m) {
        
        int sum = 0;
        int mDiv10 = m / 10;
        for (int i = 0; i < mDiv10; i++) {
            int x = getWord(U, i * 10);
            sum += x;
        }
        sum &= 0x3FF; // Ensure only 10 bits
        return writeToLocation(U, m - 10, sum, 10);
    }

    /**
     * Retrieves a 16-bit word from the given BigInteger starting at the specified position.
     *
     * @param number   the BigInteger to extract from
     * @param position the bit position to start
     * @return the extracted word as an int
     */
    private static int getWord(BigInteger number, int position) {
        return number.shiftRight(position).intValue() & 0xFFFF;
    }

    /**
     * Writes the provided value into the target BigInteger at the specified position and bit-length.
     *
     * @param target   the original BigInteger
     * @param position the bit position to write to
     * @param value    the value to write
     * @param bits     the number of bits for the value
     * @return the updated BigInteger
     */
    private static BigInteger writeToLocation(BigInteger target, int position, int value, int bits) {
        BigInteger mask = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE).shiftLeft(position).not();
        BigInteger shiftedValue = BigInteger.valueOf(value).shiftLeft(position);
        return target.and(mask).or(shiftedValue);
    }

    /**
     * Transforms 10-bit words in the user data to 11-bit words using a mapping table.
     *
     * @param userdata the user data as a BigInteger
     * @param m        the length parameter (830 for long, 210 for short)
     * @return the transformed data as a BigInteger
     */
    public static BigInteger transform10to11(BigInteger userdata, int m) {
        
        BigInteger transformData = BigInteger.ZERO;
        final int offset = OFFSET_SHAPED_DATA;
        int iterations = m / 10;
        for (int i = iterations - 1; i >= 0; i--) {
            int p = getWord(userdata, i * 10) & 0x03FF;
            int q = WORDS_11[p];
            transformData = writeToLocation(transformData, i * 11 + offset, q, 11);
        }
        return transformData;
    }

    /**
     * Sets the next SB and ESB values for the telegram until valid 11-bit words are produced.
     *
     * @param telegram the telegram to update
     * @return the new SB value as an int
     */
    public static int setNextSbEsb(Telegram telegram) {
        
        BigInteger temp = BigInteger.ZERO;
        BigInteger first11MSB = getIntFromBigIntAtIndex(temp, 14, 11);
        BigInteger second11MSB = getIntFromBigIntAtIndex(temp, 3, 11);
        int first11MSBVal, second11MSBVal;
        while (true) {
            first11MSBVal = WORDS_11_REVERSE[first11MSB.intValue()];
            second11MSBVal = WORDS_11_REVERSE[second11MSB.intValue()];
            if (first11MSBVal != -1 && second11MSBVal != -1) {
                break;
            }
            if (SB.intValue() < 4096) {
                if (ESB.intValue() < 1024) {
                    ESB = ESB.add(BigInteger.ONE);
                } else {
                    SB = SB.add(BigInteger.ONE);
                    ESB = BigInteger.ZERO;
                }
            }
            temp = STATIC_VALUE.or(SB).shiftLeft(10).or(ESB);
            LOG.trace("Test - SB=" +SB.toString(16)+" - ESB=" +ESB.toString(16));
            first11MSB = getIntFromBigIntAtIndex(temp, 14, 11);
            second11MSB = getIntFromBigIntAtIndex(temp, 3, 11);
            telegram.transformData = BigInteger.ZERO;
            try {
                telegram.transformData = writeToLocation(telegram.transformData, 85, temp.intValueExact(), 25);
            } catch (ArithmeticException e) {
                
            }
        }
        return SB.intValue();
    }

    /**
     * Computes the check bits for the telegram using GF(2) division.
     *
     * @param telegram   the telegram's transformation data as a BigInteger
     * @param isLongFormat true if the telegram is long, false if short
     * @return the computed check bits as a BigInteger, or null if there is a mismatch
     */
    public static BigInteger computeCheckBits(BigInteger telegram, Boolean isLongFormat) {
        
        BigInteger f, g, fg;
        if (isLongFormat) {
            f = FLONG;
            g = GLONG;
            fg = FGLONG;
        } else {
            f = FSHORT;
            g = GSHORT;
            fg = FGSHORT;
        }
        BigInteger[] results = gf2Division2(telegram, fg);
        BigInteger remainder = results[1];
        if (remainder == null) {
            return null;
        }
        BigInteger checkbits = g.xor(remainder);
        if (!g.xor(checkbits).equals(remainder)) {
            LOG.debug("Nevyšly check bits");
            LOG.trace("Check bits - " + checkbits.toString(16));
            return null;
        }
        return checkbits;
    }

    /**
     * Extracts a sub-sequence of bits from a BigInteger, wrapping the bit index if necessary.
     *
     * @param bigInt   the source BigInteger
     * @param bitIndex the starting bit index
     * @param length   the number of bits to extract
     * @return the extracted bits as a BigInteger
     */
    public static BigInteger getIntFromBigIntAtIndexWrap(BigInteger bigInt, int bitIndex, int length) {
        
        int size = bigInt.bitLength();
        if (size == 0) return BigInteger.ZERO;
        bitIndex = Math.floorMod(bitIndex, size);
        BigInteger mask = BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE);
        BigInteger result = bigInt.shiftRight(bitIndex).and(mask);
        if (bitIndex + length > size) {
            int overflowLength = (bitIndex + length) - size;
            BigInteger overflowMask = BigInteger.ONE.shiftLeft(overflowLength).subtract(BigInteger.ONE);
            BigInteger overflowPart = bigInt.and(overflowMask);
            result = overflowPart.shiftLeft(length - overflowLength).or(result.shiftRight(size - bitIndex));
        }
        return result;
    }

    /**
     * Extracts a sub-sequence of bits from a BigInteger.
     *
     * @param bigInt   the source BigInteger
     * @param bitIndex the starting bit index
     * @param length   the number of bits to extract
     * @return the extracted bits as a BigInteger
     */
    public static BigInteger getIntFromBigIntAtIndex(BigInteger bigInt, int bitIndex, int length) {
        if (length == 11) {
            long shifted = bigInt.shiftRight(bitIndex).longValue();
            long mask = 0x7FF;
            return BigInteger.valueOf(shifted & mask);
        }
        BigInteger mask = MASKS[length];
        return bigInt.shiftRight(bitIndex).and(mask);
    }

    /**
     * Multiplies two BigIntegers in GF(2).
     *
     * @param p the first operand
     * @param q the second operand
     * @return the product in GF(2)
     */
    public static BigInteger GF2_multiply(BigInteger p, BigInteger q) {
        
        BigInteger result = BigInteger.ZERO;
        for (int i = q.bitLength() - 1; i >= 0; i--) {
            if (q.testBit(i)) {
                result = result.xor(p.shiftLeft(i));
            }
        }
        return result;
    }

    /**
     * Performs GF(2) division of p by q.
     *
     * @param p the dividend
     * @param q the divisor
     * @return an array with quotient (null) and remainder as BigInteger
     */
    public static BigInteger[] gf2Division2(BigInteger p, BigInteger q) {
        
        byte[] remainderBytes = p.toByteArray();
        byte[] qBytes = q.toByteArray();
        int remainderOrder = (remainderBytes.length - 1) * 8 + bitLength(remainderBytes[0]) - 1;
        int qOrder = (qBytes.length - 1) * 8 + bitLength(qBytes[0]) - 1;
        while (remainderOrder >= qOrder) {
            int shift = remainderOrder - qOrder;
            int byteShift = shift / 8;
            int bitShift = shift % 8;
            for (int i = 0; i < qBytes.length; i++) {
                int index = remainderBytes.length - qBytes.length + i - byteShift;
                if (index >= 0) {
                    remainderBytes[index] ^= (qBytes[i] & 0xFF) << bitShift;
                    if (bitShift > 0 && index > 0) {
                        remainderBytes[index - 1] ^= (qBytes[i] & 0xFF) >>> (8 - bitShift);
                    }
                }
            }
            while (remainderOrder >= 0 && (remainderBytes[remainderBytes.length - 1 - remainderOrder / 8]
                    & (1 << (remainderOrder % 8))) == 0) {
                remainderOrder--;
            }
        }
        BigInteger remainder = new BigInteger(1, remainderBytes);
        return new BigInteger[]{null, remainder};
    }

    /**
     * Computes the bit length of a byte value.
     *
     * @param value the byte value
     * @return the bit length
     */
    private static int bitLength(byte value) {
        int length = 0;
        while (value != 0) {
            value >>>= 1;
            length++;
        }
        return length;
    }

    /**
     * Checks the aperiodicity condition for the telegram.
     *
     * @param telegram the telegram to check
     * @return true if the condition is satisfied, false otherwise
     */
    public static boolean check_aperiodicity_condition(Telegram telegram) {
        
        if (!telegram.isLongTelegram) {
            return true;
        }
        for (int i = 0; i < telegram.getSize(); i += 11) {
            BigInteger wordHigh = getIntFromBigIntAtIndexWrap(telegram.transformData, i, 22);
            for (int k = -3; k <= 3; k++) {
                BigInteger wordLow = getIntFromBigIntAtIndexWrap(telegram.transformData, i - 341 - 5, 22);
                int hammingDistance = calc_hamming_distance(wordHigh, wordLow, 22);
                if ((k == 0 && hammingDistance < 3) || (k != 0 && hammingDistance < 2)) {
                    
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calculates the Hamming distance between two BigIntegers up to n bits.
     *
     * @param word1 the first BigInteger
     * @param word2 the second BigInteger
     * @param n     the number of bits to compare
     * @return the Hamming distance (number of differing bits)
     */
    public static int calc_hamming_distance(BigInteger word1, BigInteger word2, int n) {
        BigInteger differingBits = word1.xor(word2);
        return Integer.bitCount(differingBits.intValue());
    }

    /**
     * Pads the start of a string up to the specified length with the given character.
     *
     * @param originalString the original string
     * @param length         the desired total length
     * @param padChar        the character to pad with
     * @return the padded string
     */
    public static String padStart(String originalString, int length, char padChar) {
        if (originalString.length() >= length) {
            return originalString;
        }
        StringBuilder sb = new StringBuilder(length);
        while (sb.length() < length - originalString.length()) {
            sb.append(padChar);
        }
        sb.append(originalString);
        return sb.toString();
    }

    /**
     * Pads the end of a string up to the specified length with the given character.
     *
     * @param originalString the original string
     * @param length         the desired total length
     * @param padChar        the character to pad with
     * @return the padded string
     */
    public static String padEnd(String originalString, int length, char padChar) {
        if (originalString.length() >= length) {
            return originalString;
        }
        StringBuilder sb = new StringBuilder(length);
        sb.append(originalString);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }



    /**
     * Inner class representing a Telegram, holding data and providing accessor methods.
     */
    static class Telegram {
        BigInteger userdata;
        BigInteger transformData;
        boolean isLongTelegram;
        int size = -1;

        public Telegram(String userdataStr) {

            this.userdata = new BigInteger(userdataStr, 2);
            this.transformData = BigInteger.ZERO;
            this.isLongTelegram = (this.userdata.bitLength() > 500);


            // Shift right by 2 bits as per your original logic
            this.userdata = this.userdata.shiftRight(2);
            this.size = isLongTelegram ? 1023 : 341;
        }

        public int getSize() {
            return size;
        }

        BigInteger getSB() {
            return TelegramEncoder.getIntFromBigIntAtIndex(transformData, 95, 12);
        }

        void setSB(int sb) {
            transformData = TelegramEncoder.writeToLocation(transformData, 95, sb, 12);
        }

        BigInteger getESB() {
            return TelegramEncoder.getIntFromBigIntAtIndex(transformData, 85, 10);
        }

        void setESB(int esb) {
            transformData = TelegramEncoder.writeToLocation(transformData, 85, esb, 10);
        }
    }

}
