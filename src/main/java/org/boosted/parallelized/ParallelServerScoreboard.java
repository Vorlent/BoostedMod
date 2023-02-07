package org.boosted.parallelized;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.SynchronizedResource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ParallelServerScoreboard extends ServerScoreboard {
    private SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> synchronizedServer;
    // TODO implement ServerScoreBoard synchronizationn

    public ParallelServerScoreboard(MinecraftServer server) {
        super(server);
        // TODO share synchronized resource with server
        synchronizedServer = new SynchronizedResource<>(server, new UnmodifiableMinecraftServer(server));
    }

    @Override
    public void updateScore(ScoreboardPlayerScore score) {
        super.updateScore(score);
        if (this.objectives.contains(score.getObjective())) {
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
        super.updatePlayerScore(playerName);
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.REMOVE, null, playerName, 0))
        );
        this.runUpdateListeners();
    }

    @Override
    public void updatePlayerScore(String playerName, ScoreboardObjective objective) {
        super.updatePlayerScore(playerName, objective);
        if (this.objectives.contains(objective)) {
            synchronizedServer.write(server ->
                server.getPlayerManager().sendToAll(new ScoreboardPlayerUpdateS2CPacket(UpdateMode.REMOVE, objective.getName(), playerName, 0))
            );
        }
        this.runUpdateListeners();
    }

    @Override
    public void setObjectiveSlot(int slot, @Nullable ScoreboardObjective objective) {
        ScoreboardObjective scoreboardObjective = this.getObjectiveForSlot(slot);
        super.setObjectiveSlot(slot, objective);
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
        if (super.addPlayerToTeam(playerName, team)) {
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
        super.removePlayerFromTeam(playerName, team);
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(TeamS2CPacket.changePlayerTeam(team, playerName, TeamS2CPacket.Operation.REMOVE))
        );
        this.runUpdateListeners();
    }

    @Override
    public void updateObjective(ScoreboardObjective objective) {
        super.updateObjective(objective);
        this.runUpdateListeners();
    }

    @Override
    public void updateExistingObjective(ScoreboardObjective objective) {
        super.updateExistingObjective(objective);
        if (this.objectives.contains(objective)) {
            synchronizedServer.write(server ->
                server.getPlayerManager().sendToAll(new ScoreboardObjectiveUpdateS2CPacket(objective, ScoreboardObjectiveUpdateS2CPacket.UPDATE_MODE))
            );
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedObjective(ScoreboardObjective objective) {
        super.updateRemovedObjective(objective);
        if (this.objectives.contains(objective)) {
            this.removeScoreboardObjective(objective);
        }
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeamAndPlayers(Team team) {
        super.updateScoreboardTeamAndPlayers(team);
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, true))
        );
        this.runUpdateListeners();
    }

    @Override
    public void updateScoreboardTeam(Team team) {
        super.updateScoreboardTeam(team);
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateTeam(team, false))
        );
        this.runUpdateListeners();
    }

    @Override
    public void updateRemovedTeam(Team team) {
        super.updateRemovedTeam(team);
        synchronizedServer.write(server ->
            server.getPlayerManager().sendToAll(TeamS2CPacket.updateRemovedTeam(team))
        );
        this.runUpdateListeners();
    }

    @Override
    public void addScoreboardObjective(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createChangePackets(objective);
        synchronizedServer.write(server -> {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                for (Packet<?> packet : list) {
                    serverPlayerEntity.networkHandler.sendPacket(packet);
                }
            }
        });
        this.objectives.add(objective);
    }

    @Override
    public void removeScoreboardObjective(ScoreboardObjective objective) {
        List<Packet<?>> list = this.createRemovePackets(objective);
        synchronizedServer.write(server -> {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                for (Packet<?> packet : list) {
                    serverPlayerEntity.networkHandler.sendPacket(packet);
                }
            }
        });
        this.objectives.remove(objective);
    }
}
