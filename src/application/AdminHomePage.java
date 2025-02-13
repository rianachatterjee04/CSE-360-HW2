package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminHomePage {
    private final DatabaseHelper databaseHelper;
    private final ObservableList<User> users = FXCollections.observableArrayList();
    
    public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    private String generateOTP() {
        int randomPIN = (int)(Math.random()*900000)+100000;
        return String.valueOf(randomPIN);
    }
    
    public void show(Stage primaryStage) {
        TableView<User> tableView = new TableView<>();
        
        // Adding a column for displaying user names
        TableColumn<User, String> userColumn = new TableColumn<>("User");
        userColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserName()));
        userColumn.setPrefWidth(200);
        
        // Adding a column for displaying user roles
        TableColumn<User, String> roleColumn = new TableColumn<>("Roles");
        roleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        roleColumn.setPrefWidth(250);
        
        // Adding a column with buttons to assign multiple roles
        TableColumn<User, Void> actionColumn = new TableColumn<>("Assign Roles");
        actionColumn.setPrefWidth(250);
        actionColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final List<String> roles = Arrays.asList("staff", "instructor", "admin", "user");
            private final HBox buttonBox = new HBox(5);

            {
                for (String role : roles) {
                    Button button = new Button(role);
                    button.setStyle("-fx-background-color: lightblue;");
                    button.setOnAction(event -> toggleRole(getTableView().getItems().get(getIndex()), role));
                    buttonBox.getChildren().add(button);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
        
        // Adding a delete button to remove a user
        TableColumn<User, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setPrefWidth(100);
        deleteColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setStyle("-fx-background-color: lightcoral;");
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    long adminCount = users.stream().filter(u -> u.getRole().contains("admin")).count();
                    
                    if (adminCount <= 1 && user.getRole().contains("admin")) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot delete the last admin.", ButtonType.OK);
                        alert.showAndWait();
                        return;
                    }
                    
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this user?", ButtonType.YES, ButtonType.NO);
                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            deleteUser(user);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        
        
        //this table processes the one time password that is generated for the user 
        TableColumn<User, Void> otpColumn = new TableColumn<>("Set OTP");
        otpColumn.setPrefWidth(100);
        otpColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button otpButton = new Button("Set OTP");
            {
                otpButton.setStyle("-fx-background-color: lightblue;");
                otpButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        String otp = generateOTP();
                        try {
                            databaseHelper.setOneTimePassword(user.getUserName(), otp);
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("OTP Generated");
                            alert.setHeaderText(null);
                            alert.setContentText("One-Time Password for " + user.getUserName() + ": " + otp);
                            alert.showAndWait();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : otpButton);
            }
        });

        

        // Adding all columns to the table
        tableView.getColumns().addAll(userColumn, roleColumn, actionColumn, deleteColumn, otpColumn);
        tableView.setItems(users);
        loadUsers();
        
        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(event -> primaryStage.setScene(new Scene(new VBox(), 800, 400))); // Replace with actual back navigation
        
        VBox layout = new VBox(tableView, backButton);
        layout.setSpacing(10);
        layout.setStyle("-fx-alignment: center;");
        
        Scene scene = new Scene(layout, 1200, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Page");
    }
    
    // Method to load users from the database
    private void loadUsers() {
        users.clear();
        try (ResultSet rs = databaseHelper.getAllUsers()) {
            while (rs.next()) {
                users.add(new User(rs.getString("userName"), "", rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void toggleRole(User user, String role) {
        List<String> roles = Arrays.asList(user.getRole().split(","));
        roles = roles.stream().map(String::trim).filter(r -> !r.isEmpty()).collect(Collectors.toList());

        // Count number of admins before making changes
        long adminCount = users.stream().filter(u -> u.getRole().contains("admin")).count();

        // Prevent removing "admin" role if it's the last admin
        if (role.equals("admin") && roles.contains("admin") && adminCount <= 1) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot remove the last admin!", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // Toggle role
        if (roles.contains(role)) {
            roles.remove(role);
        } else {
            roles.add(role);
        }

        // If no roles are left, delete the user
        if (roles.isEmpty()) {
            databaseHelper.deleteUser(user.getUserName());
            users.remove(user);
            return;
        }

        // Ensure "admin" role remains at the start if assigned
        if (roles.contains("admin")) {
            roles.remove("admin");
            roles.add(0, "admin"); // Keep admin role as the first in the list
        }

        // Update role in database and UI
        String newRole = String.join(",", roles);
        databaseHelper.updateUserRole(user.getUserName(), newRole);
        user.setRole(newRole);
        users.set(users.indexOf(user), user);
    }

    
    // Method to delete a user from the database
    private void deleteUser(User user) {
        databaseHelper.deleteUser(user.getUserName());
        users.remove(user);
    }
}