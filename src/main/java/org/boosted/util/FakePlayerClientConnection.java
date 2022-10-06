package org.boosted.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
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
    public void send(Packet<?> packet) { }
    public void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> callback) { }
    protected void updateStats() { }
    public void disconnect(Text disconnectReason) {}
    public boolean isLocal() { return true; }
    public void setupEncryption(Cipher decryptionCipher, Cipher encryptionCipher) { }
    public boolean isEncrypted() { return false; }
    public boolean isOpen() { return true; }
    public boolean hasChannel() { return false; }
    public void disableAutoRead() { }
    public void setCompressionThreshold(int compressionThreshold, boolean rejectsBadPackets) { }
    public void handleDisconnection() { }
}
