package com.maroonags;

/**
 * SELECTOR object used to define a specific column in which a QUERY object may target<br>
 * 
 * A SELECTOR is made up of a column name and (possibly) a modifier which allows for finding a specific point of data.
 * 
 * @author Primary: Benjamin Beauchamp 
 * @author benbeauchamp7@gmail.com
 * @author Others: Sawyer Cowan, Deepansh Bhatia, Matthew Casavecchia
 */
public class SELECTOR {
	/** Types of modifications that can be applied to a column */
	public enum mod_types {
		/** Return the number of entries in this column */
		COUNT,
		/** Return the smallest entry in this column */
		MIN,
		/** Return the largest entry in this column */
		MAX,
		/** Return the sum of the entries in this column */
		SUM,
		/** Return the averages of the entries in this column */
		AVG
	};

	private boolean isModified;
	private column col;
	private mod_types mod;

	/**
	 * Creates a SELECTOR object with no modifications targeted at a column
	 * @param col The target column for the selector
	 */
	public SELECTOR(column col) {
		isModified = false;
		this.col = col;
	}

	/**
	 * Sets the column the SELECTOR object
	 * @param col The column for the SELECTOR to target
	 * @return A SELECTOR with the target column
	 */
	public SELECTOR withCol(column col) {
		this.col = col;
		return this;
	}

	/**
	 * Sets a modification for the SELECTOR object
	 * @param mod The type of modification to be applied
	 * @return SELECTOR object with the modification applied
	 */
	public SELECTOR withMod(mod_types mod) {
		isModified = true;
		this.mod = mod;
		return this;
	}

	/** Returns a string representing the SELECTOR and its modification in SQL form
	 * @return a string representing the SELECTOR and its modification in SQL form
	 */
	public String toSQL() {
		if (isModified) {
			switch (mod) {
				case COUNT:
					return "COUNT(" + col + ")";
				case MAX:
					return "MAX(" + col + ")";
				case MIN:
					return "AVERAGE(" + col + ")";
				case SUM:
					return "SUM(" + col + ")";
				case AVG:
					return "AVG(" + col + ")";
				default:
					return "";
			}
		} else {
			return col.toString();
		}
	}

	/** Returns a human readable string representing the SELECTOR and its modification
	 * @return a human readable string representing the SELECTOR and its modification
	 */
	public String toString() {
		if (isModified) {
			switch (mod) {
				case COUNT:
					return "Number of " + col;
				case MAX:
					return "Max of " + col;
				case MIN:
					return "Min of " + col;
				case SUM:
					return "Sum of " + col;
				case AVG:
					return "Avg of " + col;
				default:
					return "";
			}
		} else {
			return col.toString();
		}
	}

}
