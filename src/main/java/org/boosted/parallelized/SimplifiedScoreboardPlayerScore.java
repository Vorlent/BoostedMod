package org.boosted.parallelized;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;

/**
 * This class is either protected via {@link SynchronizedServerScoreboard}
 * or via {@link SynchronizedScoreboardPlayerScore}.
 * This class contains the state of the ScoreboardPlayerScore.
 */
public class SimplifiedScoreboardPlayerScore extends ScoreboardPlayerScore {
    public SimplifiedScoreboardPlayerScore(Scoreboard scoreboard, ScoreboardObjective objective, String playerName) {
        super(scoreboard, objective, playerName);
    }

    public void incrementScore(int amount, ServerScoreboard serverScoreboard) {
        if (this.getObjective().getCriterion().isReadOnly()) {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        this.setScore(this.getScore(serverScoreboard) + amount, serverScoreboard);
    }
    public void incrementScore(ServerScoreboard serverScoreboard) {
        this.incrementScore(1, serverScoreboard);
    }

    public int getScore(ServerScoreboard serverScoreboard) {
        return super.getScore();
    }

    public void clearScore(ServerScoreboard serverScoreboard) {
        this.setScore(0, serverScoreboard);
    }

    public void setScore(int score, ServerScoreboard serverScoreboard) {
        int i = this.score;
        this.score = score;
        if (i != score || this.forceUpdate) {
            this.forceUpdate = false;
            serverScoreboard.updateScore(this);
        }
    }

    public void incrementScore(int amount) {
        throw new UnsupportedOperationException("Specify serverScoreboard explicitly");
    }
    public void incrementScore() {
        throw new UnsupportedOperationException("Specify serverScoreboard explicitly");
    }

    public int getScore() {
        return super.getScore();
    }

    public void clearScore() {
        throw new UnsupportedOperationException("Specify serverScoreboard explicitly");
    }

    public void setScore(int score) {
        throw new UnsupportedOperationException("Specify serverScoreboard explicitly");
    }

    public boolean isLocked() {
        return super.isLocked();
    }

    public void setLocked(boolean locked) {
        super.setLocked(locked);
    }
}
