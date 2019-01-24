package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

@PacketTypeCode(PacketTypeCode.CHANNEL_ENUMERATION_REQUEST)
public class ChannelEnumerationRequestPacket extends AbstractMutableDataPacket {

	public ChannelEnumerationRequestPacket(ByteBuffer buf) {
		super(buf);
		this.setTypeCode(PacketTypeCode.CHANNEL_ENUMERATION_REQUEST);
	}
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH;
	}

}
