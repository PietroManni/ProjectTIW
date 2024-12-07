package it.polimi.tiw.progettoTIW.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.progettoTIW.beans.User;

@WebFilter("/TeacherChecker")
public class TeacherChecker implements Filter {
       
    public TeacherChecker() {
        super();
    }

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		System.out.print("Teacher Checker Filter executing...\n");
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/login.html";
		
		HttpSession session = req.getSession();
		User user = null;
		user = (User) session.getAttribute("user");
		if (!user.getRole().equals("teacher")) {
			res.sendRedirect(loginpath);
			return;
		}

		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {
	}
}