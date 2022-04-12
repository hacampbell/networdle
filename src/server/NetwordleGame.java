package src.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import src.shared.ProtocolHandler;
import src.shared.Utils;

public class NetwordleGame extends Thread{
    private Socket client;
    private PrintWriter writer;
    private BufferedReader reader;
    private String cAddress;
    private String targetWord;

    /**
     * Constructor for the NetWordleGame class.
     * @param client - The client the game has been created for
     */
    public NetwordleGame (Socket client) {
        try {
            this.client = client;
            this.cAddress = client.getLocalSocketAddress().toString();
            this.targetWord = selectTargetWord();

            // Setup for Buffered Reader
            InputStreamReader in = new InputStreamReader(client.getInputStream());
            this.reader = new BufferedReader(in);

            // Steup for Print Writer
            this.writer = new PrintWriter(client.getOutputStream(), true);
            
        } catch (IOException e) {
            Utils.error("Unable to create new Networdle Game.", e);
            
        }
    }

    /**
     * Main entry point for a Networdle Game. 
     */
    public void run () {
        try {
            // Write some generic test data. This isn't Protocol Compliant and
            // needs to be removed before submission.
            writer.println("Welcome to the server!");
            writer.println("You are client: " + cAddress);

            // Test writing something using the protocol specifications.
            client.getOutputStream().write(ProtocolHandler.encodeMessage("START GAME"));
            client.getOutputStream().write(ProtocolHandler.encodeMessage(targetWord));

        } catch (Exception e) {
            Utils.error("An error occured trying to create client at: " +
                        cAddress, e);
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

}
