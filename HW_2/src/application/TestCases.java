package application;

public class TestCases {
    public static void main(String[] args) {
        testQuestionCreation();
        testQuestionValidation();
        testAnswerCreation();
        testAnswerValidation();
    }

    private static void testQuestionCreation() {
        System.out.println("--- Testing Question Creation ---");
        String content = "How do I learn Java programming?";
        String author = "testUser";

        Question question = new Question(content, author);

        // Validate question creation
        assert question != null : "Question should not be null";
        assert question.getContent().equals(content) : "Question content should match";
        assert question.getAuthorUserName().equals(author) : "Author should match";
        assert !question.isResolved() : "New question should not be resolved";
        assert question.getId() != null : "Question ID should be generated";

        System.out.println("Question Creation Test Passed!");
    }

    private static void testQuestionValidation() {
        System.out.println("--- Testing Question Validation ---");

        // Test empty question
        String emptyValidation = Question.validate("");
        assert !emptyValidation.isEmpty() : "Empty question should be invalid";

        // Test short question
        String shortValidation = Question.validate("Too short");
        assert !shortValidation.isEmpty() : "Short question should be invalid";

        // Test valid question
        String validQuestion = "This is a valid question about Java programming?";
        String validValidation = Question.validate(validQuestion);
        assert validValidation.isEmpty() : "Valid question should pass validation";

        System.out.println("Question Validation Test Passed!");
    }

    private static void testAnswerCreation() {
        System.out.println("--- Testing Answer Creation ---");
        String questionId = "test-question-id";
        String content = "Here's a detailed answer about Java programming.";
        String author = "testUser";

        Answer answer = new Answer(questionId, content, author);

        // Validate answer creation
        assert answer != null : "Answer should not be null";
        assert answer.getQuestionId().equals(questionId) : "Question ID should match";
        assert answer.getContent().equals(content) : "Answer content should match";
        assert answer.getAuthorUserName().equals(author) : "Author should match";
        assert !answer.isAccepted() : "New answer should not be accepted";
        assert answer.getId() != null : "Answer ID should be generated";

        System.out.println("Answer Creation Test Passed!");
    }

    private static void testAnswerValidation() {
        System.out.println("--- Testing Answer Validation ---");
        String questionId = "test-question-id";

        // Test empty answer
        String emptyValidation = Answer.validate("", questionId);
        assert !emptyValidation.isEmpty() : "Empty answer should be invalid";

        // Test short answer
        String shortValidation = Answer.validate("Too short", questionId);
        assert !shortValidation.isEmpty() : "Short answer should be invalid";

        // Test valid answer
        String validAnswer = "This is a comprehensive and detailed answer to the question.";
        String validValidation = Answer.validate(validAnswer, questionId);
        assert validValidation.isEmpty() : "Valid answer should pass validation";

        System.out.println("Answer Validation Test Passed!");
    }
}