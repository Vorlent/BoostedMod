package org.boosted.parallelized.scoreboard;

import com.google.common.collect.Maps;
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
 * This SynchronizedServerScoreboard essentially acts like a SynchronizedResource<SimplifiedServerScoreboard>
 * but with the interface of a ServerScoreboard.
 *
 * What this tells you is that the caller should always be responsible for synchronization,
 * after all, if SimplifiedServerScoreboard was synchronized, it would call its own methods,
 * synchronizing twice and thereby running into a deadlock. But if the caller ensures synchronization,
 * then the resource itself can be carefree in calling its own functions.
 * It also minimizes the need to write synchronized wrapper classes like this one.
 * The downside is that changing the calling code in a minecraft mod requires a lot of mixins.
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
            return synchronizedScoreboardObjective(objective);
        }
    }

    @Nullable
    @Override
    public ScoreboardObjective getNullableObjective(@Nullable String name) {
        synchronized (lock) {
            ScoreboardObjective objective = serverScoreboard.getObjective(name);
            serverScoreboard.noDelayedAction();
            return synchronizedScoreboardObjective(objective);
        }
    }

    @Override
    public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion2, Text displayName, ScoreboardCriterion.RenderType renderType) {
        ScoreboardObjective scoreboardObjective;
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            serverScoreboard.noDelayedAction();
            scoreboardObjective = serverScoreboard.addObjective(name, criterion2, displayName, renderType);
        }
        serverScoreboard.runDelayedAction();
        return synchronizedScoreboardObjective(scoreboardObjective);
    }

    @Override
    public void forEachScore(ScoreboardCriterion criterion, String player, Consumer<ScoreboardPlayerScore> action) {
        synchronized (lock) {
            serverScoreboard.forEachScore(criterion, player, (scoreboardPlayerScore) -> {
                action.accept(synchronizedScoreboardPlayerScore(scoreboardPlayerScore));
            });
            serverScoreboard.noDelayedAction();
        }
    }

    @Override
    public boolean playerHasObjective(String playerName, ScoreboardObjective objective) {
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            boolean playerHasObjective = serverScoreboard.playerHasObjective(playerName, unsyncObjective);
            serverScoreboard.noDelayedAction();
            return playerHasObjective;
        }
    }

    @Override
    public ScoreboardPlayerScore getPlayerScore(String playerName, ScoreboardObjective objective2) {
        ScoreboardPlayerScore playerScore;
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective2);
            playerScore = serverScoreboard.getPlayerScore(playerName, unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
        return synchronizedScoreboardPlayerScore(playerScore);
    }

    @Override
    public Collection<ScoreboardPlayerScore> getAllPlayerScores(ScoreboardObjective objective) {
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            Collection<ScoreboardPlayerScore> allPlayerScores = serverScoreboard.getAllPlayerScores(unsyncObjective);
            serverScoreboard.noDelayedAction();
            List<ScoreboardPlayerScore> scores = new ArrayList<>();
            for (ScoreboardPlayerScore score : allPlayerScores) {
                scores.add(synchronizedScoreboardPlayerScore(score));
            }
            return scores;
        }
    }

    @Override
    public Collection<ScoreboardObjective> getObjectives() {
        synchronized (lock) {
            Collection<ScoreboardObjective> objectives1 = serverScoreboard.getObjectives();
            serverScoreboard.noDelayedAction();

            List<ScoreboardObjective> synObjectives = new ArrayList<>();
            for (ScoreboardObjective scoreboardObjective : objectives1) {
                synObjectives.add(synchronizedScoreboardObjective(scoreboardObjective));
            }
            return synObjectives;
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
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.resetPlayerScore(playerName, unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public Map<ScoreboardObjective, ScoreboardPlayerScore> getPlayerObjectives(String playerName) {
        synchronized (lock) {
            Map<ScoreboardObjective, ScoreboardPlayerScore> playerObjectives = serverScoreboard.getPlayerObjectives(playerName);
            serverScoreboard.noDelayedAction();
            Map<ScoreboardObjective, ScoreboardPlayerScore> syncPlayerObjectives = Maps.newHashMap();
            for (Map.Entry<ScoreboardObjective, ScoreboardPlayerScore> entry : playerObjectives.entrySet()) {
                syncPlayerObjectives.put(synchronizedScoreboardObjective(entry.getKey()), synchronizedScoreboardPlayerScore(entry.getValue()));
            }
            return syncPlayerObjectives;
        }
    }

    @Override
    public void removeObjective(ScoreboardObjective objective) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.removeObjective(unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.setObjectiveSlot(slot, unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Nullable
    @Override
    public ScoreboardObjective getObjectiveForSlot(int slot) {
        synchronized (lock) {
            @Nullable ScoreboardObjective objectiveForSlot = serverScoreboard.getObjectiveForSlot(slot);
            serverScoreboard.noDelayedAction();
            return synchronizedScoreboardObjective(objectiveForSlot);
        }
    }

    @Nullable
    @Override
    public Team getTeam(String name) {
        @Nullable Team team;
        synchronized (lock) {
            team = serverScoreboard.getTeam(name);
            serverScoreboard.noDelayedAction();
        }
        return synchronizedTeam(team);
    }

    @Override
    public Team addTeam(String name) {
        Team team;
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            team = serverScoreboard.addTeam(name);
        }
        serverScoreboard.runDelayedAction();
        return synchronizedTeam(team);
    }

    @Override
    public void removeTeam(Team team) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            Team unsyncTeam = unsynchronizedTeam(team);
            serverScoreboard.removeTeam(unsyncTeam);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public boolean addPlayerToTeam(String playerName, Team team) {
        boolean addPlayerToTeam;
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            Team unsyncTeam = unsynchronizedTeam(team);
            addPlayerToTeam = serverScoreboard.addPlayerToTeam(playerName, unsyncTeam);
        }
        serverScoreboard.runDelayedAction();
        return addPlayerToTeam;
    }

    @Override
    public boolean clearPlayerTeam(String playerName) {
        boolean clearPlayerTeam;
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            clearPlayerTeam = serverScoreboard.clearPlayerTeam(playerName);
        }
        serverScoreboard.runDelayedAction();
        return clearPlayerTeam;
    }

    @Override
    public void removePlayerFromTeam(String playerName, Team team) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            Team unsyncTeam = unsynchronizedTeam(team);
            serverScoreboard.removePlayerFromTeam(playerName, unsyncTeam);
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
            serverScoreboard.noDelayedAction();
            playerTeam = serverScoreboard.getPlayerTeam(playerName);
            serverScoreboard.noDelayedAction();
        }
        return synchronizedTeam(playerTeam);
    }

    @Override
    public void resetEntityScore(Entity entity) {
        serverScoreboard.acceptDelayedAction();
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
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardPlayerScore unsycScore = unsynchronizedScoreboardPlayerScore(score);
            serverScoreboard.updateScore(unsycScore);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updatePlayerScore(String playerName) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            serverScoreboard.updatePlayerScore(playerName);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.updatePlayerScore(playerName, unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.updateObjective(unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.updateExistingObjective(unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.updateRemovedObjective(unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            serverScoreboard.updateScoreboardTeamAndPlayers(team);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            serverScoreboard.updateScoreboardTeam(team);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public void updateRemovedTeam(Team team) {
        serverScoreboard.acceptDelayedAction();
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
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            changePackets = serverScoreboard.createChangePackets(unsyncObjective);
            serverScoreboard.noDelayedAction();
        }
        return changePackets;
    }

    @Override
    public void addScoreboardObjective(ScoreboardObjective objective) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.addScoreboardObjective(unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
        List<Packet<?>> removePackets;
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            removePackets = serverScoreboard.createRemovePackets(unsyncObjective);
            serverScoreboard.noDelayedAction();
        }
        return removePackets;
    }

    @Override
    public void removeScoreboardObjective(ScoreboardObjective objective) {
        serverScoreboard.acceptDelayedAction();
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            serverScoreboard.removeScoreboardObjective(unsyncObjective);
        }
        serverScoreboard.runDelayedAction();
    }

    @Override
    public int getSlot(ScoreboardObjective objective) {
        int slot = 0;
        synchronized (lock) {
            ScoreboardObjective unsyncObjective = unsynchronizedScoreboardObjective(objective);
            slot = serverScoreboard.getSlot(unsyncObjective);
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

    @Override
    public ScoreboardState stateFromNbt(NbtCompound nbt) {
        serverScoreboard.acceptDelayedAction();
        ScoreboardState scoreboardState;
        synchronized (lock) {
            scoreboardState = serverScoreboard.stateFromNbt(nbt);
        }
        serverScoreboard.runDelayedAction();
        return scoreboardState;
    }

    /**
     * SynchronizedTeam represents the external interface of the scoreboard.
     */
    private Team synchronizedTeam(Team team) {
        if (team == null) {
            return null;
        }
        if (team instanceof SimplifiedTeam simplifiedTeam) {
            return new SynchronizedTeam(this, simplifiedTeam);
        } else {
            throw new UnsupportedOperationException("Can only synchronize SimplifiedTeam");
        }
    }

    private Team unsynchronizedTeam(Team team) {
        if (team == null) {
            return null;
        }
        if (team instanceof SynchronizedTeam syncTeam) {
            return syncTeam.getSimplifiedTeam();
        } else {
            throw new UnsupportedOperationException("Can only unsynchronize SynchronizedTeam");
        }
    }

    /**
     * SynchronizedScoreboardPlayerScore represents the external interface of the scoreboard.
     * @param scoreboardPlayerScore
     * @return
     */
    private ScoreboardPlayerScore synchronizedScoreboardPlayerScore(ScoreboardPlayerScore scoreboardPlayerScore) {
        if (scoreboardPlayerScore instanceof SimplifiedScoreboardPlayerScore score) {
            return new SynchronizedScoreboardPlayerScore(this, score);
        } else {
            throw new UnsupportedOperationException("Can only synchronize SimplifiedScoreboardPlayerScore");
        }
    }

    private ScoreboardPlayerScore unsynchronizedScoreboardPlayerScore(ScoreboardPlayerScore scoreboardPlayerScore) {
        if (scoreboardPlayerScore == null) {
            return null;
        }
        if (scoreboardPlayerScore instanceof SynchronizedScoreboardPlayerScore score) {
            return score.getSimplifiedScoreboardPlayerScore();
        } else {
            throw new UnsupportedOperationException("Can only unsynchronize SynchronizedScoreboardPlayerScore");
        }
    }

    /**
     * SynchronizedScoreboardObjective represents the external interface of the scoreboard.
     */
    private ScoreboardObjective synchronizedScoreboardObjective(ScoreboardObjective scoreboardObjective) {
        if (scoreboardObjective == null) {
            return null;
        }
        if (scoreboardObjective instanceof SimplifiedScoreboardObjective objective) {
            return new SynchronizedScoreboardObjective(this, objective);
        } else {
            throw new UnsupportedOperationException("Can only synchronize SimplifiedScoreboardObjective");
        }
    }

    private ScoreboardObjective unsynchronizedScoreboardObjective(ScoreboardObjective scoreboardObjective) {
        if (scoreboardObjective == null) {
            return null;
        }
        if (scoreboardObjective instanceof SynchronizedScoreboardObjective objective) {
            return objective.getSimplifiedScoreboardObjective();
        } else {
            throw new UnsupportedOperationException("Can only unsynchronize SynchronizedScoreboardObjective");
        }
    }
}
