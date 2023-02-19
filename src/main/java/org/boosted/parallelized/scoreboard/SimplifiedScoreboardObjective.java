package org.boosted.parallelized.scoreboard;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

public class SimplifiedScoreboardObjective extends ScoreboardObjective {
    public SimplifiedScoreboardObjective(Scoreboard scoreboard, String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType) {
        super(scoreboard, name, criterion, displayName, renderType);
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    @Override
    public Text toHoverableText() {
        return this.bracketedDisplayName;
    }

    public void setDisplayName(Text name) {
        throw new UnsupportedOperationException("Specify serverScoreboard explicitly");
    }

    public void setDisplayName(Text name, Scoreboard scoreboard) {
        this.displayName = name;
        this.bracketedDisplayName = this.generateBracketedDisplayName();
        scoreboard.updateExistingObjective(this);
    }

    @Override
    public ScoreboardCriterion.RenderType getRenderType() {
        return this.renderType;
    }

    public void setRenderType(ScoreboardCriterion.RenderType renderType) {
        throw new UnsupportedOperationException("Specify serverScoreboard explicitly");
    }

    public void setRenderType(ScoreboardCriterion.RenderType renderType, Scoreboard scoreboard) {
        this.renderType = renderType;
        scoreboard.updateExistingObjective(this);
    }
}
