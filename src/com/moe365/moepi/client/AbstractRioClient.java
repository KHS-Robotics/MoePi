package com.moe365.moepi.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import com.moe365.moepi.client.packets.RioPacket;

public abstract class AbstractRioClient implements RioClient {
	
	/**
	 * Packet number. This number is to allow the client to ignore packets that
	 * are received out of order. Always increasing.
	 */
	protected AtomicInteger packetNum = new AtomicInteger(0);
	
	/**
	 * Buffer backing packets.
	 * TODO fix multithreading issues
	 */
	protected final ByteBuffer buffer = ByteBuffer.allocate(RioClient.BUFFER_SIZE);
	
	@Override
	public void broadcast(RioPacket packet) throws IOException {
		synchronized (buffer) {
			buffer.clear();
			buffer.putInt(packetNum.incrementAndGet());
			buffer.putShort((short)packet.getStatus());
			packet.writeTo(buffer);
			buffer.flip();
			send(buffer);
		}
	}
	
	protected abstract void send(ByteBuffer buffer) throws IOException;
}