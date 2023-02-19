package org.boosted.parallelized;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

/**
 * Wraps ParallelScoreboardPlayerScore but with a reference to SynchronizedServerScoreboard.
 * This is essentially the remote interface for ScoreboardObjective references that have been
 * returned by SynchronizedServerScoreboard
 */
public class SynchronizedScoreboardObjective extends ScoreboardObjective {

    private final ParallelScoreboardObjective parallelScoreboardObjective;

    public SynchronizedScoreboardObjective(SynchronizedServerScoreboard serverScoreboard, ParallelScoreboardObjective objective) {
        super(serverScoreboard, objective.getName(), objective.getCriterion(), objective.getDisplayName(), objective.getRenderType());
        parallelScoreboardObjective = objective;
    }

    @Override
    public Text getDisplayName() {
        return parallelScoreboardObjective.getDisplayName();
    }

    @Override
    public Text toHoverableText() {
        return parallelScoreboardObjective.toHoverableText();
    }

    @Override
    public void setDisplayName(Text name) {
        parallelScoreboardObjective.setDisplayNameSynchronized(name);
        this.getScoreboard().updateExistingObjective(parallelScoreboardObjective); // this uses synchronized scoreboard
    }

    @Override
    public ScoreboardCriterion.RenderType getRenderType() {
        return parallelScoreboardObjective.getRenderType();
    }

    @Override
    public void setRenderType(ScoreboardCriterion.RenderType renderType) {
        parallelScoreboardObjective.setRenderTypeSynchronized(renderType);
        this.getScoreboard().updateExistingObjective(parallelScoreboardObjective); // this uses synchronized scoreboard
    }
}
