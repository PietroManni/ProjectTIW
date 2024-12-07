package it.polimi.tiw.progettoTIW.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.progettoTIW.beans.User;
import it.polimi.tiw.progettoTIW.dao.UserDAO;
import it.polimi.tiw.progettoTIW.utils.ConnectionHandler;


@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
	
    public CheckLogin() {
        super();
    }
    
    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath();
		String number = null;
		String password = null;
		
		number = StringEscapeUtils.escapeJava(request.getParameter("number"));
		password = StringEscapeUtils.escapeJava(request.getParameter("password"));
		
		if (number == null || number.isEmpty() || password == null || password.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter values");
			return;
		}
		
		UserDAO userDAO = new UserDAO(connection);
		User user = null;
		try {
			user = userDAO.checkCredentials(number, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve user from db");
			return;
		}
		if (user == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
			String path = getServletContext().getContextPath();
			path += (user.getRole().equals("teacher")) ? "/GoToHomeTeacher" : "/GoToHomeStudent";
			response.sendRedirect(path);
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
