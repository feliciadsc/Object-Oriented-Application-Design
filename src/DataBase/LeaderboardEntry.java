package DataBase;

public class LeaderboardEntry {
    private String playerName;
    private int completionTimeSeconds;
    private String completionDate;

    //constructor
    public LeaderboardEntry(String playerName, int completionTimeSeconds, String completionDate) {
        this.playerName = playerName;
        this.completionTimeSeconds = completionTimeSeconds;
        this.completionDate = completionDate;
    }

    //getter pentru numele jucatorului
    public String getPlayerName() {
        return playerName;
    }

    //getter pentru afisarea corecta a timpului
    public String getFormattedTime() {
        int minutes = completionTimeSeconds / 60;
        int seconds = completionTimeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}