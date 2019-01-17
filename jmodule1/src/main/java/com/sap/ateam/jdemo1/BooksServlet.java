package com.sap.ateam.jdemo1;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
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
		listBooks(response);
	}

	private void listBooks(HttpServletResponse response) {
		Statement stmt = null;
		String query = "SELECT id, title, author FROM books ORDER BY id";
		Connection conn = getConnection();
		PrintWriter pw = null;

		try {
			response.setContentType("text/html");
			pw = response.getWriter();
			pw.append("<html><body>").println();
			pw.append("<table border=2>").println();
			pw.append("<tr><td>ID</td><td>Title</td><td>Author</td></tr>").println();
		
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
	            int id = rs.getInt("id");
	            String title = rs.getString("title");
	            String author = rs.getString("author");
				pw.append("<tr><td>" + id + "</td>").println();
				pw.append("<td>" + title + "</td>").println();
				pw.append("<td>" + author + "</td></tr>").println();
			}
			
			pw.append("</table><p></p><p></p>").println();
			pw.append("<code>Served by node id " + uuid + "</code>").println();
			
		} catch (SQLException e ) {
			logger.error("listBooks ran into an exception.", e);
		} catch (IOException ioe) {
			logger.error("listBooks ran into an exception.", ioe);
		} finally {
			if (stmt != null) { try { stmt.close(); } catch (SQLException e) { } }
			pw.append("</body></html>").println();
		}
	}

	private Connection getConnection() {
		Connection conn = null;

		try {
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/hanadb");
			if (ds == null) {
				logger.error("Error resolving DataSource context.");
				return null;
			}
			
			conn = ds.getConnection();
			if (!conn.isValid(1000)) {
				logger.error("Connection is invalid.");
				return null;
			}
		} catch (Exception e) {
			logger.error("Error getting connection.", e);
		}
		
		return conn;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		addBook(request, response);
	}
	
	private void addBook(HttpServletRequest request, HttpServletResponse response) {
		PreparedStatement stmt = null;
		String title = request.getParameter("title");
		String author = request.getParameter("author");
		String query = "INSERT INTO BOOKS(title, author) VALUES(?, ?)";
		Connection conn = getConnection();
		
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, title);
			stmt.setString(2, author);
			stmt.execute();
		} catch (SQLException e ) {
			logger.error("addBook ran into an exception.", e);
		} finally {
			if (stmt != null) { try { stmt.close(); } catch (SQLException e) { } }
		}
	}

	@SuppressWarnings("unused")
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

	private static String uuid = UUID.randomUUID().toString().substring(0, 6);
	private Logger logger = LoggerFactory.getLogger(BooksServlet.class);
}
