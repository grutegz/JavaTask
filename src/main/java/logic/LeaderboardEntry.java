package logic;

public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    private final String username;
    private final long score;

    public LeaderboardEntry(String username, long score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public long getScore() {
        return score;
    }

    @Override
    public int compareTo(LeaderboardEntry other) {
        // Long.compare(other.score, this.score) для сортировки по убыванию
        return Long.compare(this.score, other.score);
    }
}