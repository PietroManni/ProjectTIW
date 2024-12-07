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

import it.polimi.tiw.progettoTIW.beans.Course;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/GoToAvailableCourses")
public class GoToAvailableCourses extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       

    public GoToAvailableCourses() {
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
		String courseid= null;
		
		String subscribedcourse = null;
		Integer subscribedCourseInt = null;
		
		String subscriptionsuccess = null;
		Integer subscriptionSuccess = 0; // default value, no subscription success
		
		courseid = StringEscapeUtils.escapeJava(request.getParameter("currentcourse"));
		subscriptionsuccess = StringEscapeUtils.escapeJava(request.getParameter("subscriptionsuccess"));
		subscribedcourse = StringEscapeUtils.escapeJava(request.getParameter("subscribedcourse"));
		
		try {
			if (subscriptionsuccess != null && !subscriptionsuccess.isEmpty()) {
				subscriptionSuccess = Integer.parseInt(subscriptionsuccess);
			} else {
				subscriptionSuccess = 0;
			}
			if (subscriptionSuccess != 0 && subscriptionSuccess != 1) {
				subscriptionSuccess = 0; // default value, no subscription success
			}
		} catch (NumberFormatException | NullPointerException e) {
			subscriptionSuccess = 0;
		}
		
		// find list of courses to whom the user is not subscribed yet
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		CourseDAO courseDAO = new CourseDAO(connection);
		List<Course> courses = null;
		try {
			courses = courseDAO.findAvailableCoursesForStudent(user.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve courses from db");
			return;
		}
		
		// check if course with that id exists and if user is subscribed
		Course subscribedCourse = null;
		try {
			if (subscribedcourse != null && !subscribedcourse.isEmpty()) {
				subscribedCourseInt = Integer.parseInt(subscribedcourse);
				subscribedCourse = courseDAO.findCourseById(subscribedCourseInt);
				if (subscribedCourse == null) {
					response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course not found");
					return;
				}
				if (!courseDAO.studentIsSubscribedToCourse(user.getId(), subscribedCourse.getId())) {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
					return;
				}
			}
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve course from db");
			return;
		}
		
		String path = "/WEB-INF/AvailableCourses.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("courses", courses);
		ctx.setVariable("currentcourse", courseid);
		ctx.setVariable("subscriptionsuccess", subscriptionSuccess);
		if (subscribedCourse != null) {
			ctx.setVariable("subscribedcourse", subscribedCourse);
		}
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