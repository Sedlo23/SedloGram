package tools.crypto;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

public class CalculatorMD4Test {

    @Test
    public void testEngineReset() {
        CalculatorMD4 md4 = new CalculatorMD4();
        md4.update("test data".getBytes(StandardCharsets.UTF_8));

        md4.reset();

        // After reset, processing empty string should give MD4 hash of empty string
        byte[] emptyHash = md4.digest();
        assertEquals("31d6cfe0d16ae931b73c59d7e0c089c0", toHexString(emptyHash));
    }

    @Test
    public void testClone() {
        CalculatorMD4 original = new CalculatorMD4();
        original.update("test".getBytes(StandardCharsets.UTF_8));

        CalculatorMD4 clone = (CalculatorMD4) original.clone();

        // Both should produce the same digest
        byte[] originalDigest = original.digest();
        byte[] cloneDigest = clone.digest();

        assertArrayEquals(originalDigest, cloneDigest);
    }

    @Test
    public void testSingleByteUpdate() {
        CalculatorMD4 md4 = new CalculatorMD4();
        byte[] input = "test".getBytes(StandardCharsets.UTF_8);

        // Update one byte at a time
        for (byte b : input) {
            md4.update(b);
        }

        byte[] digest = md4.digest();
        assertEquals("db346d691d7acc4dc2625db19f9e3f52", toHexString(digest));
    }

    @Test
    public void testBlockUpdate() {
        CalculatorMD4 md4 = new CalculatorMD4();
        byte[] input = "test".getBytes(StandardCharsets.UTF_8);

        md4.update(input, 0, input.length);

        byte[] digest = md4.digest();
        assertEquals("db346d691d7acc4dc2625db19f9e3f52", toHexString(digest));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testInvalidOffset() {
        CalculatorMD4 md4 = new CalculatorMD4();
        byte[] input = "test".getBytes(StandardCharsets.UTF_8);

        md4.update(input, -1, input.length);
    }



    @Test
    public void testKnownValues() {
        // Test vectors from RFC 1320 (The MD4 Message-Digest Algorithm)
        testHash("", "31d6cfe0d16ae931b73c59d7e0c089c0");
        testHash("a", "bde52cb31de33e46245e05fbdbd6fb24");
        testHash("abc", "a448017aaf21d8525fc10ae87aa6729d");
        testHash("message digest", "d9130a8164549fe818874806e1c7014b");
        testHash("abcdefghijklmnopqrstuvwxyz", "d79e1c308aa5bbcdeea8ed63df412da9");
        testHash("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
                "043f8582f241db351ce627e153e7f0e4");
        testHash("12345678901234567890123456789012345678901234567890123456789012345678901234567890",
                "e33b4ddc9c38f2199c3e7b164fcc0536");
    }

    @Test
    public void testLargeInput() {
        // Test with input larger than the block size (64 bytes)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }
        String largeInput = sb.toString();

        CalculatorMD4 md4 = new CalculatorMD4();
        md4.update(largeInput.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md4.digest();

        // The expected hash for 1000 'a' characters
        assertEquals("5f1bf26a8067c9159b91f1440f7c9e8a", toHexString(digest));
    }

    @Test
    public void testMultipleUpdates() {
        CalculatorMD4 md4 = new CalculatorMD4();
        md4.update("abc".getBytes(StandardCharsets.UTF_8));
        md4.update("def".getBytes(StandardCharsets.UTF_8));
        byte[] digest = md4.digest();

        // The hash for "abcdef"
        assertEquals("804e7f1c2586e50b49ac65db5b645131", toHexString(digest));
    }

    @Test
    public void testResetBetweenDigests() {
        CalculatorMD4 md4 = new CalculatorMD4();

        md4.update("test".getBytes(StandardCharsets.UTF_8));
        byte[] digest1 = md4.digest(); // digest() should reset

        md4.update("different".getBytes(StandardCharsets.UTF_8));
        byte[] digest2 = md4.digest();

        // Digests should be different
        assertFalse(toHexString(digest1).equals(toHexString(digest2)));

        // First digest should match expected
        assertEquals("db346d691d7acc4dc2625db19f9e3f52", toHexString(digest1));

        // Second digest should match expected
        assertEquals("8be058f4c19e1cc3da8414156e9cd55f", toHexString(digest2));
    }

    // Helper methods
    private void testHash(String input, String expectedHash) {
        CalculatorMD4 md4 = new CalculatorMD4();
        md4.update(input.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md4.digest();

        assertEquals(expectedHash, toHexString(digest));
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}