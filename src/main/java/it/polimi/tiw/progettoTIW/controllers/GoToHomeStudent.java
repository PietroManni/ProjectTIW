package it.polimi.tiw.progettoTIW.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.progettoTIW.beans.Appeal;
import it.polimi.tiw.progettoTIW.beans.Course;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.AppealDAO;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/GoToHomeStudent")
public class GoToHomeStudent extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       

    public GoToHomeStudent() {
        super();
    }
    
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
    	templateResolver.setTemplateMode(TemplateMode.HTML);
    	this.templateEngine = new TemplateEngine();
    	this.templateEngine.setTemplateResolver(templateResolver);
    	templateResolver.setSuffix(".html");
    	
    	connection = ConnectionHandler.getConnection(servletContext);
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// get and parse parameters
		String courseid = null;
		Integer courseId = null;
		
		courseid = StringEscapeUtils.escapeJava(request.getParameter("currentcourse"));
		
		// extract courses for the student from the database
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		CourseDAO courseDAO = new CourseDAO(connection);
		List<Course> courses = null;
		try {
			courses = courseDAO.findCoursesByStudentId(user.getId());
			if (courses == null || courses.isEmpty()) { // student is not subscribed to any course
				String path = "WEB-INF/HomeStudent.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				templateEngine.process(path, ctx, response.getWriter());
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve courses from db");
			return;
		}
		
		// find current course
		Course course = null;
		try {
			if (courseid == null || courseid.isEmpty()) { // no current course, find default
				course = courseDAO.findDefaultCourseForStudent(user.getId());
				if (course == null) {
					response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Default course not found");
					return;
				}
				courseId = course.getId();
			} else {
				courseId = Integer.parseInt(courseid);
				course = courseDAO.findCourseById(courseId);
				if (course == null) {
					response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course not found");
					return;
				}
			}
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve default course from db");
			return;
		}
		
		// check if user is a student subscribed to that course
		try {
			if (!courseDAO.studentIsSubscribedToCourse(user.getId(), course.getId())) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve current course or subscription from db");
			return;
		}
		
		// extract appeals for the current course from the database
		AppealDAO appealDAO = new AppealDAO(connection);
		List<Appeal> appeals = null;
		try {
			appeals = appealDAO.findStudentAppealsByCourseId(courseId, user.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve appeals from db");
			return;
		}
		
		String path = "WEB-INF/HomeStudent.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("currentcourse", courseId);
		ctx.setVariable("courses", courses);
		ctx.setVariable("appeals", appeals);
		templateEngine.process(path, ctx, response.getWriter());
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