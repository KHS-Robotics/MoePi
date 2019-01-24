package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;
import java.util.Objects;

public class ChannelSubscribePacket extends AbstractMutableDataPacket {
	protected int[] ids;

	public ChannelSubscribePacket() {
		this.setTypeCode(PacketTypeCode.CHANNEL_SUBSCRIBE);
	}

	public ChannelSubscribePacket(int... ids) {
		Objects.requireNonNull(ids, "Ids must not be null");
		if (ids.length > 0xFF_FF)
			throw new IllegalArgumentException("Invalid length " + ids.length + ": max length is 65535.");
		this.ids = ids;
		this.setTypeCode(PacketTypeCode.CHANNEL_SUBSCRIBE);
	}

	public ChannelSubscribePacket(ByteBuffer buf) {
		super(buf);
		final int length = buf.getShort() & 0xFF_FF;
		this.ids = new int[length];
		for (int i = 0; i < length; i++)
			ids[i] = buf.getShort() & 0xFF_FF;
		this.setTypeCode(PacketTypeCode.CHANNEL_SUBSCRIBE);
	}

	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2 + ids.length * 2;
	}

	public int[] getChannelIds() {
		return ids;
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putShort((short) ids.length);
		for (int i = 0; i < ids.length; i++)
			buf.putShort((short) ids[i]);
		return buf;
	}
}
