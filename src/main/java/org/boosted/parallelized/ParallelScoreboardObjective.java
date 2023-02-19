package org.boosted.parallelized;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

public class ParallelScoreboardObjective extends ScoreboardObjective {

    private final Object lock = new Object();

    public ParallelScoreboardObjective(Scoreboard scoreboard, String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType) {
        super(scoreboard, name, criterion, displayName, renderType);
    }

    @Override
    public Text getDisplayName() {
        synchronized (lock) {
            return this.displayName;
        }
    }

    @Override
    public Text toHoverableText() {
        synchronized (lock) {
            return this.bracketedDisplayName;
        }
    }

    /**
     * Avoids calling updateExistingObjective()
     */
    public void setDisplayNameSynchronized(Text name) {
        synchronized (lock) {
            this.displayName = name;
            this.bracketedDisplayName = this.generateBracketedDisplayName();
        }
    }

    @Override
    public void setDisplayName(Text name) {
        setDisplayNameSynchronized(name);
        this.getScoreboard().updateExistingObjective(this);
    }

    @Override
    public ScoreboardCriterion.RenderType getRenderType() {
        synchronized (lock) {
            return this.renderType;
        }
    }

    /**
     * Avoids calling updateExistingObjective()
     */
    public void setRenderTypeSynchronized(ScoreboardCriterion.RenderType renderType) {
        synchronized (lock) {
            this.renderType = renderType;
        }
    }

    @Override
    public void setRenderType(ScoreboardCriterion.RenderType renderType) {
        setRenderTypeSynchronized(renderType);
        this.getScoreboard().updateExistingObjective(this);
    }
}
