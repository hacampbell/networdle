package src.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import src.shared.ProtocolHandler;
import src.shared.Utils;

public class NetwordleGame extends Thread{
    private final int MAX_BYTES = 256;

    private Socket client;
    private String cAddress;
    private String targetWord;
    private boolean gameActive;

    /**
     * Constructor for the NetWordleGame class.
     * @param client - The client the game has been created for
     */
    public NetwordleGame (Socket client) {
        this.client = client;
        this.cAddress = client.getLocalSocketAddress().toString();
        this.targetWord = selectTargetWord();
        this.gameActive = true;
    }

    /**
     * Main entry point for a Networdle Game. 
     */
    public void run () {
        try {
            // Test writing something using the protocol specifications.
            client.getOutputStream().write(ProtocolHandler.encodeMessage(targetWord));

            // Main game loop
            while (gameActive) {
                byte[] message = readMessage();
                checkGameMessage(message);
            }

        } catch (Exception e) {
            // If there's an error, display it and kill the client.
            Utils.error("An error occured during execution for client: " +
                        cAddress, e);
            closeClient();
        }
    }

    /**
     * Selects and returns a random word from the target words files. In the
     * case that an error occurs trying to read the file, the generic word
     * 'apple' will be returned so that the program can continue.
     * @return
     */
    private String selectTargetWord () {
        final String targetPath = "./resources/target.txt";
        String target = "APPLE"; // Generic word in case reading the file fails

        try {
            int lc = 0;
            FileReader fr = new FileReader(targetPath);
            BufferedReader br = new BufferedReader(fr);

            // Count the number of lines in the file
            while(br.readLine() != null) lc++;
            br.close();

            // Select and read a radom line
            Random rnd = new Random();
            int line = rnd.nextInt(lc);
            target = Files.readAllLines(Paths.get(targetPath)).get(line);

        } catch (Exception e) {
            Utils.error("An error has occured trying to select a target word",
                        e);
        }

        return target; 
    }

    /**
     * Wrapper function for closing the client socket and setting the game
     * state to inactive.
     */
    private void closeClient () {
        try {
            this.client.close();
            this.gameActive = false;
        } catch (IOException e) {
            Utils.error("Error closing client " + cAddress, e);
            this.gameActive = false;
        }
    }

    /**
     * Sends a protocol compliant message to the client.
     * @param message - The message to send to the client
     */
    private void writeMessage (String message) {
        try {
            OutputStream cOS = client.getOutputStream();
            byte[] encoded = ProtocolHandler.encodeMessage(message);
            cOS.write(encoded);
        } catch (IOException e) {
            // Display the error and then close the associated client
            Utils.error(
                "Error trying to send message to client " + cAddress, 
                e
            );
            closeClient();
        }  
    }

    /**
     * Reads messages from the client and returns them as a byte array.
     * Because data is sent over the network as an array of bytes, this
     * function trims the data down into just what was intended to be sent by
     * the client.
     * @return - The message from the client as an array of bytes
     */
    private byte[] readMessage () {
        try {
            InputStream stream = client.getInputStream();
            byte[] rawData = new byte[MAX_BYTES];
            int count = stream.read(rawData);

            byte[] trimmedData = new byte[count];

            for (int i = 0; i < count; i++) {
                trimmedData[i] = rawData[i];
            }

            return trimmedData;
        } catch (IOException e) {
            // Display the error and then close the associated client
            Utils.error(
                "Error trying to read message from client " + cAddress, 
                e
            );
            closeClient();
        }

        return null;
    }

    /**
     * The main function for managing the game and guesses made by the client.
     * @param message - The message sent to the server by the client
     */
    private void checkGameMessage (byte[] message) {
        for (byte b: message) System.out.println(b);
        System.out.println(ProtocolHandler.isValidProtocolMessage(message));
        writeMessage("message");
    }

}
