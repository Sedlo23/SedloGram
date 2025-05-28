package Encoding;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TelegramDecoderTest {

    // Globální timeout pro testy
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    // Logger pro výkonnostní metriky
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
    public void decode_long_functional() {
        for (String[] temp : TestValues.telegrams_long) {
            String expected = temp[0].replace(" ", "");
            String input = temp[1].replace(" ", "");

            String result = TelegramDecoder.decodeTelegram(input);

            assertEquals("Decoding failed for input: " + input, expected, result);
        }
    }

    @Test
    public void decode_short_functional() {
        for (String[] temp : TestValues.telegrams_short) {
            String expected = temp[0].replace(" ", "");
            String input = temp[1].replace(" ", "");

            String result = TelegramDecoder.decodeTelegram(input);

            assertEquals("Decoding failed for input: " + input, expected, result);
        }
    }

    @Test
    public void performance_decode_long() {
        // Test výkonu pro dlouhé telegramy
        String[] testData = TestValues.telegrams_long[0];
        String input = testData[1].replace(" ", "");

        // Warm-up
        for (int i = 0; i < 10; i++) {
            TelegramDecoder.decodeTelegram(input);
        }

        // Měření výkonu
        long startTime = System.nanoTime();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            TelegramDecoder.decodeTelegram(input);
        }

        long endTime = System.nanoTime();
        double avgTime = (endTime - startTime) / (double) iterations / 1_000_000.0;

        System.out.println(String.format("Average decode time for long telegram: %.3f ms", avgTime));

        // Assert že průměrný čas je pod 50ms
        assertTrue("Decoding is too slow: " + avgTime + " ms", avgTime < 50);
    }

    @Test
    public void performance_decode_short() {
        // Test výkonu pro krátké telegramy
        String[] testData = TestValues.telegrams_short[0];
        String input = testData[1].replace(" ", "");

        // Warm-up
        for (int i = 0; i < 10; i++) {
            TelegramDecoder.decodeTelegram(input);
        }

        // Měření výkonu
        long startTime = System.nanoTime();
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            TelegramDecoder.decodeTelegram(input);
        }

        long endTime = System.nanoTime();
        double avgTime = (endTime - startTime) / (double) iterations / 1_000_000.0;

        System.out.println(String.format("Average decode time for short telegram: %.3f ms", avgTime));

        // Assert že průměrný čas je pod 20ms
        assertTrue("Decoding is too slow: " + avgTime + " ms", avgTime < 20);
    }

    @Test
    public void test_invalid_input_empty() {
        String result = TelegramDecoder.decodeTelegram("");
        assertEquals("Empty input should return empty string", "", result);
    }

    @Test
    public void test_invalid_input_too_short() {
        // Telegram kratší než minimální délka
        String shortInput = "ABCD";
        String result = TelegramDecoder.decodeTelegram(shortInput);
        assertEquals("Too short input should return empty string", "", result);
    }

    @Test
    public void test_invalid_input_non_hex() {
        // Input obsahující ne-hex znaky
        String invalidHex = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG";
        try {
            String result = TelegramDecoder.decodeTelegram(invalidHex);
            // Pokud nepřijde výjimka, měl by vrátit prázdný string
            assertEquals("", result);
        } catch (Exception e) {
            // Očekáváme nějakou výjimku při zpracování ne-hex vstupu
            assertTrue(e instanceof IllegalArgumentException || e instanceof NumberFormatException);
        }
    }

    @Test
    public void test_boundary_case_minimum_length() {
        // Test s minimální délkou pro short telegram (86 znaků)
        String minLengthHex = "A".repeat(86);
        String result = TelegramDecoder.decodeTelegram(minLengthHex);
        // Nemělo by to spadnout
        assertNotNull(result);
    }

    @Test
    public void test_boundary_case_between_short_and_long() {
        // Test na hranici mezi short a long (255/256 znaků)
        String boundary255 = "F".repeat(255);
        String boundary256 = "F".repeat(256);

        String result255 = TelegramDecoder.decodeTelegram(boundary255);
        String result256 = TelegramDecoder.decodeTelegram(boundary256);

        assertNotNull(result255);
        assertNotNull(result256);
    }

    @Test
    public void test_concurrent_decoding() throws InterruptedException {
        // Test thread safety
        final int threadCount = 10;
        final int iterationsPerThread = 10;
        final String[] testData = TestValues.telegrams_short[0];
        final String input = testData[1].replace(" ", "");
        final String expected = testData[0].replace(" ", "");

        Thread[] threads = new Thread[threadCount];
        final boolean[] success = new boolean[threadCount];
        Arrays.fill(success, true);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    String result = TelegramDecoder.decodeTelegram(input);
                    if (!expected.equals(result)) {
                        success[threadId] = false;
                        break;
                    }
                }
            });
        }

        // Spustit všechny vlákna
        for (Thread t : threads) {
            t.start();
        }

        // Počkat na dokončení
        for (Thread t : threads) {
            t.join();
        }

        // Ověřit výsledky
        for (int i = 0; i < threadCount; i++) {
            assertTrue("Thread " + i + " failed", success[i]);
        }
    }

    @Test
    public void test_memory_efficiency() {
        // Test paměťové efektivity při opakovaném dekódování
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();

        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        // Dekódovat 1000x
        String[] testData = TestValues.telegrams_short[0];
        String input = testData[1].replace(" ", "");

        for (int i = 0; i < 1000; i++) {
            TelegramDecoder.decodeTelegram(input);
        }

        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = (memoryAfter - memoryBefore) / (1024 * 1024); // v MB

        System.out.println("Memory used for 1000 decodings: " + memoryUsed + " MB");

        // Assert že paměťová spotřeba není nadměrná (< 50 MB)
        assertTrue("Excessive memory usage: " + memoryUsed + " MB", memoryUsed < 50);
    }

    @Test
    public void test_all_test_vectors_consistency() {
        // Test že encode -> decode vrátí původní data
        for (String[] temp : TestValues.telegrams_short) {
            String original = temp[0].replace(" ", "");
            String encoded = temp[1].replace(" ", "");

            // Decode
            String decoded = TelegramDecoder.decodeTelegram(encoded);
            assertEquals(original, decoded);

            // Re-encode (pokud máme encoder)
            String reEncoded = TelegramEncoder.encode(
                    tools.crypto.ArithmeticalFunctions.hex2Bin(decoded)
            );

            if (reEncoded != null) {
                // Re-decode
                String reDecoded = TelegramDecoder.decodeTelegram(reEncoded);
                assertEquals("Re-encoding/decoding failed", original, reDecoded);
            }
        }
    }
}