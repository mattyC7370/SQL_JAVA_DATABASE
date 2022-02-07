package com.maroonags;

import java.util.Vector;

/**
	DATA object for use in conjunction with QUERY object<br>
<br>
	Represents the subject in an SQL query, ie. SELECT * FROM ~DATA~<br>
	Each DATA object can house one modifier attribute determined by enumerated member data_type,<br>
<br>
	COMBO: Allows for a combination of two DATA objects by an SQL JOIN<br>
-		Requires DATA type to be COMBO<br>
-		Requires withSort method to be called with a sort type and column to sort by<br>
<br>
	WHERE: Allows for a filter to be applied to a set of data<br>
-		Requires DATA type to be WHERE<br>
-		Requires at least one conditional to be applied, for which there are multiple methods<br>
-			addCondition, which can be passed a SUB_COND object and a column to apply that condition on, as well as an optional joining operation<br>
-	-			ie. OR would combine two filters as ~filter1~ OR ~filter2~. This is only important in the case multiple filters are applied. Default is AND<br>
-			addCondition can also be passed a COND object, which contains a SUB_COND, column.<br>
-			withConditions / withConnectors, which can be passed a vector of COND or OP objects respectively. When one is used, the other<br>
-	-			MUST be used as well, and withConnectors should be passed a vector of length one less than that of the vector passed into withConditions<br>
<br>
	SHORTEN: Sets a limit on how many rows a query will return<br>
-		Requires DATA type to be SHORTEN<br>
-		Requires a limit value to be set using withLimit.<br>
-	-		A query with a limit of 8 will return 8 or less rows as a result<br>
<br>
	SORTED: Sorts the data before it is returned<br>
-		Requires DATA type to be SORTED<br>
-		Requires a sorting direction to be specified using withSort of the form (direction, col)<br>
-	-		Where col is what the data will be sorted by<br>
<br>
	TABLE: Sets DATA to be the contents of a database view or table<br>
-		Requires DATA type to be Table<br>
-		Requires a table name to be set using withTable<br>
<br>
		This serves as the most basic datatype as all derivations and recursions eventually converge to relationships
		between single Table DATAs<br>


	@author Primary: Benjamin Beauchamp 
	@author benbeauchamp7@gmail.com
	@author Others: Sawyer Cowan, Deepansh Bhatia, Matthew Casavecchia

*/
public class DATA {
	/** Types of sorts for use in SORT */
	public enum sort_type {
		/** Descending order */
		DESC,
		/** Ascending order */
		ASC
	};
	/** Container for information required for a DATA object to sort its contents */
	public class SORT {
		private sort_type type;
		private column col;

		/** Creates a sort object with defined sort order and column to reference
		 * @param type From the sort_type enumeration, defines direction of the sort
		 * @param col Defines the column of data in which the sort direction is applied
		 */
		public SORT(sort_type type, column col) {
			this.type = type;
			this.col = col;
		}

		/** Creates a string that when added to a query will apply the desired sort effect
		 * @return a string that when added to a query will apply the desired sort effect
		 */
		public String toSQL() {
			switch (type) {
				case DESC:
					return " ORDER BY " + col + " DESC";
				case ASC:
					return " ORDER BY " + col + " ASC";
				default:
					return "";
			}
		}
	
		/** Creates a human readable string representing the intended sort
		 * @return a human readable string representing the intended sort
		 */
		public String toString() {
			switch (type) {
				case DESC:
					return " sorted by " + col + " descending";
				case ASC:
					return " sorted by " + col + " ascending";
				default:
					return "";
			}
		}
	}

	/** Container for information required for a DATA object to limit the amount of rows it shows */
	public class SHORTENER {
		private int amount;

		/** Creates a SHORTENER object with a defined number of rows to be shown
		 * @param amt The number of rows to be shown
		 */
		public SHORTENER(int amt) {
			amount = amt;
		}

		/** Returns a string that when added to a query will limit the amount of rows returned
		 * @return a string that when added to a query will limit the amount of rows returned
		 */
		public String toSQL() {
			return " LIMIT " + amount;
		}
	
		/** Returns a human readable string representing the number of rows to be shown
		 * @return a human readable string representing the number of rows to be shown
		 */
		public String toString() {
			return " showing first " + amount + " rows";
		}
	}

	/** Container for information required for a DATA object to filter its results */
	public class COND {
		private SUB_COND sub_condition;
		private column col;

		/** Creates a COND object with a condition and target column in which to apply that condition
		 * @param condition A SUB_COND object defining the filter to be applied
		 * @param col The column in which the filter is to be applied
		 */
		public COND(SUB_COND condition, column col) {
			sub_condition = condition;
			this.col = col;
		}

		/** Returns a string that when added to a query will apply the conditional to the DATA object
		 * @return a string that when added to a query will apply the conditional to the DATA object
		 */
		public String toSQL() {
			return col + " " + sub_condition.toSQL();
		}
	
		/** Returns a human readable string representing the filter to be applied and which column that is targeted
		 * @return a human readable string representing the filter to be applied and which column that is targeted
		 */
		public String toString() {
			return " where " + col + " " + sub_condition;
		}
	}

	/** Logical operators for connecting COND objects */
	public enum op_type {
		/** Logical AND */
		AND,
		/** Logical OR */
		OR
	};
	/** Represents a logical operator to join two COND statements */
	public class OP {
		private op_type type;

		/** Creates an OP object with an operator type
		 * @param type the logical operator OP represents
		 */
		public OP(op_type type) {
			this.type = type;
		}

		/** Returns a string that when added to a query will apply the conditional to the DATA object
		 * @return a string that when added to a query will apply the conditional to the DATA object
		 */
		public String toSQL() {
			switch (type) {
				case AND:
					return "AND";
				case OR:
					return "OR";
				default:
					return "";
			}
		}
	
		/** Returns a human readable string representing the filter to be applied and which column that is targeted
		 * @return a human readable string representing the filter to be applied and which column that is targeted
		 */
		public String toString() {
			switch (type) {
				case AND:
					return "and";
				case OR:
					return "or";
				default:
					return "";
			}
		}
	}

	/** Types representing PSQL JOIN types */
	public enum combination_type {
		/** SQL inner join, where DATA sets without common data are not included after the join */
		INNER,
		/** SQL left outer join, where all of the leftmost (or parent) DATA is included and any rightmost (or child) data that matches is included.
		 * Any leftmost elements with no matching rightmost data has the rightmost data column set to NULL
		 */
		LEFT,
		/** SQL right outer join, where all of the rightmost (or child) DATA is included and any leftmost (or parent) data that matches is included.
		 * Any rightmost elements with no matching leftmost data has the leftmost data column set to NULL
		 */
		RIGHT,
		/** Combines both tables completly, lining up where both DATA sets having matching data but NULL when one table does not match the other */
		FULL
	}

	/**
	 * Contains information required for a join of two DATA objects.
	 * Created exclusively by withCombination
	 */
	private class COMBINATION {
		private combination_type type;
		private DATA data;
		private column col1;
		private column col2;

		/** Creates a COMBINATION object with specified params. Intended to be managed by {@link #DATA(combination_type, DATA, column, column)}
		 * @param t An enumerated combination_type representing the kind of join to be performed
		 * @param d The right side (external) data set to be joined with
		 * @param c1 The left side (parent) column to perform the join on
		 * @param c2 The right side (external) column to perform the join on
		 */
		public COMBINATION(combination_type t, DATA d, column c1, column c2) {
			type = t;
			data = d;
			col1 = c1;
			col2 = c2;
		}

		/** Returns a string that when added to a query will apply the specified join on the parent and external DATA objects
		 * @return a string that when added to a query will apply the specified join on the parent and external DATA objects
		 */
		public String toSQL() {
			String return_str = " JOIN " + data + " ON " + col1 + " = " + col2;
			switch (type) {
				case FULL:
					return_str = " FULL" + return_str;
					break;
				case INNER:
					return_str = " INNER" + return_str;
					break;
				case LEFT:
					return_str = " LEFT" + return_str;
					break;
				case RIGHT:
					return_str = " RIGHT" + return_str;
					break;
				default:
					return "";
			}
	
			return return_str;
		}
	
		/** Returns a human readable string detailing the DATAs to be joined and which columns the join will be performed on, as well as the join type
		 * @return a human readable string detailing the DATAs to be joined and which columns the join will be performed on, as well as the join type
		*/
		public String toString() {
			String return_str = " combined with " + data + " on " + col1 + " = " + col2 + " by";
			switch (type) {
				case FULL:
					return_str += " full outer ";
					break;
				case INNER:
					return_str += " inner ";
					break;
				case LEFT:
					return_str += " left outer ";
					break;
				case RIGHT:
					return_str += " right outer ";
					break;
				default:
					return "";
			}
	
			return return_str + "join";
		}
	}

	/** Defines how the DATA object will be altered */
	public enum data_type {
		/** Placeholder type, for internal use only */
		NONE,
		/** DATA is to be joined with another DATA object */
		COMBO,
		/** DATA is to be filtered by a set of conditionals */
		WHERE,
		/** Only the first /n/ rows of data are to be shown */
		SHORTEN,
		/** DATA is to be sorted according to a column */
		SORTED,
		/** DATA is a table or view from the database */
		TABLE
	};

	private data_type type;
	private DATA innerData;
	private COMBINATION combo;
	private Vector<COND> conditions = new Vector<COND>();
	private Vector<OP> condition_connectors = new Vector<OP>();
	private SHORTENER lim;
	private SORT sort;
	private String tableName;

	/** Creates a DATA object with a defined mutation type
	 * @param type The mutation to be applied to the data (from enumerated data_type)
	 */
	public DATA(data_type type) {
		this.type = type;
		this.innerData = null;
	}

	/** Adds a modifier to the selected DATA by nesting it inside a new DATA object, allowing for complex, multilayered queries
	 * @param type Type of modifier to apply
	 * @return New DATA object with designated type
	 */
	public DATA addType(data_type type) {
		if (this.type != data_type.NONE) {
			DATA autoCompress = this.compress();
			autoCompress.type = type;
			return autoCompress;
		} else {
			this.type = type;
			return this;
		}
	}

	/**
	 * @param obj Data object to be nested
	 * @return Current data object with obj nested inside
	 */
	private DATA setInner(DATA obj) {
		this.innerData = obj;
		return this;
	}

	/** Compresses the current DATA object
	 * @return A new DATA object with the parent nested inside it
	 */
	public DATA compress() {
		return new DATA(data_type.NONE)
			.setInner(this);
	}

	/**
	 * Removes the outermost modifier by returning the DATA nested inside the current one. Good for undoing a modification
	 * @return The DATA object nested inside the current DATA object
	 */
	public DATA regress() {
		return this.innerData;
	}

	/**
	 * Defines the combination type to apply to the current DATA object<br>
	 * DATA is joined by comparing lhsCol and rhsCol. When both cols have matching values, those
	 * rows of data are combined to form one row of the new DATA<br>
	 * <br>
	 * To be used in conjunction with COMBO
	 * 
	 * @param combo_type The type of join to perform
	 * @param data The right side (external) DATA object to perform the join with
	 * @param lhsCol The left side (parent) column in which to base the join
	 * @param rhsCol The right side (parent) column in which to base the join
	 * @return The DATA object with the combination applied
	 */
	public DATA withCombination(combination_type combo_type, DATA data, column lhsCol, column rhsCol) {
		this.combo = new COMBINATION(combo_type, data, lhsCol, rhsCol);
		return this;
	}

	
	/**
	 * Specifies the filters in which the DATA is to be put through. These filters are defined by COND objects<br>
	 * This method MUST be called in conjunction with {@link #withConnectors(Vector)} to define the linking operations<br>
	 * 
	 * To be used in conjunction with WHERE
	 * 
	 * @param conds COND objects to be applied to DATA
	 * @return DATA object populated with filters
	 */
	public DATA withConditions(Vector<COND> conds) {
		conditions = conds;
		return this;
	}

	/**
	 * Specifies the logical connectors for conditions specified in {@link #withConditions(Vector)} which must be called
	 * in conjunction with this method <br>
	 * 
	 * To be used in conjunction with WHERE
	 * 
	 * @param operations OP objects to connect COND condition objects. If there are N conditions defined, there should be exactly N-1 elements in this parameter
	 * @return DATA object populated with COND connectors
	 */
	public DATA withConnectors(Vector<OP> operations) {
		condition_connectors = operations;
		return this;
	}

	/**
	 * adds a single condition defined by a SUB_COND object and a column to apply the condition to.<br>
	 * If a condition is already applied, this condition will be joined to it with a logical AND<br>
	 * 
	 * To be used in conjunction with WHERE
	 * 
	 * @param condition A SUB_COND object defining the comparison to be performed
	 * @param col The column in which the condition is to be applied
	 * @return DATA object with this condition applied
	 */
	public DATA addCondition(SUB_COND condition, column col) {
		if (conditions.size() > 0) {
			condition_connectors.add(new OP(op_type.AND));
		}
		conditions.add(new COND(condition, col));
		return this;
	}

	/**
	 * adds a single condition defined by a SUB_COND object and a column to apply the condition to.<br>
	 * If a condition is already applied, this condition will be joined to it by logical AND<br>
	 * 
	 * To be used in conjunction with WHERE
	 * 
	 * @param condition A SUB_COND object defining the comparison to be performed
	 * @param col The column in which the condition is to be applied
	 * @param operator The logical operator to use when connecting this condition to a previous one
	 * @return DATA object with this condition applied
	 */
	public DATA addCondition(SUB_COND condition, column col, op_type operator) {
		if (conditions.size() == 0) {
			System.out.println("No conditions to connect, ignoring operator");
		}
		conditions.add(new COND(condition, col));
		condition_connectors.add(new OP(operator));
		return this;
	}

	/**
	 * Adds a single condition defined by a COND object. If a condition is already applied, this condition will be joined to it
	 * by logical AND<br>
	 * 
	 * To be used in conjunction with WHERE
	 * 
	 * @param conditionSet A COND object that defines the condition and column it is applied to
	 * @return DATA object with this condition applied
	 */
	public DATA addCondition(COND conditionSet) {
		if (conditions.size() > 0) {
			condition_connectors.add(new OP(op_type.AND));
		}
		conditions.add(conditionSet);
		return this;
	}

	/**
	 * Adds a single condition defined by a COND object. If a condition is already applied, this condition will be joined to it
	 * by the specified operator<br>
	 * 
	 * To be used in conjunction with WHERE
	 * 
	 * @param conditionSet A COND object that defines the condition and column it is applied to
	 * @param operator The logical operator to use when connecting this condition to a previous one
	 * @return DATA object with this condition applied
	 */
	public DATA addCondition(COND conditionSet, op_type operator) {
		if (conditions.size() == 0) {
			System.out.println("No conditions to connect, ignoring operator");
		}
		conditions.add(conditionSet);
		condition_connectors.add(new OP(operator));
		return this;
	}

	/**
	 * Defines the amount of lines to show when returning a query<br>
	 * 
	 * To be used in conjunction with SHORTEN
	 * 
	 * @param limit The number of entries to show
	 * @return DATA with the limit applied
	 */
	public DATA withLimit(int limit) {
		lim = new SHORTENER(limit);
		return this;
	}

	/**
	 * Defines the sorting direction and column to sort by<br>
	 * 
	 * To be used in conjunction with SORTED
	 * 
	 * @param sort_direction Determines the direction of the sort
	 * @param col The column in which to apply the sort
	 * @return DATA with the sort applied
	 */
	public DATA withSort(sort_type sort_direction, column col) {
		sort = new SORT(sort_direction, col);
		return this;
	} 

	/**
	 * Defines the name of the view or table located in the database this DATA object represents
	 * @param tableName The name of the view or table this DATA object should represent
	 * @return DATA object with the table name set
	 */
	public DATA withTable(String tableName) {
		this.tableName = tableName;
		return this;
	}

	// public Vector<String> getCols {}

	/** Returns a string representing this DATA object and all its mutations in PSQL language
	 * @return a string representing this DATA object and all its mutations in PSQL language
	 */
	public String toSQL() {
		String returnString = "";
		
		// Make a recursive call to innermost nest, so that text grows from the inside out
		if (innerData != null) {
			returnString += innerData.toSQL();
		}

		switch (type) {
			case COMBO:
				returnString += combo.toSQL();
				break;
				
			case WHERE:
				// Chain consecutive filters with their connectors
				returnString += " WHERE ";
				for (int i = 0; i < condition_connectors.size(); i++) {
					returnString += conditions.elementAt(i).toSQL() + " " + condition_connectors.elementAt(i).toSQL() + " ";
				}
				returnString += conditions.elementAt(conditions.size() - 1).toSQL();
				break;

			case SHORTEN:
				returnString += lim.toSQL();
				break;
			case SORTED:
				returnString += sort.toSQL();
				break;
			case TABLE:
				returnString += tableName;
				break;
			default:
				break;
		}

		return returnString;
	}

	/** Returns a human readable string representing this DATA object and all its mutations
	 * @return a human readable string representing this DATA object and all its mutations
	 */
	public String toString() {
		String returnString = "";
		
		// Make a recursive call to innermost nest, so that text grows from the inside out
		if (innerData != null) {
			returnString += innerData;
		}

		switch (type) {
			case COMBO:
				returnString += combo.toString();
				break;
				
			case WHERE:
				// Chain consecutive filters with their connectors
				for (int i = 0; i < condition_connectors.size(); i++) {
					returnString += conditions.elementAt(i) + " " + condition_connectors.elementAt(i);
				}
				returnString += conditions.elementAt(conditions.size() - 1);
				break;

			case SHORTEN:
				returnString += lim.toString();
				break;
			case SORTED:
				returnString += sort;
				break;
			case TABLE:
				returnString += tableName;
				break;
			default:
				break;
		}

		return returnString;
	}
}