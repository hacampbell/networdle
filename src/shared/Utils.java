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

    /**
     * A ultilty function used for printing an error to standard out.
     * @param message - The error message
     */
    public static void error (String message) {
        System.out.println("\n[ERROR] " + message);
        System.out.println();
    }

    /**
     * A utilty function used for printing an error to standard out as well
     * as its associated exceptions error message.
     * @param message - The message to be displayed
     * @param e - The associated exception with the error
     */
    public static void error (String message, Exception e) {
        System.out.println("\n[ERROR] " + message);
        System.out.println("\tThe error was:\n\t\t" + e.getMessage());
        System.out.println();
    }
}
