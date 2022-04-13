package src.client;

import java.io.IOException;
import java.net.Socket;

import src.shared.Utils;

public class Client {

    public static void main(String[] args) {
        String address;
        int port;

        // Check that we've been given the right arguments
        checkArgs(args);

        // Set address and port
        address = args[0];
        port = processPort(args[1]);

        // Connect to the server
        Socket connection = connectToServer(address, port);

        // Disconnect when we're done
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
}
