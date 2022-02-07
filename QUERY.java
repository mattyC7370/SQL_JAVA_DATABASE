package com.maroonags;

import java.util.Vector;

/**
 * Top level QUERY object that represents a PSQL query.<br>
 * <br>
 * Designed such that a user can customize a query as simple or as complex as they want without knowing anything about SQL or databases.
 * This framework can submit a user's query to the database and receive a response back and allow for intelligent selection of tables, views, and columns,
 * allowing users to select items as they go, rather than looking them up.
 * 
 * @author Primary: Benjamin Beauchamp 
 * @author benbeauchamp7@gmail.com
 * @author Others: Sawyer Cowan, Deepansh Bhatia, Matthew Casavecchia
 */
public class QUERY {

	private DATA data;
	private Vector<SELECTOR> cols;
	private boolean isUnique;

	/** Creates a query object with no columns selected and no data */
	public QUERY() {
		this.data = null;
		isUnique = false;
		cols = new Vector<SELECTOR>();
	}

	/**
	 * Creates a query object with no columns selected and a fresh data object
	 * @param data The user's selected DATA
	 */
	public QUERY(DATA data) {
		this.data = data;
		isUnique = false;
		cols = new Vector<SELECTOR>();
	}

	/**
	 * Creates a query object with columns already selected and a fresh data object
	 * @param data The user's selected DATA
	 * @param cols A vector of SELECTORS, which specify how the column is to be displayed
	 */
	public QUERY(DATA data, Vector<SELECTOR> cols) {
		this.data = data;
		this.cols = cols;
		isUnique = false;
	}

	/**
	 * Adds data to the QUERY
	 * @param data The user's selected DATA
	 * @return The QUERY object with specified DATA
	 */
	public QUERY withData(DATA data) {
		this.data = data;
		return this;
	}

	/**
	 * Determines if the query will return all possible columns
	 * @return if the query will return all possible columns (everything is selected)
	 */
	public boolean isSelectingEverything() { return cols.isEmpty(); }

	/**
	 * Returns the query in sql with no column selectors
	 * @return the query in sql with no column selectors
	 */
	public String queryAllCols() { return "SELECT * FROM " + data.toSQL(); }

	/**
	 * Adds a set of columns to the QUERY
	 * @param cols Vector of SELECTOR objects that the user selected
	 * @return The QUERY object populated with specified columns
	 */
	public QUERY withCols(Vector<SELECTOR> cols) {
		this.cols = cols;
		return this;
	}

	/**
	 * Adds a single column to the QUERY
	 * @param col A single SELECTOR object depicting what data to return
	 * @return The QUERY object with the column added
	 */
	public QUERY addCol(SELECTOR col) {
		cols.add(col);
		return this;
	}
	
	/**
	 * Sets the query to only return unique instances of the column set (if true)
	 * @param isUnique boolean value determining if output should be unique or not
	 * @return The QUERY object with the uniqueness value set
	 */
	public QUERY withUnique(boolean isUnique) {
		this.isUnique = isUnique;
		return this;
	}

	/** Returns the query as a string in SQL form
	 * @return the query as a string in SQL form
	 */
	public String toSQL() {
		if (cols.size() == 0) {
			return "SELECT * FROM " + data.toSQL();
		} else {
			String ret = "SELECT ";
			if (isUnique) { ret += "DISTINCT "; }
			// ret += "(";
			for (int i = 0; i < cols.size() - 1; i++) {
				ret = ret + cols.elementAt(i).toSQL() + ", ";
			}
			ret += cols.elementAt(cols.size() - 1).toSQL();
			ret += " FROM " + data.toSQL();
			return ret;
		}
	}

	/** Returns a human readable string representing the built query
	 * @return a human readable string representing the built query
	 */
	public String toString() {
		if (cols.size() == 0) {
			return "From [" + data + "] display everything";
		} else {
			String ret = "From [" + data + "] display ";
			if (isUnique) { ret += "unqiue "; }
			ret += "(";
			for (int i = 0; i < cols.size() - 1; i++) {
				ret = ret + cols.elementAt(i) + ", ";
			}
			ret += cols.elementAt(cols.size() - 1);
			ret += ")";
			return ret;
		}
	}
}