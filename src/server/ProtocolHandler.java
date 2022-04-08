package src.server;

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
        byte[] msg = data.getBytes(StandardCharsets.UTF_8);
        return msg;
    }
}
