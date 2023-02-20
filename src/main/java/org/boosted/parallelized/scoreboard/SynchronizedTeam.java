package org.boosted.parallelized.scoreboard;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.boosted.mixin.getServer.SetServerAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SynchronizedTeam extends Team {

    private final SimplifiedTeam team;
    private final ServerScoreboard serverScoreboard;

    public SynchronizedTeam(ServerScoreboard serverScoreboard, SimplifiedTeam team) {
        super(team.getScoreboard(), team.getName());
        this.team = team;
        this.serverScoreboard = serverScoreboard;
    }

    @Override
    public String getName() {
        synchronized (team) {
            return team.getName();
        }
    }

    @Override
    public Text getDisplayName() {
        synchronized (team) {
            return team.getDisplayName();
        }
    }

    @Override
    public MutableText getFormattedName() {
        synchronized (team) {
            return team.getFormattedName();
        }
    }

    @Override
    public void setDisplayName(Text displayName) {
        synchronized (team) {
            team.setDisplayNameNoUpdate(displayName);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public void setPrefix(@Nullable Text prefix) {
        synchronized (team) {
            team.setPrefix(prefix);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public Text getPrefix() {
        synchronized (team) {
            return team.getPrefix();
        }
    }

    @Override
    public void setSuffix(@Nullable Text suffix) {
        synchronized (team) {
            team.setSuffix(suffix);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public Text getSuffix() {
        synchronized (team) {
            return team.getSuffix();
        }
    }

    @Override
    public Collection<String> getPlayerList() {
        // this leaks a mutable reference
        // which is modified in Scoreboard

        synchronized (team) {
            return team.getPlayerList();
        }
    }

    @Override
    public MutableText decorateName(Text name) {
        synchronized (team) {
            return team.decorateName(name);
        }
    }

    @Override
    public boolean isFriendlyFireAllowed() {
        synchronized (team) {
            return team.isFriendlyFireAllowed();
        }
    }

    @Override
    public void setFriendlyFireAllowed(boolean friendlyFire) {
        synchronized (team) {
            team.setFriendlyFireAllowedNoUpdate(friendlyFire);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public boolean shouldShowFriendlyInvisibles() {
        synchronized (team) {
            return team.shouldShowFriendlyInvisibles();
        }
    }

    @Override
    public void setShowFriendlyInvisibles(boolean showFriendlyInvisible) {
        synchronized (team) {
            team.setShowFriendlyInvisiblesNoUpdate(showFriendlyInvisible);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public VisibilityRule getNameTagVisibilityRule() {
        synchronized (team) {
            return team.getNameTagVisibilityRule();
        }
    }

    @Override
    public VisibilityRule getDeathMessageVisibilityRule() {
        synchronized (team) {
            return team.getDeathMessageVisibilityRule();
        }
    }

    @Override
    public void setNameTagVisibilityRule(VisibilityRule nameTagVisibilityRule) {
        synchronized (team) {
            team.setNameTagVisibilityRuleNoUpdate(nameTagVisibilityRule);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public void setDeathMessageVisibilityRule(VisibilityRule deathMessageVisibilityRule) {
        synchronized (team) {
            team.setNameTagVisibilityRuleNoUpdate(deathMessageVisibilityRule);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public CollisionRule getCollisionRule() {
        synchronized (team) {
            return team.getCollisionRule();
        }
    }

    @Override
    public void setCollisionRule(CollisionRule collisionRule) {
        synchronized (team) {
            team.setCollisionRuleNoUpdate(collisionRule);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public int getFriendlyFlagsBitwise() {
        synchronized (team) {
            return team.getFriendlyFlagsBitwise();
        }
    }

    @Override
    public void setFriendlyFlagsBitwise(int flags) {
        synchronized (team) {
            team.setFriendlyFlagsBitwiseNoUpdate(flags);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public void setColor(Formatting color) {
        synchronized (team) {
            team.setColorNoUpdate(color);
        }
        serverScoreboard.updateScoreboardTeam(this);
    }

    @Override
    public Formatting getColor() {
        synchronized (team) {
            return team.getColor();
        }
    }

    /**
     * Do not use this except inside SynchronizedServerScoreboard
     * @return
     */
    public Team getSimplifiedTeam() {
        return team;
    }
}
