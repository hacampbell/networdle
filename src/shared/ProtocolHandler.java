package src.shared;

import java.nio.charset.StandardCharsets;

public class ProtocolHandler {
    // Constants to be used throughout the project as control messages
    public static final String START_GAME = "START GAME";
    public static final String START_RESPONSE = "_____";
    public static final String INVALID_GUESS = "INVALID GUESS";
    public static final String END_GAME = "GAME OVER";


    /**
     * Enum used to store the differnt types of control messages used in the
     * game and for when checking if a given control message sent by either
     * the server or client is valid. For example, checking that the client
     * sends a valid START GAME message to begin the game, or that the server
     * sends a valid GAME OVER message.
     */
    public enum ControlMessage {
        CLIENT_START_GAME,
        SERVER_START_GAME_RESPONSE,
        SERVER_END_GAME
    }

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
        String chop = str.substring(0, str.length() - 1); // Drop the /n
        return chop;
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

    /**
     * Checks that a given control message for the game is valid. For example,
     * that the client sent a valid START GAME message, or that that the server
     * sent a valid GAME OVER message.
     * @param msg - The message sent by the client
     * @param ctl - The type of control message to check
     * @return
     */
    public static boolean isValidControlMessage (byte[] msg, ControlMessage ctl) {
        switch (ctl) {
            case CLIENT_START_GAME:
                return isValidProtocolMessage(msg) 
                        && decodeMessage(msg).equals(START_GAME);

            case SERVER_START_GAME_RESPONSE:
                return isValidProtocolMessage(msg)
                        && decodeMessage(msg).equals(START_RESPONSE); 

            case SERVER_END_GAME:
                return isValidProtocolMessage(msg) 
                        && decodeMessage(msg).equals(END_GAME);
            
            default:
                return false;
        }
    }
}
