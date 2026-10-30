package Model;

import com.fasterxml.jackson.databind.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

public class NHLClient {

    private List<NHLPlayer> playerList = new ArrayList<>();

    private static final String source = "https://api-web.nhle.com/v1";
    private final HttpClient http = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String date = LocalDate.now().toString();

    private final Map<Integer, JsonNode> landingCache = new ConcurrentHashMap<>();
    private final Map<String, JsonNode> rosterCache = new ConcurrentHashMap<>();

    private JsonNode get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(source + path)).build();
        return mapper.readTree(http.send(req, HttpResponse.BodyHandlers.ofString()).body());
    }

    // Find teams that are playing tonight
    public List<String[]> getGames() throws Exception {
        List<String[]> games = new ArrayList<>();
        for (JsonNode day : get("/schedule/" + date).path("gameWeek")) {
            if(!day.path("date").asText().equals(date)) continue;
            for (JsonNode g : day.path("games")) {
                games.add(new String[] {
                    g.path("homeTeam").path("abbrev").asText(),
                    g.path("awayTeam").path("abbrev").asText() });
            }
        }
        return games;
    }

    // Find players on a team
    public List<Integer> getPlayerIds(String team, String group) throws Exception {
        JsonNode roster = rosterCache.get(team);
        if (roster == null) {
            roster = get("/roster/" + team + "/current");
            rosterCache.put(team, roster);
        }
        List<Integer> ids = new ArrayList<>();
        for (JsonNode p : roster.path(group)) {
            ids.add(p.path("id").asInt());
        }
        return ids;
    }

    // Get general player info
    public JsonNode getPlayerInfo(int playerId) throws Exception {
        JsonNode cached = landingCache.get(playerId);
        if (cached == null) {
            cached = get("/player/" + playerId + "/landing");
            landingCache.put(playerId, cached);
        }
        return cached;
    }

        // Downloads many players at once to fill the cache
    public void preFetch(List<Integer> ids) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int id : ids) {
            pool.submit(() -> {
                try { getPlayerInfo(id);} catch (Exception e) {}
                });
            }
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.MINUTES);
    }

    // Extract necessary data from a player
    public String parsePlayerInfo(int playerId) throws Exception {
        
        JsonNode playerInfo = getPlayerInfo(playerId);
        String fName = playerInfo.path("firstName")
            .path("default").asText();
        String lName = playerInfo.path("lastName")
            .path("default").asText();
        int jerseyNum = playerInfo.path("sweaterNumber").asInt();
        String headshot = playerInfo.path("headshot").asText();

        String playerDetails = fName + "," + lName + "," + jerseyNum + "," + headshot;
        return playerDetails;
    }

    // Get the opposing starting goalie's save percentage
    public List<Object> parseGoalieInfo(int playerId) throws Exception {
        List<Object> goalieStats = new ArrayList<>();
        JsonNode playerInfo = getPlayerInfo(playerId);

        int gamesPlayed = playerInfo.path("featuredStats")
                .path("regularSeason").path("subSeason")
                .path("gamesPlayed").asInt();
            double savePercent = playerInfo.path("featuredStats")
                .path("regularSeason").path("subSeason")
                .path("savePctg").asDouble();
        if(playerInfo.path("featuredStats").path("season").asText().equals("20262027")) {
            goalieStats.add(gamesPlayed);
            goalieStats.add(savePercent);
        }
        else {
            goalieStats.add(gamesPlayed = 0);
            goalieStats.add(savePercent = 0);
        }
        return goalieStats;
    }

    // Get last 5 games for a player
    public JsonNode getLastFive(int playerId) throws Exception {
        return getPlayerInfo(playerId).path("last5Games");
    }

    // Extract necessary data from a player's last five games
    public String parseLastFive(int playerID) throws Exception {

        JsonNode last5 = getLastFive(playerID);
        int shots = 0;
        int goals = 0;
        int totalToi = 0;
        String toiStr = "";
        int ppGoals = 0;
        String team = "";

        for (JsonNode game : last5) {
            shots += game.path("shots").asInt();
            goals += game.path("goals").asInt();
            ppGoals += game.path("powerPlayGoals").asInt();
            team = game.path("teamAbbrev").toString();

            toiStr = game.path("toi").asText();
            String [] toiParts = toiStr.split(":");
            int mins = Integer.parseInt(toiParts[0]);
            int seconds = Integer.parseInt(toiParts[1]);
            int time = (mins*60) + seconds;
            totalToi += time;
        }
        String lastFiveStats = team + "," + shots + "," + goals + "," + totalToi + "," + ppGoals;
        return lastFiveStats;
    }

    public NHLPlayer ConvertToPlayer(String player) {
        String[] fields = player.split(",");
        
        NHLPlayer createdPlayer = new NHLPlayer(
            fields[0], 
            fields[1],
            Integer.parseInt(fields[2]),
            fields[3],
            fields[4],
            Integer.parseInt(fields[5]),
            Integer.parseInt(fields[6]),
            Double.parseDouble(fields[7]),
            Integer.parseInt(fields[8]),
            Double.parseDouble(fields[9]),
            0.00
        );
        return createdPlayer;
    }

    public double calculateXGoals(NHLPlayer player) {
        double goalsPerGame = player.getGoalsLastFive() / 5;
        double shotsPerGame = player.getShotsLastFive() / 5;
        double playerFactor = 0.7 * (shotsPerGame * 0.1)
            + 0.3 * (goalsPerGame);

        // Check goalie factor - 
        double svP = player.getOppGoalieSaveP();
        double goalieFactor = (svP > 0) ? (1 - svP) / (1 - 0.905) : 1.0; // Only checks if goalie has played a game, maps against league average save percentage .905

        double xGoals = playerFactor * goalieFactor;
        return (1 - Math.exp(-xGoals)) * 100;
    }
}
