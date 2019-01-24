package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

import com.moe365.moepi.net.channel.UnsubscriptionReason;

public class ChannelUnsubscribePacket extends AbstractMutableDataPacket {
	protected int[] ids;
	protected UnsubscriptionReason[] reasons;

	public ChannelUnsubscribePacket() {
		this.setTypeCode(PacketTypeCode.CHANNEL_UNSUBSCRIBE);
	}

	public ChannelUnsubscribePacket(int[] ids, UnsubscriptionReason[] reasons) {
		this();
		this.ids = ids;
		this.reasons = reasons;
	}

	public ChannelUnsubscribePacket(ByteBuffer buf) {
		super(buf);
		final int length = buf.getShort() & 0xFF_FF;
		this.ids = new int[length];
		this.reasons = new UnsubscriptionReason[length];
		for (int i = 0; i < length; i++) {
			this.ids[i] = buf.getShort() & 0xFF_FF;
			int reasonId = buf.get() & 0xFF;
			this.reasons[i] = reasonId == 0xFF ? null : UnsubscriptionReason.values()[reasonId];
		}
		this.setTypeCode(PacketTypeCode.CHANNEL_UNSUBSCRIBE);
	}

	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2 + ids.length * 3;
	}

	public int[] getChannelIds() {
		return ids;
	}

	public UnsubscriptionReason[] getReasons() {
		return reasons;
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putShort((short) ids.length);
		for (int i = 0; i < ids.length; i++) {
			buf.putShort((short) ids[i]);
			UnsubscriptionReason reason = reasons[i];
			buf.put((byte) (reason == null ? 0xFF : reason.ordinal()));
		}
		return buf;
	}
}
