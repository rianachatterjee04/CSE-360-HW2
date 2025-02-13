package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class QuestionsPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private final Questions questionsManager;
    private final Answers answersManager;
    private VBox questionsContainer;
    private Label errorLabel;

    public QuestionsPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
        this.questionsManager = new Questions(databaseHelper);
        this.answersManager = new Answers(databaseHelper);
    }

    public void show(Stage primaryStage) {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: white;");

        // Header section
        HBox headerSection = new HBox(20);
        headerSection.setAlignment(Pos.CENTER_LEFT);
        
        Label headerLabel = new Label("Questions");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        try {
            int unresolvedQuestionsCount = questionsManager.getUnresolvedQuestions(currentUser.getUserName()).size();
            int unreadPotentialAnswersCount = questionsManager.getUnreadPotentialAnswersCount(currentUser.getUserName());
            
            Label unresolvedLabel = new Label(
                String.format("Unresolved Questions: %d | Potential Answers: %d", 
                unresolvedQuestionsCount, unreadPotentialAnswersCount)
            );
            unresolvedLabel.setStyle("-fx-text-fill: #007bff; -fx-cursor: hand;");
            unresolvedLabel.setOnMouseClicked(e -> showUnresolvedQuestionsDialog());
            
            headerSection.getChildren().addAll(headerLabel, unresolvedLabel);
        } catch (SQLException e) {
            headerSection.getChildren().add(headerLabel);
            errorLabel.setText("Error loading unresolved questions: " + e.getMessage());
        }

        
        // Error label
        errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(600);

        // Ask Question Section
        VBox askQuestionBox = createAskQuestionSection();
        
        // Questions Container
        ScrollPane scrollPane = new ScrollPane();
        questionsContainer = new VBox(15);
        questionsContainer.setPadding(new Insets(10));
        questionsContainer.setStyle("-fx-background-color: white;");
        scrollPane.setContent(questionsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");

        // Back button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #f0f0f0;");
        backButton.setOnAction(e -> new UserHomePage(databaseHelper, currentUser).show(primaryStage));

        mainLayout.getChildren().addAll(headerLabel, errorLabel, askQuestionBox, scrollPane, backButton);

        // Load questions
        loadQuestions();

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Questions");
    }
    
    private void showUnresolvedQuestionsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Unresolved Questions");
        dialog.setHeaderText("Your Unresolved Questions");

        VBox questionsContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(questionsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        try {
            List<Question> unresolvedQuestions = questionsManager.getUnresolvedQuestions(currentUser.getUserName());

            if (unresolvedQuestions.isEmpty()) {
                questionsContainer.getChildren().add(new Label("No unresolved questions."));
            } else {
                for (Question question : unresolvedQuestions) {
                    VBox questionCard = createUnresolvedQuestionCard(question);
                    questionsContainer.getChildren().add(questionCard);
                }
            }
        } catch (SQLException e) {
            questionsContainer.getChildren().add(new Label("Error loading unresolved questions: " + e.getMessage()));
        }

        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        dialog.getDialogPane().setContent(scrollPane);

        dialog.showAndWait();
    }

    private VBox createUnresolvedQuestionCard(Question question) throws SQLException {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5px; -fx-padding: 15px;");

        // Question content
        Label contentLabel = new Label(question.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");

        // Potential answers count
        int potentialAnswersCount = answersManager.getAnswersForQuestion(question.getId())
            .stream()
            .filter(answer -> !answer.isAccepted())
            .toList()
            .size();

        Label potentialAnswersLabel = new Label("Potential Answers: " + potentialAnswersCount);
        potentialAnswersLabel.setStyle("-fx-text-fill: #6c757d;");

        card.getChildren().addAll(contentLabel, potentialAnswersLabel);
        return card;
    }


    private VBox createAskQuestionSection() {
        VBox askQuestionBox = new VBox(10);
        askQuestionBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15px; -fx-background-radius: 5px;");

        TextField questionField = new TextField();
        questionField.setPromptText("Type your question here (minimum 10 characters)");
        questionField.setPrefColumnCount(3);
        questionField.setPrefWidth(600);
        
        //related questions
        VBox relatedQuestionsBox = new VBox(5);
        relatedQuestionsBox.setStyle("-fx-background-color: #e9ecef; -fx-padding: 10px; -fx-background-radius: 5px;");
        Label relatedQuestionsTitle = new Label("Potential Related Questions:");
        relatedQuestionsTitle.setStyle("-fx-font-weight: bold;");
        relatedQuestionsBox.getChildren().add(relatedQuestionsTitle);
        relatedQuestionsBox.setVisible(false);
        
        Label sectionErrorLabel = new Label();
        sectionErrorLabel.setStyle("-fx-text-fill: red;");
        sectionErrorLabel.setVisible(false); 
        
        questionField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.trim().length() >= 10) {
                    List<Question> relatedQuestions = questionsManager.findRelatedQuestions(newValue);
                    
                    // Clear previous related questions
                    relatedQuestionsBox.getChildren().removeIf(node -> node instanceof Label && !node.equals(relatedQuestionsTitle));
                    
                    if (!relatedQuestions.isEmpty()) {
                        for (Question relatedQ : relatedQuestions) {
                            Label relatedQLabel = new Label(relatedQ.getContent());
                            relatedQLabel.setStyle("-fx-text-fill: #007bff; -fx-cursor: hand;");
                            relatedQLabel.setOnMouseClicked(e -> {
                                // Optional: Implement action when clicking a related question
                                // For example, open a dialog with the full question details
                            });
                            relatedQuestionsBox.getChildren().add(relatedQLabel);
                        }
                        relatedQuestionsBox.setVisible(true);
                    } else {
                        relatedQuestionsBox.setVisible(false);
                    }
                } else {
                    relatedQuestionsBox.setVisible(false);
                }
            } catch (SQLException e) {
                errorLabel.setText("Error finding related questions: " + e.getMessage());
            }
        });
        
        Button askButton = new Button("Ask Question");
        askButton.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white;");
        
        askButton.setOnAction(e -> {
            try {
                String questionText = questionField.getText().trim();
                
                // This will trigger the validation and show specific error messages
                String validationError = Question.validate(questionText);
                if (!validationError.isEmpty()) {
                    sectionErrorLabel.setText(validationError);
                    sectionErrorLabel.setVisible(true);
                    return;
                }
                
                questionsManager.addQuestion(questionText, currentUser.getUserName());
                questionField.clear();
                sectionErrorLabel.setVisible(false);
                loadQuestions();
                
            } catch (IllegalArgumentException ex) {
                // This will catch specific error messages like "This question already exists"
                sectionErrorLabel.setText(ex.getMessage());
                sectionErrorLabel.setVisible(true);
            } catch (Exception ex) {
                sectionErrorLabel.setText("Error: " + ex.getMessage());
                sectionErrorLabel.setVisible(true);
            }
        });

        
        askQuestionBox.getChildren().addAll(questionField, sectionErrorLabel, askButton);
        return askQuestionBox;
    }
   

    private void loadQuestions() {
        try {
            questionsContainer.getChildren().clear();
            for (Question question : questionsManager.getAllQuestions()) {
                questionsContainer.getChildren().add(createQuestionCard(question));
            }
        } catch (SQLException e) {
            errorLabel.setText("Failed to load questions: " + e.getMessage());
        }
    }

    private VBox createQuestionCard(Question question) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5px; -fx-padding: 15px;");

        // Question header with author info
        HBox authorInfo = new HBox(10);
        Label authorLabel = new Label("Asked by: " + question.getAuthorUserName());
        authorLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");
        authorInfo.getChildren().add(authorLabel);

        // Question content
        Label contentLabel = new Label(question.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");

        // Action buttons
        HBox actionButtons = new HBox(10);
        Button answerButton = new Button("Answer");
        answerButton.setStyle("-fx-background-color: #e9ecef;");
        answerButton.setOnAction(e -> showAnswerDialog(question));

        
        actionButtons.getChildren().add(answerButton);

        if (question.getAuthorUserName().equals(currentUser.getUserName())) {
        	// Edit button
            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-background-color: #ffc107;");
            editButton.setOnAction(e -> showEditQuestionDialog(question));
            actionButtons.getChildren().add(editButton);

            // Delete button
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> {
                try {
                    questionsManager.deleteQuestion(question.getId(), currentUser.getUserName());
                    loadQuestions();
                } catch (SQLException ex) {
                    errorLabel.setText("Error deleting question: " + ex.getMessage());
                }
            });
            actionButtons.getChildren().add(deleteButton);
        }

        // Add all components to card
        card.getChildren().addAll(authorInfo, contentLabel, actionButtons);

        // Load and display answers
        try {
            VBox answersBox = new VBox(10);
            answersBox.setPadding(new Insets(10, 0, 0, 20));
            for (Answer answer : answersManager.getAnswersForQuestion(question.getId())) {
                answersBox.getChildren().add(createAnswerCard(answer, question));
            }
            card.getChildren().add(answersBox);
        } catch (SQLException e) {
            errorLabel.setText("Error loading answers: " + e.getMessage());
        }

        return card;
    }
    
    private void showEditQuestionDialog(Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Question");
        dialog.setHeaderText("Edit your question");

        TextField questionField = new TextField(question.getContent());
        questionField.setPrefWidth(400);

        ButtonType submitButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.getChildren().add(questionField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                String editedQuestion = questionField.getText().trim();
                if (editedQuestion.length() >= 10) {
                    return editedQuestion;
                }
                errorLabel.setText("Question must be at least 10 characters long");
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editedQuestion -> {
            if (editedQuestion != null) {
                try {
                    questionsManager.updateQuestion(question.getId(), editedQuestion, currentUser.getUserName());
                    loadQuestions();
                } catch (SQLException e) {
                    errorLabel.setText("Error updating question: " + e.getMessage());
                }
            }
        });
    }
    

    private VBox createAnswerCard(Answer answer, Question question) {
        VBox answerCard = new VBox(5);
        answerCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10px; -fx-background-radius: 5px;");

        Label authorLabel = new Label("Answered by: " + answer.getAuthorUserName());
        authorLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px;");

        Label contentLabel = new Label(answer.getContent());
        contentLabel.setWrapText(true);

        HBox statusBox = new HBox(10);
        if (answer.isAccepted()) {
            Label acceptedLabel = new Label("✓ Accepted Answer");
            acceptedLabel.setStyle("-fx-text-fill: #28a745;");
            statusBox.getChildren().add(acceptedLabel);
        } else if (question.getAuthorUserName().equals(currentUser.getUserName())) {
            Button acceptButton = new Button("Accept Answer");
            acceptButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            acceptButton.setOnAction(e -> {
                try {
                    answersManager.markAnswerAccepted(answer.getId(), question.getId());
                    loadQuestions();
                } catch (SQLException ex) {
                    errorLabel.setText("Error accepting answer: " + ex.getMessage());
                }
            });
            statusBox.getChildren().add(acceptButton);
        }
        
        if (answer.getAuthorUserName().equals(currentUser.getUserName())) {
            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-background-color: #ffc107;");
            editButton.setOnAction(e -> showEditAnswerDialog(answer, question));
            statusBox.getChildren().add(editButton);
            
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            deleteButton.setOnAction(e -> {
                try {
                    answersManager.deleteAnswer(answer.getId(), currentUser.getUserName());
                    loadQuestions();
                } catch (SQLException ex) {
                    errorLabel.setText("Error deleting answer: " + ex.getMessage());
                }
            });
            statusBox.getChildren().add(deleteButton);
        }

        answerCard.getChildren().addAll(authorLabel, contentLabel, statusBox);
        return answerCard;
    }
    
    private void showEditAnswerDialog(Answer answer, Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Answer");
        dialog.setHeaderText("Edit your answer");

        TextField answerField = new TextField(answer.getContent());
        answerField.setPrefWidth(400);

        ButtonType submitButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Question: " + question.getContent()),
            answerField
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                String editedAnswer = answerField.getText().trim();
                if (editedAnswer.length() >= 10) {
                    return editedAnswer;
                }
                errorLabel.setText("Answer must be at least 10 characters long");
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editedAnswer -> {
            if (editedAnswer != null) {
                try {
                	answersManager.updateAnswer(answer.getId(), editedAnswer, currentUser.getUserName());
                    loadQuestions();
                } catch (SQLException e) {
                    errorLabel.setText("Error updating answer: " + e.getMessage());
                }
            }
        });
    }
    
    

    private void showAnswerDialog(Question question) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Answer Question");
        dialog.setHeaderText("Provide your answer");

        TextField answerField = new TextField();
        answerField.setPromptText("Type your answer here (minimum 10 characters)");
        answerField.setPrefWidth(400);

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Question: " + question.getContent()),
            answerField
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                String answer = answerField.getText().trim();
                if (answer.length() >= 10) {
                    return answer;
                }
                errorLabel.setText("Answer must be at least 10 characters long");
                return null;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(answer -> {
            if (answer != null) {
                try {
                    answersManager.addAnswer(question.getId(), answer, currentUser.getUserName());
                    loadQuestions();
                } catch (SQLException e) {
                    errorLabel.setText("Error submitting answer: " + e.getMessage());
                }
            }
        });
    }
}