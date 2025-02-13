package application;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import databasePart1.DatabaseHelper;

public class Questions {
    private final DatabaseHelper databaseHelper;
    
    public Questions(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        try {
            createQuestionsTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void createQuestionsTable() throws SQLException {
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
            + "id VARCHAR(36) PRIMARY KEY, "
            + "content TEXT NOT NULL, "
            + "authorUserName VARCHAR(255), "
            + "createDate TIMESTAMP, "
            + "isResolved BOOLEAN DEFAULT FALSE, "
            + "FOREIGN KEY (authorUserName) REFERENCES cse360users(userName))";
            
        databaseHelper.getConnection().createStatement().execute(questionsTable);
    }
    
    // Create a new question
    public Question addQuestion(String content, String authorUserName) throws SQLException {
        String validationError = Question.validate(content);
        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError);
        }
        
        // Check for duplicate questions
        if (isDuplicateQuestion(content)) {
            throw new IllegalArgumentException("This question already exists");
        }
        
        Question question = new Question(content, authorUserName);
        String sql = "INSERT INTO questions (id, content, authorUserName, createDate, isResolved) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, question.getId());
            pstmt.setString(2, question.getContent());
            pstmt.setString(3, question.getAuthorUserName());
            pstmt.setTimestamp(4, Timestamp.valueOf(question.getCreateDate()));
            pstmt.setBoolean(5, question.isResolved());
            pstmt.executeUpdate();
        }
        return question;
    }
    
    // Read all questions
    public List<Question> getAllQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions ORDER BY createDate DESC";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                questions.add(new Question(
                    rs.getString("id"),
                    rs.getString("content"),
                    rs.getString("authorUserName"),
                    rs.getTimestamp("createDate").toLocalDateTime(),
                    rs.getBoolean("isResolved")
                ));
            }
        }
        return questions;
    }
    
    // Delete a question
    public void deleteQuestion(String id, String userName) throws SQLException {
        // First check if the user owns the question
        String checkSql = "SELECT authorUserName FROM questions WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(checkSql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("You cannot delete a question that doesn't exist");
                }
                if (!rs.getString("authorUserName").equals(userName)) {
                    throw new IllegalArgumentException("You can only delete your own questions");
                }
            }
        }
        
        String sql = "DELETE FROM questions WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }
    
    public void updateQuestion(String id, String newContent, String authorUserName) throws SQLException {
        // First check if the user owns the question
        String checkSql = "SELECT authorUserName FROM questions WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(checkSql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next() || !rs.getString("authorUserName").equals(authorUserName)) {
                    throw new IllegalArgumentException("You can only update your own questions");
                }
            }
        }
        
        // Validate the new content
        String validationError = Question.validate(newContent);
        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError);
        }
        
        String sql = "UPDATE questions SET content = ? WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, newContent);
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        }
    }
    
    public List<Question> findRelatedQuestions(String questionContent) throws SQLException {
        List<Question> relatedQuestions = new ArrayList<>();
        
        // Split the question into keywords
        String[] keywords = questionContent.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .split("\\s+");
        
        // Build a dynamic SQL query to find related questions
        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT * FROM questions WHERE isResolved = false AND ("
        );
        
        // Add conditions for each keyword
        List<String> conditions = new ArrayList<>();
        for (String keyword : keywords) {
            if (keyword.length() > 2) { // Ignore very short words
                conditions.add("LOWER(content) LIKE ?");
            }
        }
        
        sqlBuilder.append(String.join(" OR ", conditions));
        sqlBuilder.append(") LIMIT 5");
        
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sqlBuilder.toString())) {
            int paramIndex = 1;
            for (String keyword : keywords) {
                if (keyword.length() > 2) {
                    pstmt.setString(paramIndex++, "%" + keyword + "%");
                }
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    relatedQuestions.add(new Question(
                        rs.getString("id"),
                        rs.getString("content"),
                        rs.getString("authorUserName"),
                        rs.getTimestamp("createDate").toLocalDateTime(),
                        rs.getBoolean("isResolved")
                    ));
                }
            }
        }
        
        return relatedQuestions;
    }

    // Add a method to get unresolved questions for a user
    public List<Question> getUnresolvedQuestions(String userName) throws SQLException {
        List<Question> unresolvedQuestions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE authorUserName = ? AND isResolved = false ORDER BY createDate DESC";
        
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    unresolvedQuestions.add(new Question(
                        rs.getString("id"),
                        rs.getString("content"),
                        rs.getString("authorUserName"),
                        rs.getTimestamp("createDate").toLocalDateTime(),
                        rs.getBoolean("isResolved")
                    ));
                }
            }
        }
        
        return unresolvedQuestions;
    }

    // Method to count unread potential answers for unresolved questions
    public int getUnreadPotentialAnswersCount(String userName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM answers a " +
                     "JOIN questions q ON a.questionId = q.id " +
                     "WHERE q.authorUserName = ? AND q.isResolved = false AND a.isAccepted = false";
        
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, userName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    // Helper method to check for duplicate questions
    private boolean isDuplicateQuestion(String content) throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE LOWER(content) = LOWER(?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, content);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}