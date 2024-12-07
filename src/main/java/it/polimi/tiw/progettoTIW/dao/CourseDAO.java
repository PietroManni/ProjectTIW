package it.polimi.tiw.progettoTIW.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.progettoTIW.beans.Course;
import it.polimi.tiw.progettoTIW.beans.Teacher;

public class CourseDAO {
	
	private Connection connection;
	
	public CourseDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Course> findCoursesByTeacherId(int teacherId) throws SQLException {
		List<Course> courses = new ArrayList<Course>();
		String query = "SELECT * FROM course WHERE teacherid = ? ORDER BY name DESC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, teacherId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Course course = new Course();
					course.setId(result.getInt("id"));
					course.setName(result.getString("name"));
					course.setDescription(result.getString("description"));
					course.setHours(result.getInt("hours"));
					course.setCfu(result.getInt("cfu"));
					course.setTeacherId(result.getInt("teacherid"));
					courses.add(course);
				}
			}
		}
		return courses;
	}
	
	public Course findDefaultCourseForTeacher(int teacherId) throws SQLException {
		Course course = null;
		String query = "SELECT * FROM course WHERE teacherid = ? ORDER BY name DESC LIMIT 1";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, teacherId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					course = new Course();
					course.setId(result.getInt("id"));
					course.setName(result.getString("name"));
					course.setDescription(result.getString("description"));
					course.setHours(result.getInt("hours"));
					course.setCfu(result.getInt("cfu"));
					course.setTeacherId(result.getInt("teacherid"));
				}
			}
		}
		return course;
	}
	
	public boolean studentIsSubscribedToCourse(int studentId, int courseId) throws SQLException {
		String query = "SELECT studentid FROM subscription WHERE studentid = ? AND courseid = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			pstatement.setInt(2, courseId);
			try (ResultSet result = pstatement.executeQuery();) {
				if(result.isBeforeFirst()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Course findCourseById(int courseId) throws SQLException {
		Course course = null;
		String query = "SELECT * FROM course WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, courseId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					course = new Course();
					course.setId(result.getInt("id"));
					course.setName(result.getString("name"));
					course.setDescription(result.getString("description"));
					course.setHours(result.getInt("hours"));
					course.setCfu(result.getInt("cfu"));
					course.setTeacherId(result.getInt("teacherid"));
				}
			}
		}
		return course;
	}
	
	public Course findDefaultCourseForStudent(int studentId) throws SQLException { //ordered by course name
		Course course = null;
		String query = "SELECT C.id FROM subscription S JOIN course C ON S.courseid = C.id WHERE S.studentid = ? ORDER BY C.name DESC LIMIT 1";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.isBeforeFirst()) {
					result.next();
					course = new Course();
					
					course.setId(result.getInt("C.id"));
				}
			}
		}
		return course;
	}
	
	public List<Course> findCoursesByStudentId(int studentId) throws SQLException {
		List<Course> courses = new ArrayList<Course>();
		String query = "SELECT * FROM course C JOIN subscription S ON  S.courseid = C.id WHERE S.studentid = ? ORDER BY C.name DESC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Course course = new Course();
					course.setId(result.getInt("C.id"));
					course.setName(result.getString("C.name"));
					course.setHours(result.getInt("C.hours"));
					course.setCfu(result.getInt("C.cfu"));
					course.setDescription(result.getString("C.description"));
					course.setTeacherId(result.getInt("C.teacherid"));
					
					courses.add(course);
				}
			}
		}
		return courses;
	}
	
	public List<Course> findAvailableCoursesForStudent(int studentId) throws SQLException {
		List<Course> courses = new ArrayList<Course>();
		String query = "SELECT * FROM course C JOIN teacher T ON C.teacherid = T.id WHERE C.id NOT IN "
					 + "(SELECT S.courseid FROM subscription S WHERE S.studentid = ?) "
					 + "ORDER BY C.name ASC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, studentId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Course course = new Course();
					course.setId(result.getInt("C.id"));
					course.setName(result.getString("C.name"));
					course.setCfu(result.getInt("C.cfu"));
					course.setHours(result.getInt("C.hours"));
					course.setDescription(result.getString("C.description"));
					course.setTeacherId(result.getInt("C.teacherid"));
					
					Teacher teacher = new Teacher();
					teacher.setId(result.getInt("T.id"));
					teacher.setNumber(result.getString("T.number"));
					teacher.setName(result.getString("T.name"));
					teacher.setSurname(result.getString("T.surname"));
					teacher.setEmail(result.getString("T.email"));
					
					course.setTeacher(teacher);
					
					courses.add(course);
				}
			}
		}
		return courses;
	}
}