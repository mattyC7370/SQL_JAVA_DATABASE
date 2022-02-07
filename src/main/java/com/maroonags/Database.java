package com.maroonags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Database {
    private static SQLWrapper sql = new SQLWrapper();

    public static ArrayList<String> getTableNames() {
        System.out.println(sql.getTables());
        return sql.getTables();
    }

    public static ArrayList<String> getAttributes(ArrayList<String> selectedTables) {
        ArrayList<String> attributeList = new ArrayList<>();
        for(var table : selectedTables) {
            attributeList.addAll(getAttributeNames(table));
        }
        return attributeList;
    }

    public static ArrayList<String> getAttributeNames(String tableName) {
        return new ArrayList<>(sql.getColumns(new QUERY(new DATA(DATA.data_type.TABLE).withTable(tableName))));
    }

    public ArrayList<String> getAttributeNames(DATA data) {
        return new ArrayList<>(sql.getColumns(new QUERY(data)));
    }

    public ArrayList<String> getAttributeNames(QUERY query) {
        return new ArrayList<>(sql.getColumns(query));
    }

    public static ArrayList<String> getCommonAttributes(ArrayList<String> tableNames) {
        ArrayList<String> common = new ArrayList<>();
        ArrayList<ArrayList<String>> allCols = new ArrayList<>();
        for (String tableName : tableNames) {
            allCols.add(new ArrayList<>(sql.getColumns(new QUERY(new DATA(DATA.data_type.TABLE).withTable(tableName)))));
        }

        for (int tblNum = 0; tblNum < allCols.size(); tblNum++) {                                   // For all tables
            for (int colNum = 0; colNum < allCols.get(tblNum).size(); colNum++) {                   // For all columns in that table
                boolean isCommon = true;
                for (int cmpTbl = tblNum; cmpTbl < allCols.size(); cmpTbl++) {                      // For all other tables
                    if (!allCols.get(cmpTbl).contains(allCols.get(tblNum).get(colNum))) {           // If the other table doesn't contain the column
                        isCommon = false;                                                           // End the loop and do not add to common list
                        break;
                    }
                }
                if (isCommon) { common.add(allCols.get(tblNum).get(colNum)); }
            }
        }
        // make common a set to remove duplicate elements
        Set<String> commonSet = new HashSet<>(common);
        return new ArrayList<>(commonSet);
    }

    public static ArrayList<String> sendQuery(String query) {
        return new ArrayList<String>(sql.submitQuery(query));
    }
}