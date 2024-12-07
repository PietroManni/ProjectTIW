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
import it.polimi.tiw.progettoTIW.beans.Exam;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.beans.Verbal;
import it.polimi.tiw.progettoTIW.dao.AppealDAO;
import it.polimi.tiw.progettoTIW.dao.CourseDAO;
import it.polimi.tiw.progettoTIW.dao.ExamDAO;
import it.polimi.tiw.progettoTIW.dao.VerbalDAO;
import it.polimi.tiw.progettoTIW.exceptions.OrderParameterException;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;
import it.polimi.tiw.progettoTIW.utils.OrderParser;


@WebServlet("/GoToAppeal")
public class GoToAppeal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       

    public GoToAppeal() {
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
		String appealid = null;
		Integer appealId = null;
		
		String order = null;
		String columnindex = null;
		Integer columnIndex = null;
		
		String alertstring = null;
		Integer alert = null;
		
		appealid = StringEscapeUtils.escapeJava(request.getParameter("appealid"));
		
		order = StringEscapeUtils.escapeJava(request.getParameter("order"));
		columnindex = StringEscapeUtils.escapeJava(request.getParameter("columnindex"));
		
		alertstring = StringEscapeUtils.escapeJava(request.getParameter("alert"));
		try {
			if (alertstring != null && !alertstring.isEmpty()) {
				alert = Integer.parseInt(alertstring);
				} else {
					alert = 0; // default no alert
				}
			} catch (NumberFormatException | NullPointerException e) {
				alert = 0;
			}
		if (appealid == null || appealid.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
			return;
		}
		try {
			if (columnindex == null || columnindex.isEmpty()) { // default column index
				columnIndex = 1;
				} else {
				columnIndex = Integer.parseInt(columnindex);
				if (columnIndex < 1 || columnIndex > 7) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect parameter values");
					return;
				}
			}
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
		if (order == null || order.isEmpty()) { // default order string
			order = "0000000";
		}
		int orderInt[] = OrderParser.parseOrderString(order);
		if (orderInt == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
		try {
			appealId = Integer.parseInt(appealid);
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
		
		
		// check if user is owner teacher of the course of the appeal
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		Integer ownerTeacherId = null;
		try {
			ownerTeacherId = appealDAO.findOwnerTeacherId(appealId);
			if (ownerTeacherId == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Owner teacher not found");
				return;
			}
			if (ownerTeacherId != user.getId()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve owner teacher from db");
			return;
		}
		
		// get course of the appeal
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
		
		// get exams for that appeal
		ExamDAO examDAO = new ExamDAO(connection);
		List<Exam> exams = null;
		try {
			exams = examDAO.findExamsByAppealId(appealId, columnIndex, orderInt[columnIndex - 1]);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve exams from db");
			return;
		}
		
		// get current user or find default one if not present
		String currentstudentid = null;
		Integer currentStudentId = null;
		currentstudentid = StringEscapeUtils.escapeJava(request.getParameter("currentstudent"));
		if (currentstudentid == null || currentstudentid.isEmpty()) { // no current student, find default
			if (!exams.isEmpty()) {
				currentStudentId = exams.get(0).getStudent().getId();
			}
		} else {
			try {
				currentStudentId = Integer.parseInt(currentstudentid);
			} catch (NumberFormatException | NullPointerException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
				return;
			}
		}
		
		// find all the already created verbals for that appeal
		VerbalDAO verbalDAO = new VerbalDAO(connection);
		List<Verbal> verbals = null;
		try {
			verbals = verbalDAO.findVerbalsForAppeal(appeal.getId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve verbals from db");
			return;
		}
	
		orderInt[columnIndex - 1] = (orderInt[columnIndex - 1] == 0) ? 1 : 0;
		try {
			order = OrderParser.produceOrderString(orderInt, columnIndex);
		} catch (OrderParameterException e) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, e.getMessage());
			return;
		}

		// go to the template
		String path = "/WEB-INF/AppealPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext);
		ctx.setVariable("course", course);
		ctx.setVariable("appeal", appeal);
		ctx.setVariable("exams", exams);
		ctx.setVariable("verbals", verbals);
		ctx.setVariable("currentstudent", currentStudentId);
		ctx.setVariable("alert", alert);
		ctx.setVariable("order", order);
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