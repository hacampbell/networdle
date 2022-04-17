package src.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import src.shared.ProtocolHandler;
import src.shared.ProtocolHandler.ControlMessage;
import src.shared.Utils;

public class NetwordleGame extends Thread{
    private final int MAX_BYTES = 256;

    private Socket client;              // The socket connection to the client
    private String cAddress;            // The clients IP address for logging
    private String targetWord;          // The word for the client to guess
    private boolean gameActive;         // The active state of the game
    private Integer guessCount;         // The clients number fo guesses
    private HashSet<String> guessList;  // The list of valid guesses to be made

    /************************************************************************
     * Constructor for the NetWordleGame class.
     * @param client - The client the game has been created for
     ***********************************************************************/
    public NetwordleGame (Socket client) {
        this.client = client;
        this.cAddress = client.getLocalSocketAddress().toString();
        this.targetWord = selectTargetWord();
        this.guessList = loadGuessList();
        this.guessCount = 0;

        Utils.info("Client " + cAddress + " connected. Target word is " 
                    + targetWord);
    }


    /**************************************************************************
     * Main entry point for a Networdle Game. 
     *************************************************************************/
    public void run () {
        try {
            // Check that we're sent a START GAME message from the client
            byte[] initMessage = readMessage();
            if (ProtocolHandler.isValidControlMessage(initMessage, 
                ControlMessage.CLIENT_START_GAME)) 
            {
                this.gameActive = true;
            } else {
                // If we don't get a valid START GAME message, close the client
                // and exit this wordle game.
                Utils.info("Dropping client " + cAddress 
                            + "due to bad START GAME message");
                closeClient();
                return;
            }

            // Send the first hint
            writeMessage(ProtocolHandler.START_RESPONSE);

            // Main game loop
            while (gameActive) {
                byte[] message = readMessage();
                checkGuess(message);
            }

        } catch (Exception e) {
            // If there's an error, drop the client.
            Utils.error("Dropped client " + cAddress);
            closeClient();
        }
    }


    /**************************************************************************
     * Selects and returns a random word from the target words files. In the
     * case that an error occurs trying to read the file, the generic word
     * 'apple' will be returned so that the program can continue.
     * @return
     *************************************************************************/
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


    /**************************************************************************
     * Gets the list of valid guesses a client can make and returns it as a
     * hasset for fast lookup.
     * @return - A hashset containing the valid guesses a client can make.
     *************************************************************************/
    private HashSet<String> loadGuessList () {
        final String targetPath = "./resources/guess.txt";
        HashSet<String> guessList = new HashSet<String>();

        // Read each guess from the file and add it to the hashset.
        try {
            BufferedReader br = new BufferedReader(new FileReader(targetPath));
            for(String line; (line = br.readLine()) != null;) {
                guessList.add(line);
            }
            br.close();
        }catch (Exception e) {
            Utils.errorAndDie("Error trying to read guess list.");
        }

        return guessList;
    }


    /**************************************************************************
     * Wrapper function for closing the client socket and setting the game
     * state to inactive.
     *************************************************************************/
    private void closeClient () {
        try {
            this.client.close();
            this.gameActive = false;
        } catch (IOException e) {
            Utils.error("Error closing client " + cAddress, e);
            this.gameActive = false;
        }
    }


    /**************************************************************************
     * Sends a protocol compliant message to the client.
     * @param message - The message to send to the client
     *************************************************************************/
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


    /**************************************************************************
     * Reads messages from the client and returns them as a byte array.
     * Because data is sent over the network as an array of bytes, this
     * function trims the data down into just what was intended to be sent by
     * the client.
     * @return - The message from the client as an array of bytes
     *************************************************************************/
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


    /**************************************************************************
     * Checks if a word guess is valid. That is, checks if a given guess is
     * contained in the guess list.
     * @param guess - The guess to check for validity
     * @return - True if the guess is in the guess list, otherwise false.
     *************************************************************************/
    private boolean isValidGuess (String guess) {
        return guessList.contains(guess);
    }


    /**************************************************************************
     * Gets the indexes of all occurances of a character in a string
     * @param word - The word to get the char indexes from
     * @param ch - The char to get the indexes of
     * @return - An array containing the index of the char occurances
     *************************************************************************/
    private Integer[] getIndexesOfChar (String word, char ch) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();

        for(int i = 0; i < word.length(); i++){
            if(word.charAt(i) == ch){
               indexes.add(i);
            }
        }

        Integer[] data = new Integer[indexes.size()];
        data = indexes.toArray(data);
        return data;
    }


    /**************************************************************************
     * Generates a new hint for a given guess
     * @param guess - The guess to generate a hint for
     * @return - The hint for a given guess
     *************************************************************************/
    private String generateHint (String guess) {
        StringBuilder hintBuilder = new StringBuilder(targetWord.length());
        boolean[] matched = new boolean[targetWord.length()];

        // Set the hint to all underscores to begin with
        hintBuilder.append(ProtocolHandler.START_RESPONSE);

        // Loop through, set all correct letters in the hint.
        for (int i = 0; i < targetWord.length(); i++) {
            char gLtr = guess.charAt(i);
            char tLtr = targetWord.charAt(i);

            if (gLtr == tLtr) {
                hintBuilder.setCharAt(i, gLtr);
                matched[i] = true;
            }
        }

        // Loop through, set all characters that are in the word, but are in
        // the wrong place
        for (int i = 0; i < targetWord.length(); i++) {
            // The characters we want to work with
            char gLtr = guess.charAt(i);
            char tLtr = targetWord.charAt(i);

            // Get the posiion in the target wod of this character
            int occurance = targetWord.indexOf(gLtr);
            occurance = (occurance >= 0) ? occurance : i;

            // Get all the positions of this character in the target word and
            // check to see if they've all been matched
            Integer[] indexes = getIndexesOfChar(targetWord, gLtr);
            boolean allMatched = false;
            int matches = 0;

            for (int index: indexes) {
                if (matched[index]) matches++;
            }

            if (matches == indexes.length && indexes.length > 0) {
                allMatched = true;
            } 

            // If this character isn't in the right position, but does appear
            // in the target word at another position, and all of those other
            // positions haven't been matched, show this character in the hint
            // in lower case.
            if (gLtr != tLtr && targetWord.contains("" + gLtr) && !allMatched){
                hintBuilder.setCharAt(i, Character.toLowerCase(gLtr));
                matched[occurance] = true;
            }
        }

        return hintBuilder.toString();
    }


    /**************************************************************************
     * The main function for managing the game and guesses made by the client.
     * @param message - The message sent to the server by the client
     *************************************************************************/
    private void checkGuess (byte[] message) {
        // Check that the message the client sent adheres to the protocol. If
        // not, drop the client.
        if (!ProtocolHandler.isValidProtocolMessage(message)) {
            Utils.info(
                "Dropping Client " 
                + cAddress + 
                " due to sending a message that wasn't protocol compliant."
            );
            this.gameActive = false;
            closeClient();
            return;
        }

        // Once we know we've got a valid protocol message, convert it to an
        // uppercase string for use in other functions.
        String guess = ProtocolHandler.decodeMessage(message).toUpperCase();

        // Check that the client made a valid guess
        if (!isValidGuess(guess)) {
            writeMessage(ProtocolHandler.INVALID_GUESS);
            return;
        }

        // A valid guess has been made, increment the guess count.
        this.guessCount++;

        // Check if the user got the correct word
        if (guess.equals(this.targetWord)) {
            writeMessage(this.guessCount.toString());
            writeMessage(ProtocolHandler.END_GAME);
            Utils.info(
                this.cAddress + " correctly guessed the target word after " +
                this.guessCount + " guesses. \n\tDisconnecting client."
            );
            this.gameActive = false;
            closeClient();
            return;
        }

        String hint = generateHint(guess);
        writeMessage(hint);

    }

}
