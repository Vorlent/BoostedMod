package org.boosted.parallelized;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * The goal of this class is to provide a public interface over ServerScoreboard that synchronizes all accesses.
 * The reason why it delegates to another ServerScoreboard instance is that the wrapped instance
 * will use its own public API internally, which means it is not supposed to use these synchronized methods.
 *
 */
public class SynchronizedServerScoreboard extends ServerScoreboard {

    private final Object lock = new Object();
    private final SimplifiedServerScoreboard serverScoreboard;

    public SynchronizedServerScoreboard(SimplifiedServerScoreboard serverScoreboard) {
        super(null);
        this.serverScoreboard = serverScoreboard;
    }

    @Override
    public boolean containsObjective(String name) {
        synchronized (lock) {
            boolean containsObjective = serverScoreboard.containsObjective(name);
            serverScoreboard.noDelayedAction();
            return containsObjective;
        }
    }

    @Override
    public ScoreboardObjective getObjective(String name) {
        synchronized (lock) {
            ScoreboardObjective objective = serverScoreboard.getObjective(name);
            serverScoreboard.noDelayedAction();
            return objective;
        }
    }

    @Nullable
    @Override
    public ScoreboardObjective getNullableObjective(@Nullable String name) {
        synchronized (lock) {
            ScoreboardObjective objective = serverScoreboard.getObjective(name);
            serverScoreboard.noDelayedAction();
            return objective;
        }
    }

    @Override
    public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion2, Text displayName, ScoreboardCriterion.RenderType renderType) {
        ScoreboardObjective scoreboardObjective;
        synchronized (lock) {
            serverScoreboard.noDelayedAction();
            scoreboardObjective = serverScoreboard.addObjective(name, criterion2, displayName, renderType);
        }
        serverScoreboard.runDelayedAction();
        return  scoreboardObjective;
    }

    @Override
    public void forEachScore(ScoreboardCriterion criterion, String player, Consumer<ScoreboardPlayerScore> action) {
        synchronized (lock) {
            serverScoreboard.forEachScore(criterion, player, action);
            serverScoreboard.noDelayedAction();
        }
    }

    @Override
    public boolean playerHasObjective(String playerName, ScoreboardObjective objective) {
        synchronized (lock) {
            boolean playerHasObjective = serverScoreboard.playerHasObjective(playerName, objective);
            serverScoreboard.noDelayedAction();
            return playerHasObjective;
        }
    }

    @Override
    public ScoreboardPlayerScore getPlayerScore(String playerName, ScoreboardObjective objective2) {
        synchronized (lock) {
            ScoreboardPlayerScore playerScore = serverScoreboard.getPlayerScore(playerName, objective2);
            serverScoreboard.noDelayedAction();
            return playerScore;
        }
    }

    @Override
    public Collection<ScoreboardPlayerScore> getAllPlayerScores(ScoreboardObjective objective) {
        synchronized (lock) {
            Collection<ScoreboardPlayerScore> allPlayerScores = serverScoreboard.getAllPlayerScores(objective);
            serverScoreboard.noDelayedAction();
            return allPlayerScores;
        }
    }

    @Override
    public Collection<ScoreboardObjective> getObjectives() {
        synchronized (lock) {
            Collection<ScoreboardObjective> objectives1 = serverScoreboard.getObjectives();
            serverScoreboard.noDelayedAction();
            return objectives1;
        }
    }

    @Override
    public Collection<String> getObjectiveNames() {
        synchronized (lock) {
            Collection<String> objectiveNames = serverScoreboard.getObjectiveNames();
            serverScoreboard.noDelayedAction();
            return objectiveNames;
        }
    }

    @Override
    public Collection<String> getKnownPlayers() {
        synchronized (lock) {
            Collection<String> knownPlayers = serverScoreboard.getKnownPlayers();
            serverScoreboard.noDelayedAction();
            return knownPlayers;
        }
    }

    @Override
    public void resetPlayerScore(String playerName, @Nullable ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.resetPlayerScore(playerName, objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public Map<ScoreboardObjective, ScoreboardPlayerScore> getPlayerObjectives(String playerName) {
        synchronized (lock) {
            Map<ScoreboardObjective, ScoreboardPlayerScore> playerObjectives = serverScoreboard.getPlayerObjectives(playerName);
            serverScoreboard.noDelayedAction();
            return playerObjectives;
        }
    }

    @Override
    public void removeObjective(ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.removeObjective(objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.setObjectiveSlot(slot, objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Nullable
    @Override
    public ScoreboardObjective getObjectiveForSlot(int slot) {
        synchronized (lock) {
            @Nullable ScoreboardObjective objectiveForSlot = serverScoreboard.getObjectiveForSlot(slot);
            serverScoreboard.noDelayedAction();
            return objectiveForSlot;
        }
    }

    @Nullable
    @Override
    public Team getTeam(String name) {
        synchronized (lock) {
            @Nullable Team team = serverScoreboard.getTeam(name);
            serverScoreboard.noDelayedAction();
            return team;
        }
    }

    @Override
    public Team addTeam(String name) {
        Team team;
        synchronized (lock) {
            team = serverScoreboard.addTeam(name);
        }
        serverScoreboard.runDelayedAction();
        return team;
    }

    @Override
    public void removeTeam(Team team) {
        synchronized (lock) {
            serverScoreboard.removeTeam(team);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public boolean addPlayerToTeam(String playerName, Team team) {
        boolean addPlayerToTeam;
        synchronized (lock) {
            addPlayerToTeam = serverScoreboard.addPlayerToTeam(playerName, team);
        }
        serverScoreboard.runDelayedAction();
        return addPlayerToTeam;
    }

    @Override
    public boolean clearPlayerTeam(String playerName) {
        boolean clearPlayerTeam;
        synchronized (lock) {
            clearPlayerTeam = serverScoreboard.clearPlayerTeam(playerName);
        }
        serverScoreboard.runDelayedAction();
        return clearPlayerTeam;
    }

    @Override
    public void removePlayerFromTeam(String playerName, Team team) {
        synchronized (lock) {
            serverScoreboard.removePlayerFromTeam(playerName, team);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public Collection<String> getTeamNames() {
        Collection<String> teamNames;
        synchronized (lock) {
            teamNames = serverScoreboard.getTeamNames();
            serverScoreboard.noDelayedAction();
        }
        return teamNames;
    }

    @Override
    public Collection<Team> getTeams() {
        Collection<Team> scoreboardTeams;
        synchronized (lock) {
            scoreboardTeams = serverScoreboard.getTeams();
            serverScoreboard.noDelayedAction();
        }
        return scoreboardTeams;
    }

    @Nullable
    @Override
    public Team getPlayerTeam(String playerName) {
        @Nullable Team playerTeam;
        synchronized (lock) {
            playerTeam = serverScoreboard.getPlayerTeam(playerName);
            serverScoreboard.noDelayedAction();
        }
        return playerTeam;
    }

    @Override
    public void resetEntityScore(Entity entity) {
        synchronized (lock) {
            serverScoreboard.resetEntityScore(entity);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    protected NbtList toNbt() {
        NbtList nbtElements;
        synchronized (lock) {
            nbtElements = serverScoreboard.toNbt();
            serverScoreboard.noDelayedAction();
        }
        return nbtElements;
    }

    @Override
    protected void readNbt(NbtList list) {
        synchronized (lock) {
            serverScoreboard.readNbt(list);
        }
        serverScoreboard.noDelayedAction();
    }

    @Override
    public void updateScore(ScoreboardPlayerScore score) {
        synchronized (lock) {
            serverScoreboard.updateScore(score);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updatePlayerScore(String playerName) {
        synchronized (lock) {
            serverScoreboard.updatePlayerScore(playerName);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.updatePlayerScore(playerName, objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.updateObjective(objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.updateExistingObjective(objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.updateRemovedObjective(objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        synchronized (lock) {
            serverScoreboard.updateScoreboardTeamAndPlayers(team);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        synchronized (lock) {
            serverScoreboard.updateScoreboardTeam(team);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateRemovedTeam(Team team) {
        synchronized (lock) {
            serverScoreboard.updateRemovedTeam(team);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void addUpdateListener(Runnable listener) {
        synchronized (lock) {
            serverScoreboard.addUpdateListener(listener);
            serverScoreboard.noDelayedAction();
        }
    }

    @Override
    public List<Packet<?>> createChangePackets(ScoreboardObjective objective) {
        List<Packet<?>> changePackets;
        synchronized (lock) {
            changePackets = serverScoreboard.createChangePackets(objective);
            serverScoreboard.noDelayedAction();
        }
        return changePackets;
    }

    @Override
    public void addScoreboardObjective(ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.addScoreboardObjective(objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
        List<Packet<?>> removePackets;
        synchronized (lock) {
            removePackets = serverScoreboard.createRemovePackets(objective);
            serverScoreboard.noDelayedAction();
        }
        return removePackets;
    }

    @Override
    public void removeScoreboardObjective(ScoreboardObjective objective) {
        synchronized (lock) {
            serverScoreboard.removeScoreboardObjective(objective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public int getSlot(ScoreboardObjective objective) {
        int slot = 0;
        synchronized (lock) {
            slot = serverScoreboard.getSlot(objective);
            serverScoreboard.noDelayedAction();
        }
        return slot;
    }

    @Override
    public ScoreboardState createState() {
        ScoreboardState state;
        synchronized (lock) {
            state = serverScoreboard.createState();
            serverScoreboard.noDelayedAction();
        }
        return state;
    }

    public ScoreboardState stateFromNbt(NbtCompound nbt) {
        ScoreboardState scoreboardState;
        synchronized (lock) {
            scoreboardState = serverScoreboard.stateFromNbt(nbt);
            serverScoreboard.noDelayedAction();
        }
        return scoreboardState;
    }

}
