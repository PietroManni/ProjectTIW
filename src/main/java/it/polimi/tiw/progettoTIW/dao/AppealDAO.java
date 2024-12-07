package it.polimi.tiw.progettoTIW.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.progettoTIW.beans.Appeal;

public class AppealDAO {
	private Connection connection;
	
	public AppealDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Appeal> findAppealsByCourseId(int courseId) throws SQLException {
		List<Appeal> appeals = new ArrayList<Appeal>();
		String query = "SELECT * FROM appeal WHERE courseid = ? ORDER BY date DESC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, courseId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Appeal appeal = new Appeal();
					appeal.setId(result.getInt("id"));
					appeal.setDate(result.getDate("date"));
					appeal.setCourseId(result.getInt("courseid"));
					appeals.add(appeal);
				}
			}
		}
		return appeals;
	}
	
	public List<Appeal> findStudentAppealsByCourseId(int courseId, int studentId) throws SQLException {
		List<Appeal> appeals = new ArrayList<Appeal>();
		String query = "SELECT A.* FROM appeal A JOIN exam E ON A.id = E.appealid WHERE A.courseid = ?  AND E.studentid = ? " // appeals to which student is already subscribed 
					 + "UNION "
					 + "SELECT * FROM appeal A WHERE A.courseid = ? AND A.date >= ? AND A.id NOT IN "
					 + "(SELECT E.appealid FROM exam E WHERE E.studentid = ? AND E.appealid = A.id) "
					 + "ORDER BY date DESC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, courseId);
			pstatement.setInt(2, studentId);
			pstatement.setInt(3, courseId);
			pstatement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
			pstatement.setInt(5, studentId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Appeal appeal = new Appeal();
					appeal.setId(result.getInt("id"));
					appeal.setDate(result.getDate("date"));
					appeal.setCourseId(result.getInt("courseid"));
					appeals.add(appeal);
				}
			}
		}
		return appeals;
	}
	
	public Appeal findAppealById(int appealId) throws SQLException {
		Appeal appeal = null;
		String query = "SELECT * FROM appeal WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, appealId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					appeal = new Appeal();
					appeal.setId(result.getInt("id"));
					appeal.setDate(result.getDate("date"));
					appeal.setCourseId(result.getInt("courseid"));
				}
			}
		}
		return appeal;
	}
	
	public Integer findOwnerTeacherId(int appealId) throws SQLException {
		Integer ownerTeacherId = null;
		String query = "SELECT C.teacherid FROM appeal A JOIN course C ON A.courseid = C.id WHERE A.id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, appealId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					ownerTeacherId = result.getInt("C.teacherid");
				}
			}
		}
		return ownerTeacherId;
	}
}