package it.polimi.tiw.progettoTIW.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.progettoTIW.beans.Appeal;
import it.polimi.tiw.progettoTIW.beans.Course;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.AppealDAO;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/GoToHomeTeacher")
public class GoToHomeTeacher extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       

    public GoToHomeTeacher() {
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
		
		// extract courses from the database
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		CourseDAO courseDAO = new CourseDAO(connection);
		List<Course> courses = null;
		try {
			courses = courseDAO.findCoursesByTeacherId(user.getId());
			if (courses == null || courses.isEmpty()) {
				String path = "/WEB-INF/HomeTeacher.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("courses", courses);
				templateEngine.process(path, ctx, response.getWriter());
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve courses from db");
			return;
		}
		
		// find current course and check if it exists
		Course course = null;
		try {
			if (courseid == null || courseid.isEmpty()) {
				course = courseDAO.findDefaultCourseForTeacher(user.getId());
				if (course == null) {
					response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Default course not found");
					return;
				} else {
					courseId = course.getId();
				}
			} else {
				courseId = Integer.parseInt(courseid);
				course = courseDAO.findCourseById(courseId);
				if(course == null) { // course with that id doesn't exist
					response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course not found");
					return;
				}
			}
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve course or default course from db");
			return;
		}
			
		// verify that user is owner teacher.
		if (course.getTeacherId() != user.getId()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
			return;
		}
		
		
		// extract appeals for the current course from the database
		AppealDAO appealDAO = new AppealDAO(connection);
		List<Appeal> appeals = null;
		try {
			appeals = appealDAO.findAppealsByCourseId(courseId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve courses from db");
			return;
		}
		
		String path = "/WEB-INF/HomeTeacher.html";
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