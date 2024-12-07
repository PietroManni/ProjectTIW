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
import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.AppealDAO;
import it.polimi.tiw.progettoTIW.dao.ExamDAO;
import it.polimi.tiw.progettoTIW.exceptions.NoUpdateException;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/PublishMarks")
public class PublishMarks extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       

    public PublishMarks() {
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
		Integer appealId = null;
		String currentstudent = null;
		
		currentstudent = StringEscapeUtils.escapeJava(request.getParameter("currentstudent"));
		appealid = StringEscapeUtils.escapeJava(request.getParameter("appealid"));
		if (appealid == null || appealid.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
			return;
		}
		try {
			appealId = Integer.parseInt(appealid);
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect parameter values");
			return;
		}
		
		// check if appeal with that id exists and if teacher is owner of the course of the appeal
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		AppealDAO appealDAO = new AppealDAO(connection);
		Appeal appeal = null;
		try {
			appeal = appealDAO.findAppealById(appealId);
			if (appeal == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Appeal not found");
				return;
			}
			Integer ownerTeacherId = appealDAO.findOwnerTeacherId(appeal.getId());
			if (ownerTeacherId == null) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Owner teacher not found");
				return;
			}
			if (ownerTeacherId != user.getId()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not allowed");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve appeal or owner teacher from db");
			return;
		}
		
		// set evaluations to PUBLISHED
		ExamDAO examDAO = new ExamDAO(connection);
		try {
			examDAO.publishMarks(appealId);
		} catch (NoUpdateException e) {
			String path = (currentstudent != null && !currentstudent.isEmpty()) ? getServletContext().getContextPath() + "/GoToAppeal?appealid=" + appeal.getId() + "&alert=" + e.getMessage() + "&currentstudent=" + currentstudent : getServletContext().getContextPath() + "/GoToAppeal?appealid=" + appeal.getId() + "&alert=" + e.getMessage();
			response.sendRedirect(path);
			return;
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't upload marks in db");
			return;
		}
		
		// go back to appeal page
		String path = (currentstudent != null && !currentstudent.isEmpty()) ? getServletContext().getContextPath() + "/GoToAppeal?appealid=" + appealId + "&currentstudent=" + currentstudent : getServletContext().getContextPath() + "/GoToAppeal?appealid=" + appealId;
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
