package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

public class ChannelMetadataRequestPacket extends AbstractMutableDataPacket {
	int targetChannelId;
	public ChannelMetadataRequestPacket() {
		super(PacketTypeCode.CHANNEL_METADATA_REQUEST);
	}
	
	public ChannelMetadataRequestPacket(int targetChannelId) {
		super(PacketTypeCode.CHANNEL_METADATA_REQUEST);
		this.targetChannelId = targetChannelId;
	}
	
	public ChannelMetadataRequestPacket(ByteBuffer buf) {
		super(buf);
		this.targetChannelId = buf.getShort() & 0xFF_FF;
	}
	
	public int getTargetChannelId() {
		return targetChannelId;
	}
	
	public ChannelMetadataRequestPacket setTargetChannelId(int targetChannelId) {
		this.targetChannelId = targetChannelId;
		return this;
	}
	
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2;
	}
	
	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		return super.writeTo(buf)
			.putShort((short) this.targetChannelId);
	}

}
