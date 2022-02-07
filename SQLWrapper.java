package com.maroonags;

import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Method of communication between user and database. Designed to establish a connection and send queries to the database and recieve their results
 *
 * @author Tim McGuire, adapted from Robert lightfoot
 * CSCE 315
 * 2020-02-06
 * @author Adaptation by Benjamin Beauchamp
 * 2020-02-28
 */
public class SQLWrapper {
	private boolean connected = false;
	private Connection channel = null;

	// Allows for small-scale predictions of table outputs
	private ResultSet stash = null;

	/**
	 * Connects the SQLWrapper to the database
	 * @return true if connection is successful, false if connection failed
	 */
	public boolean connect() {
		try {
			Credentials cred = new Credentials();
			Class.forName("org.postgresql.Driver");
			channel = DriverManager.getConnection("jdbc:postgresql://csce-315-db.engr.tamu.edu/maroon_ags", cred.user, cred.pswd);

			// Connection was successful, return true
			if (ProjectGlobals.DEBUG) { System.out.println("Connection Successful"); }
			return true;

		} catch (Exception e) {
			if (ProjectGlobals.DEBUG) {
				e.printStackTrace();
				System.out.println(e.getClass().getName()+": "+e.getMessage());
				// System.exit(0);
			}

			// Connection failed, return false
			return false;
		}
	}

	/**
	 * Closes the connection from database to SQLWrapper
	 * @return true if the connection was closed (or was already closed), false if the connection could not be closed
	 */
	public boolean closeConnection() {
		if (!connected) { return true; }

		try {
			channel.close();
			connected = false;
			return true;
		} catch(Exception e) {
			if (ProjectGlobals.DEBUG) { System.out.println("Connection was not closed: "); }
			if (ProjectGlobals.DEBUG) { System.out.println(e); }
			return false;
		}
	}

	public Vector<String> getColumns(QUERY query) {
		Vector<String> columnNames = new Vector<String>();

		// Connect to the database if needed
		if (!connected) { connected = connect(); }

		// Check for successful connection
		if (!connected) {
			if (ProjectGlobals.DEBUG) { System.out.println("There was an error connecting to the database..."); }
			return columnNames;
		}

		try {
			//create a statement object
			Statement stmt = channel.createStatement();

			if (ProjectGlobals.DEBUG) { System.out.println("cols for [" + query.queryAllCols() + "]"); }

			// Submit query to the database and stash the result in case the user requests an 'everything' query (if enabled)
			ResultSet result = stmt.executeQuery(query.queryAllCols());
			if (ProjectGlobals.DO_USE_STASH) { stash = result; }

			ResultSetMetaData resultInfo = result.getMetaData();

			// Record column names into vector
			for (int i = 1; i <= resultInfo.getColumnCount(); i++) {
				columnNames.add(resultInfo.getColumnName(i));
			}

		} catch (Exception e){
			if (ProjectGlobals.DEBUG) { System.out.println(e); }
			if (ProjectGlobals.DEBUG) { System.out.println("Error accessing Database."); }
		}

		return columnNames;

	}

	/**
	 * Given a QUERY object, submits that query to the database and returns a vector of strings containing the results of the query
	 * @param query A QUERY object, specifically containing a .toSQL() function to create a query string
	 * @return A vector of string objects, where each string is a row, and each element is seperated by a comma
	 */
	public Vector<String> submitQuery(QUERY query) {
		// Connect to the database if needed
		if (!connected) { connected = connect(); }

		// Check for successful connection
		if (!connected) {
			if (ProjectGlobals.DEBUG) { System.out.println("There was an error connecting to the database..."); }
			return new Vector<String>();
		}

		try {
			//create a statement object
			Statement stmt = channel.createStatement();

			if (ProjectGlobals.DEBUG) { System.out.println("SUBMIT QUERY: [" + query.toSQL() + "]"); }

			// Submit query to the database or use stashed query
			ResultSet result = null;
			if (stash != null && query.isSelectingEverything()) {
				if (ProjectGlobals.DEBUG) { System.out.println("> Stash used <"); }
				result = stash;
			} else {
				return parseResults(stmt.executeQuery(query.toSQL()));
			}

			// Clear stash, single use
			stash = null;

		} catch (Exception e){
			if (ProjectGlobals.DEBUG) { System.out.println(e); }
			if (ProjectGlobals.DEBUG) { System.out.println("Error accessing Database."); }
		}

		return new Vector<String>();

	}

	/**
	 * Given a query string, submits that query to the database and returns a vector of strings containing the results of the query
	 * @param query A propperly formatted string
	 * @return A vector of string objects, where each string is a row, and each element is seperated by a comma
	 */
	public Vector<String> submitQuery(String query) {

		// Connect to the database if needed
		if (!connected) { connected = connect(); }

		// Check for successful connection
		if (!connected) {
			if (ProjectGlobals.DEBUG) { System.out.println("There was an error connecting to the database..."); }
			return new Vector<String>();
		}

		try {
			//create a statement object
			Statement stmt = channel.createStatement();

			if (ProjectGlobals.DEBUG) { System.out.println("SUBMIT QUERY: [" + query + "]"); }

			return parseResults(stmt.executeQuery(query));

		} catch (Exception e){
			if (ProjectGlobals.DEBUG) { System.out.println(e); }
			if (ProjectGlobals.DEBUG) { System.out.println("Error accessing Database."); }
		}

		return new Vector<String>();
	}

	private Vector<String> parseResults(ResultSet rs) {
		Vector<String> table = new Vector<>();
		// Record output into vector
		try {
			ResultSetMetaData resultInfo = rs.getMetaData();
			while (rs.next()) {
				String line = "";

				for (int i = 1; i <= resultInfo.getColumnCount(); i++) {

					// Case numbers from java.sql.Type
					switch (resultInfo.getColumnType(i)) {
						case -7:
							if (ProjectGlobals.DEBUG) {
								System.out.print(rs.getBoolean(i));
							}
							line += rs.getBoolean(i);
							break;
						case 1:
						case 12:
						case 91:
							if (ProjectGlobals.DEBUG) {
								System.out.print(rs.getString(i));
							}
							line += rs.getString(i);
							break;
						case 4:
							if (ProjectGlobals.DEBUG) {
								System.out.print(rs.getInt(i));
							}
							line += rs.getInt(i);
							break;
						case 8:
							if (ProjectGlobals.DEBUG) {
								System.out.print(rs.getDouble(i));
							}
							line += rs.getDouble(i);
							break;
						default:
							// There was an undefined datatype
							if (ProjectGlobals.DEBUG) {
								System.out.print("<" + resultInfo.getColumnType(i) + ">");
							}
					}

					// Add comma delimiter
					if (ProjectGlobals.DEBUG) {
						System.out.print(",");
					}
					line += ",";
				}

				// Add the line to table return
				if (ProjectGlobals.DEBUG) {
					System.out.println("");
				}
				table.add(line);
			}
		} catch (Exception e){
			if (ProjectGlobals.DEBUG) { System.out.println(e); }
			if (ProjectGlobals.DEBUG) { System.out.println("Error parsing data from database."); }
		}
		return table;
	}

	/**
	 * Finds the names of all tables and views in the database
	 * @return an ArrayList of Strings where each element is the name of a table
	 */
	public ArrayList<String> getTables() {
		ArrayList<String> tableNames = new ArrayList<String>();

		// Connect to the database if needed
		if (!connected) { connected = connect(); }

		// Check for successful connection
		if (!connected) {
			if (ProjectGlobals.DEBUG) { System.out.println("There was an error connecting to the database..."); }
			return tableNames;
		}

		try {
			//create a statement object
			Statement stmt = channel.createStatement();

			if (ProjectGlobals.DEBUG) { System.out.println("Getting all table and view names"); }

			// Submit query to the database
			ResultSet result = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public'");

			// Record table names into ArrayList
			while (result.next()) {
				tableNames.add(result.getString(1));
			}

		} catch (Exception e){
			if (ProjectGlobals.DEBUG) { System.out.println(e); }
			if (ProjectGlobals.DEBUG) { System.out.println("Error accessing Database."); }
		}

		return tableNames;

	}
}