package src.shared;

public class Utils {
    /**
     * A utility function used for printing an error to standard out and then
     * exiting the program.
     * @param message - The message to be displayed before exiting
     */
    public static void errorAndDie (String message) {
        System.out.println("\n[ERROR] " + message);
        System.exit(1);
    }
}
