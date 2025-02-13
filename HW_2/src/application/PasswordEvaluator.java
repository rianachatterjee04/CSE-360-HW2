package application;

public class PasswordEvaluator {
    /**
     * <p> Title: Directed Graph-translated Password Assessor. </p>
     *
     * <p> Description: A demonstration of the mechanical translation of Directed Graph
     * diagram into an executable Java program using the Password Evaluator Directed Graph.
     * The code detailed design is based on a while loop with a cascade of if statements</p>
     *
     * <p> Copyright: Lynn Robert Carter © 2022 </p>
     *
     * @author Lynn Robert Carter
     *
     * @version 0.00        2018-02-22    Initial baseline
     *
     */
    /**********************************************************************************************
     *
     * Result attributes to be used for GUI applications where a detailed error message and a
     * pointer to the character of the error will enhance the user experience.
     *
     */
    public static String passwordErrorMessage = "";
    public static String passwordInput = "";
    public static int passwordIndexofError = -1;
    public static boolean foundUpperCase = false;
    public static boolean foundLowerCase = false;
    public static boolean foundNumericDigit = false;
    public static boolean foundSpecialChar = false;
    public static boolean foundLongEnough = false;
    private static String inputLine = "";
    private static char currentChar;
    private static int currentCharNdx;
    private static boolean running;
    private static int state = 0;
    private static int charCounter = 0;

    public static void FSMState() {
        System.out.println("State: " + state + " CurrentChar: " + currentChar + ", CharCounter: " + charCounter);
    }

    public static String evaluatePassword(String input) {
        passwordErrorMessage = "";
        passwordIndexofError = 0;
        inputLine = input;
        currentCharNdx = 0;
        charCounter = 0;
        
        if (input.isEmpty()) return "*** ERROR *** The password is empty!";
        if (input.length() < 8) return "*** ERROR *** Password must be at least 8 characters long.";
        if (input.contains(" ")) return "*** ERROR *** Password cannot contain spaces.";
        
        currentChar = input.charAt(0);
        passwordInput = input;
        foundUpperCase = false;
        foundLowerCase = false;
        foundNumericDigit = false;
        foundSpecialChar = false;
        foundLongEnough = false;
        running = true;
        state = 0;

        while (running) {
            FSMState();

            if (Character.isUpperCase(currentChar)) {
                foundUpperCase = true;
                state = 1;
            } else if (Character.isLowerCase(currentChar)) {
                foundLowerCase = true;
                state = 2;
            } else if (Character.isDigit(currentChar)) {
                foundNumericDigit = true;
                state = 3;
            } else if ("@#$%&*!?+=^~".indexOf(currentChar) >= 0) {
                foundSpecialChar = true;
                state = 4;
            } else {
                passwordIndexofError = currentCharNdx;
                return "*** ERROR *** An invalid character has been found!";
            }
            
            charCounter++;
            if (charCounter >= 8) {
                foundLongEnough = true;
            }

            currentCharNdx++;
            if (currentCharNdx >= inputLine.length()) {
                running = false;
            } else {
                currentChar = input.charAt(currentCharNdx);
            }
        }

        String errMessage = "";
        if (!foundUpperCase) errMessage += "Uppercase letter missing; ";
        if (!foundLowerCase) errMessage += "Lowercase letter missing; ";
        if (!foundNumericDigit) errMessage += "Numeric digit missing; ";
        if (!foundSpecialChar) errMessage += "Special character missing; ";

        if (errMessage.isEmpty()) return "";

        passwordIndexofError = currentCharNdx;
        return errMessage + "conditions were not satisfied.";
    }
} 