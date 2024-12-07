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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.progettoTIW.beans.Appeal;
import it.polimi.tiw.progettoTIW.beans.Course;
import it.polimi.tiw.progettoTIW.beans.EvaluationStatus;
import it.polimi.tiw.progettoTIW.beans.Exam;
import it.polimi.tiw.progettoTIW.beans.Review;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.AppealDAO;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.dao.ExamDAO;
import it.polimi.tiw.progettoTIW.dao.ReviewDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/GoToResults")
public class GoToResults extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       

    public GoToResults() {
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
		Integer appealId = null;
		Integer alertInt = null;
		String appealid = null;
		String alert = null;
		String coursereview = null;
		appealid = StringEscapeUtils.escapeJava(request.getParameter("appealid"));
		alert = StringEscapeUtils.escapeJava(request.getParameter("alert"));
		coursereview = StringEscapeUtils.escapeJava(request.getParameter("coursereview"));
		try {
			if (alert == null || alert.isEmpty()) {
				alertInt = 0; // default value, no alert
			} else {
				alertInt = Integer.parseInt(alert);
			}
		} catch (NumberFormatException | NullPointerException e) {
			alertInt = 0; // default value, no alert
		}
		try {
			if (appealid == null || appealid.isEmpty()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
				return;
			}
			appealId = Integer.parseInt(appealid);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
		
		// get the appeal from the database
		AppealDAO appealDAO = new AppealDAO(connection);
		Appeal appeal = null;
		try {
			appeal = appealDAO.findAppealById(appealId);
			if (appeal == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Appeal not found");
				return;
			}	
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot retrieve appeal from db");
			return;
		}
		
		// get the course with the course id of appeal
		CourseDAO courseDAO = new CourseDAO(connection);
		Course course = null;
		try {
			course = courseDAO.findCourseById(appeal.getCourseId());
			if (course == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course not found");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot retrieve course from db");
			return;
		}
		
		
		// get the exam from the database
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		ExamDAO examDAO = new ExamDAO(connection);
		Exam exam = null;
		try {
			exam = examDAO.findExamOfAppealByStudentId(user.getId(), appeal.getId());
			if (exam == null) { // student is not subscribed to appeal
				ReviewDAO reviewDAO = new ReviewDAO(connection);
				Review review = null;
				review = reviewDAO.getReviewByStudentIdAndCourseId(user.getId(), course.getId());
				String path = "/WEB-INF/SubscriptionForm.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("appeal", appeal);
				ctx.setVariable("course", course);
				ctx.setVariable("alert", alertInt);
				ctx.setVariable("coursereview", coursereview);
				ctx.setVariable("review", review);
				templateEngine.process(path, ctx, response.getWriter());
				return;
			}
			if (exam.getEvaluation().getEvaluationStatus() == EvaluationStatus.NOT_INSERTED 
					|| exam.getEvaluation().getEvaluationStatus() == EvaluationStatus.INSERTED) {
				exam = null;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve exam or review from db");
			return;
		}
		
		String path = "/WEB-INF/Results.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("exam", exam);
		ctx.setVariable("course", course);
		ctx.setVariable("appeal", appeal);
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