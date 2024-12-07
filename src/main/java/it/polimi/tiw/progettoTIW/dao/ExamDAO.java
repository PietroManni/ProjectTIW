package it.polimi.tiw.progettoTIW.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.polimi.tiw.progettoTIW.beans.Evaluation;
import it.polimi.tiw.progettoTIW.beans.EvaluationStatus;
import it.polimi.tiw.progettoTIW.beans.Student;
import it.polimi.tiw.progettoTIW.exceptions.NoUpdateException;
import it.polimi.tiw.progettoTIW.beans.Exam;
import it.polimi.tiw.progettoTIW.beans.MarkStatus;

public class ExamDAO {
	
	private Connection connection;
	Map<Integer, String> queryMap;
	
	public ExamDAO(Connection connection) {
		this.connection = connection;
		queryMap = Map.of(1, "S.number", 2, "S.surname", 3, "S.name", 4, "S.email", 5, "S.degree", 7, "E.status");
	}
	
	public List<Exam> findExamsByAppealId(int appealId, int columnIndex, int orderBool) throws SQLException {
		List<Exam> exams =  new ArrayList<Exam>();
		String query = null;
		if (columnIndex != 6) {
			query = (orderBool == 0) ? "SELECT * FROM student S JOIN exam E ON S.id = E.studentid WHERE E.appealid = ? ORDER BY " + queryMap.get(columnIndex) + " ASC" 
					: "SELECT * FROM student S JOIN exam E ON S.id = E.studentid WHERE E.appealid = ? ORDER BY " + queryMap.get(columnIndex) + " DESC";
		} else {
			query = (orderBool == 0) ? "SELECT * FROM student S JOIN exam E ON S.id = E.studentid WHERE E.appealid = ? ORDER BY E.markstatus ASC, E.mark ASC, E.laud ASC" 
					: "SELECT * FROM student S JOIN exam E ON S.id = E.studentid WHERE E.appealid = ? ORDER BY E.markstatus DESC, E.mark DESC, E.laud DESC";
		}
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, appealId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Student student = new Student();
					Evaluation evaluation = new Evaluation();
					Exam exam = new Exam();
					exam.setId(result.getInt("E.id"));
					exam.setAppealId(result.getInt("E.appealid"));
					
					student.setId(result.getInt("S.id"));
					student.setNumber(result.getString("S.number"));
					student.setName(result.getString("S.name"));
					student.setSurname(result.getString("S.surname"));
					student.setEmail(result.getString("S.email"));
					student.setDegree(result.getString("S.degree"));
					
					evaluation.setEvaluationStatus(result.getInt("E.status"));
					evaluation.setMarkStatus(result.getInt("E.markstatus"));
					evaluation.setMark(result.getInt("E.mark"));
					evaluation.setLaud(result.getBoolean("E.laud"));
					
					exam.setStudent(student);
					exam.setEvaluation(evaluation);
					exams.add(exam);
				}
			}
		}
		return exams;
	}
	
	public Exam findExamOfAppealByStudentId(int studentId, int appealId) throws SQLException {
		Exam exam = null;
		String query = "SELECT * FROM exam E JOIN student S ON E.studentid = S.id WHERE E.studentid = ? AND E.appealid = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			pstatement.setInt(2, appealId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					exam = new Exam();
					exam.setId(result.getInt("E.id"));
					exam.setAppealId(result.getInt("E.appealid"));
					
					Student student = new Student();
					student.setId(result.getInt("S.id"));
					student.setNumber(result.getString("S.number"));
					student.setName(result.getString("S.name"));
					student.setSurname(result.getString("S.surname"));
					student.setEmail(result.getString("S.email"));
					student.setDegree(result.getString("S.degree"));
					
					Evaluation evaluation = new Evaluation();
					evaluation.setMark(result.getInt("E.mark"));
					evaluation.setLaud(result.getBoolean("E.laud"));
					evaluation.setMarkStatus(result.getInt("E.markstatus"));
					evaluation.setEvaluationStatus(result.getInt("E.status"));
				
					exam.setStudent(student);
					exam.setEvaluation(evaluation);
				}
			}
		}
		return exam;
	}
	
	public Exam findExamById(int examId) throws SQLException {
		Exam exam = null;
		String query = "SELECT * FROM exam WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, examId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					
					exam = new Exam();
					exam.setId(result.getInt("id"));
					exam.setAppealId(result.getInt("appealid"));
					
					Student student = new Student();
					student.setId(result.getInt("studentid"));
					
					Evaluation evaluation =  new Evaluation();
					evaluation.setEvaluationStatus(result.getInt("status"));
					evaluation.setMarkStatus(result.getInt("markstatus"));
					evaluation.setMark(result.getInt("mark"));
					evaluation.setLaud(result.getBoolean("laud"));
					
					exam.setStudent(student);
					exam.setEvaluation(evaluation);
				}
			}
		}
		return exam;
	}
	
	public void subscribeStudent(int studentId, int appealId) throws SQLException {
		String query = "INSERT INTO exam (appealid, studentid) VALUES(?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, appealId);
			pstatement.setInt(2, studentId);
			pstatement.executeUpdate();
		}
	}
	
	public void subscribeStudentAndSaveReviewAndVote(int studentId, int appealId, String review, Integer vote, int courseId) throws SQLException {
		String query = "INSERT INTO exam (appealid, studentid) VALUES(?, ?)";
		connection.setAutoCommit(false);
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, appealId);
			pstatement.setInt(2, studentId);
			pstatement.executeUpdate();
			
			if (review != null && !review.isEmpty() && vote != null) { // both vote and review present
				ReviewDAO reviewDAO = new ReviewDAO(connection);
				reviewDAO.saveReviewAndVote(review, vote, courseId, studentId);
			} else if (review != null && !review.isEmpty() && vote == null) { // there is only review
				ReviewDAO reviewDAO = new ReviewDAO(connection);
				reviewDAO.saveReview(review, courseId, studentId);
			} else if (vote != null && (review == null || review.isEmpty())) { // there is only vote
				ReviewDAO reviewDAO = new ReviewDAO(connection);
				reviewDAO.saveVote(vote, courseId, studentId);
			} // if no vote and review are present, skip.
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}
	
	public Exam findExamAndStudentDataByExamId(int examId) throws SQLException {
		Exam exam = null;
		String query = "SELECT * FROM exam E JOIN student S ON E.studentid = S.id WHERE E.id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, examId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					
					exam = new Exam();
					exam.setId(result.getInt("E.id"));
					exam.setAppealId(result.getInt("E.appealid"));
					
					Student student = new Student();
					student.setId(result.getInt("S.id"));
					student.setNumber(result.getString("S.number"));
					student.setName(result.getString("S.name"));
					student.setSurname(result.getString("S.surname"));
					student.setEmail(result.getString("S.email"));
					student.setDegree(result.getString("S.degree"));
					
					Evaluation evaluation = new Evaluation();
					evaluation.setEvaluationStatus(result.getInt("E.status"));
					evaluation.setMarkStatus(result.getInt("E.markstatus"));
					evaluation.setMark(result.getInt("E.mark"));
					evaluation.setLaud(result.getBoolean("E.laud"));
					
					exam.setStudent(student);
					exam.setEvaluation(evaluation);
				}
			}
		}
		return exam;
	}
	
	public void refuseMark(int examId) throws SQLException {
		String query = "UPDATE exam SET status = " + EvaluationStatus.REFUSED.getValue() + ", markstatus = " + MarkStatus.DEFERRED.getValue() + " WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, examId);
			pstatement.executeUpdate();
		}
	}
	
	public void setEvaluation(Integer examId, Evaluation evaluation) throws SQLException {
		String query = (evaluation.getMarkStatus() == MarkStatus.EVALUATED) ? "UPDATE exam SET status = ?, markstatus = ?, mark = ?, laud = ? WHERE id = ?" 
				: "UPDATE exam SET status = ?, markstatus = ? WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, evaluation.getEvaluationStatus().getValue());
			pstatement.setInt(2, evaluation.getMarkStatus().getValue());
			if (evaluation.getMarkStatus() != MarkStatus.EVALUATED) {
				pstatement.setInt(3, examId);
			} else if (evaluation.getMarkStatus() == MarkStatus.EVALUATED) {
				pstatement.setInt(3, evaluation.getMark());
				pstatement.setBoolean(4, evaluation.getLaud());
				pstatement.setInt(5, examId);
			}
			pstatement.executeUpdate();
		}
	}
	
	public void publishMarks(int appealId) throws SQLException, NoUpdateException {
		String query = "UPDATE exam SET status = ? WHERE appealid = ? AND status = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, EvaluationStatus.PUBLISHED.getValue());
			pstatement.setInt(2, appealId);
			pstatement.setInt(3, EvaluationStatus.INSERTED.getValue());
			int updatedRows = pstatement.executeUpdate();
			if (updatedRows == 0) {
				throw new NoUpdateException("2");
			}
		}
	}
	
	public int verbalizeMarks(int appealId) throws SQLException, NoUpdateException {
		String query = "UPDATE exam SET status = ?, verbalid = ? WHERE appealid = ? AND (status = ? OR status = ?)";
		connection.setAutoCommit(false);
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			VerbalDAO verbalDAO = new VerbalDAO(connection);
			int verbalId = verbalDAO.createVerbal(appealId);
			pstatement.setInt(1, EvaluationStatus.VERBALIZED.getValue());
			pstatement.setInt(2, verbalId);
			pstatement.setInt(3, appealId);
			pstatement.setInt(4, EvaluationStatus.PUBLISHED.getValue());
			pstatement.setInt(5, EvaluationStatus.REFUSED.getValue());
			int updatedRows = pstatement.executeUpdate();
			if (updatedRows == 0) {
				connection.rollback();
				throw new NoUpdateException("1");
			}
 			connection.commit();
			return verbalId;
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}
	
	public List<Exam> findExamsByVerbalId(int verbalId) throws SQLException {
		List<Exam> exams = new ArrayList<Exam>();
		String query = "SELECT * FROM exam E JOIN student S ON E.studentid = S.id WHERE E.status = ? AND E.verbalid = ? ORDER BY E.markstatus DESC, E.mark DESC, E.laud DESC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, EvaluationStatus.VERBALIZED.getValue());
			pstatement.setInt(2, verbalId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Exam exam = new Exam();
					exam.setId(result.getInt("E.id"));
					exam.setAppealId(result.getInt("E.appealid"));
					
					Student student = new Student();
					student.setId(result.getInt("S.id"));
					student.setNumber(result.getString("S.number"));
					student.setName(result.getString("S.name"));
					student.setSurname(result.getString("S.surname"));
					student.setEmail(result.getString("S.email"));
					student.setDegree(result.getString("S.degree"));
					
					Evaluation evaluation = new Evaluation();
					evaluation.setEvaluationStatus(result.getInt("E.status"));
					evaluation.setMarkStatus(result.getInt("E.markstatus"));
					evaluation.setMark(result.getInt("E.mark"));
					evaluation.setLaud(result.getBoolean("E.laud"));
					
					exam.setStudent(student);
					exam.setEvaluation(evaluation);
					
					exams.add(exam);
				}
			}
		}
		return exams;	
	}
}