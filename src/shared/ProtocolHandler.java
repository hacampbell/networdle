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

    /**
     * Checks to see if a given message is valid by the rules of the networdle
     * protocol. That is, checks to see that the message contains only ASCII
     * characters, and that the final character is the line feed character.
     * @param msg - The message received to be checked for validity
     * @return - True if the message follows the protocol, otherwise false
     */
    public static boolean isValidProtocolMessage (byte[] msg) {
        boolean isValid = true;
        
        // Check that the final character is a line feed character
        if (msg[msg.length - 1] != 10) isValid = false;

        // Make sure that, in accordance to the protocol specifications, the
        // message contains only ASCII characters.
        for (byte b: msg) {
            if (b < 0 || b > 127) isValid = false;
        }

        return isValid;
    }
}
