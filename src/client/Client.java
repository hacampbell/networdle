package src.client;

import src.shared.Utils;

public class Client {

    public static void main(String[] args) {
        String address;
        int port;

        for (String s: args) {
            System.out.println(s);
        }

        // Check that we've been given the right arguments
        checkArgs(args);

        // Set address
        address = args[0];

        // Check and set port number
        port = processPort(args[1]);
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
}
