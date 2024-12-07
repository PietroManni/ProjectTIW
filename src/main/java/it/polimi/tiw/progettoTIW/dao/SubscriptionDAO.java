package it.polimi.tiw.progettoTIW.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SubscriptionDAO {
	private Connection connection;
	
	public SubscriptionDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void subscribeStudentToCourse(int studentId, int courseId) throws SQLException {
		String query = "INSERT INTO subscription (courseid, studentid) VALUES (?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, courseId);
			pstatement.setInt(2, studentId);
			pstatement.executeUpdate();
		}
	}
}