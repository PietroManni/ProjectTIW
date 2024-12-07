package it.polimi.tiw.progettoTIW.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.progettoTIW.beans.Course;
import it.polimi.tiw.progettoTIW.beans.ReviewsData;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.dao.ReviewDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/GoToStudentReviews")
public class GoToStudentReviews extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       

    public GoToStudentReviews() {
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
				
		String currentcourse = null;
		currentcourse = StringEscapeUtils.escapeJava(request.getParameter("currentcourse"));
				
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
				
		// check if course with that id exists 
		CourseDAO courseDAO = new CourseDAO(connection);
		Course course = null;
		try {
			course = courseDAO.findCourseById(courseId);
			if (course == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course not found");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve course from db");
			return;
		}
				
		// get all the reviews for that course
		ReviewDAO reviewDAO = new ReviewDAO(connection);
		ReviewsData reviews = null;
		try {
			reviews = reviewDAO.getReviewsForCourse(course.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve reviews from db");
			return;
		}
				
		String path  = "/WEB-INF/StudentsReviewsPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("reviewsdata", reviews);
		ctx.setVariable("course", course);
		if (currentcourse != null && !currentcourse.isEmpty()) {
			ctx.setVariable("currentcourse", currentcourse);
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