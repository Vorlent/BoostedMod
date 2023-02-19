package org.boosted.parallelized.scoreboard;

import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

/**
 * Wraps ParallelScoreboardPlayerScore but with a reference to SynchronizedServerScoreboard.
 * This is essentially the remote interface for ScoreboardObjective references that have been
 * returned by SynchronizedServerScoreboard
 */
public class SynchronizedScoreboardObjective extends ScoreboardObjective {

    private final SimplifiedScoreboardObjective parallelScoreboardObjective;

    public SynchronizedScoreboardObjective(SynchronizedServerScoreboard serverScoreboard, SimplifiedScoreboardObjective objective) {
        super(serverScoreboard, objective.getName(), objective.getCriterion(), objective.getDisplayName(), objective.getRenderType());
        parallelScoreboardObjective = objective;
    }

    @Override
    public Text getDisplayName() {
        synchronized (parallelScoreboardObjective) {
            return parallelScoreboardObjective.getDisplayName();
        }
    }

    @Override
    public Text toHoverableText() {
        synchronized (parallelScoreboardObjective) {
            return parallelScoreboardObjective.toHoverableText();
        }
    }

    @Override
    public void setDisplayName(Text name) {
        synchronized (parallelScoreboardObjective) {
            parallelScoreboardObjective.setDisplayName(name, getScoreboard());
        }
    }

    @Override
    public ScoreboardCriterion.RenderType getRenderType() {
        synchronized (parallelScoreboardObjective) {
            return parallelScoreboardObjective.getRenderType();
        }
    }

    @Override
    public void setRenderType(ScoreboardCriterion.RenderType renderType) {
        synchronized (parallelScoreboardObjective) {
            parallelScoreboardObjective.setRenderType(renderType, getScoreboard());
        }
    }

    /**
        This should only ever be called by SynchronizedServerScoreboard
     */
    public SimplifiedScoreboardObjective getSimplifiedServerScoreboard() {
        return parallelScoreboardObjective;
    }
}
