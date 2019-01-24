package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

public class PropertyEnumerationRequestPacket extends AbstractMutableDataPacket {
	public PropertyEnumerationRequestPacket (ByteBuffer buf) {
		super(buf);
		this.setTypeCode(PacketTypeCode.PROPERTY_ENUMERATION_REQUEST);
	}
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH;
	}
	//No properties
}
