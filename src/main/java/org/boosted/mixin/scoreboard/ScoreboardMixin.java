package org.boosted.mixin.scoreboard;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.Text;
import org.boosted.parallelized.scoreboard.SimplifiedScoreboardObjective;
import org.boosted.parallelized.scoreboard.SimplifiedScoreboardPlayerScore;
import org.boosted.parallelized.scoreboard.SimplifiedServerScoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Scoreboard.class)
public abstract class ScoreboardMixin {

    @Redirect(method = "method_1187(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardObjective;)Lnet/minecraft/scoreboard/ScoreboardPlayerScore;",
        at = @At(value = "NEW", target = "net/minecraft/scoreboard/ScoreboardPlayerScore"))
    public ScoreboardPlayerScore redirectScoreboardPlayerScore(Scoreboard scoreboard, ScoreboardObjective objective, String playerName) {
        if ((Object)this instanceof SimplifiedServerScoreboard) {
            return new SimplifiedScoreboardPlayerScore(scoreboard, objective, playerName);
        }
        return new ScoreboardPlayerScore(scoreboard, objective, playerName);
    }

    @Redirect(method = "method_1187(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardObjective;)Lnet/minecraft/scoreboard/ScoreboardPlayerScore;",
            at = @At(value = "INVOKE", target = "net/minecraft/scoreboard/ScoreboardPlayerScore.setScore(I)V"))
    public void redirectSetScore(ScoreboardPlayerScore instance, int score) {
        if (instance instanceof SimplifiedScoreboardPlayerScore simplified) {
            simplified.setScore(score, (SimplifiedServerScoreboard)(Object)this);
        } else {
            instance.setScore(score);
        }
    }

    @Redirect(method = "readNbt(Lnet/minecraft/nbt/NbtList;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/scoreboard/ScoreboardPlayerScore.setScore(I)V"))
    public void redirectSetScore2(ScoreboardPlayerScore instance, int score) {
        if (instance instanceof SimplifiedScoreboardPlayerScore simplified) {
            simplified.setScore(score, (SimplifiedServerScoreboard)(Object)this);
        } else {
            instance.setScore(score);
        }
    }
    @Redirect(method = "addObjective(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardCriterion;Lnet/minecraft/text/Text;Lnet/minecraft/scoreboard/ScoreboardCriterion$RenderType;)Lnet/minecraft/scoreboard/ScoreboardObjective;",
            at = @At(value = "NEW", target = "net/minecraft/scoreboard/ScoreboardObjective"))
    public ScoreboardObjective redirectScoreboardPlayerScore(Scoreboard scoreboard, String name, ScoreboardCriterion criterion,
                                                            Text displayName, ScoreboardCriterion.RenderType renderType) {
        if ((Object)this instanceof SimplifiedServerScoreboard) {
            return new SimplifiedScoreboardObjective(scoreboard, name, criterion, displayName, renderType);
        }
        return new ScoreboardObjective(scoreboard, name, criterion, displayName, renderType);
    }
}
