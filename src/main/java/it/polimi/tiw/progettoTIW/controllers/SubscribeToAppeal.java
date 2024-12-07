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

import it.polimi.tiw.progettoTIW.beans.Appeal;
import it.polimi.tiw.progettoTIW.beans.Course;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.AppealDAO;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.dao.ExamDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/SubscribeToAppeal")
public class SubscribeToAppeal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       

    public SubscribeToAppeal() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// get and parse parameters
		String appealid = null;
		String courseid = null;
		String coursereview = null;
		String vote = null;
		Integer appealId = null;
		Integer courseId = null;
		Integer voteInt = null;
		appealid = StringEscapeUtils.escapeJava(request.getParameter("appealid"));
		courseid = StringEscapeUtils.escapeJava(request.getParameter("courseid"));
		coursereview = StringEscapeUtils.escapeJava(request.getParameter("coursereview"));
		vote = StringEscapeUtils.escapeJava(request.getParameter("vote"));
				
		try {
			if (vote != null && !vote.isEmpty()) {
				voteInt = Integer.parseInt(vote);
				if (voteInt < 1 || voteInt > 5) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameter values");
					return;
				}
			}
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
				
		if (appealid == null || appealid.isEmpty() || courseid == null || courseid.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
			return;
		}
		try {
			appealId = Integer.parseInt(appealid);
			courseId = Integer.parseInt(courseid);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
				
		// check if appeal with that id exists
		AppealDAO appealDAO = new AppealDAO(connection);
		Appeal appeal = null;
		try {
			appeal = appealDAO.findAppealById(appealId);
			if (appeal == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Appeal not found");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve appeal from db");
			return;
		}
				
		// check if course with that id exists and if user is subscribed to that course
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
			if (course.getId() != appeal.getCourseId()) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course doesn't match appeal");
				return;
			}
			if (!courseDAO.studentIsSubscribedToCourse(user.getId(), course.getId())) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve course or subscription from db");
			return;
		}

		// check length of review		
		if (coursereview != null && !coursereview.isEmpty()) { // if string is too long show alert message
			if (coursereview.length() > 255) {
				String path = getServletContext().getContextPath() + "/GoToResults?appealid=" + appeal.getId() + "&alert=3" + "&coursereview=" + coursereview;
				response.sendRedirect(path);
				return;
			}
		}
				
		// subscribe student to appeal
		ExamDAO examDAO = new ExamDAO(connection);
		try {
			if ((coursereview == null || coursereview.isEmpty()) && voteInt == null) { // no vote and no review, use normal DAO method
				examDAO.subscribeStudent(user.getId(), appeal.getId());
			} else {
				examDAO.subscribeStudentAndSaveReviewAndVote(user.getId(), appeal.getId(), coursereview, voteInt, course.getId());
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't create subscription in db");
			return;
		}

		String path = getServletContext().getContextPath() + "/GoToSubscriptionSuccess?appealid=" + appeal.getId();
		response.sendRedirect(path);
	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}