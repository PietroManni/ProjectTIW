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

import it.polimi.tiw.progettoTIW.beans.EvaluationStatus;
import it.polimi.tiw.progettoTIW.beans.Exam;
import it.polimi.tiw.progettoTIW.beans.MarkStatus;
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.ExamDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/RefuseMark")
public class RefuseMark extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       

    public RefuseMark() {
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
		Integer examId = null;
		
		
		examid = StringEscapeUtils.escapeJava(request.getParameter("examid"));
		if (examid == null || examid.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
			return;
		}
		try {
			examId = Integer.parseInt(examid);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
		
		// check if exam exists and if the user did that exam
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		ExamDAO examDAO = new ExamDAO(connection);
		Exam exam = null;
		try {
			exam = examDAO.findExamById(examId);
			if (exam == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Exam not found");
				return;
			}
			if (exam.getStudent().getId() != user.getId()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve exam from db");
			return;
		}
		
		// check if exam is PUBLISHED and if MarkStatus == MarkStatus.EVALUATED
		if (exam.getEvaluation().getEvaluationStatus() != EvaluationStatus.PUBLISHED) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Mark cannot be refused");
			return;
		}
		if (exam.getEvaluation().getMarkStatus() != MarkStatus.EVALUATED) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Mark cannot be refused");
			return;
		}
		
		// refuse mark
		try {
			examDAO.refuseMark(examId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't update mark");
			return;
		}
		
		String path = getServletContext().getContextPath() + "/GoToResults?appealid=" + exam.getAppealId();
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