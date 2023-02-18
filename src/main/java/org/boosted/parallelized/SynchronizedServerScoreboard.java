package org.boosted.parallelized;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

/**
 * The goal of this class is to provide a public interface over ServerScoreboard that synchronizes all accesses.
 * The reason why it delegates to another ServerScoreboard instance is that the wrapped instance
 * will use its own public API internally, which means it is not supposed to use these synchronized methods.
 *
 */
public class SynchronizedServerScoreboard extends ServerScoreboard {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Object lock = new Object();
    private final ServerScoreboard serverScoreboard;


    public SynchronizedServerScoreboard(ServerScoreboard serverScoreboard) {
        super(null);
        this.serverScoreboard = serverScoreboard;
    }

    @Override
    public boolean containsObjective(String name) {
        return serverScoreboard.containsObjective(name);
    }

    @Override
    public ScoreboardObjective getObjective(String name) {
        return serverScoreboard.getObjective(name);
    }

    @Nullable
    @Override
    public ScoreboardObjective getNullableObjective(@Nullable String name) {
        return serverScoreboard.getObjective(name);
    }

    @Override
    public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion2, Text displayName, ScoreboardCriterion.RenderType renderType) {
        return serverScoreboard.addObjective(name, criterion2, displayName, renderType);
    }

    @Override
    public void forEachScore(ScoreboardCriterion criterion, String player, Consumer<ScoreboardPlayerScore> action) {
        serverScoreboard.forEachScore(criterion, player, action);
    }

    @Override
    public boolean playerHasObjective(String playerName, ScoreboardObjective objective) {
        return serverScoreboard.playerHasObjective(playerName, objective);
    }

    @Override
    public ScoreboardPlayerScore getPlayerScore(String playerName, ScoreboardObjective objective2) {
        return serverScoreboard.getPlayerScore(playerName, objective2);
    }

    @Override
    public Collection<ScoreboardPlayerScore> getAllPlayerScores(ScoreboardObjective objective) {
        return serverScoreboard.getAllPlayerScores(objective);
    }

    @Override
    public Collection<ScoreboardObjective> getObjectives() {
        return serverScoreboard.getObjectives();
    }

    @Override
    public Collection<String> getObjectiveNames() {
        return serverScoreboard.getObjectiveNames();
    }

    @Override
    public Collection<String> getKnownPlayers() {
        return serverScoreboard.getKnownPlayers();
    }

    @Override
    public void resetPlayerScore(String playerName, @Nullable ScoreboardObjective objective) {
        serverScoreboard.resetPlayerScore(playerName, objective);
    }

    @Override
    public Map<ScoreboardObjective, ScoreboardPlayerScore> getPlayerObjectives(String playerName) {
        return serverScoreboard.getPlayerObjectives(playerName);
    }

    @Override
    public void removeObjective(ScoreboardObjective objective) {
        serverScoreboard.removeObjective(objective);
    }

    @Override
    public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
        serverScoreboard.setObjectiveSlot(slot, objective);
    }

    @Nullable
    @Override
    public ScoreboardObjective getObjectiveForSlot(int slot) {
        return serverScoreboard.getObjectiveForSlot(slot);
    }

    @Nullable
    @Override
    public Team getTeam(String name) {
        return serverScoreboard.getTeam(name);
    }

    @Override
    public Team addTeam(String name) {
        return serverScoreboard.addTeam(name);
    }

    @Override
    public void removeTeam(Team team) {
        serverScoreboard.removeTeam(team);
    }

    @Override
    public boolean addPlayerToTeam(String playerName, Team team) {
        return serverScoreboard.addPlayerToTeam(playerName, team);
    }

    @Override
    public boolean clearPlayerTeam(String playerName) {
        return serverScoreboard.clearPlayerTeam(playerName);
    }

    @Override
    public void removePlayerFromTeam(String playerName, Team team) {
        serverScoreboard.removePlayerFromTeam(playerName, team);
    }

    @Override
    public Collection<String> getTeamNames() {
        return serverScoreboard.getTeamNames();
    }

    @Override
    public Collection<Team> getTeams() {
        return serverScoreboard.getTeams();
    }

    @Nullable
    @Override
    public Team getPlayerTeam(String playerName) {
        return serverScoreboard.getPlayerTeam(playerName);
    }

    @Override
    public void resetEntityScore(Entity entity) {
        serverScoreboard.resetEntityScore(entity);
    }

    @Override
    protected NbtList toNbt() {
        return serverScoreboard.toNbt();
    }

    @Override
    protected void readNbt(NbtList list) {
        serverScoreboard.readNbt(list);
    }

    @Override
    public void updateScore(ScoreboardPlayerScore score) {
        serverScoreboard.updateScore(score);
    }

    @Override
    public void updatePlayerScore(String playerName) {
        serverScoreboard.updatePlayerScore(playerName);
    }

    @Override
    public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
        serverScoreboard.updatePlayerScore(playerName, objective);
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        serverScoreboard.updateObjective(objective);
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        serverScoreboard.updateExistingObjective(objective);
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        serverScoreboard.updateRemovedObjective(objective);
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        serverScoreboard.updateScoreboardTeamAndPlayers(team);
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        serverScoreboard.updateScoreboardTeam(team);
    }

    @Override
    public void updateRemovedTeam(Team team) {
        serverScoreboard.updateRemovedTeam(team);
    }

    @Override
    public void addUpdateListener(Runnable listener) {
        serverScoreboard.addUpdateListener(listener);
    }

    @Override
    protected void runUpdateListeners() {
        serverScoreboard.runUpdateListeners();
    }

    @Override
    public List<Packet<?>> createChangePackets(ScoreboardObjective objective) {
        return serverScoreboard.createChangePackets(objective);
    }

    @Override
    public void addScoreboardObjective(ScoreboardObjective objective) {
        serverScoreboard.addScoreboardObjective(objective);
    }

    @Override
    public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
        return serverScoreboard.createRemovePackets(objective);
    }

    @Override
    public void removeScoreboardObjective(ScoreboardObjective objective) {
        serverScoreboard.removeScoreboardObjective(objective);
    }

    @Override
    public int getSlot(ScoreboardObjective objective) {
        return serverScoreboard.getSlot(objective);
    }

    @Override
    public ScoreboardState createState() {
        return serverScoreboard.createState();
    }

    public ScoreboardState stateFromNbt(NbtCompound nbt) {
        return serverScoreboard.stateFromNbt(nbt);
    }

}
