package it.polimi.tiw.progettoTIW.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.progettoTIW.beans.Review;
import it.polimi.tiw.progettoTIW.beans.ReviewsData;
import it.polimi.tiw.progettoTIW.beans.Student;

public class ReviewDAO {

	private Connection connection;
	
	public ReviewDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void saveReview(String review, int courseId, int studentId) throws SQLException {
		String query = "INSERT INTO review (review, courseid, studentid) VALUES (?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, review);
			pstatement.setInt(2, courseId);
			pstatement.setInt(3, studentId);
			pstatement.executeUpdate();
		}
	}
	
	public void saveVote(Integer vote, int courseId, int studentId) throws SQLException {
		String query = "INSERT INTO review (vote, courseid, studentid) VALUES (?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, vote);
			pstatement.setInt(2, courseId);
			pstatement.setInt(3, studentId);
			pstatement.executeUpdate();
		}
	}
	
	public void saveReviewAndVote(String review, Integer vote, int courseId, int studentId) throws SQLException {
		String query = "INSERT INTO review (review, vote, courseid, studentid) VALUES (?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, review);
			pstatement.setInt(2, vote);
			pstatement.setInt(3, courseId);
			pstatement.setInt(4, studentId);
			pstatement.executeUpdate();
		}
	}
	
	public Review getReviewByStudentIdAndCourseId(int studentId, int courseId) throws SQLException{
		Review review = null;
		String query = "SELECT review, vote FROM review WHERE studentid = ? AND courseid = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			pstatement.setInt(2, courseId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					
					review = new Review();
					review.setReview(result.getString("review"));
					if (result.wasNull()) {
						review.setReview("");
					}
					review.setVote(result.getInt("vote"));
					if (result.wasNull()) {
						review.setVote(0);
					}
				}
			}
		}
		return review;
	}
	
	public ReviewsData getReviewsForCourse(int courseId) throws SQLException {
		ReviewsData reviews = new ReviewsData();
		String query = "SELECT * FROM review R JOIN student S ON R.studentid = S.id WHERE R.courseid = ? ORDER BY R.id DESC"; // DESC so it shows the newer ones (primary key is AI)
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, courseId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Review review = new Review();
					
					Student student = new Student();
					student.setId(result.getInt("S.id"));
					student.setNumber(result.getString("S.number"));
					student.setSurname(result.getString("S.surname"));
					student.setName(result.getString("S.name"));
					student.setEmail(result.getString("S.email"));
					student.setDegree(result.getString("S.degree"));
					
					review.setId(result.getInt("R.id"));
					review.setReview(result.getString("R.review"));
					if (result.wasNull()) {
						review.setReview("");
					}
					review.setVote(result.getInt("R.vote"));
					if (result.wasNull()) {
						review.setVote(0);
					}
					review.setCourseId(result.getInt("R.courseid"));				
					review.setStudent(student);
					
					reviews.getReviews().add(review);
				}
				reviews.setAvgVote(Math.round(getAvgVoteOfCourse(courseId) * 100.0) / 100.0);
			}
		}
		return reviews;
	}
	
	public double getAvgVoteOfCourse(int courseId) throws SQLException {
		double avg = 0;
		String query = "SELECT AVG(vote) AS avgVote FROM review WHERE courseid = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, courseId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					avg = result.getDouble("avgVote");
				}
			}
		}
		return avg;
	}
}