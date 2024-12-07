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
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.AppealDAO;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.dao.ExamDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/GoToSetMarkForm")
public class GoToSetMarkForm extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       

    public GoToSetMarkForm() {
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
		Integer examId = null;
		Integer studentId = null;
		String examid = null;
		String studentid = null;
		
		examid = StringEscapeUtils.escapeJava(request.getParameter("examid"));
		studentid = StringEscapeUtils.escapeJava(request.getParameter("studentid"));
		if (examid == null || examid.isEmpty() || studentid == null || studentid.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
			return;
		}
		try {
			examId = Integer.parseInt(examid);
			studentId = Integer.parseInt(studentid);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
		
		// check if exam exists
		ExamDAO examDAO = new ExamDAO(connection);
		Exam exam = null;
		try {
			exam = examDAO.findExamAndStudentDataByExamId(examId);
			if (exam == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Exam not found");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve exam from db");
			return;
		}
		
		// check if student id parameter is the same of the exam student
		if (studentId != exam.getStudent().getId()) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "User not allowed");
			return;
		}
		
		// check if user is teacher for the course of the exam
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		AppealDAO appealDAO = new AppealDAO(connection);
		try {
			if (appealDAO.findOwnerTeacherId(exam.getAppealId()) != user.getId()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve teacher id from db");
			return;
		}
		
		// check if exam evaluation is NOT_INSERTED or INSERTED
		if (exam.getEvaluation().getEvaluationStatus() != EvaluationStatus.NOT_INSERTED 
				&& exam.getEvaluation().getEvaluationStatus() != EvaluationStatus.INSERTED) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "User not allowed, mark not modifiable");
			return;
		}
		
		// get appeal date and name of the course
		Appeal appeal = null;
		try {
			appeal = appealDAO.findAppealById(exam.getAppealId());
			if (appeal == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Appeal not found");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve appeal from db");
			return;
		}
		CourseDAO courseDAO = new CourseDAO(connection);
		Course course = null;
		try {
			course = courseDAO.findCourseById(appeal.getCourseId());
			if (course == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course not found");
				return;
			}
			if (course.getTeacherId() != user.getId()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve course from db");
			return;
		}
		
		String path = "WEB-INF/SetMarkForm.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("exam", exam);
		ctx.setVariable("coursename", course.getName());
		ctx.setVariable("courseid", course.getId());
		ctx.setVariable("appealid", appeal.getId());
		ctx.setVariable("date", appeal.getDate());
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