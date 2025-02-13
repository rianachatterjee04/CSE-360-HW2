package application;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import databasePart1.DatabaseHelper;

public class Answers {
    private final DatabaseHelper databaseHelper;
    
    public Answers(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        createAnswersTable();
    }
    
    private void createAnswersTable() {
        try {
            String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
                + "id VARCHAR(36) PRIMARY KEY, "
                + "questionId VARCHAR(36) NOT NULL, "
                + "content TEXT NOT NULL, "
                + "authorUserName VARCHAR(255), "
                + "createDate TIMESTAMP, "
                + "isAccepted BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (questionId) REFERENCES questions(id), "
                + "FOREIGN KEY (authorUserName) REFERENCES cse360users(userName))";
            databaseHelper.getConnection().createStatement().execute(answersTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Create: Add a new answer to the collection
    public Answer addAnswer(String questionId, String content, String authorUserName) throws SQLException {
        String validationError = Answer.validate(content, questionId);
        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError);
        }
        
        // Check for duplicate answers
        if (isDuplicateAnswer(questionId, content)) {
            throw new IllegalArgumentException("Put a different answer");
        }
        
        Answer answer = new Answer(questionId, content, authorUserName);
        String sql = "INSERT INTO answers (id, questionId, content, authorUserName, createDate, isAccepted) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, answer.getId());
            pstmt.setString(2, answer.getQuestionId());
            pstmt.setString(3, answer.getContent());
            pstmt.setString(4, answer.getAuthorUserName());
            pstmt.setTimestamp(5, Timestamp.valueOf(answer.getCreateDate()));
            pstmt.setBoolean(6, answer.isAccepted());
            pstmt.executeUpdate();
        }
        return answer;
    }
    
    // Read: Get all answers for a specific question
    public List<Answer> getAnswersForQuestion(String questionId) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM answers WHERE questionId = ? ORDER BY isAccepted DESC, createDate DESC";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(new Answer(
                        rs.getString("id"),
                        rs.getString("questionId"),
                        rs.getString("content"),
                        rs.getString("authorUserName"),
                        rs.getTimestamp("createDate").toLocalDateTime(),
                        rs.getBoolean("isAccepted")
                    ));
                }
            }
        }
        return answers;
    }
    
    // Read: Get unread answers count for a question
    public int getUnreadAnswersCount(String questionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM answers WHERE questionId = ? AND isAccepted = false";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    // Update: Modify an existing answer
    public void updateAnswer(String id, String content, String authorUserName) throws SQLException {
        // First check if the user owns the answer
        String checkSql = "SELECT authorUserName FROM answers WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(checkSql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next() || !rs.getString("authorUserName").equals(authorUserName)) {
                    throw new IllegalArgumentException("You can only update your own answers");
                }
            }
        }
        
        String sql = "UPDATE answers SET content = ? WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, content);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        }
    }
    
    // Update: Mark an answer as accepted
    public void markAnswerAccepted(String answerId, String questionId) throws SQLException {
        // First, unmark any previously accepted answers for this question
        String unmarkSql = "UPDATE answers SET isAccepted = false WHERE questionId = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(unmarkSql)) {
            pstmt.setString(1, questionId);
            pstmt.executeUpdate();
        }
        
        // Then mark the selected answer as accepted
        String markSql = "UPDATE answers SET isAccepted = true WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(markSql)) {
            pstmt.setString(1, answerId);
            pstmt.executeUpdate();
        }
        
        // Update the question's resolved status
        String updateQuestionSql = "UPDATE questions SET isResolved = true WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(updateQuestionSql)) {
            pstmt.setString(1, questionId);
            pstmt.executeUpdate();
        }
    }
    
    // Delete: Remove an answer
    public void deleteAnswer(String id, String authorUserName) throws SQLException {
        // First check if the user owns the answer
        String checkSql = "SELECT authorUserName FROM answers WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(checkSql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("You cannot delete an answer that doesn't exist");
                }
                if (!rs.getString("authorUserName").equals(authorUserName)) {
                    throw new IllegalArgumentException("You can only delete your own answers");
                }
            }
        }
        
        String sql = "DELETE FROM answers WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }
   
    
    // Helper method to check for duplicate answers
    private boolean isDuplicateAnswer(String questionId, String content) throws SQLException {
        String sql = "SELECT COUNT(*) FROM answers WHERE questionId = ? AND LOWER(content) = LOWER(?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, questionId);
            pstmt.setString(2, content);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}