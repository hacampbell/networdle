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
        // Check that we've been given the right arguments
        checkArgs(args);

        boolean gameActive = true;
        //boolean serverResponse = false;

        // Set address and port
        String address = args[0];
        int port = processPort(args[1]);

        // Setup scanner to read user input.
        Scanner input = new Scanner(System.in);

        // Connect to the server
        Socket connection = connectToServer(address, port);

        // Send message to start the game.
        writeMessage("START GAME", connection);

        // Check that the server responds correctly to a new game bring started
        byte[] initMessage = readMessage(connection);
        if (isValidServerInit(initMessage)) {
            gameActive = true;
            System.out.println(ProtocolHandler.decodeMessage(initMessage));
        } else {
            Utils.error("Invalid game init from server.");
        }

        // Main game loop
        while (gameActive) {
            // Read data from client input
            String message = input.nextLine();
            writeMessage(message, connection);

            // Read response from server
            byte[] resp = readMessage(connection);
            String data = ProtocolHandler.decodeMessage(resp);
            System.out.println(data);
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
            Utils.error("Error reading message rom server", e);
            disconnectFromServer(conn);
        }

        return null;
    }

    private static boolean isValidServerInit (byte[] msg) {
        return ProtocolHandler.isValidControlMessage(msg, ControlMessage.SERVER_START_GAME_RESPONSE);
    }
}
