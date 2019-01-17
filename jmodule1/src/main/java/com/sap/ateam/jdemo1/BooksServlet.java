package com.sap.ateam.jdemo1;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class BooksServlet
 */
@WebServlet("/books")
public class BooksServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			initConnection();
			listBooks(response);
		} catch (Exception e) {
			logger.error("doGet ran into an exception.", e);
		}
	}

	private void listBooks(HttpServletResponse response) throws IOException, SQLException {
		Statement stmt = null;
		String query = "SELECT id, title, author FROM books";

		response.getWriter().append("<html><body><table border=2>").println();
		response.getWriter().append("<tr><td>ID</td><td>Title</td><td>Author</td></tr>").println();
		
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
	            int id = rs.getInt("id");
	            String title = rs.getString("title");
	            String author = rs.getString("author");
				response.getWriter().append("<tr><td>" + id + "</td>").println();
				response.getWriter().append("<td>" + title + "</td>").println();
				response.getWriter().append("<td>" + author + "</td></tr>").println();
			}
		} catch (SQLException e ) {
			logger.error("getData ran into an exception.", e);
		} finally {
			if (stmt != null) { stmt.close(); }
			response.getWriter().append("</table></body></html>").println();
		}
	}

	private void initConnection() throws NamingException, SQLException {
		if (conn == null || !conn.isValid(1000)) {
			Context ctx = new InitialContext();
		
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/hanadb");
			if (ds == null) {
				logger.error("Error resolving DataSource context.");
				return;
			} else {
				logger.info("Successfully got DataSource.");
			}
	
			conn = ds.getConnection();
			if (conn.isValid(1000)) {
				logger.info("Connection is valid!");
			} else {
				logger.error("Connection is invalid.");
				return;
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private String getData(Connection con) throws SQLException {
		Statement stmt = null;
		String name = null;
		String query = "SELECT database_name FROM m_database";
		
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next())
	            name = rs.getString("database_name");
		    } catch (SQLException e ) {
		    	logger.error("getData ran into an exception.", e);
		    } finally {
		        if (stmt != null) { stmt.close(); }
		    }
        return name;
	}


	private Logger logger = LoggerFactory.getLogger(BooksServlet.class);
	private Connection conn = null;
}
