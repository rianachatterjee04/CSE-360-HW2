package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
    private final DatabaseHelper databaseHelper;
    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage) {
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        Button loginButton = new Button("Login");
        
        loginButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String password = passwordField.getText();
            try {
                // Check if user has an OTP first
                if(databaseHelper.hasOneTimePassword(userName)) {
                    // Validate the OTP
                    if(databaseHelper.isOneTimePassword(userName, password)) {
                        showNewPasswordDialog(userName, primaryStage);
                    } else {
                        errorLabel.setText("Invalid one-time password");
                    }
                    return;
                }
                
                // Normal login process
                String role = databaseHelper.getUserRole(userName);
                if(role != null) {
                    User user = new User(userName, password, role);
                    if(databaseHelper.login(user)) {
                        new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                    } else {
                        errorLabel.setText("Error logging in");
                    }
                } else {
                    errorLabel.setText("User account doesn't exist");
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel);
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }

    //new password dialog that checks for a one time password, confirms password, and has the user generate a new one and also checks if they match
    private void showNewPasswordDialog(String userName, Stage primaryStage) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Set New Password");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        newPasswordField.setMaxWidth(250);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setMaxWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button submitButton = new Button("Set New Password");
        submitButton.setOnAction(e -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!newPassword.equals(confirmPassword)) {
                errorLabel.setText("Passwords do not match");
                return;
            }

            String passwordError = PasswordEvaluator.evaluatePassword(newPassword);
            if (!passwordError.isEmpty()) {
                errorLabel.setText(passwordError);
                return;
            }

            try {
                // Update password and clear OTP
                databaseHelper.updatePassword(userName, newPassword);
                databaseHelper.clearOneTimePassword(userName);
                dialogStage.close();

             // Instead of directly going to WelcomeLoginPage, return to the login page
                show(primaryStage); // This will show the login page again
            } catch (SQLException ex) {
                errorLabel.setText("Error setting new password");
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(
            new Label("Please set a new password"),
            newPasswordField,
            confirmPasswordField,
            errorLabel,
            submitButton
        );

        dialogStage.setScene(new Scene(layout, 400, 300));
        dialogStage.show();
    }
} 