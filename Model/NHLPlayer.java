package Model;
public class NHLPlayer {

    private String fName;
    private String lName;
    private int jerseyNum;
    private String team;
    private String headShot;
    private int shotsLastFive;
    private int goalsLastFive;
    private double toiLastFive;
    private int ppGoalsLastFive;
    private double oppGoalieSaveP;
    private double xGoals;

    public NHLPlayer(String fName, String lName, int jerseyNum, String headShot, String team, int shotsLastFive,
            int goalsLastFive, double toiLastFive, int ppGoalsLastFive, double oppGoalieSaveP, double xGoals) {
        this.fName = fName;
        this.lName = lName;
        this.jerseyNum = jerseyNum;
        this.headShot = headShot;
        this.team = team;
        this.shotsLastFive = shotsLastFive;
        this.goalsLastFive = goalsLastFive;
        this.toiLastFive = toiLastFive;
        this.ppGoalsLastFive = ppGoalsLastFive;
        this.oppGoalieSaveP = oppGoalieSaveP;
        this.xGoals = xGoals;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public int getJerseyNum() {
        return jerseyNum;
    }

    public void setJerseyNum(int jerseyNum) {
        this.jerseyNum = jerseyNum;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getHeadShot() {
        return headShot;
    }

    public void setHeadShot(String headShot) {
        this.headShot = headShot;
    }

    public int getShotsLastFive() {
        return shotsLastFive;
    }

    public void setShotsLastFive(int shotsLastFive) {
        this.shotsLastFive = shotsLastFive;
    }


    public int getGoalsLastFive() {
        return goalsLastFive;
    }

    public void setGoalsLastFive(int goalsLastFive) {
        this.goalsLastFive = goalsLastFive;
    }

    public double getToiLastFive() {
        return toiLastFive;
    }

    public void setToiLastFive(double toiLastFive) {
        this.toiLastFive = toiLastFive;
    }

    public double getPpGoalsLastFive() {
        return ppGoalsLastFive;
    }

    public void setPpGoalsLastFive(int ppGoalsLastFive) {
        this.ppGoalsLastFive = ppGoalsLastFive;
    }

    public double getOppGoalieSaveP() {
        return oppGoalieSaveP;
    }

    public void setOppGoalieSaveP(double oppGoalieSaveP) {
        this.oppGoalieSaveP = oppGoalieSaveP;
    }

    public double getxGoals() {
        return xGoals;
    }

    public void setxGoals(double xGoals) {
        this.xGoals = xGoals;
    }

}
