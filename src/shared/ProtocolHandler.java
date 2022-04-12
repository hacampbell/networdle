package src.shared;

import java.nio.charset.StandardCharsets;

public class ProtocolHandler {
    /**
     * Converts a string to a Protocol complian message i.e. to a series of
     * ASCII values ending with the line feed character.
     * @param str
     * @return
     */
    public static byte[] encodeMessage (String str) {
        String data = str + "\n";
        byte[] msg = data.getBytes(StandardCharsets.US_ASCII);
        return msg;
    }

    /**
     * Converts a Protocol compliant message to a string i.e. a series of ASCII
     * values to a string.
     * @param msg - The protocol message to be decoded
     * @return - The decoded string
     */
    public static String decodeMessage (byte[] msg) {
        String str = new String(msg, StandardCharsets.US_ASCII);
        return str;
    }
}
