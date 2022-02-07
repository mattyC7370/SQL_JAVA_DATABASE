package com.maroonags;

import java.util.Vector;
// import javafx.application.Application;
// import javafx.scene.Scene;
// import javafx.scene.control.Label;
// import javafx.stage.Stage;
// import javafx.scene.layout.HBox;
// import javafx.scene.control.ChoiceBox;

public class QueryTest {

    // @Override
    //     public void start(Stage primaryStage) throws Exception {
    //     primaryStage.setTitle("My First JavaFX App");

    //     ChoiceBox choiceBox1 = new ChoiceBox();
    //     choiceBox1.getItems().add("Choice 1");
    //     choiceBox1.getItems().add("Choice 2");
    //     choiceBox1.getItems().add("Choice 3");

    //     Label text = new Label("Some Words that trail after");

    //     HBox hbox = new HBox(choiceBox1, text);

    //     Scene scene = new Scene(hbox, 200, 100);
        
    //     primaryStage.setScene(scene);
    //     primaryStage.setHeight(800);
    //     primaryStage.setWidth(1000);

    //     primaryStage.show();
    // }
    
    public static void main(String[] args) {
        // Application.launch(args);

        // SELECT pass_yard, player_code, game_code 
        // FROM player_game_stat
        // ORDER BY pass_yard DESC
        // LIMIT 10;

        SQLWrapper db = new SQLWrapper();

        QUERY q = new QUERY(
            new DATA(DATA.data_type.TABLE)
                .withTable("player_game_stat")

                .addType(DATA.data_type.SORTED)
                .withSort(DATA.sort_type.DESC, new column("pass_yard"))

                .addType(DATA.data_type.SHORTEN)
                .withLimit(10)
        );

        Vector<SELECTOR> c = new Vector<SELECTOR>();
        c.add(new SELECTOR(new column("pass_yard")));
        c.add(new SELECTOR(new column("player_code")));
        c.add(new SELECTOR(new column("game_code")));

        // q = q.withCols(c);

        System.out.println(q.toSQL()); 
        ProjectGlobals.DEBUG = true;
        System.out.println(ProjectGlobals.DEBUG);
        System.out.println(q.isSelectingEverything());
        
        Vector<String> cols = db.getColumns(q);
        for (String string : cols) {
            System.out.print(string + " ");
        }
        System.out.println("");
        
        Vector<String> dbRet = db.submitQuery(q);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        for (String string : dbRet) {
            System.out.println(string);
        }

        // QUERY q1 = new QUERY(
        //     new DATA(DATA.data_type.Table)
        //         .withTable("player_game_stat")
        //         .addType(DATA.data_type.WHERE)
        //         .addCondition(new SUB_COND(SUB_COND.condition_type.IS_NULL), new column("Plum"))

        // ).withUnique(true);
            
        // Vector<SELECTOR> c2 = new Vector<SELECTOR>();
        // c.add(new SELECTOR(new column("pass_yard")));
        // c.add(new SELECTOR(new column("player_code")));
        // c.add(new SELECTOR(new column("game_code")));

        // q1 = q1.withCols(c2);

        // System.out.println(q1); 
        // System.out.println(""); 

        // SELECT COUNT(fumble_lost)
        // FROM kickoff_return
        // JOIN player ON player.player_code = kickoff_return.player_code
        // JOIN team ON team.team_code = player.team_code
        // WHERE player.player_class = 'FR'
        // AND team.team_name = 'Texas A&M'
        // AND fumble_lost = 1;

        // DATA d = new DATA(DATA.data_type.NONE);
        // QUERY q2 = new QUERY(
        //     new DATA(DATA.data_type.Table)
        //         .withTable("kickoff_return")
        //         .addType(DATA.data_type.COMBO)
        //         .withCombination(
        //             DATA.combination_type.INNER,
        //             new DATA(DATA.data_type.Table)
        //                 .withTable("player"),
        //             new column("player.player_code"),
        //             new column("kickoff_return.player_code")
        //         )
        //         .addType(DATA.data_type.COMBO)
        //         .withCombination(
        //             DATA.combination_type.INNER,
        //             new DATA(DATA.data_type.Table)
        //                 .withTable("team"),
        //             new column("team.team_code"),
        //             new column("player.team_code")
        //         )
        //         .addType(DATA.data_type.WHERE)
        //         .addCondition(
        //             new SUB_COND(SUB_COND.condition_type.MATCHES)
        //                 .withSearchString("FR"), 
        //             new column("player.player_class")
        //         )
        //         .addCondition(
        //             new SUB_COND(SUB_COND.condition_type.MATCHES)
        //                 .withSearchString("Texas A&M"), 
        //             new column("team.team_name"),
        //             DATA.op_type.OR
        //         )
        //         .addCondition(
        //             new SUB_COND(SUB_COND.condition_type.MATCHES)
        //                 .withSearchString("1"), 
        //             new column("fumble_lost")
        //         )
        // ).addCol(
        //     new SELECTOR(new column("fumble_lost"))
        //         .withMod(SELECTOR.mod_types.COUNT)
        // );

        // System.out.println(q2.toString()); 
        // System.out.println(q2.toSQL()); 


        db.closeConnection();

    }
    

}