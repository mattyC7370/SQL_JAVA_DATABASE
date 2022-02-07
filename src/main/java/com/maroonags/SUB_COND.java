package com.maroonags;

/**
 * SUB_COND object that represents a condition for a column object to be compared against<br>
 * 
 * The SUB_COND will be called for each element in the column and return true or false based on the condition. If true, the element and its row 
 * will be included in the final DATA, otherwise it and its row will not be included
 * 
 * @author Primary: Benjamin Beauchamp 
 * @author benbeauchamp7@gmail.com
 * @author Others: Sawyer Cowan, Deepansh Bhatia, Matthew Casavecchia
 */
public class SUB_COND {
	/** Contains the different kind of conditions elements can be compared under */
	public enum condition_type {
		/** Check if the element contains a specified string */
		CONTAINS,
		/** Check if the element starts with a specified string */
		STARTS,
		/** Check if the element ends with a specified string */
		ENDS,
		/** Check if the element matches a specified string */
		MATCHES,
		/** Check if the element follows the specified regex patten */
		FOLLOWS,
		/** Check if the element is between two values */
		BETWEEN,
		/** Check if the element exists */
		IS_NULL,
		/** Check if the element is a specified value */
		IS_VAL,
		/** Check if the element is greater than a specified value */
		GREATER,
		/** Check if the element is less than a specified value */
		LESS
	}

	private condition_type type;
	private String str;
	private double compVal;
	private double betweenRHS;
	private double betweenLHS;
	
	/**
	 * Creates a SUB_COND object of specified condition type
	 * @param type The type of SUB_COND
	 */
	public SUB_COND(condition_type type) { 
		this.type = type; 
	}

	/**
	 * Specifies the search string<br>
	 * MUST be called when type is CONTAINS, STARTS, ENDS, MATCHES, or FOLLOWS
	 * @param str The search string
	 * @return SUB_COND object with specified search string
	 */
	public SUB_COND withSearchString(String str) { 
		this.str = str;
		return this;
	}

	/**
	 * Specifies the direct comparison value
	 * MUST be called when type is IS_VAL, GREATER, or LESS
	 * @param val The comparison value
	 * @return SUB_COND object with specified comparison value
	 */
	public SUB_COND withValue(double val) {
		compVal = val;
		return this;
	}

	/**
	 * Specifies values the element must be between
	 * @param lhs The lower bound (left hand side)
	 * @param rhs The upper bound (right hand side)
	 * @return SUB_COND object with specified bounds
	 */
	public SUB_COND withBetweenVals(double lhs, double rhs) {
		betweenLHS = lhs;
		betweenRHS = rhs;
		return this;
	}

	/** Returns an SQL type string representing the comparison
	 * @return an SQL type string representing the comparison
	 */
	public String toSQL() {
		switch (type) {
			case CONTAINS:
				return "LIKE \"%" + str + "%\"";
			case STARTS:
				return "LIKE \"" + str + "%\"";
			case ENDS:
				return "LIKE \"%" + str + "\"";
			case MATCHES:
				return "= \"" + str + "\"";
			case FOLLOWS:
				return "LIKE \"" + str + "\"";
			case BETWEEN:
				return "BETWEEN " + betweenLHS + " AND " + betweenRHS;
			case IS_NULL:
				return "IS NULL";
			case IS_VAL:
				return "= " + compVal;
			case GREATER:
				return "> " + compVal;
			case LESS:
				return "< " + compVal;
			default:
				return "";
		}
	}

	/** Returns a human readable string representing the comparison
	 * @return a human readable string representing the comparison
	 */
	public String toString() {
		switch (type) {
			case CONTAINS:
				return "contains \"" + str + "\"";
			case STARTS:
				return "starts with \"" + str + "\"";
			case ENDS:
				return "ends with \"" + str + "\"";
			case MATCHES:
				return "matches \"" + str + "\"";
			case FOLLOWS:
				return "has the form \"" + str + "\"";
			case BETWEEN:
				return "Is between " + betweenLHS + " and " + betweenRHS;
			case IS_NULL:
				return "is empty";
			case IS_VAL:
				return "is equal to " + compVal;
			case GREATER:
				return "is greater than " + compVal;
			case LESS:
				return "is less than " + compVal;
			default:
				return "";
		}
	}
}