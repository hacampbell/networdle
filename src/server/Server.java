package src.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import src.shared.Utils;

public class Server {
    private int port;
    private ServerSocket ss;

    /**
     * Server constructor
     * @param port - The port to start the server on
     */
    public Server (int port) {
        this.port = port;
    }

    /**
     * Main driver function for the server
     */
    public void start () {
        try {
            ss = new ServerSocket(this.port);

            System.out.println("Server listening on port " + this.port);

            // Main sever loop
            while (true) {
                Socket client = ss.accept();
                NetwordleGame clientGame = new NetwordleGame(client);
                clientGame.start();
            }

        } catch (IOException e) {
            Utils.errorAndDie("Unable to create server. Please try again, " +
                                "perhaps with another port.");
        } finally {
            closeServer();
        }
    }

    /**
     * A small ultilty function used for closing the server.
     */
    private void closeServer () {
        try {
            ss.close();
        } catch (IOException e) {
            Utils.errorAndDie("An error occured while closing the server");
        }
    }
}
