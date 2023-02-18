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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The goal of this class is to provide a simplified implementation nof ServerScoreboard.
 * The simplication primarily involves removing updateListener calls, so that external callers can decide when to trigger
 * update listeners.
 * The class is meant to be exhaustive, so any functions that hasn't been changed must be documented.
 */
public class SimplifiedServerScoreboard extends ServerScoreboard {

    private static final Logger LOGGER = LogUtils.getLogger();

    public SimplifiedServerScoreboard() {
        super(null);
    }

    /** Does not call updateListeners */
    @Override
    public boolean containsObjective(String name) {
        return super.containsObjective(name);
    }

    /** Does not call updateListeners */
    @Override
    public ScoreboardObjective getObjective(String name) {
        return super.getObjective(name);
    }

    /** Does not call updateListeners */
    @Nullable
    @Override
    public ScoreboardObjective getNullableObjective(@Nullable String name) {
        return super.getObjective(name);
    }

    /** Does call updateListeners indirectly */
    @Override
    public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion2, Text displayName, ScoreboardCriterion.RenderType renderType) {
        return super.addObjective(name, criterion2, displayName, renderType);
    }

    /** Does not call updateListeners */
    @Override
    public void forEachScore(ScoreboardCriterion criterion, String player, Consumer<ScoreboardPlayerScore> action) {
        super.forEachScore(criterion, player, action);
    }

    /** Does not call updateListeners */
    @Override
    public boolean playerHasObjective(String playerName, ScoreboardObjective objective) {
        return super.playerHasObjective(playerName, objective);
    }

    /** Does not call updateListeners */
    @Override
    public ScoreboardPlayerScore getPlayerScore(String playerName, ScoreboardObjective objective2) {
        return super.getPlayerScore(playerName, objective2);
    }

    /** Does not call updateListeners */
    @Override
    public Collection<ScoreboardPlayerScore> getAllPlayerScores(ScoreboardObjective objective) {
        return super.getAllPlayerScores(objective);
    }

    /** Does not call updateListeners */
    @Override
    public Collection<ScoreboardObjective> getObjectives() {
        return super.getObjectives();
    }

    /** Does not call updateListeners */
    @Override
    public Collection<String> getObjectiveNames() {
        return super.getObjectiveNames();
    }

    /** Does not call updateListeners */
    @Override
    public Collection<String> getKnownPlayers() {
        return super.getKnownPlayers();
    }

    /** Does call updateListeners indirectly */
    @Override
    public void resetPlayerScore(String playerName, @Nullable ScoreboardObjective objective) {
        super.resetPlayerScore(playerName, objective);
    }

    /** Does not call updateListeners */
    @Override
    public Map<ScoreboardObjective, ScoreboardPlayerScore> getPlayerObjectives(String playerName) {
        return super.getPlayerObjectives(playerName);
    }

    /** Does call updateListeners indirectly */
    @Override
    public void removeObjective(ScoreboardObjective objective) {
        super.removeObjective(objective);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
        super.setObjectiveSlot(slot, objective);
    }

    /** Does not call updateListeners */
    @Nullable
    @Override
    public ScoreboardObjective getObjectiveForSlot(int slot) {
        return super.getObjectiveForSlot(slot);
    }

    /** Does not call updateListeners */
    @Nullable
    @Override
    public Team getTeam(String name) {
        return super.getTeam(name);
    }

    /** Does call updateListeners indirectly */
    @Override
    public Team addTeam(String name) {
        return super.addTeam(name);
    }

    /** Does call updateListeners indirectly */
    @Override
    public void removeTeam(Team team) {
        super.removeTeam(team);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public boolean addPlayerToTeam(String playerName, Team team) {
        return super.addPlayerToTeam(playerName, team);
    }

    /** Does call updateListeners indirectly */
    @Override
    public boolean clearPlayerTeam(String playerName) {
        return super.clearPlayerTeam(playerName);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void removePlayerFromTeam(String playerName, Team team) {
        super.removePlayerFromTeam(playerName, team);
    }

    /** Does not call updateListeners */
    @Override
    public Collection<String> getTeamNames() {
        return super.getTeamNames();
    }

    /** Does not call updateListeners */
    @Override
    public Collection<Team> getTeams() {
        return super.getTeams();
    }

    /** Does not call updateListeners */
    @Nullable
    @Override
    public Team getPlayerTeam(String playerName) {
        return super.getPlayerTeam(playerName);
    }

    /** Does call updateListeners indirectly */
    @Override
    public void resetEntityScore(Entity entity) {
        super.resetEntityScore(entity);
    }

    /** Does not call updateListeners */
    @Override
    protected NbtList toNbt() {
        return super.toNbt();
    }

    /** Does not call updateListeners */
    @Override
    protected void readNbt(NbtList list) {
        super.readNbt(list);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateScore(ScoreboardPlayerScore score) {
        super.updateScore(score);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updatePlayerScore(String playerName) {
        super.updatePlayerScore(playerName);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
        super.updatePlayerScore(playerName, objective);
    }

    /** Does call updateListeners, maybe sends packets */
    @Override
    public void updateObjective(ScoreboardObjective objective) {
        super.updateObjective(objective);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        super.updateExistingObjective(objective);
    }

    /** Does call updateListeners */
    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        super.updateRemovedObjective(objective);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        super.updateScoreboardTeamAndPlayers(team);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateScoreboardTeam(Team team) {
        super.updateScoreboardTeam(team);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateRemovedTeam(Team team) {
        super.updateRemovedTeam(team);
    }

    /** Modifies updateListeners */
    @Override
    public void addUpdateListener(Runnable listener) {
        super.addUpdateListener(listener);
    }

    /** Directly calls runUpdateListeners */
    @Override
    protected void runUpdateListeners() {
        throw new UnsupportedOperationException("SimplifiedServerScoreboard should not call runUpdateListeners. SynchronizedServerScoreboard should call it");
    }

    /** Allows SynchronnizedServerScoreboard to call the updateListeners without synchronization */
    protected void runUnsynchronizedUpdateListeners() {
        super.runUpdateListeners();
    }

    /** Does not call updateListeners, and creates packets,
     * but may keep the lock open for a long time */
    @Override
    public List<Packet<?>> createChangePackets(ScoreboardObjective objective) {
        return super.createChangePackets(objective);
    }

    /** Sends network packets */
    @Override
    public void addScoreboardObjective(ScoreboardObjective objective) {
        super.addScoreboardObjective(objective);
    }

    /** Does not call updateListeners, and creates packets,
     * but may keep the lock open for a long time */
    @Override
    public List<Packet<?>> createRemovePackets(ScoreboardObjective objective) {
        return super.createRemovePackets(objective);
    }

    /** Sends network packets */
    @Override
    public void removeScoreboardObjective(ScoreboardObjective objective) {
        super.removeScoreboardObjective(objective);
    }

    /** Does not call updateListeners */
    @Override
    public int getSlot(ScoreboardObjective objective) {
        return super.getSlot(objective);
    }

    /** Does not call updateListeners */
    @Override
    public ScoreboardState createState() {
        return super.createState();
    }

    /** Does not call updateListeners */
    public ScoreboardState stateFromNbt(NbtCompound nbt) {
        return super.stateFromNbt(nbt);
    }

}
