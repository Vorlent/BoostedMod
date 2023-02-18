package org.boosted.parallelized;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.SynchronizedResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The goal of this class is to provide a simplified implementation nof ServerScoreboard.
 * The simplication primarily involves delaying sending packets and updateListener calls,
 * so that external callers can decide when to trigger update listeners, etc.
 * The class is meant to be exhaustive, so any functions that hasn't been changed must be documented.
 */
public class SimplifiedServerScoreboard extends ServerScoreboard {

    private final SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> synchronizedServer;
    private final ThreadLocal<List<Consumer<MinecraftServer>>> delayedAction = ThreadLocal.withInitial(() -> new ArrayList<>());

    public SimplifiedServerScoreboard(MinecraftServer server) {
        super(null);
        synchronizedServer = server.getSynchronizedServer();
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
        ScoreboardObjective scoreboardObjective = this.getObjectiveForSlot(slot);
        this.objectiveSlots[slot] = objective;
        if (scoreboardObjective != objective && scoreboardObjective != null) {
            if (this.getSlot(scoreboardObjective) > 0) {
                delayAction(server -> {
                    server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
                });
            } else {
                this.removeScoreboardObjective(scoreboardObjective);
            }
        }
        if (objective != null) {
            if (this.objectives.contains(objective)) {
                delayAction(server -> {
                    server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective));
                });
            } else {
                this.addScoreboardObjective(objective);
            }
        }
        this.runUpdateListeners();
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

    /**
     * Taken from Scoreboard.addPlayerToTeam
     */
    private boolean addPlayerToTeamFromScoreboard(String playerName, Team team) {
        if (this.getPlayerTeam(playerName) != null) {
            this.clearPlayerTeam(playerName);
        }
        this.teamsByPlayer.put(playerName, team);
        return team.getPlayerList().add(playerName);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public boolean addPlayerToTeam(String playerName, Team team) {
        if (addPlayerToTeamFromScoreboard(playerName, team)) {
            delayAction(server -> {
                server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.ADD));
            });
            this.runUpdateListeners();
            return true;
        }
        return false;
    }

    /** Does call updateListeners indirectly */
    @Override
    public boolean clearPlayerTeam(String playerName) {
        return super.clearPlayerTeam(playerName);
    }

    /**
     * Taken from Scoreboard.removePlayerFromTeam
     */
    private void removePlayerFromTeamFromScoreboard(String playerName, Team team) {
        if (this.getPlayerTeam(playerName) != team) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + team.getName() + "'.");
        }
        this.teamsByPlayer.remove(playerName);
        team.getPlayerList().remove(playerName);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void removePlayerFromTeam(String playerName, Team team) {
        removePlayerFromTeamFromScoreboard(playerName, team);
        delayAction(server -> {
            server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.REMOVE));
        });
        this.runUpdateListeners();
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
        if (this.objectives.contains(score.getObjective())) {
            delayAction(server -> {
                server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.CHANGE, score.getObjective().getName(), score.getPlayerName(), score.getScore()));
            });
        }
        this.runUpdateListeners();
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updatePlayerScore(String playerName) {
        delayAction(server -> {
            server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.REMOVE, null, playerName, 0));
        });
        this.runUpdateListeners();
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
        if (this.objectives.contains(objective)) {
            delayAction(server -> {
                server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.REMOVE, objective.getName(), playerName, 0));
            });
        }
        this.runUpdateListeners();
    }

    /** Does call updateListeners, maybe sends packets (doesn't send packets) */
    @Override
    public void updateObjective(ScoreboardObjective objective) {
        super.updateObjective(objective);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        if (this.objectives.contains(objective)) {
            delayAction(server -> {
                server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE));
            });
        }
        this.runUpdateListeners();
    }

    /** Does call updateListeners */
    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        super.updateRemovedObjective(objective);
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        delayAction(server -> {
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true));
        });
        this.runUpdateListeners();
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateScoreboardTeam(Team team) {
        delayAction(server -> {
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, false));
        });
        this.runUpdateListeners();
    }

    /** Does call updateListeners and sends packets */
    @Override
    public void updateRemovedTeam(Team team) {
        delayAction(server -> {
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(team));
        });
        this.runUpdateListeners();
    }

    /** Modifies updateListeners */
    @Override
    public void addUpdateListener(Runnable listener) {
        super.addUpdateListener(listener);
    }

    /** Directly calls runUpdateListeners.
     * runUpdateListeners now delays. */
    @Override
    protected void runUpdateListeners() {
        delayAction(server -> {
            super.runUpdateListeners();
        });
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
        List<Packet<?>> list = this.createChangePackets(objective);
        delayAction(server -> {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                for (Packet<?> packet : list) {
                    serverPlayerEntity.networkHandler.sendPacket(packet);
                }
            }
        });
        this.objectives.add(objective);
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
        List<Packet<?>> list = this.createRemovePackets(objective);
        delayAction((server) -> {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                for (Packet<?> packet : list) {
                    serverPlayerEntity.networkHandler.sendPacket(packet);
                }
            }
        });
        this.objectives.remove(objective);
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

    private void delayAction(Consumer<MinecraftServer> consumer) {
        delayedAction.get().add(consumer);
    }

    /**
     * After calling any method, you must call runDelayedAction in SynchronizedServerScoreboard.
     */
    public void runDelayedAction() {
        synchronizedServer.write(server -> {
            for (Consumer<MinecraftServer> consumer : delayedAction.get()) {
                consumer.accept(server);
            }
            delayedAction.get().clear();
        });
    }

}
