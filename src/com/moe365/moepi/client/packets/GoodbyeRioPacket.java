package com.moe365.moepi.client.packets;

import java.nio.ByteBuffer;

public class GoodbyeRioPacket implements RioPacket {
	@Override
	public int getStatus() {
		return RioPacket.STATUS_GOODBYE;
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