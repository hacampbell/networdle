package src.server;

import src.shared.Utils;

public class Entrypoint {
    /**************************************************************************
     * Processes the command line arguments given to the server. That is,
     * ensures that a valid port has been given. If no valid port has been
     * given, the program will exit.
     * 
     * @param args - The command line arguments given to the program
     * @return A port number for the server o be opened on
     *************************************************************************/
    private static int processPort (String[] args) {
        int port = 0;

        // Check we've been given at least one argument from the command line
        if (args.length < 1) {
            Utils.errorAndDie("No port number specified.\nUsage:" + 
                        "./startServer {port number}");
        }

        // Check that we were given a valid integer as the port number
        try{
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            Utils.errorAndDie("Invalid port number specified.\nUsage:" +
                        "./startServer {port number}");
        }

        return port;
    }


    /**************************************************************************
     * Main entry point for the server application.
     * @param args - The command line arguments given to the program.
     *************************************************************************/
    public static void main(String[] args) {
        int port = processPort(args);
        Server server = new Server(port);
        server.start();
    }
}
