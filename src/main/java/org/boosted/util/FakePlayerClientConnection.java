package org.boosted.util;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;

public class FakePlayerClientConnection extends ClientConnection {

    public FakePlayerClientConnection(NetworkSide side) {
        super(side);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext context) { }
    public void setState(NetworkState state) { }
    @Override
    public void channelInactive(ChannelHandlerContext context) { }
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable ex) { }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet) { }
    @Override
    public void send(Packet<?> packet) { }
    @Override
    public void send(Packet<?> packet, @Nullable PacketCallbacks callbacks) { }
    @Override
    protected void updateStats() { }
    @Override
    public void disconnect(Text disconnectReason) {}
    @Override
    public boolean isLocal() { return true; }
    @Override
    public void setupEncryption(Cipher decryptionCipher, Cipher encryptionCipher) { }
    @Override
    public boolean isEncrypted() { return false; }
    @Override
    public boolean isOpen() { return true; }
    @Override
    public boolean hasChannel() { return false; }
    @Override
    public void disableAutoRead() { }
    @Override
    public void setCompressionThreshold(int compressionThreshold, boolean rejectsBadPackets) { }
    @Override
    public void handleDisconnection() { }
}
