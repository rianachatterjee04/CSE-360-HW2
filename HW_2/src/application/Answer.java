package application;

import java.time.LocalDateTime;
import java.util.UUID;

public class Answer {
    private String id;
    private String questionId;
    private String content;
    private String authorUserName;
    private LocalDateTime createDate;
    private boolean isAccepted;
    
    public Answer(String questionId, String content, String authorUserName) {
        this.id = UUID.randomUUID().toString();
        this.questionId = questionId;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createDate = LocalDateTime.now();
        this.isAccepted = false;
    }
    
    // Constructor for loading from database
    public Answer(String id, String questionId, String content, String authorUserName, 
                 LocalDateTime createDate, boolean isAccepted) {
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createDate = createDate;
        this.isAccepted = isAccepted;
    }
    
    // Getters
    public String getId() { return id; }
    public String getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getAuthorUserName() { return authorUserName; }
    public LocalDateTime getCreateDate() { return createDate; }
    public boolean isAccepted() { return isAccepted; }
    
    // Setters
    public void setContent(String content) { this.content = content; }
    public void setAccepted(boolean accepted) { this.isAccepted = accepted; }
    
    // Validation methods
    public static String validate(String content, String questionId) {
        if (content == null || content.trim().isEmpty()) {
            return "Answer cannot be empty";
        }
        if (content.length() < 10) {
            return "Please answer with more than 10 characters";
        }
        if (!content.matches("^[a-zA-Z0-9\\s.,!?()-]+$")) {
            return "Invalid characters in answer text";
        }
        if (questionId == null || questionId.trim().isEmpty()) {
            return "Answer must be associated with a valid question";
        }
        return "";
    }
}