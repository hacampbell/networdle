package src.server;

public class Server {
    private int port;

    /**
     * Server constructor
     * @param args - The command line arguments given to the program
     */
    public Server (int port) {
        this.port = port;
    }

    /**
     * Main driver function for the server
     */
    public void start () {
        System.out.println("Starting server on port " + this.port);
    }
}
