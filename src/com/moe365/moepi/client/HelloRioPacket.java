package com.moe365.moepi.client;
import java.nio.ByteBuffer;

/**
 * A {@link RioPacket} basically for handshaking with the Rio.
 * The main purpose of this packet is that, if <code>stronghold-pi</code> is restarted, it's packet
 * ids will restart from 0 (expected behavior), but any devices listening to the UDP packets that it
 * emits will keep dropping packets until the id of the packets emitted by the current instance of
 * stronghold-pi 'catches up' with the previous one.
 * @author mailmindlin
 */
public class HelloRioPacket implements RioPacket {
	@Override
	public int getStatus() {
		return RioPacket.STATUS_HELLO_WORLD;
	}
	
	@Override
	public int getLength() {
		return 0;
	}
	
	@Override
	public void writeTo(ByteBuffer buffer) {
		//Nop
	}
}