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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;


/**
 * JavaFX App
 */
public class App extends Application {
    // Data values to generate query
    private static String selectedTable = "";
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

    private static final String SELECT_TABLE_ID = "Select-Table";
    private static final String COL1_DRP_ID = "Col1-Dropdown";
    private static final String OP_DRP_ID = "Op-Dropdown";
    private static final String COL2_DRP_ID = "Col2-Dropdown";
    private static final String CONDITION_HBOX_ID = "Condition-HBox";
    private static final String MODIFY_CONDITION_HBOX_ID = "Modify-Condition-HBox";
    private static final String FILE_HBOX_ID = "File-HBox";
    private static final String TEXT_COL2_STR = "Text-Item-Col";

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

        // UI items
        var selectTableBtn = new MenuButton(SELECT_TABLE);
        selectTableBtn.setUserData(SELECT_TABLE_ID);
        var sortDropDwn = new MenuButton(ATTRIBUTE_STR);
        final var attributeList = new ListView<String>();
        var col1Drop = new MenuButton(ATTRIBUTE_STR);
        col1Drop.setUserData(COL1_DRP_ID);
        var opDrop = new MenuButton(OPERATOR_STR);
        opDrop.setUserData(OP_DRP_ID);
        var col2Drop = new MenuButton(ATTRIBUTE_STR);
        col2Drop.setUserData(COL2_DRP_ID);
        var tablesLbl = new Label();

        // Boxes
        var addTableHBox = new HBox(10);
        var tablesVBox = new VBox(20);
        var sceneVBox = new VBox(20);
        var conditionHBox = new HBox(10);
        conditionHBox.setUserData(CONDITION_HBOX_ID);
        var conditionModifyHBox = new HBox(10);
        conditionModifyHBox.setUserData(MODIFY_CONDITION_HBOX_ID);
        var fileHBox = new HBox(10);
        fileHBox.setUserData(FILE_HBOX_ID);


        // Table selection
        var selectLbl = new Label("Select a table:");
        selectLbl.setLabelFor(selectTableBtn);

        // Add all the table names as menu items to the dropdown menu "selectTableBtn"
        // This also adds an action event to each button, where they clear the attribute list
        // and add the new attributes from the new table selected
        for (String tableName : Database.getTableNames()) {
            var tableItem = new MenuItem(tableName);
            tableItem.setOnAction(a->{
                selectTableBtn.setText(tableItem.getText());
                selectedTable = tableItem.getText();
                attributeList.getItems().clear();
                attributeList.getItems().addAll(Database.getAttributes(new ArrayList<>(Collections.singletonList(selectTableBtn.getText()))));
                updateConditions(sceneVBox);
            });
            selectTableBtn.getItems().add(tableItem);
        }

        // Example Questions Button
        var questionsBtn = new Button("Example Questions");
        setQuestionsBtnAction(questionsBtn, stage);

        // Pack the select-table label and the two buttons (select table dropdown & add table button) in an HBox
        addTableHBox.getChildren().addAll(selectLbl, selectTableBtn, questionsBtn);

        // Pack the HBox and the list of selected tables and columns in a VBox
        tablesVBox.getChildren().addAll(addTableHBox, tablesLbl);



        // ================= Attribute List ====================

        var attributeLbl = new Label("Attributes: \n(Hold ctrl to select multiple items)");
        attributeLbl.setLabelFor(attributeList);

        attributeList.getItems().addAll(Database.getAttributes(new ArrayList<>(Collections.singletonList(selectTableBtn.getText()))));
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
        conditionHBox.getChildren().addAll(col1Drop, opDrop, col2Drop);

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
                if(child.getUserData() != null && child.getUserData().equals(CONDITION_HBOX_ID)) {
                    sceneVBox.getChildren().remove(child);
                    conditionConnectors.remove(conditionConnectors.size() - 1 );
                    break;
                }
            }
        });

        // HBox containing add and remove condition buttons

        conditionModifyHBox.getChildren().addAll(newConditionBtn, removeConditionBtn);

        conditionLbl.setLabelFor(conditionModifyHBox);

        // action events for AND & OR Menu Items
        var andItem = new MenuItem("AND");
        andItem.setOnAction(actionEvent1 -> {
            // On button press, repeat the steps above to add a new row
            var col1 = new MenuButton(ATTRIBUTE_STR);
            var op = new MenuButton(OPERATOR_STR);
            var col2 = new MenuButton(ATTRIBUTE_STR);
            col1.setUserData(COL1_DRP_ID);
            col2.setUserData(COL2_DRP_ID);
            op.setUserData(OP_DRP_ID);
            addConditionDropDowns(col1, op, col2);
            var conditionBox = new HBox(10);
            conditionBox.getChildren().addAll(col1,op,col2);
            conditionBox.setUserData(CONDITION_HBOX_ID);

            // Add "AND" to condition connector list
            conditionConnectors.add("AND");

            // Find the correct index to add a new row and then add it
            int i = sceneVBox.getChildren().size() - 1;
            var childrenList = new ArrayList<>(sceneVBox.getChildren());
            Collections.reverse(childrenList);
            // We create a new array list because we cannot reverse children of sceneVBox
            // without affecting the GUI
            for (var child : childrenList) {
                if (child.getUserData() != null && child.getUserData().equals(CONDITION_HBOX_ID)) {
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
            col1.setUserData(COL1_DRP_ID);
            col2.setUserData(COL2_DRP_ID);
            op.setUserData(OP_DRP_ID);
            addConditionDropDowns(col1, op, col2);
            var conditionBox = new HBox(10);
            conditionBox.getChildren().addAll(col1,op,col2);
            conditionBox.setUserData(CONDITION_HBOX_ID);

            // Add "OR" to condition connector list
            conditionConnectors.add("OR");

            // Find the correct index to add a new row and then add it
            int i = sceneVBox.getChildren().size() - 1;
            var childrenList = new ArrayList<>(sceneVBox.getChildren());
            Collections.reverse(childrenList);
            // We create a new array list because we cannot reverse children of sceneVBox
            // without affecting the GUI
            for (var child : childrenList) {
                if (child.getUserData() != null && child.getUserData().equals(CONDITION_HBOX_ID)) {
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

        fileHBox.getChildren().addAll(fileLbl, fileNameField);

        sceneVBox.getChildren().addAll(attributeLbl, attributeList, conditionLbl, conditionHBox, conditionModifyHBox, sortHBox, limitHBox, fileHBox);

        // ============================== Main Panel ============================
        // Create a new pane and add all the components created

        var mainGrid = new GridPane();
        mainGrid.setMinSize(400,400);
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(20,50,50,20));
        mainGrid.add(tablesVBox,0, 0);
        mainGrid.add(sceneVBox,  0,1);

        // ========================= Output Pane =======================
        var outputPane = new Pane();
        outputPane.setPrefSize(300,600);
        var outputTxtLbl = new Label();
        outputPane.getChildren().add(outputTxtLbl);
        var outputLbl = new Label("Output:");
        outputLbl.setLabelFor(outputPane);
        mainGrid.add(outputLbl, 1, 0);
        mainGrid.add(outputPane, 1,1);


        // ================= Done Button ======================
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
                outputTxtLbl.setText(selectedAttributes + "\n" + output.toString());
            }
            else {
                try {
                    var outputWriter =  new FileWriter(fileNameField.getText());
                    outputWriter.write(output.toString());
                    outputWriter.close();
                    var appendedOutput = new StringBuilder();
                    for (var line : result.subList(0,30)) {
                        appendedOutput.append(line).append("\n");
                    }
                    outputTxtLbl.setText(selectedAttributes + "\n" + appendedOutput.toString());
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

    private void updateConditions(VBox sceneVBox) {
        var childrenList = new ArrayList<>(sceneVBox.getChildren());
        for (var child : childrenList) {
            if(child.getUserData() != null && child.getUserData().equals(CONDITION_HBOX_ID)) {
                var conditionHBox = (HBox)child;
                var col1 = (MenuButton)conditionHBox.getChildren().get(0);
                var op = (MenuButton)conditionHBox.getChildren().get(1);
                var col2 = (MenuButton)conditionHBox.getChildren().get(2);
                addConditionDropDowns(col1, op, col2);
            }
        }
    }

    private void updateConditionLists(VBox sceneVBox) {
        conditionList.clear();
        conditionConnectors.clear();
        var childrenList = new ArrayList<>(sceneVBox.getChildren());
        for (var child : childrenList) {
            if(child.getUserData() != null && child.getUserData().equals(CONDITION_HBOX_ID)) {
                var conditionHBox = (HBox)child;
                var col1 = ((MenuButton)conditionHBox.getChildren().get(0)).getText();
                var op = ((MenuButton)conditionHBox.getChildren().get(1)).getText();;
                var col2 = ((MenuButton)conditionHBox.getChildren().get(2)).getText();;
                String[] conditions = {col1, op, col2};
                conditionList.add(conditions);

            }
        }
    }

    private void addConditionDropDowns(MenuButton col1, MenuButton op, MenuButton col2) {
        //clear existing data
        col1.getItems().clear();
        col2.getItems().clear();
        op.getItems().clear();

        // add a text field to attribute2
        var textItem = new MenuItem("Constant");
        var textValue = new TextField();
        textValue.setPrefWidth(60);
        textItem.setGraphic(textValue);
        textItem.setUserData(TEXT_COL2_STR);
        boolean fieldInMenu = false;
        for (var child : col2.getItems())
            if (child.getUserData() != null && child.getUserData().equals(TEXT_COL2_STR)) fieldInMenu = true;
        textItem.setOnAction(actionEvent -> {
            col2.setText(textValue.getText());
        });
        if (!fieldInMenu) col2.getItems().add(textItem);

        // Add the list of attributes
        for (var attributeName : Database.getAttributes(new ArrayList<>(Collections.singletonList(selectedTable)))) {
            var attributeItem1 = new MenuItem(attributeName);
            attributeItem1.setOnAction(actionEvent -> {
                col1.setText(attributeName);
            });
            var attributeItem2 = new MenuItem(attributeName);
            attributeItem2.setOnAction(actionEvent -> {
                col2.setText(attributeName);
            });
            col1.getItems().add(attributeItem1);
            col2.getItems().add(attributeItem2);
        }

        // Add the operations
        if (op.getItems().size() == 0) {
            for (String opName : Arrays.asList("=", "<", ">", "<=", ">=", "<>")) {
                var opItem = new MenuItem(opName);
                opItem.setOnAction(actionEvent -> {
                    op.setText(opName);
                });
                op.getItems().add(opItem);
            }
        }

    }


    private void addMenuItemsSort(MenuButton sortDropDwn) {
        sortDropDwn.getItems().removeAll(sortDropDwn.getItems());
        for (var attribute : selectedAttributes) {
            var menuItem = new MenuItem(attribute);
            menuItem.setOnAction(actionEvent -> { sortDropDwn.setText(attribute); });
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
            query.append(" from ").append(selectedTable);


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

    public static void setQuestionsBtnAction(Button popupBtn, Stage stage) {
        popupBtn.setOnAction(actionEvent -> {
            // Initialize the dialog with a new stage
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(stage);

            // add a dialogGrid where all the components will be placed
            var dialogVBox = new VBox(30);
            dialogVBox.setPadding(new Insets(20,20,20,20));

            // Create a toggle group for the radio buttons to allow only one choice
            var questionGroup = new ToggleGroup();

            var andLbl = new Label("and");

            // ============ Q1: Victory chain b/w two teams =================
            // Radio button
            final String victoryChainTxt = "Victory Chain between";
            var victoryChainRadio = new RadioButton(victoryChainTxt);
            victoryChainRadio.setToggleGroup(questionGroup);
            // Inputs
            var victoryTeam1 = new MenuButton("Select Team");
            var victoryTeam2 = new MenuButton("Select Team");
            for(var teamName : Database.sendQuery("select distinct(team_name) from team order by team_name")) {
                // Add items to dropdown lists
                var teamItem1 = new MenuItem(teamName);
                teamItem1.setOnAction(actionEvent1 -> victoryTeam1.setText(teamName.strip().replace(",","")));
                victoryTeam1.getItems().add(teamItem1);

                var teamItem2 = new MenuItem(teamName);
                teamItem2.setOnAction(actionEvent1 -> victoryTeam2.setText(teamName.strip().replace(",","")));
                victoryTeam2.getItems().add(teamItem2);
            }
            //HBox for victory chain
            var victoryHBox = new HBox(10);
            victoryHBox.getChildren().addAll(victoryChainRadio, victoryTeam1, andLbl, victoryTeam2);
            // Add to the dialog
            dialogVBox.getChildren().add(victoryHBox);


            //===========Q2: Shortest Chain b/w two players =============
            // Radio button
            final var shortestChainTxt = "Shortest Chain between";
            var shortestChainRadio = new RadioButton(shortestChainTxt);
            shortestChainRadio.setToggleGroup(questionGroup);
            // Inputs
            var player1Txt = new TextField("Player code...");
            player1Txt.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*"))
                    player1Txt.setText(newValue.replaceAll("[^\\d]", ""));
            });
            var player2Txt = new TextField("Player code...");
            player2Txt.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*"))
                    player2Txt.setText(newValue.replaceAll("[^\\d]", ""));
            });

            // HBox for shortest chain
            var shortestChainHBox = new HBox(10);
            shortestChainHBox.getChildren().addAll(shortestChainRadio, player1Txt, andLbl, player2Txt);
            // Add to the dialog
            dialogVBox.getChildren().add(shortestChainHBox);

            //==========Q3: Team with the most rush yards against team ==========
            // Radio button
            final var rushyardsTxt = "Team with the most rush yards against";
            var rushyardsRadio = new RadioButton(rushyardsTxt);
            rushyardsRadio.setToggleGroup(questionGroup);
            // Input
            var rushyardsTeam = new MenuButton("Select team");
            for (var teamName : Database.sendQuery("select distinct(team_name) from team order by team_name")){
                var teamItem = new MenuItem(teamName);
                teamItem.setOnAction(actionEvent1 -> rushyardsTeam.setText(teamName.strip().replace(",","")));
                rushyardsTeam.getItems().add(teamItem);
            }
            // Rushyards HBox
            var rushyardsHbox = new HBox(10);
            rushyardsHbox.getChildren().addAll(rushyardsRadio,rushyardsTeam);
            // Add to the dialog Vbox
            dialogVBox.getChildren().add(rushyardsHbox);

            //==============Q4: Average Home Field advantage in conference=======
            // Home Field Advantage Radio
            final var homeFieldTxt = "Average Home Field advantage in";
            var homeFieldRadio = new RadioButton(homeFieldTxt);
            homeFieldRadio.setToggleGroup(questionGroup);
            // Conference Input
            var conferenceBtn = new MenuButton("Select conference");
            for (var conferenceName : Database.sendQuery("select distinct(con_name) from conference")){
                var conferenceItem = new MenuItem(conferenceName);
                conferenceItem.setOnAction(actionEvent1 -> conferenceBtn.setText(conferenceName.strip().replace(",","")));
                conferenceBtn.getItems().add(conferenceItem);
            }
            // Home Field HBox
            var homeFieldHBox = new HBox(10);
            homeFieldHBox.getChildren().addAll(homeFieldRadio,conferenceBtn);
            // Add it to the VBox
            dialogVBox.getChildren().add(homeFieldHBox);

            // ============== Display Panel ==============
            var separator = new Separator();
            var outputText = new Label();


            // ================ Results Button =============
            var resultsBtn = new Button("Results");
            resultsBtn.setOnAction(actionEvent1 -> {
                var selectedRadio = (RadioButton)questionGroup.getSelectedToggle();
                var result = "";
                switch (selectedRadio.getText()) {
                    case victoryChainTxt : {
                        String team1 = victoryTeam1.getText();
                        String team2 = victoryTeam2.getText();
                        try {
                            result = Questions.getVictoryChain(team1, team2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case shortestChainTxt: {
                        String player1 = player1Txt.getText();
                        String player2 = player2Txt.getText();
                        result = Questions.getShortestChain(player1, player2);
                    }
                    break;
                    case rushyardsTxt : {
                        String team = rushyardsTeam.getText();
                        result = Questions.getRushyards(team);
                    }
                    break;
                    case homeFieldTxt : {
                        String conference = conferenceBtn.getText();
                        result = Questions.getHomeFieldAdvantage(conference);
                    }
                    break;
                    default: result = "Toggle does not match anything";
                }
                // Print the result to display panel
                outputText.setText(result);

            });
            // Add to the dialog
            dialogVBox.getChildren().add(resultsBtn);
            dialogVBox.getChildren().addAll(separator, outputText);

            Scene dialogScene = new Scene(dialogVBox);
            dialogScene.getStylesheets().add((new File(BOOTSTRAP_CSS_FILENAME)).toURI().toString());
            dialog.setScene(dialogScene);
            dialog.show();
        });
    }


    public static void main(String[] args) {
        launch();
    }

}