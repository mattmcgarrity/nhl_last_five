import Model.*;
import com.fasterxml.jackson.databind.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {

    public static void main (String args[]) throws Exception {
        NHLClient nhl = new NHLClient();
        String allInfo;
        long start = System.currentTimeMillis();

        List<Integer> allIds = new ArrayList<>();
        for (String[] game : nhl.getGames()) {
            for (String team : game) {
                allIds.addAll(nhl.getPlayerIds(team, "forwards"));
                allIds.addAll(nhl.getPlayerIds(team, "defensemen"));
                allIds.addAll(nhl.getPlayerIds(team, "goalies"));
            }
        }
        nhl.preFetch(allIds);

        List<NHLPlayer> players = new ArrayList<>();
        System.out.println("--- Games on Tonight ---");
        for (String[] game : nhl.getGames()) {
            String home = game[0], away = game[1];
            System.out.println(home + " (H) vs " + away + " (A)");
            int mostGamesPlayed = 0;
            String starterSaveP = "";
            // Get away player info
            for (int id : nhl.getPlayerIds(home, "goalies")) {
                List<Object> goalieInfo = nhl.parseGoalieInfo(id);

                if(Integer.parseInt(goalieInfo.get(0).toString()) > mostGamesPlayed) {
                    starterSaveP = goalieInfo.get(1).toString();     
                }
            }
            for (int id : nhl.getPlayerIds(away, "forwards")) {
                String playerInfo = nhl.parsePlayerInfo(id);
                String lastFive = nhl.parseLastFive(id);
                allInfo = playerInfo + "," + lastFive + "," + starterSaveP;
                NHLPlayer player = nhl.ConvertToPlayer(allInfo);
                players.add(player); 
            }
/*             for (int id : nhl.getPlayerIds(away, "defensemen")) {
                String playerInfo = nhl.parsePlayerInfo(id);
                String lastFive = nhl.parseLastFive(id);
                allInfo = playerInfo + "," + lastFive + "," + starterSaveP;
                NHLPlayer player = nhl.ConvertToPlayer(allInfo);
                players.add(player); 
            } */
            // Get Home player info
            for (int id : nhl.getPlayerIds(away, "goalies")) {
                List<Object> goalieInfo = nhl.parseGoalieInfo(id);

                if(Integer.parseInt(goalieInfo.get(0).toString()) > mostGamesPlayed) {
                    starterSaveP = goalieInfo.get(1).toString();     
                }
            }
            for (int id : nhl.getPlayerIds(home, "forwards")) {
                String playerInfo = nhl.parsePlayerInfo(id);
                String lastFive = nhl.parseLastFive(id);
                allInfo = playerInfo + "," + lastFive + "," + starterSaveP;
                NHLPlayer player = nhl.ConvertToPlayer(allInfo);
                players.add(player); 
            }
/*             for (int id : nhl.getPlayerIds(home, "defensemen")) {
                String playerInfo = nhl.parsePlayerInfo(id);
                String lastFive = nhl.parseLastFive(id);
                allInfo = playerInfo + "," + lastFive + "," + starterSaveP;
                NHLPlayer player = nhl.ConvertToPlayer(allInfo);
                players.add(player); 
            } */
        }
        for(NHLPlayer player : players) {
            player.setxGoals(nhl.calculateXGoals(player));
        }

        players.sort(Comparator.comparingDouble(NHLPlayer::getxGoals).reversed());
        System.out.println("--- Top ten most likely scorers ---");
        for (int i = 0; i < 11; i++) {
            NHLPlayer player = players.get(i);
            System.out.printf(player.getfName().substring(0, 1) + ". " + player.getlName()
                + " (" + player.getTeam().substring(1, 4) + ") percentage chance to score tonight: ");
            System.out.printf("%.2f", player.getxGoals());
            System.out.println("%");
        }
        System.out.println("--- Most likely to break a cold streak ---");
        int count = 0;
        for(NHLPlayer player : players) {
            if(player.getGoalsLastFive() == 0 && count < 5) {
                System.out.printf(player.getfName().substring(0, 1) + ". " + player.getlName()
                    + " (" + player.getTeam().substring(1, 4) + ") percentage chance to score tonight: ");
                System.out.printf("%.2f", player.getxGoals());
                System.out.println("%");
                count++;
            }
        }
        System.out.println((System.currentTimeMillis() - start) / 1000.0 + " seconds");

    }
}
    
