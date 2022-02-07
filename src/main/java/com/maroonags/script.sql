--PROJECT 1: Sports Database 
--TEAM 1: Maroon Ags
--Members: Sawyer Cowan, Deepansh Bhatia, Benjamin Beauchamp, and Matthew Casavecchia
--Current Date: 02/25/2020 


-------
--2.1--
-------

SELECT pass_yard, player_code, game_code 
FROM player_game_stat
ORDER BY pass_yard DESC
LIMIT 10;

SELECT yards, player_code FROM punt
ORDER BY yards DESC
LIMIT 10;

SELECT DISTINCT team_code, team_name FROM team
ORDER BY team_code;

SELECT DISTINCT team_code
FROM team
WHERE team_name='Texas A&M';

-------
--2.2--
-------
SELECT MAX(yards)
FROM punt_return
WHERE team_code = 697;

SELECT COUNT(reception)
FROM reception
WHERE player_code = 5 AND reception = 1;

SELECT MAX(stadium_capacity)
FROM stadium;

SELECT COUNT(home_team_code)
FROM game
WHERE home_team_code IN (697,365) AND visit_team_code IN (365,697);

SELECT home_state, COUNT(home_state)
FROM player
GROUP BY home_state;

-------
--2.3--
-------

SELECT DISTINCT player.player_code, player.first_name, player.last_name
FROM player
INNER JOIN team ON player.team_code = team.team_code
WHERE team.team_name = 'Texas A&M';

SELECT DISTINCT stadium.stadium_name
FROM stadium
JOIN game ON game.stadium_code = stadium.stadium_code
JOIN team on team.team_code = game.visit_team_code
WHERE team.team_name = 'Texas A&M';

SELECT COUNT(touchdown)
FROM punt_return
JOIN team on team.team_code = punt_return.team_code
WHERE team.team_name = 'Texas A&M' AND touchdown = 1;

SELECT DISTINCT player_code, first_name, last_name
FROM player
JOIN team on team.team_code = player.team_code
WHERE player_position = 'QB' and team.team_name = 'Texas A&M';

SELECT MAX(duration)
FROM game_statistics
JOIN game on game.game_code = game_statistics.game_code
WHERE game.game_date >= '2012-07-31' AND game.game_date <= '2012=12-31';

SELECT COUNT(fumble_lost)
FROM kickoff_return
JOIN player ON player.player_code = kickoff_return.player_code
JOIN team ON team.team_code = player.team_code
WHERE player.player_class = 'FR'
AND team.team_name = 'Texas A&M'
AND fumble_lost = 1;

SELECT DISTINCT first_name, last_name, team.team_name, pass.yards
FROM player
JOIN pass on player.player_code = pass.passer_player_code
JOIN team on team.team_code = player.team_code
WHERE pass.yards = (SELECT MAX(yards) FROM pass);

SELECT DISTINCT stadium_name, gs.duration
FROM stadium
JOIN game on game.stadium_code = stadium.stadium_code
JOIN game_statistics gs on gs.game_code = game.game_code
WHERE gs.duration = (SELECT MAX(duration) from game_statistics);



-------
--2.4--
-------


SELECT DISTINCT first_name, last_name
FROM player
WHERE first_name LIKE 'J%' AND last_name LIKE 'P%';

SELECT DISTINCT first_name, last_name, height
FROM player
JOIN team ON team.team_code = player.team_code
WHERE team.team_name = 'Texas A&M'
AND height = (
	SELECT MAX(height)
	FROM player
	JOIN team ON team.team_code = player.team_code
	WHERE team.team_name = 'Texas A&M'
);

SELECT DISTINCT stadium_name, stadium_capacity, stadium_state, year_opened
FROM stadium
WHERE stadium_state IN ('TX', 'LA', 'NM', 'AK', 'OK')
AND year_opened BETWEEN 1980 AND 2019
AND stadium_capacity = (
	SELECT MAX(stadium_Capacity)
	FROM stadium
	WHERE stadium_state IN ('TX', 'LA', 'NM', 'AK', 'OK')
	AND year_opened BETWEEN 1980 AND 2019
);

-------
--2.5--
-------

CREATE LOCAL TEMP VIEW TAMU_Players AS
SELECT DISTINCT player.player_code, player.first_name, player.last_name
FROM player
INNER JOIN team ON player.team_code = team.team_code
WHERE team.team_name = 'Texas A&M';

CREATE LOCAL TEMP VIEW All_Teams AS
SELECT DISTINCT team_code, team_name FROM team
ORDER BY team_code;

CREATE LOCAL TEMP VIEW Top10_Punts AS
SELECT yards, player_code FROM punt
ORDER BY yards DESC
LIMIT 10;
