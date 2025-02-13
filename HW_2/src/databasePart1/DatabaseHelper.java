package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.time.LocalDateTime;

import application.User;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			// statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
	    String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "userName VARCHAR(255) UNIQUE, "
	            + "password VARCHAR(255), "
	            + "role VARCHAR(20), "     //displays the role 
	            + "otp VARCHAR(10))";      // displays one time password
	    statement.execute(userTable);
	    
	    // Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "\"ROLE\" VARCHAR(50), "
	            + "expiration_date TIMESTAMP, "  // adds expiration date for a one time password
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	    

	 // 3. Create questions table (depends on users)
	    String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
	            + "id VARCHAR(36) PRIMARY KEY, "
	            + "content TEXT NOT NULL, "
	            + "authorUserName VARCHAR(255), "
	            + "createDate TIMESTAMP, "
	            + "isResolved BOOLEAN DEFAULT FALSE, "
	            + "FOREIGN KEY (authorUserName) REFERENCES cse360users(userName))";
	    statement.execute(questionsTable);
	    
	    // 4. Create answers table (depends on questions and users)
	    String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
	            + "id VARCHAR(36) PRIMARY KEY, "
	            + "questionId VARCHAR(36) NOT NULL, "
	            + "content TEXT NOT NULL, "
	            + "authorUserName VARCHAR(255), "
	            + "createDate TIMESTAMP, "
	            + "isAccepted BOOLEAN DEFAULT FALSE, "
	            + "FOREIGN KEY (questionId) REFERENCES questions(id), "
	            + "FOREIGN KEY (authorUserName) REFERENCES cse360users(userName))";
	    statement.execute(answersTable);
	    
	    // 5. Create private messages table (depends on questions and users)
	    String privateMessagesTable = "CREATE TABLE IF NOT EXISTS private_messages ("
	            + "id VARCHAR(36) PRIMARY KEY, "
	            + "questionId VARCHAR(36), "
	            + "senderUserName VARCHAR(255), "
	            + "message TEXT, "
	            + "createDate TIMESTAMP, "
	            + "isRead BOOLEAN DEFAULT FALSE, "
	            + "FOREIGN KEY (questionId) REFERENCES questions(id), "
	            + "FOREIGN KEY (senderUserName) REFERENCES cse360users(userName))";
	    statement.execute(privateMessagesTable);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}
	
	public Connection getConnection() {
	    return connection;
	}

	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Validate login with OTP
	public boolean isOneTimePassword(String userName, String password) throws SQLException {
	    String query = "SELECT otp FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            String storedOtp = rs.getString("otp");
	            return storedOtp != null && storedOtp.equals(password);
	        }
	    }
	    return false;
	}

	// Update user's password and clear OTP
	public void updatePassword(String userName, String newPassword) throws SQLException {
	    String query = "UPDATE cse360users SET password = ?, otp = NULL WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    }
	}
	
	// Set a one-time password for a user
	public void setOneTimePassword(String userName, String otp) throws SQLException {
	    String query = "UPDATE cse360users SET otp = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, otp);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    }
	}

	// Check if a user has an OTP set
	public boolean hasOneTimePassword(String userName) throws SQLException {
	    String query = "SELECT otp FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        return rs.next() && rs.getString("otp") != null;
	    }
	}

	// Clear OTP after user resets password
	public void clearOneTimePassword(String userName) throws SQLException {
	    String query = "UPDATE cse360users SET otp = NULL WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        pstmt.executeUpdate();
	    }
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String role) {
	    String code = String.format("%s%d", 
	        UUID.randomUUID().toString().substring(0, 4),
	        (int)(Math.random() * 10000));
	        
	    String query = "INSERT INTO InvitationCodes (code, \"ROLE\", expiration_date, isUsed) VALUES (?, ?, ?, false)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, role);
	        pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
	        pstmt.executeUpdate();
	        System.out.println("Generated code: " + code + " for role: " + role);
	        return code;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	
	// Validates an invitation code to check if it is unused.
	public String validateInvitationCode(String code) {
	    String query = "SELECT \"ROLE\", expiration_date, isUsed FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            boolean isUsed = rs.getBoolean("isUsed");
	            Timestamp expirationDate = rs.getTimestamp("expiration_date");
	            LocalDateTime now = LocalDateTime.now();
	            
	            System.out.println("Found code: " + code);
	            System.out.println("Is used: " + isUsed);
	            System.out.println("Expiration: " + expirationDate);
	            
	            if (isUsed) {
	                System.out.println("Code already used");
	                return null;
	            }
	            
	            if (expirationDate != null && expirationDate.toLocalDateTime().isBefore(now)) {
	                System.out.println("Code expired");
	                return null;
	            }
	            
	            String role = rs.getString("ROLE");
	            markInvitationCodeAsUsed(code);
	            System.out.println("Code valid, role: " + role);
	            return role;
	        }
	        
	        System.out.println("Code not found: " + code);
	        return null;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
//------------------------------
	// Retrieves all users from the database
	public ResultSet getAllUsers() throws SQLException {
	    String query = "SELECT userName, role FROM cse360users";
	    return statement.executeQuery(query);
	}

	// Updates the role of a user in the database
	public void updateUserRole(String userName, String newRole) {
	    String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newRole);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Deletes a user from the database
	public void deleteUser(String userName) {
	    String query = "DELETE FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
//------------------------------


	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}

}