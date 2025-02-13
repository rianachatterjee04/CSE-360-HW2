package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;

    public UserHomePage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10); // 10 is the spacing between elements
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Welcome label
        Label userLabel = new Label("Hello, " + currentUser.getUserName() + "!");
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
     // Questions button
        Button questionsButton = new Button("Questions");
        questionsButton.setOnAction(e -> {
            new QuestionsPage(databaseHelper, currentUser).show(primaryStage);
            System.out.println("Questions button clicked!");
        });

        
        layout.getChildren().addAll(userLabel, questionsButton);
        
        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
    }
}