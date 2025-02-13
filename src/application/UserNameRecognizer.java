package application;

public class UserNameRecognizer {
    /**
     * <p> Title: FSM-translated UserNameRecognizer. </p>
     * 
     * <p> Description: A demonstration of the mechanical translation of Finite State Machine 
     * diagram into an executable Java program using the UserName Recognizer. The code 
     * detailed design is based on a while loop with a select list</p>
     * 
     * <p> Copyright: Lynn Robert Carter © 2024 </p>
     * 
     * @author Lynn Robert Carter
     * 
     * @version 1.00        2024-09-13    Initial baseline derived from the Even Recognizer
     * @version 1.01        2024-09-17    Correction to address UNChar coding error, improper error
     *                                     message, and improve internal documentation
     * 
     */

    public static String userNameRecognizerErrorMessage = "";
    public static String userNameRecognizerInput = "";
    public static int userNameRecognizerIndexofError = -1;

    public static String checkForValidUserName(String input) {
        if (input.isEmpty()) {
            return "*** ERROR *** The input is empty";
        }
        
        if (input.length() < 4 || input.length() > 16) {
            return "*** ERROR *** Username must be between 4 and 16 characters long.";
        }
        
        if (!Character.isLetter(input.charAt(0))) {
            return "*** ERROR *** Username must start with a letter (A-Z, a-z).";
        }
        
        if (!input.matches("^[A-Za-z][A-Za-z0-9_.-]{2,14}[A-Za-z0-9]$")) {
            return "*** ERROR *** Username can only contain A-Z, a-z, 0-9, _, -, . and cannot end with _, -, .";
        }
        
        return "";
    }
} 