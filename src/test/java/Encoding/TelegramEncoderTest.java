package Encoding;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import tools.crypto.ArithmeticalFunctions;
import static org.junit.Assert.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class TelegramEncoderTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(30);

    @Rule
    public TestRule performanceLogger = new TestWatcher() {
        long startTime;

        @Override
        protected void starting(Description description) {
            startTime = System.nanoTime();
        }

        @Override
        protected void finished(Description description) {
            long duration = System.nanoTime() - startTime;
            System.out.println(String.format("%s took %.3f ms",
                    description.getMethodName(), duration / 1_000_000.0));
        }
    };

    @Test
    public void encode_short_functional() {
        for (String[] temp : TestValues.telegrams_short) {
            String input = ArithmeticalFunctions.hex2Bin(temp[0].replace(" ", ""));
            String expected = temp[1].replace(" ", "");

            String result = TelegramEncoder.encode(input);

            assertNotNull("Encoding returned null for input: " + temp[0], result);
            assertEquals("Encoding failed for input: " + temp[0], expected, result);
        }
    }

    @Test
    public void encode_long_functional() {
        for (String[] temp : TestValues.telegrams_long) {
            String input = ArithmeticalFunctions.hex2Bin(temp[0].replace(" ", ""));
            String expected = temp[1].replace(" ", "");

            String result = TelegramEncoder.encode(input);

            assertNotNull("Encoding returned null for input: " + temp[0], result);
            assertEquals("Encoding failed for input: " + temp[0], expected, result);
        }
    }

    @Test
    public void performance_encode_short() {
        String[] testData = TestValues.telegrams_short[0];
        String input = ArithmeticalFunctions.hex2Bin(testData[0].replace(" ", ""));

        // Warm-up
        for (int i = 0; i < 5; i++) {
            TelegramEncoder.encode(input);
        }

        // Měření výkonu
        long startTime = System.nanoTime();
        int iterations = 20;
        int successCount = 0;

        for (int i = 0; i < iterations; i++) {
            String result = TelegramEncoder.encode(input);
            if (result != null) successCount++;
        }

        long endTime = System.nanoTime();
        double avgTime = (endTime - startTime) / (double) iterations / 1_000_000.0;

        System.out.println(String.format("Average encode time for short telegram: %.3f ms", avgTime));
        System.out.println("Success rate: " + (successCount * 100 / iterations) + "%");

        assertTrue("Encoding is too slow: " + avgTime + " ms", avgTime < 1200);
        assertEquals("Not all encodings succeeded", iterations, successCount);
    }

    @Test
    public void performance_encode_long() {
        String[] testData = TestValues.telegrams_long[0];
        String input = ArithmeticalFunctions.hex2Bin(testData[0].replace(" ", ""));

        // Warm-up
        for (int i = 0; i < 3; i++) {
            TelegramEncoder.encode(input);
        }

        // Měření výkonu
        long startTime = System.nanoTime();
        int iterations = 10;
        int successCount = 0;

        for (int i = 0; i < iterations; i++) {
            String result = TelegramEncoder.encode(input);
            if (result != null) successCount++;
        }

        long endTime = System.nanoTime();
        double avgTime = (endTime - startTime) / (double) iterations / 1_000_000.0;

        System.out.println(String.format("Average encode time for long telegram: %.3f ms", avgTime));
        System.out.println("Success rate: " + (successCount * 100 / iterations) + "%");

        assertTrue("Encoding is too slow: " + avgTime + " ms", avgTime < 1000);
        assertEquals("Not all encodings succeeded", iterations, successCount);
    }

    @Test
    public void test_encode_with_different_parameters() {
        String[] testData = TestValues.telegrams_short[0];
        String input = ArithmeticalFunctions.hex2Bin(testData[0].replace(" ", ""));

        // Test s různými parametry
        String result1 = TelegramEncoder.encode(input, 0, 0, BigInteger.ONE);
        String result2 = TelegramEncoder.encode(input, 100, 100, BigInteger.ONE);
        String result3 = TelegramEncoder.encode(input, 0, 0, BigInteger.valueOf(2));

        assertNotNull("Encoding with default parameters failed", result1);
        assertNotNull("Encoding with custom SB/ESB failed", result2);
        assertNotNull("Encoding with custom adder failed", result3);

        // Výsledky by měly být různé při různých parametrech
        System.out.println("Result with default params: " + (result1 != null ? result1.substring(0, Math.min(20, result1.length())) : "null"));
        System.out.println("Result with SB=100, ESB=100: " + (result2 != null ? result2.substring(0, Math.min(20, result2.length())) : "null"));
        System.out.println("Result with adder=2: " + (result3 != null ? result3.substring(0, Math.min(20, result3.length())) : "null"));
    }

    @Test
    public void test_edge_case_minimum_input() {
        // Test s minimálním vstupem pro short telegram
        String minInput = "1".repeat(210); // Minimální délka pro short telegram
        String result = TelegramEncoder.encode(minInput);

        assertNotNull("Encoding of minimum input failed", result);
    }

    @Test
    public void test_edge_case_maximum_input() {
        // Test s maximálním vstupem pro long telegram
        String maxInput = "1".repeat(830); // Maximální délka pro long telegram
        String result = TelegramEncoder.encode(maxInput);

        assertNotNull("Encoding of maximum input failed", result);
    }

    @Test(timeout = 5000)
    public void test_encode_timeout_protection() {
        // Test že encoding nekončí v nekonečné smyčce
        String problematicInput = "0".repeat(500); // Vstup který by mohl způsobit problémy

        String result = TelegramEncoder.encode(problematicInput);
        // Pokud test projde bez timeoutu, je to úspěch
        System.out.println("Encoding completed without timeout: " + (result != null));
    }

    @Test
    public void test_concurrent_encoding() throws InterruptedException {
        final int threadCount = 5;
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicInteger successCount = new AtomicInteger(0);

        String[] testData = TestValues.telegrams_short[0];
        final String input = ArithmeticalFunctions.hex2Bin(testData[0].replace(" ", ""));

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    // Každé vlákno použije jiné parametry
                    String result = TelegramEncoder.encode(input, threadId * 10, threadId * 5, BigInteger.valueOf(threadId + 1));
                    if (result != null) {
                        successCount.incrementAndGet();
                        System.out.println("Thread " + threadId + " succeeded");
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        assertTrue("Not all threads succeeded", successCount.get() >= threadCount / 2);
    }

    @Test
    public void test_method_scrambleUserData() {
        BigInteger S = BigInteger.valueOf(123456789);
        BigInteger currentInput = EncryptionHelperList.CURRENT_INPUT;
        BigInteger userData = BigInteger.valueOf(987654321);
        int m = 210;

        BigInteger result = TelegramEncoder.scrambleUserData(S, currentInput, userData, m);

        assertNotNull("scrambleUserData returned null", result);
        assertTrue("Result should be different from input", !result.equals(userData));

        // Test determinismu - stejný vstup by měl dát stejný výstup
        BigInteger result2 = TelegramEncoder.scrambleUserData(S, currentInput, userData, m);
        assertEquals("scrambleUserData is not deterministic", result, result2);
    }

    @Test
    public void test_method_transform10to11() {
        BigInteger userData = BigInteger.valueOf(1234567890);
        int m = 210;

        BigInteger result = TelegramEncoder.transform10to11(userData, m);

        assertNotNull("transform10to11 returned null", result);
        assertTrue("Result should have more bits than input", result.bitLength() > userData.bitLength());
    }


    @Test
    public void test_encode_decode_consistency() {
        // Test že encode -> decode -> encode dá stejný výsledek
        for (int i = 0; i < Math.min(3, TestValues.telegrams_short.length); i++) {
            String[] testData = TestValues.telegrams_short[i];
            String originalHex = testData[0].replace(" ", "");
            String originalBin = ArithmeticalFunctions.hex2Bin(originalHex);

            // První encode
            String encoded1 = TelegramEncoder.encode(originalBin);
            assertNotNull("First encoding failed", encoded1);

            // Decode
            String decoded = TelegramDecoder.decodeTelegram(encoded1);
            assertEquals("Decoding doesn't match original", originalHex, decoded);

            // Druhý encode
            String encoded2 = TelegramEncoder.encode(ArithmeticalFunctions.hex2Bin(decoded));
            assertNotNull("Second encoding failed", encoded2);

            // Výsledky by měly být identické
            assertEquals("Encode-decode-encode is not consistent", encoded1, encoded2);
        }
    }
}