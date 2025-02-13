# Accessibility ♿
No password will be needed to access this repository as it is public. 

# Project Overview 📂
This project implements a Question and Answer system with robust features for managing questions and answers in a database-driven JavaFX application.

# Key Classes 🔑

## Question Class ❔

The Question class represents a single question in the system.

### Key Attributes: 🔐

id: Unique identifier for the question (UUID)

content: Text of the question

authorUserName: Username of the question creator

createDate: Timestamp of question creation

isResolved: Boolean indicating if the question has been resolved

## Questions Class ⁉️

The Questions class manages database operations for questions.

### Key Methods: 🔐

#### addQuestion(String content, String authorUserName):

Creates a new question

Prevents duplicate questions

Validates question content


#### getAllQuestions(): 

Retrieves all questions from the database

#### findRelatedQuestions(String questionContent):

Searches for similar unresolved questions

Uses keyword matching

Limits results to 5 related questions

## Answer Class 🙋 

The Answer class represents a single answer in the system.

### Key Attributes: 🔐

id: Unique identifier for the answer (UUID)

questionId: ID of the associated question

content: Text of the answer

authorUserName: Username of the answer creator

createDate: Timestamp of answer creation

isAccepted: Boolean indicating if the answer is the accepted solution

## Answers Class ✅

The Answers class manages database operations for answers.

### Key Methods: 🔐

#### addAnswer(String questionId, String content, String authorUserName):

Creates a new answer

Prevents duplicate answers

Validates answer content


#### getAnswersForQuestion(String questionId):

Retrieves all answers for a specific question

Orders by acceptance status and creation date


#### markAnswerAccepted(String answerId, String questionId):

Marks a specific answer as the accepted solution

Unmarks previously accepted answers

Updates question's resolved status

## Validation Constraints

Minimum 10 characters for questions and answers

Prevents special characters

Unique content checks
