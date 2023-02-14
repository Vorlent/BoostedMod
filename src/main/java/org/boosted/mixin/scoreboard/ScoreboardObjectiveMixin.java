package org.boosted.mixin.scoreboard;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScoreboardObjective.class)
public abstract class ScoreboardObjectiveMixin {
    @Accessor("displayName")
    public abstract void setDisplayNameField(Text name);

    @Accessor("bracketedDisplayName")
    public abstract void setBracketedDisplayName(Text name);

    @Shadow public abstract Scoreboard getScoreboard();

    @Shadow protected abstract Text generateBracketedDisplayName();

    /**
     * @author Vorlent
     * @reason
     */
    @Overwrite
    public void setDisplayName(Text name) {
        ScoreboardObjective scoreboardObjective = (ScoreboardObjective) (Object) this;
        synchronized (scoreboardObjective) {
            this.setDisplayNameField(name);
            this.setBracketedDisplayName(this.generateBracketedDisplayName());
            this.getScoreboard().updateExistingObjective(scoreboardObjective);
        }
    }
}
