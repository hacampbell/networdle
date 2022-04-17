package src.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import src.shared.ProtocolHandler;
import src.shared.Utils;
import src.shared.ProtocolHandler.ControlMessage;

public class Client {

    private static final int MAX_BYTES = 256;

    public static void main(String[] args) {
        checkArgs(args); // Check that we've been given the right args

        boolean gameActive = true;               // Controls main game loop
        String address = args[0];                // The host address
        int port = processPort(args[1]);         // The host port
        Scanner input = new Scanner(System.in);  // Used for user input

        // Connect to the server and send START GAME message
        Socket connection = connectToServer(address, port);
        writeMessage(ProtocolHandler.START_GAME, connection);

        // Check that the server responds correctly to a new game being started
        byte[] initMessage = readMessage(connection);

        if (isValidServerInit(initMessage)) {
            gameActive = true;
            System.out.println("Hint: " + 
                                ProtocolHandler.decodeMessage(initMessage));
        } else {
            Utils.error("Invalid game init from server.");
        }

        // Main game loop
        while (gameActive) {
            // Read the clients guess and send it to the server
            System.out.print("Enter a guess: ");
            String message = input.nextLine();
            writeMessage(message, connection);

            // Read response from server
            byte[] resp = readMessage(connection);
            String data = ProtocolHandler.decodeMessage(resp);

            // Check we got a valid response from the server to our guess
            if (!isValidGuessResponse(resp)) {
                Utils.error("Invalid response received from server");
                gameActive = false;
            }

            // Check if the server sent us a number. If so, we know the game's
            // over and we need to do do a few special things.
            if (isNumber(data)) {
                // Read again to check for a valid GAME OVER message
                byte[] endResp = readMessage(connection);
                String endRespStr = ProtocolHandler.decodeMessage(endResp);

                // If the server sent a valid GAME OVER message, show the user
                // The number of guesses it took them to get the target word.
                if (ProtocolHandler.isValidControlMessage(endResp, 
                                            ControlMessage.SERVER_END_GAME)) {
                    System.out.printf("%s %s %s\n",
                        "Well done! You successfully guessed the word in",
                        data,
                        "guesses."
                    );
                } else {
                    Utils.error("Server sent invalid GAME OVER message.");
                }

                // We're done with the game - break out of the main loop.
                gameActive = false;
            } else {
                // If we received a valid guess response but didn't guess the
                // target word, display the hint the server sent back.
                System.out.println("Hint: " + data);   
            }
        }

        // Disconnect when we're done, close the scanner
        input.close();
        disconnectFromServer(connection);
    }

    /**
     * Checks that the program has been given the right amount of arguments in
     * order to run. Terminates the program if this is not the case.
     * @param args - The command line arguments given to the program
     */
    private static void checkArgs (String[] args) {
        // Check we've been given at least two arguments from the command line
        if (args.length < 2) {
            Utils.errorAndDie("Invalid parameters.\nUsage:" + 
                        "./startClient {address} {port number}");
        }
    }

    /**
     * Checks that a valid number has been given to be used as the port to
     * connect to the server with. Terminates the program if this is not the
     * case.
     * @param s - The string to be converted to a port number
     * @return - A valid int to use as a port number
     */
    private static int processPort (String s) {
        int port = 0;

        // Check that we were given a valid integer as the port number
        try{
            port = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Utils.errorAndDie("Invalid port number specified.\nUsage:" +
                        "./startClient {address} {port number}");
        }

        return port;
    }

    /**
     * Connects to to a server at a given address and port.
     * @param address - The address of the host to connect to
     * @param port - The port of the host to connect to
     * @return - A socket which is the connection to the given host
     */
    private static Socket connectToServer (String address, int port) {
        Socket conn = null;

        // Attempt to make a connection.
        try {
            conn = new Socket(address, port);
        } catch (Exception e) {
            Utils.errorAndDie(
                "Unable to connect to server. " + 
                "Please double check you're using the correct address and port"
            );
        }

        return conn;
    }

    /**
     * Closes the connection to the server
     * @param conn - The socket connection to the server
     */
    private static void disconnectFromServer (Socket conn) {
        try {
            conn.close();
        } catch (IOException e) {
            Utils.errorAndDie(
                "An error occured while disconnecting from the server:\n" +
                e.getMessage()
            );
        }
    }

    /**
     * Sends a protocol compliant message to the server.
     * @param message - The message to send to the server
     * @param conn = The connection to the server
     */
    private static void writeMessage (String message, Socket conn) {
        try {
            OutputStream cOS = conn.getOutputStream();
            byte[] encoded = ProtocolHandler.encodeMessage(message);
            cOS.write(encoded);
        } catch (IOException e) {
            // Display the error and then close the connection.
            Utils.error("Error sending message to server", e);
            disconnectFromServer(conn);
        }  
    }

    /**
     * Reads messages from the server and returns them as a byte array.
     * Because data is sent over the network as an array of bytes, this
     * function trims the data down into just what was intended to be sent by
     * the server.
     * @return - The message from the client as an array of bytes
     */
    private static byte[] readMessage (Socket conn) {
        try {
            InputStream stream = conn.getInputStream();
            byte[] rawData = new byte[MAX_BYTES];
            int count = stream.read(rawData);

            byte[] trimmedData = new byte[count];

            for (int i = 0; i < count; i++) {
                trimmedData[i] = rawData[i];
            }

            return trimmedData;
        } catch (IOException e) {
            // Display the error and then close the associated client
            Utils.error("Error reading message from server", e);
            disconnectFromServer(conn);
        }

        return null;
    }

    /**
     * Checks that the first message sent by the server to the client is valid
     * by the rules of the networdle protocol. I.e. The first 
     * @param msg
     * @return
     */
    private static boolean isValidServerInit (byte[] msg) {
        return ProtocolHandler.isValidControlMessage(msg, 
                                    ControlMessage.SERVER_START_GAME_RESPONSE);
    }

    /**
     * Checks is a string is a number. That is, checks if it can be parsed to
     * an integer without causing an error.
     * @param str - The string to check if it is a number
     * @return - True if the string is a number, otherwise false.
     */
    private static boolean isNumber (String str) {
        if (str == null) return false;

        try {
            int num = Integer.parseInt(str);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private static boolean isValidGuessResponse (byte[] resp) {
        boolean valid = true;
        String decoded = ProtocolHandler.decodeMessage(resp);

        // Check the message was sent using the protocol
        if (!ProtocolHandler.isValidProtocolMessage(resp)) valid = false;

        // Check we've been given an INVALID GUESS message, a hint messgae,
        // or a number of guesses.
        if (!(ProtocolHandler.isValidControlMessage(resp, 
                                ControlMessage.SERVER_INVALID_GUESS)
            || decoded.matches("^[a-zA-Z0-9_]+$")
            || isNumber(decoded)
        )) valid = false; 

        return valid;
    }
}
