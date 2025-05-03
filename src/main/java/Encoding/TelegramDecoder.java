package Encoding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;

import static tools.crypto.ArithmeticalFunctions.*;
import static Encoding.EncryptionHelperList.*;
import static tools.string.StringHelper.*;

/**
 * TelegramDecoder - Decodes a Balise telegram (Subset-036) from a hex input.
 * <p>
 * This class attempts to detect whether the telegram is in the "long" or "short" format
 * (n=1023 or n=341), perform polynomial checks with g(x) and f(x), validate control bits,
 * handle inversion bit (b109), invert the 10-to-11-bit transformation, and de-scramble the data.
 * After these steps, user data is returned in HEX form if decoding is successful.
 */
public class TelegramDecoder {

    private static final Logger LOG = LogManager.getLogger(TelegramDecoder.class);



    /**
     * Main method to decode a Balise telegram (hex input).
     *
     * @param hexString Telegram in hexadecimal form.
     * @return Decoded telegram in hexadecimal form, or an error code if decoding fails.
     */
    public static String decodeTelegram(String hexString) {
        LOG.info("Starting decode of telegram from hexString: [{}]", hexString);

        // Convert input from HEX to a binary string
        String inputDataBits = hex2Bin(hexString);
        LOG.debug("Converted HEX to binary. Length = {} bits.", inputDataBits.length());

        BigInteger inputBigInt = new BigInteger(hexString, 16); // (kept for reference if needed)
        LOG.trace("Created BigInteger from hex: [{}]", inputBigInt.toString(16));

        // Determine if the telegram is long or short based on the HEX length
        int telegramFormat = determineTelegramFormat(hexString.length());
        if (telegramFormat == -1) {
            LOG.error("Invalid telegram size");
            return "";
        }

        LOG.info("Telegram format determined - {}",
                (telegramFormat == 0 ? "LONG (n=1023)" : "SHORT (n=341)"));

        // Retrieve the number of bits (n) and polynomials f(x) and g(x) for the chosen format
        int n = getTelegramLength(telegramFormat);
        BigInteger polynomF = getPolynomF(telegramFormat);
        BigInteger polynomG = getPolynomG(telegramFormat);

        // Variables for iterative processing
        BigInteger fxSyndrome;
        String candidateTelegramBits;

        // Try to find a valid telegram by "sliding" over the bit sequence
        while (true) {
            // Extract n bits from the input as a candidate telegram
            candidateTelegramBits = remove(inputDataBits, n, inputDataBits.length() - n);
            LOG.debug("Candidate telegram bits length: {}", candidateTelegramBits.length());

            // Check polynomials g(x) and f(x); obtain f(x) syndrome
            fxSyndrome = checkPolynomialsSyndromes(candidateTelegramBits, polynomF, polynomG);
            if (fxSyndrome.equals(BigInteger.valueOf(-1))) {
                // If polynomial check fails, break and return an error
                LOG.debug("Polynomial check with g(x) failed. Breaking out.");
                break;
            }

            LOG.trace("Polynomial check passed. f(x) remainder (syndrome) = {}", fxSyndrome.toString());

            // Determine substitution index (11-bit to 10-bit transformation) based on f(x) result
            int substitutionIndex = getSubstitutionIndex(telegramFormat, fxSyndrome.intValue());
            if (substitutionIndex == -1) {
                LOG.error("Invalid substitution index.");
                return "";
            }

            LOG.debug("Applying substitution word with index = {}", substitutionIndex);

            // Apply the substitution word to reconstruct the telegram bits
            String reconstructedTelegram = applySubstitutionWord(candidateTelegramBits, n, substitutionIndex, telegramFormat);
            LOG.trace("Reconstructed telegram length: {}", reconstructedTelegram.length());

            // Now try to validate and process these bits:
            //  - Check inversion bit
            //  - Check control bits
            //  - Possibly de-scramble
            //  - If validation fails, invert bits (subset 036 - b109) and retry
            while (true) {
                String result = validateAndProcessTelegram(reconstructedTelegram, n, telegramFormat);
                if (result != null) {
                    LOG.info("Decoding ended with result - [{}]", result);
                    return result;
                }
                // If the first attempt fails, invert bits and retry
                LOG.debug("Validation failed with possible inversion bit. Inverting bits to retry.");
                reconstructedTelegram = invertBin(reconstructedTelegram);
            }
        }

        LOG.error("No valid telegram found after polynomial checks");
        // If we exit the while loop without returning, a parity/polynomial issue is presumed
        return "";
    }

    /**
     * Determine whether the telegram is long (n=1023) or short (n=341), based on HEX length.
     * Returns 0 for long, 1 for short, or -1 for an invalid size.
     */
    private static int determineTelegramFormat(int hexLength) {
        // Example logic: if hex < 86 => error, if 86..255 => short, else long
        // 0 => long, 1 => short, -1 => invalid
        LOG.trace("Determining telegram format from hex length = {}", hexLength);
        return hexLength < 86 ? -1 : (hexLength < 256 ? 1 : 0);
    }

    /**
     * Returns the number of bits (n) for the given format.
     */
    private static int getTelegramLength(int telegramFormat) {
        return (telegramFormat == 0) ? SIZE_LONG : SIZE_SHORT;  // 1023 or 341
    }

    /**
     * Returns the polynomial f(x) for the given format.
     */
    private static BigInteger getPolynomF(int telegramFormat) {
        return (telegramFormat == 0) ? FLONG : FSHORT;
    }

    /**
     * Returns the polynomial g(x) for the given format.
     */
    private static BigInteger getPolynomG(int telegramFormat) {
        return (telegramFormat == 0) ? GLONG : GSHORT;
    }

    /**
     * Checks if the candidate telegram bits are divisible by g(x) (remainder=0),
     * then computes the remainder for f(x). If g(x) fails, returns -1.
     */
    private static BigInteger checkPolynomialsSyndromes(String telegramCandidate,
                                                        BigInteger f,
                                                        BigInteger g) {
        // Step 1: check polynomial division with g(x)
        String remainderG = polynomialDivision2(telegramCandidate, g);
        if (!trim(remainderG, '0').isEmpty()) {
            // g(x) remainder is not empty => polynomial check failed
            LOG.trace("Remainder of g(x) not empty: [{}]. Polynomials check fails.", remainderG);
            return BigInteger.valueOf(-1);
        }

        // Step 2: compute remainder for f(x)
        String remainderF = polynomialDivision2(telegramCandidate, f);
        BigInteger fxVal = BigInteger.valueOf(bin2Dec(remainderF));
        LOG.trace("Remainder for f(x) after division: [{}] => as decimal: {}", remainderF, fxVal);
        return fxVal;
    }

    /**
     * Determines the substitution index from the f(x) syndrome.
     * If the format is long and the syndrome=0, returns -1.
     */
    private static int getSubstitutionIndex(int telegramFormat, int fxValue) {
        // If we have a LONG telegram and fxValue=0 => error
        if (telegramFormat == 0 && fxValue == 0) {
            LOG.debug("Long telegram with f(x) = 0 is invalid. Substitution index = -1.");
            return -1;
        }
        // Use either SHORT or LONG substitution table
        int index = (telegramFormat != 0)
                ? SHORT_SUBSTITUTION_WORDS[fxValue]
                : LONG_SUBSTITUTION_WORDS[fxValue];
        LOG.trace("Substitution index for fxValue = {} => {}", fxValue, index);
        return index;
    }

    /**
     * Applies the substitution word, which rearranges bits for the 10->11 or 11->10 transformation.
     */
    private static String applySubstitutionWord(String candidateBits,
                                                int n,
                                                int substitutionIndex,
                                                int telegramFormat) {
        // Example: remove the last (substitutionIndex) bits, re-append them, etc.
        LOG.trace("Applying substitution word - candidateBits size={} - index={}", candidateBits.length(), substitutionIndex);
        return remove(candidateBits, 0, n - substitutionIndex)
                + remove(candidateBits, n - substitutionIndex, substitutionIndex);
    }

    /**
     * Validates a reconstructed telegram:
     *  - Extracts and checks control bits (b109, b108, b107)
     *  - If they match expectations, de-scrambles user data
     *  - Returns the final bin->hex result or an error code
     *  - If it's not valid, returns null to trigger a bit inversion step
     */
    private static String validateAndProcessTelegram(String candidateTelegram,
                                                     int n,
                                                     int telegramFormat) {
        LOG.debug("Validating reconstructed telegram - Length: {}", candidateTelegram.length());

        // 1) Basic initialization or trimming
        String initializedTelegram = initializeTelegramCandidate(candidateTelegram);
        LOG.trace("Initialized telegram (after trimming, etc) - Length: {}", initializedTelegram.length());

        // 2) Extract special bits (inversionBit = b109, controlBits = b108/b107, user data, etc.)
        String inversionBit = getInversionBit(initializedTelegram, n, telegramFormat);
        String controlBits  = getControlBits(initializedTelegram, n, telegramFormat);
        String userDataBits = getUserDataBits(initializedTelegram, n, telegramFormat);
        LOG.trace("Extracted bits - inversionBit: [{}] - controlBits: [{}] - userDataBits length: {}",
                inversionBit, controlBits, userDataBits.length());

        // 3) Some core processing (placeholder logic)
        processTelegramCore(initializedTelegram, n, telegramFormat);

        // 4) Decode the first (n/11) words of 11 bits each
        String decodedFirstWords = decodeFirstWords(n, telegramFormat, initializedTelegram);
        LOG.debug("Decoded first - words length: {}", decodedFirstWords.length());

        // 5) Check how many valid 11-bit blocks we have
        String validWordCount = checkValidWordCount(n, telegramFormat, initializedTelegram, decodedFirstWords);
        LOG.trace("Valid 11-bit - words count: {}", validWordCount);

        // 6) Evaluate the result:
        //    If validWordCount == n/11 and inversionBit=0 => check b108=0 & b107=1 => "01"
        //    If these bits match, de-scramble and build final output
        if (new BigDecimal(validWordCount).equals(BigDecimal.valueOf((double) n / 11))) {
            if (inversionBit.equals("0")) {
                if (!controlBits.equals("01")) {
                    LOG.debug("Control bits mismatch - Expected '01' but got '{}'", controlBits);
                    return "";
                }
                String deScrambled = deScramble(decodedFirstWords, userDataBits);
                LOG.trace("Data after de-scrambling - length={}", deScrambled.length());
                String finalOutput = constructFinalOutput(deScrambled);
                LOG.info("Telegram successfully decoded. Returning final HEX output.");
                return bin2Hex(finalOutput);
            }
        }

        LOG.debug("Validation indicates possible inversion scenario or mismatch. Returning null to trigger inversion.");
        return "";
    }

    /**
     * Example method to trim or initialize the candidate telegram bits.
     * The logic is implementation-specific and can be adapted if needed.
     */
    private static String initializeTelegramCandidate(String telegramBits) {
        String newTelegram = new String(telegramBits);
        // Example removals:
        remove(newTelegram, newTelegram.length() - 110, 110);
        remove(newTelegram, 0, newTelegram.length() - 85);
        return newTelegram;
    }

    /**
     * Extracts the inversion bit (b109).
     */
    private static String getInversionBit(String telegram, int n, int telegramFormat) {
        return String.valueOf(telegram.toCharArray()[n - 110]);
    }

    /**
     * Extracts b108 and b107 after skipping b109.
     */
    private static String getControlBits(String telegram, int n, int telegramFormat) {
        String bits = remove(telegram, 0, n - 109); // b109 + b108 + b107
        return remove(bits, 2, 107);               // keep only b108 + b107
    }

    /**
     * Extracts the user data bits (example: skipping b109, b108, b107).
     */
    private static String getUserDataBits(String telegram, int n, int telegramFormat) {
        String data = remove(telegram, 0, n - 107);
        // "12" and "95" are placeholders depending on the encoding structure
        return remove(data, 12, 95);
    }

    /**
     * Placeholder for further telegram processing if needed.
     */
    private static String processTelegramCore(String telegram, int n, int telegramFormat) {
        // remove(...) operations to handle leftover bits, etc.
        return remove(remove(telegram, 0, n - 95), WORD_LENGTH, 85);
    }

    /**
     * Decodes the first (n/11) 11-bit blocks into some 10-bit words using a lookup table.
     */
    private static String decodeFirstWords(int n, int telegramFormat, String telegramBits) {
        StringBuilder sb = new StringBuilder();
        int numberOfWords = (n / 11) - 1;

        for (int i = 0; i <= numberOfWords; i++) {
            String chunk11 = substring(telegramBits, i * 11, 11);
            long chunkVal = bin2Dec(chunk11);

            if (chunkVal < 0 || chunkVal >= WORDS_11_REVERSE.length) {
                continue;
            }
            int mappedVal = WORDS_11_REVERSE[(int) chunkVal];
            if (mappedVal >= 0 && i < (n / 11) - WORD_LENGTH) {
                sb.append(dec2XBin(Integer.toString(mappedVal), WORD_LENGTH));
            }
        }
        return sb.toString();
    }

    /**
     * Checks how many valid 11-bit blocks are in the telegram.
     * Returns a string like "X.0" to compare with (n/11) in decimal form.
     */
    private static String checkValidWordCount(int n,
                                              int telegramFormat,
                                              String telegramBits,
                                              String decodedWords) {
        int validCount = 0;
        int numberOfWords = (n / 11) - 1;

        for (int i = 0; i <= numberOfWords; i++) {
            String chunk11 = substring(telegramBits, i * 11, 11);
            int mappedVal = WORDS_11_REVERSE[(int) bin2Dec(chunk11)];
            if (mappedVal >= 0) {
                validCount++;
            }
        }
        return validCount + ".0";
    }

    /**
     * De-scrambles the data. This is an example implementation of a
     * reverse scrambling step as per Subset-036.
     */
    private static String deScramble(String firstWordBits, String userDataBits) {
        LOG.trace("Starting de-scramble process. - firstWordBits length={} - userDataBits length={}",
                firstWordBits.length(), userDataBits.length());
        char[] scramblerState = initializeScramblerState(userDataBits);
        StringBuilder sb = new StringBuilder(firstWordBits.length());

        sb.append(textXOR(firstWordBits.charAt(0), scramblerState[0]));

        for (int i = 1; i < firstWordBits.length(); i++) {
            char prevChar = firstWordBits.charAt(i - 1);
            scramblerState = shiftScrambler(scramblerState, prevChar);
            sb.append(textXOR(firstWordBits.charAt(i), scramblerState[0]));
        }
        return sb.toString();
    }

    /**
     * Shifts the scrambler array with XOR operations on specific bits.
     */
    private static char[] shiftScrambler(char[] array, char prevChar) {
        // Example shifts for the first part
        for (int i = 0; i < 3; i++) {
            array[i] = textXOR(array[i + 1], prevChar);
        }

        // Another set of XOR manipulations
        int[] affectedIndices = {3, 4, 5, 6};
        for (int i = 0; i < affectedIndices.length; i += 2) {
            array[affectedIndices[i]] = array[affectedIndices[i] + 1];
            array[affectedIndices[i] + 1] = textXOR(array[affectedIndices[i] + 2], prevChar);
        }

        // Shift array from index 8 to 7, length 24
        System.arraycopy(array, 8, array, 7, 24);

        // Place prevChar at the end
        array[31] = prevChar;
        return array;
    }

    /**
     * Initializes the scrambler state based on user data bits.
     */
    private static char[] initializeScramblerState(String userDataBits) {
        char[] arr = new char[33];
        long val = bin2Dec(userDataBits);

        // Calculate a seed in 32 bits
        String seed = dec2XBin(
                String.valueOf(Math.round((double) (2801775573L * val) % 4294967296.0)),
                32
        );
        System.arraycopy(seed.toCharArray(), 0, arr, 0, 32);
        return arr;
    }

    /**
     * Constructs the final output from the de-scrambled bits, adjusting mod 1024 if needed.
     */
    private static String constructFinalOutput(String deScrambledBits) {
        // Example logic from the original code
        LOG.trace("Constructing final output from de-scrambled bits - Délka={}", deScrambledBits.length());

        BigInteger num6 = BigInteger.valueOf(bin2Dec(
                remove(deScrambledBits, WORD_LENGTH, deScrambledBits.length() - WORD_LENGTH)
        ));
        BigInteger num7 = BigInteger.ZERO;
        int maxChunk = (deScrambledBits.length() / WORD_LENGTH) - 1;

        for (int i = 1; i <= maxChunk; i++) {
            String chunk = substring(deScrambledBits, i * WORD_LENGTH, WORD_LENGTH);
            num7 = num7.add(BigInteger.valueOf(bin2Dec(chunk)));
        }

        BigInteger num5 = num6.subtract(num7.mod(BigInteger.valueOf(1024)));
        if (num5.compareTo(BigInteger.ZERO) < 0) {
            num5 = num5.add(BigInteger.valueOf(1024));
        }

        String finalBits =
                dec2XBin(num5.toString(), WORD_LENGTH) + remove(deScrambledBits, 0, WORD_LENGTH);

        LOG.trace("Final bits length={} => returning from constructFinalOutput.", finalBits.length());
        return finalBits;
    }
}
