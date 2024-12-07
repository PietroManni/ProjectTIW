package it.polimi.tiw.progettoTIW.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.progettoTIW.beans.Course;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.dao.SubscriptionDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/SubscribeToCourse")
public class SubscribeToCourse extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       

    public SubscribeToCourse() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// get and parse parameters
		String courseid = null;
		Integer courseId = null;
		
		courseid = StringEscapeUtils.escapeJava(request.getParameter("courseid"));
		if (courseid == null || courseid.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
			return;
		}
		try {
			courseId = Integer.parseInt(courseid);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
		
		// check if course with that id exists and if student is NOT already subscribed to that course
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		CourseDAO courseDAO = new CourseDAO(connection);
		Course course = null;
		try {
			course = courseDAO.findCourseById(courseId);
			if (course == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course not found");
				return;
			}
			if (courseDAO.studentIsSubscribedToCourse(user.getId(), course.getId())) { // if user is already subscribed
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve course or subscription from db");
			return;
		}
		
		// create subscription
		SubscriptionDAO subscriptionDAO = new SubscriptionDAO(connection);
		try {
			subscriptionDAO.subscribeStudentToCourse(user.getId(), course.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't create subscription in db");
			return;
		}
		
		String path = getServletContext().getContextPath() + "/GoToAvailableCourses?subscriptionsuccess=" + 1 + "&subscribedcourse=" + course.getId();
		response.sendRedirect(path);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
