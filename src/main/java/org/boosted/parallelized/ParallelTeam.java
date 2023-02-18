package org.boosted.parallelized;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.boosted.mixin.getServer.SetServerAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ParallelTeam extends Team {

    private final Object lock = new Object();

    public ParallelTeam(Scoreboard scoreboard, String name) {
        super(scoreboard, name);
    }

    @Override
    public String getName() {
        synchronized (lock) {
            return super.getName();
        }
    }

    @Override
    public Text getDisplayName() {
        synchronized (lock) {
            return super.getDisplayName();
        }
    }

    @Override
    public MutableText getFormattedName() {
        synchronized (lock) {
            return super.getFormattedName();
        }
    }

    @Override
    public void setDisplayName(Text displayName) {
        synchronized (lock) {
            if (displayName == null) {
                throw new IllegalArgumentException("Name cannot be null");
            }
            this.displayName = displayName;
            ((SetServerAccessor) this).setServer(null);

        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public void setPrefix(@Nullable Text prefix) {
        synchronized (lock) {
            this.prefix = prefix == null ? ScreenTexts.EMPTY : prefix;

        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public Text getPrefix() {
        synchronized (lock) {
            return super.getPrefix();
        }
    }

    @Override
    public void setSuffix(@Nullable Text suffix) {
        synchronized (lock) {
            this.suffix = suffix == null ? ScreenTexts.EMPTY : suffix;
        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public Text getSuffix() {
        synchronized (lock) {
            return super.getSuffix();
        }
    }

    @Override
    public Collection<String> getPlayerList() {
        // this leaks a mutable reference
        // which is modified in Scoreboard

        synchronized (lock) {
            return super.getPlayerList();
        }
    }

    @Override
    public MutableText decorateName(Text name) {
        synchronized (lock) {
            return super.decorateName(name);
        }
    }

    @Override
    public boolean isFriendlyFireAllowed() {
        synchronized (lock) {
            return super.isFriendlyFireAllowed();
        }
    }

    @Override
    public void setFriendlyFireAllowed(boolean friendlyFire) {
        synchronized (lock) {
            this.friendlyFire = friendlyFire;
        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public boolean shouldShowFriendlyInvisibles() {
        synchronized (lock) {
            return super.shouldShowFriendlyInvisibles();
        }
    }

    @Override
    public void setShowFriendlyInvisibles(boolean showFriendlyInvisible) {
        synchronized (lock) {
            this.showFriendlyInvisibles = showFriendlyInvisible;
        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public AbstractTeam.VisibilityRule getNameTagVisibilityRule() {
        synchronized (lock) {
            return super.getNameTagVisibilityRule();
        }
    }

    @Override
    public AbstractTeam.VisibilityRule getDeathMessageVisibilityRule() {
        synchronized (lock) {
            return super.getDeathMessageVisibilityRule();
        }
    }

    @Override
    public void setNameTagVisibilityRule(AbstractTeam.VisibilityRule nameTagVisibilityRule) {
        synchronized (lock) {
            this.nameTagVisibilityRule = nameTagVisibilityRule;
        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public void setDeathMessageVisibilityRule(AbstractTeam.VisibilityRule deathMessageVisibilityRule) {
        synchronized (lock) {
            this.deathMessageVisibilityRule = deathMessageVisibilityRule;
        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public AbstractTeam.CollisionRule getCollisionRule() {
        synchronized (lock) {
            return super.getCollisionRule();
        }
    }

    @Override
    public void setCollisionRule(AbstractTeam.CollisionRule collisionRule) {
        synchronized (lock) {
            this.collisionRule = collisionRule;
        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public int getFriendlyFlagsBitwise() {
        synchronized (lock) {
            return super.getFriendlyFlagsBitwise();
        }
    }

    @Override
    public void setFriendlyFlagsBitwise(int flags) {
        synchronized (lock) {
            this.friendlyFire = (flags & 1) > 0;
            this.showFriendlyInvisibles = (flags & 2) > 0;
        }
        this.getScoreboard().updateScoreboardTeam(this);
    }

    @Override
    public void setColor(Formatting color) {
        synchronized (lock) {
            super.setColor(color);
        }
    }

    @Override
    public Formatting getColor() {
        synchronized (lock) {
            return super.getColor();
        }
    }

}
