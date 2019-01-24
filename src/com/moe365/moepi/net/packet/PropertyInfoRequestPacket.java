package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

public class PropertyInfoRequestPacket extends AbstractMutableDataPacket {
	int propId;
	
	public PropertyInfoRequestPacket(ByteBuffer buf) {
		super(buf);
		this.propId = buf.getShort() & 0xFF_FF;
	}
	
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2;
	}
	
	public int getPropId() {
		return this.propId;
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		return super.writeTo(buf)
				.putShort((short) this.propId);
	}
}
