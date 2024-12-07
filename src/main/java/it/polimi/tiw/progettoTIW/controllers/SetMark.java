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

import it.polimi.tiw.progettoTIW.beans.Evaluation;
import it.polimi.tiw.progettoTIW.beans.EvaluationStatus;
import it.polimi.tiw.progettoTIW.beans.Exam;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.AppealDAO;
import it.polimi.tiw.progettoTIW.dao.ExamDAO;
import it.polimi.tiw.progettoTIW.exceptions.InvalidMarkFormatException;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;
import it.polimi.tiw.progettoTIW.utils.EvaluationParser;


@WebServlet("/SetMark")
public class SetMark extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       

    public SetMark() {
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
		String examid = null;
		String mark = null;
		Integer examId = null;
		Integer markInt = null;
		examid = StringEscapeUtils.escapeJava(request.getParameter("examid"));
		mark = StringEscapeUtils.escapeJava(request.getParameter("mark"));
		if (examid == null || examid.isEmpty() || mark == null || mark.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
			return;
		}
		try {
			examId = Integer.parseInt(examid);
			markInt = Integer.parseInt(mark);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
		
		// check if exam with that id exists
		ExamDAO examDAO = new ExamDAO(connection);
		Exam exam = null;
		try {
			exam = examDAO.findExamById(examId);
			if (exam == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Course not found");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve exam from db");
			return;
		}
		
		// check if user is owner teacher of the course
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		AppealDAO appealDAO = new AppealDAO(connection);
		try {
			Integer ownerTeacherId = appealDAO.findOwnerTeacherId(exam.getAppealId());
			if (ownerTeacherId == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Owner teacher not found");
				return;
			}
			if (ownerTeacherId != null && ownerTeacherId != user.getId()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve owner teacher from db");
			return;
		}
		
		// check if exam evaluation status is NOT_INSERTED or INSERTED
		if (exam.getEvaluation().getEvaluationStatus() != EvaluationStatus.NOT_INSERTED 
				&& exam.getEvaluation().getEvaluationStatus() != EvaluationStatus.INSERTED) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Cannot set mark");
			return;
		}
		
		// transform the markInt in a Evaluation
		Evaluation evaluation = new Evaluation();
		try {
			evaluation = EvaluationParser.parseMark(markInt);
		} catch (InvalidMarkFormatException e) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, e.getMessage());
			return;
		}
		
		// set mark in the database
		try {
			examDAO.setEvaluation(examId, evaluation);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't set mark in db");
			return;
		}
		
		String path = getServletContext().getContextPath() + "/GoToAppeal?appealid=" + exam.getAppealId() + "&currentstudent=" + exam.getStudent().getId();
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
