package com.maroonags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;


public class Questions {

    /**
     * Given a team code, generate the names of the teams which the given team has beaten
     * @param teamCode A String representation of the team code
     * @return an ArrayList<String> containing the names of the team beaten by the given team
     */
    private static ArrayList<String> getLosingTeams(String teamCode) throws Exception {
        // Algorithm:
        // 1. Find all the matches this team has played.
        // 2. For all the matches, get the points of this team and the opposing team
        // 3. If the points of this team > points of the opponent team, add opponent team name to the list.
        var losingTeams = new HashSet<String>();

        // Get the codes of all the games the given team has played
        ArrayList<String> visitGameCodes = Database.sendQuery("select game_code from game where visit_team_code = " + teamCode);
        for (int i = 0; i < visitGameCodes.size(); ++i)
            visitGameCodes.set(i, visitGameCodes.get(i).replace(",",""));

        ArrayList<String> homeGameCodes = Database.sendQuery("select game_code from game where home_team_code = " + teamCode);
        for (int i = 0; i < homeGameCodes.size(); ++i)
            homeGameCodes.set(i, homeGameCodes.get(i).replace(",",""));

        if (homeGameCodes.size() == 0 && visitGameCodes.size() == 0)
            throw new Exception("team has not played any games: " + teamCode);

        // For all visiting games, get the points of the given team and opponent team
        for (var visitGameCode : visitGameCodes) {
            // get the opponent team code and team name
            String opponentTeamCode = "";
            String opponentTeamName = "";
            try {
                opponentTeamCode = Database.sendQuery("select home_team_code from game where game_code = '" + visitGameCode + "'").get(0).replace(",", "");
                opponentTeamName = Database.sendQuery("select team_name from team where team_code = '" + opponentTeamCode + "'").get(0).replace(",", "");
            }
            catch (Exception e) {
                System.out.println("visitGameCode = " + visitGameCode);
            }

            // get the score of given team and opponent team
            String givenTeamScore  = "";
            try {
                givenTeamScore = Database.sendQuery("select points from team_game_stat where team_code = " + teamCode +
                        " and game_code = '" + visitGameCode + "'").get(0).replace(",", "");
            }
            catch (Exception e) {
                System.out.println("Visit game code: " + visitGameCode);
            }

            String opponentTeamScore = Database.sendQuery("select points from team_game_stat where team_code = " + opponentTeamCode +
                    " and game_code = '" + visitGameCode + "'").get(0).replace(",","");

            if (parseInt(givenTeamScore) > parseInt(opponentTeamScore))
                losingTeams.add(opponentTeamName);
        }

        // Repeat the same for home games
        for (var homeGameCode : homeGameCodes) {
            // get the opponent team code and team name
            String opponentTeamCode = Database.sendQuery("select visit_team_code from game where game_code = '" + homeGameCode + "'").get(0).replace(",","");
            String opponentTeamName = Database.sendQuery("select team_name from team where team_code = " + opponentTeamCode).get(0).replace(",","");

            // get the score of given team and opponent team
            String givenTeamScore = Database.sendQuery("select points from team_game_stat where team_code = " + teamCode +
                    " and game_code = '" + homeGameCode + "'").get(0).replace(",","");

            String opponentTeamScore = Database.sendQuery("select points from team_game_stat where team_code = " + opponentTeamCode +
                    " and game_code = '" + homeGameCode + "'").get(0).replace(",","");

            if (parseInt(givenTeamScore) > parseInt(opponentTeamScore))
                losingTeams.add(opponentTeamName);
        }

        return new ArrayList<>(losingTeams);

    }

    public static String getVictoryChain(String team1, String team2) throws Exception {
        // Node class to be used later in the BFS
        class Node {
            public String name;
            public String code;
            public ArrayList<String> path;
            public Node(String teamName, String teamCode, ArrayList<String> newPath) {
                this.name = teamName;
                this.code = teamCode;
                this.path = newPath;
            }
            public String getPathAsString() {
                var stringPath = new StringBuilder(this.path.get(0));
                for (int i = 1; i < this.path.size(); ++i)
                    stringPath.append(" - ").append(this.path.get(i));
                return stringPath.toString();
            }
        }

        // Check for valid team names and get team codes
        var result1 = Database.sendQuery("select team_code from team where team_name = '" + team1 + "'");
        var result2 = Database.sendQuery("select team_code from team where team_name = '" + team2 + "'");
        if (result1.size() == 0 || result2.size() == 0)
            return "You have entered invalid team names.";
        var teamCode1 = result1.get(0).replace(",","");

        ArrayList<Node> queue = new ArrayList<>();
        queue.add(new Node(team1, teamCode1,  new ArrayList<>()));

        int level = 1;
        final int LEVEL_MAX = 5;
        while (level < LEVEL_MAX) {
            var queueCopy = new ArrayList<>(queue);
            var visitedTeams = new HashSet<String>();
            while(queue.size() > 0) {
                // get the head of the queue and check if it is goal state
                // If it is, return the path of the goal state as string
                var checkTeam = queue.remove(0);

                if (checkTeam.name.equals(team2))
                    return checkTeam.getPathAsString();

                visitedTeams.add(checkTeam.name);

                // Otherwise append all the teams "CheckTeam" has won against to the queue
                var children = getLosingTeams(checkTeam.code);
                if (children.contains(team2)) {
                    return checkTeam.getPathAsString() + " - " + team2;
                }
                for (String teamName : children) {
                    var newPath = new ArrayList<>(checkTeam.path);
                    newPath.add(teamName);
                    String teamCode = "";
                    try {
                        teamCode = Database.sendQuery("select team_code from team where team_name = '" + teamName + "'").get(0).replace(",", "");
                    }
                    catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Node team = new Node(teamName, teamCode, newPath);
                    if (!visitedTeams.contains(teamName)) {
                        queueCopy.add(team);
                    }
                }
            }
            queue = queueCopy;
            level++;
        }

       return "Victory chain not found within " + LEVEL_MAX + " levels.";
    }

    public static String getShortestChain(String player1Code, String player2Code) {
        /*
        Shortest chain between 2 players. Given 2 players, find the shortest path
        of connections between the players. Connections can be Games, common coaches,
        common teams, and common home towns. Not Conferences, not “played in same stadium.”
         */

        // Check if the player codes are valid
        var dataTest1 = Database.sendQuery("select last_name from player where player_code = " + player1Code);
        var dataTest2 = Database.sendQuery("select last_name from player where player_code = " + player2Code);
        if (dataTest1.size() == 0 || dataTest2.size() == 0)
            return "You have entered invalid player codes";

        // Hometown
        String player1Hometown = Database.sendQuery("select home_town from player where player_code = " + player1Code).get(0);
        String player2Hometown = Database.sendQuery("select home_town from player where player_code = " + player2Code).get(0);
        if (player1Hometown.equals(player2Hometown))
            return "Players live in the same hometown: " + player1Hometown.replace(",","");

        // Teams
        String player1Team = Database.sendQuery("select team_code from player where player_code = " + player1Code).get(0);
        String player2Team = Database.sendQuery("select team_code from player where player_code = " + player2Code).get(0);
        String teamName = Database.sendQuery("select team_name from team where team_code = " + player1Team).get(0);
        if (player1Team.equals(player2Team))
            return "Players play in the same team: " + teamName.replace(",","");

        // Games
        var result = Database.sendQuery("select game_code from game where visit_team_code = " + player1Team + " and " +
                "home_team_code = " + player2Team);
        if (result.size() == 0)
            result = Database.sendQuery("select game_code from game where visit_team_code = " + player2Team + " and " +
                    "home_team_code = " + player1Team);
        if (result.size() > 0) {
            var gameCode = result.get(0);
            return "Players have played the same game with game code: " + gameCode.replace(",","");
        }

        return "Players do not have a connection.";
    }

    public static String getRushyards(String team) {
        // Steps:
        // 1. Find all the games the team has played in
        // 2. For all the opponent teams it has played against, find the rush yards for that game code

        int max_yards = 0;
        String maxTeam = "No team";

        // Get the team code
        String teamCode = Database.sendQuery("select team_code from team where team_name = '" + team + "'").get(0).replace(",","");

        // Get the codes of all the games the given team has played
        ArrayList<String> visitGameCodes = Database.sendQuery("select game_code from game where visit_team_code = " + teamCode);
        for (int i = 0; i < visitGameCodes.size(); ++i)
            visitGameCodes.set(i, visitGameCodes.get(i).replace(",",""));

        ArrayList<String> homeGameCodes = Database.sendQuery("select game_code from game where home_team_code = " + teamCode);
        for (int i = 0; i < homeGameCodes.size(); ++i)
            homeGameCodes.set(i, homeGameCodes.get(i).replace(",",""));

        // For all visiting games, get the points of the given team and opponent team
        for (var visitGameCode : visitGameCodes) {
            // get the opponent team code and team name
            String opponentTeamCode = "";
            String opponentTeamName = "";
            try {
                opponentTeamCode = Database.sendQuery("select home_team_code from game where game_code = '" + visitGameCode + "'").get(0).replace(",", "");
                opponentTeamName = Database.sendQuery("select team_name from team where team_code = '" + opponentTeamCode + "'").get(0).replace(",", "");
            }
            catch (Exception e) {
                System.out.println("visitGameCode = " + visitGameCode);
            }

            // get the yards of opponent team

            String opponentTeamScore = Database.sendQuery("select yards from rush where team_code = " + opponentTeamCode +
                    " and game_code = '" + visitGameCode + "'").get(0).replace(",","");

            if (parseInt(opponentTeamScore) > max_yards) {
                max_yards = parseInt(opponentTeamScore);
                maxTeam = opponentTeamName;
            }
        }

        // Repeat the same for home games
        for (var homeGameCode : homeGameCodes) {
            // get the opponent team code and team name
            String opponentTeamCode = Database.sendQuery("select visit_team_code from game where game_code = '" + homeGameCode + "'").get(0).replace(",","");
            String opponentTeamName = Database.sendQuery("select team_name from team where team_code = " + opponentTeamCode).get(0).replace(",","");

            // get the yards of opponent team
            String opponentTeamScore = Database.sendQuery("select yards from rush where team_code = " + opponentTeamCode +
                    " and game_code = '" + homeGameCode + "'").get(0).replace(",","");

            if (parseInt(opponentTeamScore) > max_yards) {
                max_yards = parseInt(opponentTeamScore);
                maxTeam = opponentTeamName;
            }
        }
        return maxTeam + " with rush yards = " + max_yards;
    }

    public static String getHomeFieldAdvantage(String conference) {
        String q = "select distinct " +
                "home.team_name as Home_Team, " +
                "homeStat.points as Home_Score, " +
                "visit.team_name as Visiting_Team, " +
                "visitStat.points as Visiting_Score " +
                "from game " +
                "join team home on home.team_code = game.home_team_code " +
                "join team visit on visit.team_code = game.visit_team_code " +
                "join team_game_stat homeStat on game.game_code = homeStat.game_code and home.team_code = homeStat.team_code " +
                "join team_game_stat visitStat on game.game_code = visitStat.game_code and visit.team_code = visitStat.team_code " +
                "join conference on home.con_code = conference.con_code " +
                "where conference.con_name = '" + conference + "' " +
                "order by home.team_name asc;";
        var results = Database.sendQuery(q);
        if(results.size() == 0)
            return "You have entered an invalid conference name";
        var ret = new ArrayList<String>();
        int wins = 0;
        int losses = 0;
        boolean first = true;
        int i;
        for (i = 0; i < results.size(); i++) {
            var lineArr = results.get(i).split(",");
            if (parseInt(lineArr[1])> parseInt(lineArr[3])) { wins++; } else { losses++; }
            if (!ret.contains(lineArr[0])) {
                if (!first) {
                    if (wins + losses == 0) {
                        ret.set(ret.size() - 1, ret.get(ret.size() - 1) + ", 0");
                    } else {
                        ret.set(ret.size() - 1, ret.get(ret.size() - 1) + "," + 100*((float) wins / (float) (wins + losses)));
                    }
                }
                ret.add(lineArr[0]);
                first = false;
                wins = 0;
                losses = 0;
            }
        }
        if (wins + losses == 0) {
            ret.set(ret.size() - 1, ret.get(ret.size() - 1) + ", 0");
        } else {
            ret.set(ret.size() - 1, ret.get(ret.size() - 1) + "," + 100*((float) wins / (float) (wins + losses)));
        }
        // Based from https://stackoverflow.com/questions/20480723/how-to-sort-2d-arrayliststring-by-only-the-first-element
        ret.sort((rhs, lhs) -> lhs.split(",")[1].compareTo(rhs.split(",")[1]));

        var result = new StringBuilder();
        for (var item : ret) {
            String[] teamAndScore = item.split(",");
            String teamName = teamAndScore[0];
            String teamScore = teamAndScore[1];
            result.append(teamName).append("\t").append(teamScore).append("\n");
        }

        return result.toString();
    }
}
