package org.boosted.parallelized.scoreboard;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * setXNoUpdate skips the scoreboard call. Use this in SynchronizedTeam for explicit calls to SynchronizedScoreboard
 */
public class SimplifiedTeam extends Team {

    public SimplifiedTeam(Scoreboard scoreboard, String name) {
        super(scoreboard, name);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public Text getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    public MutableText getFormattedName() {
        return super.getFormattedName();
    }

    public void setDisplayNameNoUpdate(Text displayName) {
        if (displayName == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.displayName = displayName;
    }

    @Override
    public void setDisplayName(Text displayName) {
        setDisplayNameNoUpdate(displayName);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    public void setPrefixNoUpdate(@Nullable Text prefix) {
        this.prefix = prefix == null ? ScreenTexts.EMPTY : prefix;
    }

    @Override
    public void setPrefix(@Nullable Text prefix) {
        setPrefixNoUpdate(prefix);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public Text getPrefix() {
        return super.getPrefix();
    }

    public void setSuffixNoUpdate(@Nullable Text suffix) {
        this.suffix = suffix == null ? ScreenTexts.EMPTY : suffix;
    }

    @Override
    public void setSuffix(@Nullable Text suffix) {
        setSuffixNoUpdate(suffix);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public Text getSuffix() {
        return super.getSuffix();
    }

    @Override
    public Collection<String> getPlayerList() {
        // TODO this leaks a mutable reference
        // which is modified in Scoreboard
        return super.getPlayerList();
    }

    @Override
    public MutableText decorateName(Text name) {
        return super.decorateName(name);
    }

    @Override
    public boolean isFriendlyFireAllowed() {
        return super.isFriendlyFireAllowed();
    }

    public void setFriendlyFireAllowedNoUpdate(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    @Override
    public void setFriendlyFireAllowed(boolean friendlyFire) {
        setFriendlyFireAllowedNoUpdate(friendlyFire);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public boolean shouldShowFriendlyInvisibles() {
        return super.shouldShowFriendlyInvisibles();
    }

    public void setShowFriendlyInvisiblesNoUpdate(boolean showFriendlyInvisible) {
        this.showFriendlyInvisibles = showFriendlyInvisible;
    }

    @Override
    public void setShowFriendlyInvisibles(boolean showFriendlyInvisible) {
        setShowFriendlyInvisiblesNoUpdate(showFriendlyInvisible);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public AbstractTeam.VisibilityRule getNameTagVisibilityRule() {
        return super.getNameTagVisibilityRule();
    }

    @Override
    public AbstractTeam.VisibilityRule getDeathMessageVisibilityRule() {
        return super.getDeathMessageVisibilityRule();
    }

    public void setNameTagVisibilityRuleNoUpdate(AbstractTeam.VisibilityRule nameTagVisibilityRule) {
        this.nameTagVisibilityRule = nameTagVisibilityRule;
    }

    @Override
    public void setNameTagVisibilityRule(AbstractTeam.VisibilityRule nameTagVisibilityRule) {
        setNameTagVisibilityRuleNoUpdate(nameTagVisibilityRule);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    public void setDeathMessageVisibilityRuleNoUpdate(AbstractTeam.VisibilityRule deathMessageVisibilityRule) {
        this.deathMessageVisibilityRule = deathMessageVisibilityRule;
    }

    @Override
    public void setDeathMessageVisibilityRule(AbstractTeam.VisibilityRule deathMessageVisibilityRule) {
        setDeathMessageVisibilityRuleNoUpdate(deathMessageVisibilityRule);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public AbstractTeam.CollisionRule getCollisionRule() {
        return super.getCollisionRule();
    }

    public void setCollisionRuleNoUpdate(AbstractTeam.CollisionRule collisionRule) {
        this.collisionRule = collisionRule;
    }

    @Override
    public void setCollisionRule(AbstractTeam.CollisionRule collisionRule) {
        setCollisionRuleNoUpdate(collisionRule);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public int getFriendlyFlagsBitwise() {
        return super.getFriendlyFlagsBitwise();
    }

    public void setFriendlyFlagsBitwiseNoUpdate(int flags) {
        this.friendlyFire = (flags & 1) > 0;
        this.showFriendlyInvisibles = (flags & 2) > 0;
    }

    @Override
    public void setFriendlyFlagsBitwise(int flags) {
        setFriendlyFlagsBitwiseNoUpdate(flags);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    public void setColorNoUpdate(Formatting color) {
        this.color = color;
    }

    @Override
    public void setColor(Formatting color) {
        setColorNoUpdate(color);
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public Formatting getColor() {
        return super.getColor();
    }

}
