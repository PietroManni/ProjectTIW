package it.polimi.tiw.progettoTIW.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.progettoTIW.beans.User;

public class UserDAO {
	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	public User checkCredentials(String username, String password) throws SQLException {
		User user = null;
		String query = "SELECT id, number, role, name, surname FROM user WHERE number = ? AND password = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					user = new User();
					user.setId(result.getInt("id"));
					user.setNumber(result.getString("number"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setRole(result.getString("role"));
				}
			}
		}
		return user;
	}
}