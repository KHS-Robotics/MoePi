package com.moe365.moepi.client;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A {@link RioPacket} that broadcasts an error message.
 * @author mailmindlin
 */
public class ErrorRioPacket implements RioPacket {
	private final String message;

	public ErrorRioPacket(String message) {
		this.message = message;
	}
	
	@Override
	public int getStatus() {
		return RioPacket.STATUS_ERROR;
	}

	@Override
	public int getLength() {
		return message.getBytes(StandardCharsets.UTF_8).length;
	}

	@Override
	public void writeTo(ByteBuffer buffer) {
		byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
		buffer.put(bytes);
	}
	
}
