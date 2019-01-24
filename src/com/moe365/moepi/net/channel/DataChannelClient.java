package com.moe365.moepi.net.channel;

import java.util.concurrent.CompletableFuture;

import com.moe365.moepi.net.packet.DataPacket;

public interface DataChannelClient {
	public CompletableFuture<Void> write(DataPacket packet);
}
