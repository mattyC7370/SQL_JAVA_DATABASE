package com.maroonags;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Box;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;



/**
 * JavaFX App
 */
public class App extends Application {
    // Data values to generate query
    private static ArrayList<String> selectedTables = new ArrayList<>();
    private static ArrayList<String> joinAttributes = new ArrayList<>();
    private static ObservableList<String> selectedAttributes = FXCollections.observableArrayList();
    private static ArrayList<String[]> conditionList = new ArrayList<>();
    private static ArrayList<String> conditionConnectors = new ArrayList<>();
    private static String sortAttr = "";
    private static boolean isDesc = false;
    private static boolean isSort = false;
    private static boolean isLimit = false;
    private static int limit = 0;

    // Placeholders constants
    private static final String TITLE = "Maroon Query";
    private static final String SELECT_TABLE = "Name...";
    private static final String ATTRIBUTE_STR = "Attribute...";
    private static final String OPERATOR_STR = "Operator...";

    private static final String CONDITION_HBOX_STR = "Condition-HBox";
    private static final String MODIFY_CONDITION_HBOX_STR = "Modify-Condition-HBox";

    private static final String BOOTSTRAP_CSS_FILENAME = "src/bootstrap3.css";

    @Override
    public void start(Stage stage) throws Exception{
        var scene = setInitialLayout(stage);
        stage.setScene(scene);
        stage.setTitle(TITLE);
        stage.setMaximized(true);
        stage.show();
    }

    public Scene setInitialLayout(Stage stage) {
        // ================= Initializations ==============
        MenuButton sortDropDwn = new MenuButton(ATTRIBUTE_STR);
        final var attributeList = new ListView<String>();
        var col1Drop = new MenuButton(ATTRIBUTE_STR);
        var opDrop = new MenuButton(OPERATOR_STR);
        var col2Drop = new MenuButton(ATTRIBUTE_STR);
        var tablesLbl = new Label();
        var joinedLbl = new Label();

        // Table selection
        var selectLbl = new Label("Select a table:");
        var selectTableBtn = new MenuButton(SELECT_TABLE);
        selectLbl.setLabelFor(selectTableBtn);

        // Add all the table names as menu items to the dropdown menu "selectTableBtn"
        // This also adds an action event to each button.
        for (String tableName : Database.getTableNames()) {
            var tableItem = new MenuItem(tableName);
            tableItem.setOnAction(a->{
                if (!selectedTables.contains(tableName)) selectedTables.add(tableName);
                selectTableBtn.setText(tableItem.getText());
                attributeList.getItems().addAll(Database.getAttributes(selectedTables));
                addConditionDropDowns(col1Drop, opDrop, col2Drop);
            });
            selectTableBtn.getItems().add(tableItem);
        }

        var addTableButton = new Button("Add another table...");
        addTableButton.setOnAction(actionEvent -> {
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(stage);
            var dialogGrid = new GridPane();

            var selectTableDialogBtn = new MenuButton(SELECT_TABLE);
            var joinBtn = new MenuButton("Join on...");

            for (String tableName : Database.getTableNames()) {
                var tableItem = new MenuItem(tableName);
                tableItem.setOnAction(a->{
                    if (!selectedTables.contains(tableName)) selectedTables.add(tableName);
                    selectTableDialogBtn.setText(tableItem.getText());
                    // Update the attribute list
                    var copyList = new ArrayList<>(selectedTables);
                    copyList.add(tableItem.getText());
                    var commonAttrList =  Database.getCommonAttributes(copyList);

                    //There are no common attributes
                    if (commonAttrList.size() == 0) {
                        // TODO: Create a status bar to show error
                        System.out.println("Could not find a common attribute");
                    }
                    for (String commonAttr : commonAttrList) {
                        var commonMenuItem = new MenuItem(commonAttr);
                        commonMenuItem.setOnAction(actionEvent1 -> joinBtn.setText(commonAttr));
                        joinBtn.getItems().add(commonMenuItem);
                    }
                });
                selectTableDialogBtn.getItems().add(tableItem);
            }

            var doneDialogBtn = new Button("Done");
            doneDialogBtn.setOnAction(actionEvent1 -> {
                selectedTables.add(selectTableDialogBtn.getText());
                joinAttributes.add(joinBtn.getText());
                attributeList.getItems().addAll(Database.getAttributes(selectedTables));
                tablesLbl.setText(selectedTables.toString());
                joinedLbl.setText(joinAttributes.toString());
                dialog.close();
            });

            dialogGrid.setPadding(new Insets(10,10,10,10));
            dialogGrid.setVgap(10);
            dialogGrid.setHgap(10);
            dialogGrid.add(selectTableDialogBtn, 0, 0);
            dialogGrid.add(joinBtn, 1, 0);
            dialogGrid.add(doneDialogBtn, 0, 1);

            Scene dialogScene = new Scene(dialogGrid);
            dialogScene.getStylesheets().add((new File(BOOTSTRAP_CSS_FILENAME)).toURI().toString());
            dialog.setScene(dialogScene);
            dialog.show();
        });

        // Pack the label and the two buttons in an HBox
        var addTableHBox = new HBox(10);
        addTableHBox.getChildren().addAll(selectLbl, selectTableBtn, addTableButton);

        // Pack the HBox and the list of selected tables and columns in a VBox
        var tablesVBox = new VBox(20);
        tablesLbl.setText("Tables: " + selectedTables.toString());
        joinedLbl.setText("Joined on: " + joinAttributes.toString());

        tablesVBox.getChildren().addAll(addTableHBox, tablesLbl, joinedLbl);

        // Add the components to a VBox
        var sceneVBox = new VBox(20);

        // ================= Attribute List ====================
        var attributeLbl = new Label("Attributes: \n(Hold ctrl to select multiple items)");

        attributeLbl.setLabelFor(attributeList);

        attributeList.getItems().addAll(Database.getAttributes(selectedTables));
        attributeList.setPrefSize(120,120);
        attributeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        attributeList.setOnMouseClicked(mouseEvent -> {
            selectedAttributes = attributeList.getSelectionModel().getSelectedItems();
            addMenuItemsSort(sortDropDwn);
        });

        // =================== Condition drop-downs =============
        var conditionLbl = new Label("Condition(s):");

        // add values to the drop-downs
        addConditionDropDowns(col1Drop, opDrop, col2Drop);

        // Create new HBox for the drop-downs
        var conditionHBox = new HBox(10);
        conditionHBox.getChildren().addAll(col1Drop, opDrop, col2Drop);
        conditionHBox.setUserData(CONDITION_HBOX_STR);

        // Add new-condition button
        var newConditionBtn = new MenuButton("Add another condition");


        // add a remove-condition button
        var removeConditionBtn = new Button("Remove a condition");
        removeConditionBtn.setOnAction(a -> {
            // look up that userID for each node in a reversed children list in VBox
            // so we only remove last instance of a condition HBox.
            // Also remove the condition from the conditionList
            // and the last connector from the connector list
            var childrenList = new ArrayList<>(sceneVBox.getChildren());
            Collections.reverse(childrenList);
            for (var child : childrenList) {
                if(child.getUserData() != null && child.getUserData().equals(CONDITION_HBOX_STR)) {
                    sceneVBox.getChildren().remove(child);
                    var condList = new ArrayList<>(((HBox) child).getChildren());
                    removeCondition(((MenuButton)(condList.get(0))).getText(),((MenuButton)(condList.get(1))).getText(),
                            ((MenuButton)(condList.get(2))).getText());
                    if (conditionConnectors.size() > 1) conditionConnectors.remove(conditionConnectors.size() - 1);
                    break;
                }
            }
        });

        // HBox containing add and remove condition buttons
        var conditionModifyHBox = new HBox(10);
        conditionModifyHBox.getChildren().addAll(newConditionBtn, removeConditionBtn);
        conditionModifyHBox.setUserData(MODIFY_CONDITION_HBOX_STR);

        conditionLbl.setLabelFor(conditionModifyHBox);

        // action events for AND & OR Menu Items
        var andItem = new MenuItem("AND");
        andItem.setOnAction(actionEvent1 -> {
            // On button press, repeat the steps above to add a new row
            var col1 = new MenuButton(ATTRIBUTE_STR);
            var op = new MenuButton(OPERATOR_STR);
            var col2 = new MenuButton(ATTRIBUTE_STR);
            addConditionDropDowns(col1, op, col2);
            var conditionBox = new HBox(10);
            conditionBox.getChildren().addAll(col1,op,col2);
            conditionBox.setUserData(CONDITION_HBOX_STR);

            // Add "AND" to condition connector list
            conditionConnectors.add("AND");

            // Find the correct index to add a new row and then add it
            int i = sceneVBox.getChildren().size() - 1;
            var childrenList = new ArrayList<>(sceneVBox.getChildren());
            Collections.reverse(childrenList);
            // We create a new array list because we cannot reverse children of sceneVBox
            // without affecting the GUI
            for (var child : childrenList) {
                if (child.getUserData() != null && child.getUserData().equals(CONDITION_HBOX_STR)) {
                    sceneVBox.getChildren().add(i + 1, conditionBox);
                    break;
                }
                i --;
            }
        });

        var orItem = new MenuItem("OR");
        orItem.setOnAction(actionEvent1 -> {
            // On button press, repeat the steps above to add a new row
            var col1 = new MenuButton(ATTRIBUTE_STR);
            var op = new MenuButton(OPERATOR_STR);
            var col2 = new MenuButton(ATTRIBUTE_STR);
            addConditionDropDowns(col1, op, col2);
            var conditionBox = new HBox(10);
            conditionBox.getChildren().addAll(col1,op,col2);
            conditionBox.setUserData(CONDITION_HBOX_STR);

            // Add "OR" to condition connector list
            conditionConnectors.add("OR");

            // Find the correct index to add a new row and then add it
            int i = sceneVBox.getChildren().size() - 1;
            var childrenList = new ArrayList<>(sceneVBox.getChildren());
            Collections.reverse(childrenList);
            // We create a new array list because we cannot reverse children of sceneVBox
            // without affecting the GUI
            for (var child : childrenList) {
                if (child.getUserData() != null && child.getUserData().equals(CONDITION_HBOX_STR)) {
                    sceneVBox.getChildren().add(i + 1, conditionBox);
                    break;
                }
                i --;
            }
        });

        newConditionBtn.getItems().addAll(andItem, orItem);

        // =================== Additional options ===============

        // Add sorting checkboxes and add menu items to the sort drop down initialized above
        var sortCB = new CheckBox("Sort by: ");
        sortCB.selectedProperty().addListener((observableValue, aBoolean, t1) -> isSort = t1);
        addMenuItemsSort(sortDropDwn);
        var descCB = new CheckBox("Descending");
        descCB.selectedProperty().addListener((observableValue, aBoolean, t1) -> isDesc = t1);

        var sortHBox = new HBox(10);
        sortHBox.getChildren().addAll(sortCB, sortDropDwn, descCB);

        // Add limit checkbox and text field
        var limitCB = new CheckBox("Limit results:");
        limitCB.selectedProperty().addListener((observableValue, aBoolean, t1) -> isLimit = t1);

        var limitText = new TextField();
        limitText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                limitText.setText(newValue.replaceAll("[^\\d]", ""));
            }
            limit = Integer.parseInt(limitText.getText());
        });

        var limitHBox = new HBox(10);
        limitHBox.getChildren().addAll(limitCB, limitText);


        // ========================= Text field ===================
        var fileLbl = new Label("Enter filename: ");
        var fileNameField = new TextField("log.txt");
        fileLbl.setLabelFor(fileNameField);

        var fileHBox = new HBox(10);
        fileHBox.getChildren().addAll(fileLbl, fileNameField);

        sceneVBox.getChildren().addAll(attributeLbl, attributeList, conditionLbl, conditionHBox, conditionModifyHBox, sortHBox, limitHBox, fileHBox);

        // ============================== Main Panel ============================
        // Create a new pane and add all the components created
        /*
        Following are the cell positions in the grid pane of JavaFX −
        (0, 0)	(1, 0)	(2, 0)
        (2, 1)	(1, 1)	(0, 1)
        (2, 2)	(1, 2)	(0, 2)
         */
        var mainGrid = new GridPane();
        mainGrid.setMinSize(400,400);
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(20,50,50,20));
        mainGrid.add(tablesVBox,0, 0);
        mainGrid.add(sceneVBox,  0,1);

        var outputPane = new Pane();
        outputPane.setPrefSize(300,600);
        var outputTxtLbl = new Label();
        outputPane.getChildren().add(outputTxtLbl);
        var outputLbl = new Label("Output:");
        outputLbl.setLabelFor(outputPane);
        var outputVBox = new VBox(20);
        //outputVBox.getChildren().addAll(outputLbl, outputPane);
        mainGrid.add(outputLbl, 1, 0);
        mainGrid.add(outputPane, 1,1);

        var doneBtn = new Button("Done");
        doneBtn.setPrefSize(500,10);
        doneBtn.setOnAction(actionEvent -> {
            var query = generateQuery();
            var result = Database.sendQuery(query);
            var output = new StringBuilder();
            for (var line : result) {
                output.append(line).append("\n");
            }
            if (result.size() <= 30) {
                outputTxtLbl.setText(output.toString());
            }
            else {
                try {
                    var outputWriter =  new FileWriter(fileNameField.getText());
                    outputWriter.write(output.toString());
                    outputWriter.close();
                    //TODO: Append the output and display that.
                    outputTxtLbl.setText("Output written to file!");
                } catch (IOException e) {
                    System.out.println("Could not open file...");
                    e.printStackTrace();
                }
            }
        });

        mainGrid.add(doneBtn, 0,2);

        // Create and return a new scene containing the pane
        var scene = new Scene(mainGrid);
        scene.getStylesheets().add((new File(BOOTSTRAP_CSS_FILENAME)).toURI().toString());
        return scene;
    }

    private void addConditionDropDowns(MenuButton col1, MenuButton op, MenuButton col2) {
        System.out.println("Select tables " + selectedTables);
        for (var attributeName : Database.getAttributes(selectedTables)) {
            var attributeItem1 = new MenuItem(attributeName);
            attributeItem1.setOnAction(actionEvent -> {
                // If the col1 value wasn't the default value => a previous condition being modified
                // So first remove the previous condition then add the new one.
                if(!col1.getText().equals(ATTRIBUTE_STR)) {
                    removeCondition(col1.getText(), op.getText(), col2.getText());
                }
                col1.setText(attributeName);
                addCondition(col1.getText(), op.getText(), col2.getText());
            });
            var attributeItem2 = new MenuItem(attributeName);
            attributeItem2.setOnAction(actionEvent -> {
                if(!col2.getText().equals(ATTRIBUTE_STR)) {
                    removeCondition(col1.getText(), op.getText(), col2.getText());
                }
                col2.setText(attributeName);
                addCondition(col1.getText(), op.getText(), col2.getText());
            });
            col1.getItems().add(attributeItem1);
            col2.getItems().add(attributeItem2);
        }

        if (op.getItems().size() == 0) {
            for (String opName : Arrays.asList("=", "<", ">", "<=", ">=", "<>")) {
                var opItem = new MenuItem(opName);
                opItem.setOnAction(actionEvent -> {
                    if (!op.getText().equals(OPERATOR_STR)) {
                        removeCondition(col1.getText(), op.getText(), col2.getText());
                    }
                    op.setText(opName);
                    addCondition(col1.getText(), op.getText(), col2.getText());
                });
                op.getItems().add(opItem);
            }
        }

    }

    private static class Condition {
        private String attribute1;
        private String operation;
        private String attribute2;

        public Condition(String attr1, String op, String attr2) {
            addCondition(attr1, op, attr2);
        }

        public void addCondition(String attr1, String op, String attr2) {
            /** Check if the condition is valid, and create an object only if the values aren't default*/
            if (!attr1.equals(ATTRIBUTE_STR))
                attribute1 = attr1;
            if (!op.equals(OPERATOR_STR))
                operation = op;
            if (!attr2.equals(ATTRIBUTE_STR))
                attribute2 = attr2;
        }

        public String[] getCondition() {
            /** Return the condition as an array of strings, size = 3 */
            if (attribute1 != null && attribute2 != null && operation != null) {
                return new String[]{attribute1, operation, attribute2};
            }
            return null;
        }
    }

    private void addCondition(String attr1, String op, String attr2) {
        Condition condition = new Condition(attr1, op, attr2);
        String[] conditionArr = condition.getCondition();
        if (conditionArr != null) {
            conditionList.add(conditionArr);
        }
    }

    private void removeCondition(String attr1, String op, String attr2) {
        /**
         * Function to remove a condition from the conditionList, given the two attributes and the logical operator.
         * It only removes valid conditions.
         */
        Condition conditionRemove = new Condition(attr1, op, attr2);
        System.out.println("Removing " + attr1 + " " + op + " " + attr2);
        String[] conditionArr = conditionRemove.getCondition();

        if (conditionArr != null) {
            for (int i = 0; i < conditionList.size(); ++i) {
                var condition = conditionList.get(i);
                if (condition[0].equals(conditionArr[0]) && condition[1].equals(conditionArr[1]) &&
                        condition[2].equals(conditionArr[2])) {
                    conditionList.remove(i);
                    return;
                }
            }
        }
    }

    private void addMenuItemsSort(MenuButton sortDropDwn) {
        sortDropDwn.getItems().removeAll(sortDropDwn.getItems());
        for (var attribute : selectedAttributes) {
            var menuItem = new MenuItem(attribute);
            menuItem.setOnAction(actionEvent -> { sortDropDwn.setText(attribute); sortAttr = attribute; });
            sortDropDwn.getItems().add(menuItem);
        }
    }

    public static String generateQuery() {
        try {
            StringBuilder query = new StringBuilder("select ");

            // ... attribute1, attribute2, ...
            for (var attribute : selectedAttributes) {
                query.append(attribute).append(", ");
            }
            query.replace(query.length() - 2, query.length() - 1, "");

            // from table
            query.append(" from ").append(selectedTables.get(0));

            // join [table2] on table1.attr = table2.attr
            if (selectedTables.size() > 1) {
                int i;
                for (i = 0; i < joinAttributes.size(); ++i) {
                    query.append(" join " + selectedTables.get(i + 1) + " on " + selectedTables.get(i + 1) + "."  + joinAttributes.get(i)
                            + " = " + selectedTables.get(i) + "." + joinAttributes.get(i) + " ");
                }
            }

            // where [condition1] [and] [condition2]
            if (conditionList.size() > 0) {
                query.append(" where ");
                int i;
                for (i = 0; i < conditionConnectors.size(); ++i) {
                    query.append(conditionList.get(i)[0]).append(" ")
                            .append(conditionList.get(i)[1]).append(" ")
                            .append(conditionList.get(i)[2]).append(" ");

                    query.append(conditionConnectors.get(i)).append(" ");
                }
                query.append(conditionList.get(i)[0]).append(" ")
                        .append(conditionList.get(i)[1]).append(" ")
                        .append(conditionList.get(i)[2]).append(" ");
            }

            if (isSort) {
                // sort by [attribute] [desc]
                query.append(" order by ").append(sortAttr);
                if (isDesc) query.append(" desc ");
            }

            if (isLimit) {
                // limit [num]
                query.append(" limit ").append(limit);
            }

            return query.toString();
        }
        catch (RuntimeException e) {
            System.out.println("Could not generate query");
            return null;
        }
    }


    public static void main(String[] args) {
        launch();
    }

}