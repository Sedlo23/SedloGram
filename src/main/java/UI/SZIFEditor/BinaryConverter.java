package UI.SZIFEditor;

/**
 * Utility class for binary conversions
 */
public class BinaryConverter {
    public static String decimalToBinary(String decimal, int bits) {
        try {
            int value = Integer.parseInt(decimal.trim());
            String binary = Integer.toBinaryString(value);

            // Pad with leading zeros to match bit length
            if (binary.length() < bits) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bits - binary.length(); i++) {
                    sb.append("0");
                }
                sb.append(binary);
                return sb.toString();
            } else if (binary.length() > bits) {
                return binary.substring(binary.length() - bits);
            }

            return binary;
        } catch (NumberFormatException e) {
            // Return empty string for non-numeric input
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bits; i++) {
                sb.append("0");
            }
            return sb.toString();
        }
    }

    public static String hexToBinary(String hex) {
        StringBuilder binary = new StringBuilder();

        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            int value = Character.digit(c, 16);

            if (value >= 0) {
                String bin = Integer.toBinaryString(value);
                // Pad to 4 bits
                StringBuilder padded = new StringBuilder();
                for (int j = 0; j < 4 - bin.length(); j++) {
                    padded.append("0");
                }
                padded.append(bin);
                binary.append(padded);
            }
        }

        return binary.toString();
    }

    public static String binaryToHex(String binary) {
        StringBuilder hex = new StringBuilder();

        // Ensure the binary string length is a multiple of 4
        int remainder = binary.length() % 4;
        if (remainder > 0) {
            StringBuilder padded = new StringBuilder(binary);
            for (int i = 0; i < 4 - remainder; i++) {
                padded.append("0");
            }
            binary = padded.toString();
        }

        // Convert each 4 bits to a hex digit
        for (int i = 0; i < binary.length(); i += 4) {
            String chunk = binary.substring(i, i + 4);
            int value = Integer.parseInt(chunk, 2);
            hex.append(Integer.toHexString(value).toUpperCase());
        }

        return hex.toString();
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }

        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Helper method to check if a template value reference matches
    public static boolean isTemplateReference(String value, String templateSize) {
        if (value == null || templateSize == null) {
            return false;
        }

        try {
            return value.equals(templateSize);
        } catch (Exception e) {
            return false;
        }
    }
}