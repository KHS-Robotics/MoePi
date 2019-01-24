package com.moe365.moepi.client;

import java.nio.ByteBuffer;

/**
 * {@link RioPacket} that tells the Rio that we didn't find any bounding boxes.
 * @author mailmindlin
 */
public class NoneFoundRioPacket implements RioPacket {

	@Override
	public int getStatus() {
		return RioPacket.STATUS_NONE_FOUND;
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public void writeTo(ByteBuffer buffer) {
		//We have no data to write
	}

}
