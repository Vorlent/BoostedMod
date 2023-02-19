package org.boosted.parallelized;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.Entity;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ParallelServerScoreboard extends ServerScoreboard {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> synchronizedServer;
    // TODO implement ServerScoreBoard synchronizationn
    private final Object lock = new Object();

    public ParallelServerScoreboard(MinecraftServer server) {
        super(null);
        synchronizedServer = server.getSynchronizedServer();
    }

    @Override
    public void updateScore(ScoreboardPlayerScore score) {
        boolean contains = false;
        synchronized (lock) {
            contains = this.objectives.contains(score.getObjective());
        }
        if (contains) {
            synchronizedServer.write(server ->
                server.getPlayerManager().sendToAll(
                    new ScoreboardPlayerUpdateS2CPacket(
                        UpdateMode.CHANGE, score.getObjective().getName(), score.getPlayerName(), score.getScore()))
            );

        }
        this.runUpdateListeners();
    }

    @Override
    public void updatePlayerScore(String playerName) {
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.REMOVE, null, playerName, 0))
        );
        this.runUpdateListeners();
    }

    @Override
    public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
        boolean contains = false;
        synchronized (lock) {
            contains = this.objectives.contains(objective);
        }
        if (contains) {
            synchronizedServer.write(server ->
                server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.REMOVE, objective.getName(), playerName, 0))
            );
        }
        this.runUpdateListeners();
    }

    @Override
    public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
        // TODO synchronization
        ScoreboardObjective scoreboardObjective = this.objectiveSlots[slot];
        this.objectiveSlots[slot] = objective;

        if (scoreboardObjective != objective && scoreboardObjective != null) {
            if (this.getSlot(scoreboardObjective) > 0) {
                synchronizedServer.write(server ->
                    server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective))
                );
            } else {
                this.removeScoreboardObjective(scoreboardObjective);
            }
        }
        if (objective != null) {
            if (this.objectives.contains(objective)) {
                synchronizedServer.write(server ->
                    server.getPlayerManager().sendToAll(new ScoreboardDisplayS2CPacket(slot, objective))
                );
            } else {
                this.addScoreboardObjective(objective);
            }
        }
        this.runUpdateListeners();
    }

    @Override
    public boolean addPlayerToTeam(String playerName, Team team) {
        boolean addedPlayer = false;
        synchronized (lock) {
            this.teamsByPlayer.put(playerName, team);
            addedPlayer = team.getPlayerList().add(playerName);
        }

        if (addedPlayer) {
            synchronizedServer.write(server ->
                server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.ADD))
            );
            this.runUpdateListeners();
            return true;
        }
        return false;
    }

    @Override
    public void removePlayerFromTeam(String playerName, Team team) {
        synchronized (lock) {
            if (this.getPlayerTeam(playerName) != team) {
                throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + team.getName() + "'.");
            }
            this.teamsByPlayer.remove(playerName);
            team.getPlayerList().remove(playerName);
        }

        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.REMOVE))
        );
        this.runUpdateListeners();
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        this.runUpdateListeners();
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        boolean contains = false;
        synchronized (lock) {
            contains = this.objectives.contains(objective);
        }
        if (contains) {
            synchronizedServer.write(server ->
                server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE))
            );
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        boolean contains = false;
        synchronized (lock) {
            contains = this.objectives.contains(objective);
        }
        if (contains) {
            this.removeScoreboardObjective(objective);
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true))
        );
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, false))
        );
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedTeam(Team team) {
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(team))
        );
        this.runUpdateListeners();
    }

    @Override
    public void addScoreboardObjective(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createChangePackets(objective); //TODO synchronize
        synchronizedServer.write(server -> {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                for (Packet<?> packet : list) {
                    serverPlayerEntity.networkHandler.sendPacket(packet);
                }
            }
        });
        synchronized (lock) {
            this.objectives.add(objective);
        }
    }

    @Override
    public void removeScoreboardObjective(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createRemovePackets(objective); //TODO synchronize
        synchronizedServer.write(server -> {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                for (Packet<?> packet : list) {
                    serverPlayerEntity.networkHandler.sendPacket(packet);
                }
            }
        });

        synchronized (lock) {
            this.objectives.remove(objective);
        }
    }

    /* straight from Scoreboard */

    public boolean containsObjective(String name) {
        synchronized (lock) {
            return super.containsObjective(name);
        }
    }

    public ScoreboardObjective getObjective(String name) {
        synchronized (lock) {
            return super.getObjective(name);
        }
    }

    @Nullable
    public ScoreboardObjective getNullableObjective(@Nullable String name) {
        synchronized (lock) {
            return super.getNullableObjective(name);
        }
    }

    public ScoreboardObjective addObjective(String name, ScoreboardCriterion criterion2, Text displayName, ScoreboardCriterion.RenderType renderType) {
        synchronized (lock) {
            return super.addObjective(name, criterion2, displayName, renderType);
        }
    }

    public void forEachScore(ScoreboardCriterion criterion, String player, Consumer<ScoreboardPlayerScore> action) {
        synchronized (lock) {
            super.forEachScore(criterion, player, action);
        }
    }

    public boolean playerHasObjective(String playerName, ScoreboardObjective objective) {
        synchronized (lock) {
            return super.playerHasObjective(playerName, objective);
        }
    }

    public ScoreboardPlayerScore getPlayerScore(String playerName, ScoreboardObjective objective2) {
        synchronized (lock) {
            return super.getPlayerScore(playerName, objective2);
        }
    }

    public Collection<ScoreboardPlayerScore> getAllPlayerScores(ScoreboardObjective objective) {
        synchronized (lock) {
            return super.getAllPlayerScores(objective);
        }
    }

    public Collection<ScoreboardObjective> getObjectives() {
        synchronized (lock) {
            return super.getObjectives();
        }
    }

    public Collection<String> getObjectiveNames() {
        synchronized (lock) {
            return super.getObjectiveNames();
        }
    }

    public Collection<String> getKnownPlayers() {
        synchronized (lock) {
            return super.getKnownPlayers();
        }
    }

    public void resetPlayerScore(String playerName, @Nullable ScoreboardObjective objective) {
        synchronized (lock) {
            super.resetPlayerScore(playerName, objective);
        }
    }

    public Map<ScoreboardObjective, ScoreboardPlayerScore> getPlayerObjectives(String playerName) {
        synchronized (lock) {
            return super.getPlayerObjectives(playerName);
        }
    }

    public void removeObjective(ScoreboardObjective objective) {
        synchronized (lock) {
            super.removeObjective(objective);
        }
    }

    @Nullable
    public ScoreboardObjective getObjectiveForSlot(int slot) {
        synchronized (lock) {
            return super.getObjectiveForSlot(slot);
        }
    }

    @Nullable
    public Team getTeam(String name) {
        synchronized (lock) {
            return super.getTeam(name);
        }
    }

    public Team addTeam(String name) {
        synchronized (lock) {
            Team team = this.getTeam(name);
            if (team != null) {
                LOGGER.warn("Requested creation of existing team '{}'", (Object)name);
                return team;
            }
            team = new ParallelTeam(this, name);
            this.teams.put(name, team);
            this.updateScoreboardTeamAndPlayers(team); // TODO avoid runinng update listeners inside synchronization
            return team;
        }
    }

    public void removeTeam(Team team) {
        synchronized (lock) {
            super.removeTeam(team);
        }
    }

    public boolean clearPlayerTeam(String playerName) {
        synchronized (lock) {
            return super.clearPlayerTeam(playerName);
        }
    }

    public Collection<String> getTeamNames() {
        synchronized (lock) {
            return super.getTeamNames();
        }
    }

    public Collection<Team> getTeams() {
        synchronized (lock) {
            return super.getTeams();
        }
    }

    @Nullable
    public Team getPlayerTeam(String playerName) {
        synchronized (lock) {
            return super.getPlayerTeam(playerName);
        }
    }

    public void resetEntityScore(Entity entity) {
        synchronized (lock) {
            super.resetEntityScore(entity);
        }
    }
    protected NbtList toNbt() {
        synchronized (lock) {
            return super.toNbt();
        }
    }

    protected void readNbt(NbtList list) {
        synchronized (lock) {
            super.readNbt(list);
        }
    }
}
