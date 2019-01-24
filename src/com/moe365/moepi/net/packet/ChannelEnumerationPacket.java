package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

import com.moe365.moepi.net.channel.DataChannel;

public class ChannelEnumerationPacket extends AbstractMutableDataPacket {
	DataChannel[] channels;
	public ChannelEnumerationPacket(DataChannel...channels) {
		super(PacketTypeCode.CHANNEL_ENUMERATION);
		this.channels = channels;
	}
	
	public ChannelEnumerationPacket(ByteBuffer buf) {
		super(buf);
	}
	
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2 + channels.length * 4;
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putShort((short)channels.length);
		for (int i = 0; i < channels.length; i++) {
			DataChannel channel = channels[i];
			buf.putShort((short)channel.getId());
			buf.put((byte)channel.getType().ordinal());
			buf.put((byte)channel.getDirection().ordinal());
		}
		return buf;
	}
}
