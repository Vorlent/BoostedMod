package org.boosted.parallelized.scoreboard;

import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;

public class SynchronizedScoreboardPlayerScore extends ScoreboardPlayerScore {
    private final SimplifiedScoreboardPlayerScore scoreboardPlayerScore;
    private final ServerScoreboard serverScoreboard;

    public SynchronizedScoreboardPlayerScore(ServerScoreboard serverScoreboard, SimplifiedScoreboardPlayerScore score) {
        super(serverScoreboard, score.getObjective(), score.getPlayerName());
        this.scoreboardPlayerScore = score;
        this.serverScoreboard = serverScoreboard;
    }

    public void incrementScore(int amount) {
        synchronized (scoreboardPlayerScore) {
            scoreboardPlayerScore.incrementScore(amount, serverScoreboard);
        }
    }
    public void incrementScore() {
        synchronized (scoreboardPlayerScore) {
            scoreboardPlayerScore.incrementScore(serverScoreboard);
        }
    }

    public int getScore() {
        synchronized (scoreboardPlayerScore) {
            return scoreboardPlayerScore.getScore(serverScoreboard);
        }
    }

    public void clearScore() {
        synchronized (scoreboardPlayerScore) {
            scoreboardPlayerScore.clearScore(serverScoreboard);
        }
    }

    public void setScore(int score) {
        synchronized (scoreboardPlayerScore) {
            scoreboardPlayerScore.setScore(score, serverScoreboard);
        }
    }

    public boolean isLocked() {
        synchronized (scoreboardPlayerScore) {
            return scoreboardPlayerScore.isLocked();
        }
    }

    public void setLocked(boolean locked) {
        synchronized (scoreboardPlayerScore) {
            scoreboardPlayerScore.setLocked(locked);
        }
    }

    /**
     * Do not use this outside SynchronizedScoreboardObjective
     * @return
     */
    public ScoreboardPlayerScore getSimplifiedScoreboardPlayerScore() {
        return scoreboardPlayerScore;
    }
}
