package tools.string;


import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.Patch;
import packets.Interfaces.IPacket;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Returns a substring of the specified string starting from the given start index and with the given length.
     *
     * @param string the string to extract the substring from
     * @param start  the index of the first character to include in the substring
     * @param length the number of characters to include in the substring
     * @return the substring of the specified string starting from the given start index and with the given length
     * @throws IndexOutOfBoundsException if the length is negative
     */
    public static String substring(String string, int start, int length) {
        if (length < 0) {
            throw new IndexOutOfBoundsException("parameter.length.cannot.be.negative");
        }

        int end = start + length;
        int stringLength = string.length();
        if (start >= stringLength || end > stringLength) {
            throw new IndexOutOfBoundsException("Start or end index is out of bounds.");
        }

        char[] result = new char[length];
        string.getChars(start, end, result, 0);
        return new String(result);
    }

    /**
     * Determines if a given string can be parsed as an integer.
     *
     * @param s the string to be checked
     * @return true if the string can be parsed as an integer, false otherwise
     */
    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    /**
     * Checks whether a given string can be parsed as an integer with the specified radix.
     *
     * @param s     the string to check
     * @param radix the radix to use when parsing the string
     * @return true if the string can be parsed as an integer with the specified radix, false otherwise
     */
    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    /**
     * Removes all characters in a string starting from a specified index and returns the resulting substring.
     *
     * @param string the original string to remove characters from
     * @param start  the index at which to start removing characters
     * @return the substring of the original string with all characters from the start index onwards removed
     */
    public static String remove(String string, int start) {
        if (start >= string.length()) {
            return string;
        }
        return string.substring(0, start);
    }


    /**
     * Removes a specified number of characters from a string, starting at a specified index.
     *
     * @param string the input string to be modified
     * @param start  the index to start removing characters from
     * @param count  the number of characters to remove
     * @return the modified string with the characters removed
     */
    public static String remove(String string, int start, int count) {
        int length = string.length();
        if (start >= length) {
            return string;
        }

        // Calculate the end index of the removal
        int end = Math.min(start + count, length);

        StringBuilder builder = new StringBuilder(string);
        builder.delete(start, end);

        return builder.toString();
    }






    /**
     * Trims characters from the end of a string.
     *
     * @param string      the string to trim.
     * @param charsToTrim the characters to remove from the end of the string. If empty, whitespace is trimmed.
     * @return the trimmed string.
     */


    public static String trimEnd(String string, Character... charsToTrim) {
        if (string == null || charsToTrim == null) {
            return string;
        }

        Set<Character> trimCharsSet = new HashSet<>();
        // If no characters provided, default to trimming whitespace
        if (charsToTrim.length == 0) {
            trimCharsSet.add(' '); // Assuming space is the only whitespace to trim
        } else {
            for (Character c : charsToTrim) {
                trimCharsSet.add(c);
            }
        }

        int lengthToKeep = string.length();
        for (int index = string.length() - 1; index >= 0; index--) {
            if (!trimCharsSet.contains(string.charAt(index)) && !Character.isWhitespace(string.charAt(index))) {
                break;
            }
            lengthToKeep = index;
        }

        return string.substring(0, lengthToKeep);
    }


    /**
     * Trims the characters specified in the varargs charsToTrim from the beginning of the string.
     * If string or charsToTrim is null, the original string is returned.
     *
     * @param string      The string to be trimmed.
     * @param charsToTrim The characters to be trimmed. If empty, whitespace characters will be trimmed.
     * @return The trimmed string.
     */


    public static String trimStart(String string, Character... charsToTrim) {
        if (string == null || charsToTrim == null) {
            return string;
        }

        Set<Character> trimCharsSet = new HashSet<>();
        // Populate the set with characters to trim, or treat as whitespace trimming if empty
        boolean trimWhitespace = charsToTrim.length == 0;
        for (Character c : charsToTrim) {
            trimCharsSet.add(c);
        }

        int startingIndex = 0;
        while (startingIndex < string.length()) {
            char currentChar = string.charAt(startingIndex);
            boolean isTrimChar = trimWhitespace ? Character.isWhitespace(currentChar) : trimCharsSet.contains(currentChar);
            if (!isTrimChar) {
                break;
            }
            startingIndex++;
        }

        return string.substring(startingIndex);
    }


    /**
     * Trims the given string by removing the specified characters from both the start and the end of the string.
     *
     * @param string      the string to be trimmed
     * @param charsToTrim the characters to be removed from the start and the end of the string
     * @return the trimmed string
     */
    public static String trim(String string, Character... charsToTrim) {
        return trimEnd(trimStart(string, charsToTrim), charsToTrim);
    }

    /**
     * Pads a string with spaces on the left side to reach the specified total width.
     *
     * @param string     the string to be padded
     * @param totalWidth the desired total width of the resulting string
     * @return the padded string
     */
    public static String padLeft(String string, int totalWidth) {
        return padLeft(string, totalWidth, ' ');
    }

    /**
     * Pads the given string on the left with the specified padding character
     * until it reaches the specified total width.
     *
     * @param string      the string to pad
     * @param totalWidth  the desired width of the resulting string
     * @param paddingChar the character to use for padding
     * @return the padded string
     */
    public static String padLeft(String string, int totalWidth, char paddingChar) {
        StringBuilder sb = new StringBuilder();

        while (sb.length() + string.length() < totalWidth) {
            sb.append(paddingChar);
        }

        sb.append(string);
        return sb.toString();
    }

    /**
     * Returns a string consisting of a specific character repeated a specified number of times.
     *
     * @param charToRepeat the character to repeat
     * @param count        the number of times to repeat the character
     * @return a string consisting of the repeated character
     */
    public static String repeatChar(char charToRepeat, int count) {
        String newString = "";
        for (int i = 1; i <= count; i++) {
            newString += charToRepeat;
        }
        return newString;
    }

    /**
     * Trims the first string in the given string array by the specified size and returns the trimmed substring.
     * The original string in the array is modified by removing the trimmed substring.
     *
     * @param str  The string array containing the string to be trimmed.
     * @param size The number of characters to trim from the start of the string.
     * @return The trimmed substring.
     */
    public static String TrimAR(String[] str, int size) {
        String s = str[0];

        s = str[0].substring(0, size);

        str[0] = str[0].substring(size);

        return s.substring(0, size);

    }

    /**
     * Checks if the given string is numeric or not.
     *
     * @param strNum the string to be checked
     * @return true if the string is numeric, false otherwise
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether a given string is null or empty.
     *
     * @param string the string to be checked
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static byte[] splitBinaryStringToByteArray(String binaryString) {



        byte[] result = new byte[binaryString.length() / 8];

        int i = 0;
        int j = 0;
        while (i < binaryString.length()) {
            result[j] = ((byte) Integer.parseInt((binaryString.substring(i, i + 8)), 2));
            j++;
            i += 8;
        }
        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        // Prepend a '0' if the length is odd
        if (s.length() % 2 != 0) {
            s = s + "0";
        }

        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    /**
     * Returns a boolean indicating whether the given string is null or consists entirely of whitespace characters.
     *
     * @param string the string to check
     * @return true if the string is null or consists entirely of whitespace characters, false otherwise
     */
    public boolean isNullOrWhiteSpace(String string) {
        if (string == null)
            return true;

        for (int index = 0; index < string.length(); index++) {
            if (!Character.isWhitespace(string.charAt(index)))
                return false;
        }

        return true;
    }

    /**
     * Joins an array of strings into a single string with a given separator.
     *
     * @param separator   The separator string to use between each element of the array.
     * @param stringArray The array of strings to join.
     * @param startIndex  The starting index of the range of elements to join.
     * @param count       The number of elements to join starting from startIndex.
     * @return The concatenated string with the separator between each element of the array.
     */
    public String join(String separator, String[] stringArray) {
        if (stringArray == null)
            return null;
        else
            return join(separator, stringArray, 0, stringArray.length);
    }

    /**
     * Joins an array of strings into a single string, separated by a given separator, starting at a specified index, and for a specified number of strings.
     * If the array is null, null is returned.
     *
     * @param separator   the separator to use between the strings (can be null)
     * @param stringArray the array of strings to join
     * @param startIndex  the starting index in the array
     * @param count       the number of strings to join
     * @return the resulting string
     */
    public String join(String separator, String[] stringArray, int startIndex, int count) {
        if (stringArray == null)
            return null;

        StringBuilder sb = new StringBuilder();

        for (int index = startIndex; index < stringArray.length && index - startIndex < count; index++) {
            if (separator != null && index > startIndex)
                sb.append(separator);

            if (stringArray[index] != null)
                sb.append(stringArray[index]);
        }

        return sb.toString();
    }

    /**
     * Checks if two strings are equal.
     *
     * @param s1 the first string to compare
     * @param s2 the second string to compare
     * @return true if both strings are equal, false otherwise
     */
    public boolean stringsEqual(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        else
            return s1 != null && s1.equals(s2);
    }

    /**
     * Pads the given string on the right with spaces to reach the specified total width.
     *
     * @param string     the string to pad
     * @param totalWidth the total width to reach
     * @return the padded string
     */
    public String padRight(String string, int totalWidth) {
        return padRight(string, totalWidth, ' ');
    }

    /**
     * Pads a given string on the right with a padding character until it reaches the specified total width.
     *
     * @param string      the original string to be padded
     * @param totalWidth  the total width of the resulting string, including the original string and padding characters
     * @param paddingChar the character to be used for padding
     * @return the padded string
     */
    public String padRight(String string, int totalWidth, char paddingChar) {
        int paddingLength = totalWidth - string.length();

        if (paddingLength <= 0) {
            return string;
        }

        char[] padding = new char[paddingLength];
        Arrays.fill(padding, paddingChar);

        return string + new String(padding);
    }

    /**
     * Returns the index within the specified substring of the last occurrence of the specified character,
     * searching backward starting at the specified index within the substring and considering only the
     * specified number of characters.
     *
     * @param string     the string to search within
     * @param value      the character to search for
     * @param startIndex the starting index within the string to begin the search, inclusive
     * @param count      the number of characters to consider in the search
     * @return the index within the specified substring of the last occurrence of the specified character, or -1 if the character does not occur or if the substring is not found
     */
    public int lastIndexOf(String string, char value, int startIndex, int count) {
        int leftMost = startIndex + 1 - count;
        int rightMost = startIndex + 1;
        String substring = string.substring(leftMost, rightMost);
        int lastIndexInSubstring = substring.lastIndexOf(value);
        if (lastIndexInSubstring < 0)
            return -1;
        else
            return lastIndexInSubstring + leftMost;
    }

    /**
     * Returns the index of the last occurrence of the specified substring in the specified string,
     * searching backward from the specified index and within the specified count of characters.
     *
     * @param string     the string to search
     * @param value      the substring to search for
     * @param startIndex the starting index for the search
     * @param count      the maximum number of characters to search
     * @param string     the string to search in
     * @param anyOf      the array of characters to search for
     * @return the index of the last occurrence of the substring within the specified range of the string, or -1 if the substring is not found
     * <p>
     * public int lastIndexOf(String string, String value, int startIndex, int count) {
     * int leftMost = startIndex + 1 - count;
     * int rightMost = startIndex + 1;
     * String substring = string.substring(leftMost, rightMost);
     * int lastIndexInSubstring = substring.lastIndexOf(value);
     * if (lastIndexInSubstring < 0)
     * return -1;
     * else
     * return lastIndexInSubstring + leftMost;
     * }
     * <p>
     * /**
     * Returns the index of the first occurrence of any character in the given array of characters
     * in the given string.
     * @return the index of the first occurrence of any character in the array in the string, or -1 if none of the characters are found
     */
    public int indexOfAny(String string, char[] anyOf) {
        int lowestIndex = -1;
        for (char c : anyOf) {
            int index = string.indexOf(c);
            if (index > -1) {
                if (lowestIndex == -1 || index < lowestIndex) {
                    lowestIndex = index;

                    if (index == 0)
                        break;
                }
            }
        }

        return lowestIndex;
    }

    /**
     * This method searches for the index of the first occurrence of
     * any character in the anyOf array in the string argument, starting
     * from the startIndex position in the string.
     *
     * @param string     the string to search in
     * @param anyOf      the array of characters to search for
     * @param startIndex the starting index for the search
     * @return If none of the characters in the anyOf array are found in the specified range, the method returns -1.
     */
    public int indexOfAny(String string, char[] anyOf, int startIndex) {
        int indexInSubstring = indexOfAny(string.substring(startIndex), anyOf);
        if (indexInSubstring == -1)
            return -1;
        else
            return indexInSubstring + startIndex;
    }


    public static JTable createHtmlDiffTable(String original, String revised) {
        List<String> originalLines = List.of(original.split("\n"));
        List<String> revisedLines = List.of(revised.split("\n"));



        Patch<String> patch = DiffUtils.diff(originalLines, revisedLines);

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Původní text");
        model.addColumn("Nový text");

        int origIndex = 0, revIndex = 0;

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            Chunk<String> source = delta.getSource();
            Chunk<String> target = delta.getTarget();

            while (origIndex < source.getPosition() && revIndex < target.getPosition()) {
                model.addRow(new Object[]{originalLines.get(origIndex++), revisedLines.get(revIndex++)});
            }

            switch (delta.getType()) {
                case DELETE -> {
                    for (String line : source.getLines())
                    {
                        model.addRow(new Object[]{"<html><font color='red'>" + line + "</font></html>", ""});
                        origIndex++;
                    }
                }
                case INSERT -> {
                    for (String line : target.getLines()) {
                        model.addRow(new Object[]{"", "<html><font color='green'>" + line + "</font></html>"});
                        revIndex++;
                    }
                }
                case CHANGE -> {
                    int max = Math.max(source.size(), target.size());
                    for (int i = 0; i < max; i++) {
                        String origLine = i < source.size() ? source.getLines().get(i) : "";
                        String revLine = i < target.size() ? target.getLines().get(i) : "";
                        model.addRow(new Object[]{
                                "<html><font color='blue'>" + origLine + "</font></html>",
                                "<html><font color='blue'>" + revLine + "</font></html>"
                        });
                        origIndex++;
                        revIndex++;
                    }
                }
            }
        }

        while (origIndex < originalLines.size() && revIndex < revisedLines.size()) {
            model.addRow(new Object[]{originalLines.get(origIndex++), revisedLines.get(revIndex++)});
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        return table;
    }

    /**
     * Returns the index of the first occurrence of any character from the given character array
     * in the specified substring of the given string. The search starts at the specified startIndex
     * and considers the specified count characters of the string.
     *
     * @param string     the string to search in
     * @param anyOf      the array of characters to search for
     * @param startIndex the starting index of the search
     * @param count      the number of characters to consider in the search in the specified substring of the given string, or -1 if no such occurrence is found
     */
    public int indexOfAny(String string, char[] anyOf, int startIndex, int count) {
        int endIndex = startIndex + count;
        int indexInSubstring = indexOfAny(string.substring(startIndex, endIndex), anyOf);
        if (indexInSubstring == -1)
            return -1;
        else
            return indexInSubstring + startIndex;
    }

    /**
     * Returns the index of the last occurrence of any character in the given array of characters
     * in the given string.
     *
     * @param string The string to search for characters in.
     * @param anyOf  An array of characters to search for.
     * @return The index of the last occurrence of any character in the given array of characters in the given string, or -1 if none of the characters in the array are found in the string.
     */
    public int lastIndexOfAny(String string, char[] anyOf) {
        int highestIndex = -1;
        for (char c : anyOf) {
            int index = string.lastIndexOf(c);
            if (index > highestIndex) {
                highestIndex = index;

                if (index == string.length() - 1)
                    break;
            }
        }

        return highestIndex;
    }

    /**
     * Returns the index position of the last occurrence of any character in the given array 'anyOf' in the specified
     * string 'string', starting from the given 'startIndex' position (inclusive).
     *
     * @param string     the string to search for the last occurrence of any character in the given array 'anyOf'.
     * @param anyOf      the array of characters to search for in the specified string.
     * @param startIndex the starting position in the string to search for the last occurrence of any character in the given array.
     * @return the index position of the last occurrence of any character in the given array 'anyOf' in the specified string starting from the given 'startIndex' position (inclusive), or -1 if no character in the array is found.
     */
    public int lastIndexOfAny(String string, char[] anyOf, int startIndex) {
        String substring = string.substring(0, startIndex + 1);
        int lastIndexInSubstring = lastIndexOfAny(substring, anyOf);
        if (lastIndexInSubstring < 0)
            return -1;
        else
            return lastIndexInSubstring;
    }

    /**
     * This method returns the index of the last occurrence of any character in the specified char array within a specified substring of a given string.
     * The search is performed from right to left, starting at the specified start index and proceeding to the left up to the count characters.
     *
     * @param string     the string to search in
     * @param anyOf      the array of characters to search for
     * @param startIndex the starting index of the substring to search in
     * @param count      the maximum number of characters to search in
     * @return the index of the last occurrence of any character in the specified char array within the specified substring of the given string, or -1 if none of the characters are found
     */
    public int lastIndexOfAny(String string, char[] anyOf, int startIndex, int count) {
        int leftMost = startIndex + 1 - count;
        int rightMost = startIndex + 1;
        String substring = string.substring(leftMost, rightMost);
        int lastIndexInSubstring = lastIndexOfAny(substring, anyOf);
        if (lastIndexInSubstring < 0)
            return -1;
        else
            return lastIndexInSubstring + leftMost;
    }

    public static String extractTextFromTag(String input) {
        String regex = "<s style='color:red;'>(.*?)</s>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return "<s style='color:red;'>" + matcher.group(1) + "</s>";
        }

        return "No match found";
    }

    public static String convertTagsToHtml(String text) {
        return text.replaceAll("\\*\\*(.*?)\\*\\*", "<b style='color:green;'>$1</b>")
                .replaceAll("&&(.*?)&&", "<s style='color:red;'>$1</s>");
    }

    public static String buildContentString(DefaultListModel<IPacket> packetListModel) {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < packetListModel.size(); i++) {
            IPacket packet = packetListModel.getElementAt(i);
            content.append(packet.getSimpleView()).append("\n");
        }
        return content.toString();
    }

}
