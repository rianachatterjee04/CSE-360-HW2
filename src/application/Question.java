package application;

import java.time.LocalDateTime;
import java.util.UUID;

public class Question {
    private String id;
    private String content;
    private String authorUserName;
    private LocalDateTime createDate;
    private boolean isResolved;
    
    public Question(String content, String authorUserName) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.authorUserName = authorUserName;
        this.createDate = LocalDateTime.now();
        this.isResolved = false;
    }
    
    // Constructor for loading from database
    public Question(String id, String content, String authorUserName, LocalDateTime createDate, boolean isResolved) {
        this.id = id;
        this.content = content;
        this.authorUserName = authorUserName;
        this.createDate = createDate;
        this.isResolved = isResolved;
    }
    
    // Getters
    public String getId() { return id; }
    public String getContent() { return content; }
    public String getAuthorUserName() { return authorUserName; }
    public LocalDateTime getCreateDate() { return createDate; }
    public boolean isResolved() { return isResolved; }
    
    // Setters
    public void setContent(String content) { this.content = content; }
    public void setResolved(boolean resolved) { this.isResolved = resolved; }
    
    // Validation methods
    public static String validate(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Question cannot be empty";
        }
        if (content.length() < 10) {
            return "Please ask a question with more than 10 characters";
        }
        if (!content.matches("^[a-zA-Z0-9\\s.,!?()-]+$")) {
            return "Invalid characters in question text";
        }
        return "";
    }
}