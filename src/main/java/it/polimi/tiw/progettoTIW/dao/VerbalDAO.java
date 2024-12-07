package it.polimi.tiw.progettoTIW.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.progettoTIW.beans.Verbal;

public class VerbalDAO {
	
	private Connection connection;
	
	public VerbalDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Verbal getVerbalById(int verbalId) throws SQLException {
		Verbal verbal = null;
		String query = "SELECT * FROM verbal WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, verbalId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					
					verbal = new Verbal();
					verbal.setId(result.getInt("id"));
					verbal.setDate(result.getTimestamp("date"));
					verbal.setAppealId(result.getInt("appealid"));
				}
			}
		}
		return verbal;
	}
	
	public int createVerbal(int appealId) throws SQLException {
		String query = "INSERT into verbal (appealid, date) VALUES (?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setInt(1, appealId);
			pstatement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
			pstatement.executeUpdate();
			
			ResultSet generatedKeys = pstatement.getGeneratedKeys();
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
			} else {
				throw new SQLException("Creating verbal failed, no ID obtained");
			}
		}
	}
	
	public List<Verbal> findVerbalsForAppeal(int appealId) throws SQLException {
		List<Verbal> verbals = new ArrayList<Verbal>();
		String query = "SELECT * FROM verbal WHERE appealid = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, appealId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Verbal verbal = new Verbal();
					
					verbal.setId(result.getInt("id"));
					verbal.setDate(result.getTimestamp("date"));
					verbal.setAppealId(result.getInt("appealid"));
					
					verbals.add(verbal);
				}
			}
		}
		return verbals;
	}
}