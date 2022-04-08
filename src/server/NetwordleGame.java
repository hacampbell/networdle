package src.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetwordleGame extends Thread{
    private Socket client;
    private PrintWriter writer;
    private BufferedReader reader;
    private String cAddress;

    /**
     * Constructor for the NetWordleGame class.
     * @param client - The client the game has been created for
     */
    public NetwordleGame (Socket client) {
        try {
            this.client = client;
            this.cAddress = client.getLocalSocketAddress().toString();

            // Setup for Buffered Reader
            InputStreamReader in = new InputStreamReader(client.getInputStream());
            this.reader = new BufferedReader(in);

            // Steup for Print Writer
            this.writer = new PrintWriter(client.getOutputStream(), true);
            
        } catch (IOException e) {
            System.out.println("Unable to create Networdle Client." + 
                                "Error was:\n" + e.getMessage());
            
        }
    }

    public void run () {
        try {
            writer.println("Welcome to the server!");
            writer.println("You are client: " + cAddress);

        } catch (Exception e) {
            System.out.println("An error occured for client at address: " + 
                                cAddress);
            System.out.println("Error was:\n" + e.getMessage());
        }
    }

}
