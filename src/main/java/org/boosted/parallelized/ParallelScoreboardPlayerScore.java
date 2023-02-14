package org.boosted.parallelized;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;

public class ParallelScoreboardPlayerScore extends ScoreboardPlayerScore {
    private final Object lock = new Object();

    public ParallelScoreboardPlayerScore(Scoreboard scoreboard, ScoreboardObjective objective, String playerName) {
        super(scoreboard, objective, playerName);
    }

    public void incrementScore(int amount) {
        synchronized (lock) {
            super.incrementScore(amount);
        }
    }
    public void incrementScore() {
        synchronized (lock) {
            super.incrementScore();
        }
    }

    public int getScore() {
        synchronized (lock) {
            return super.getScore();
        }
    }

    public void clearScore() {
        synchronized (lock) {
            super.clearScore();
        }
    }

    public void setScore(int score) {
        synchronized (lock) {
            super.setScore(score);
        }
    }

    public boolean isLocked() {
        synchronized (lock) {
            return super.isLocked();
        }
    }

    public void setLocked(boolean locked) {
        synchronized (lock) {
            super.setLocked(locked);
        }
    }
}
