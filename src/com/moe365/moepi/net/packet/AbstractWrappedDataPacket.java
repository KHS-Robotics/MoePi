package com.moe365.moepi.net.packet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class AbstractWrappedDataPacket implements DataPacket {
	ByteBuffer buf;
	/**
	 * For deserialization
	 */
	protected AbstractWrappedDataPacket() {
		
	}
	protected AbstractWrappedDataPacket(ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public int getLength() {
		return buf.limit();
	}

	@Override
	public int getId() {
		return this.buf.getInt(DataPacket.PACKET_ID_OFFSET);
	}

	@Override
	public int getAckId() {
		return this.buf.getInt(DataPacket.ACK_PACKET_ID_OFFSET);
	}

	@Override
	public int getChannelId() {
		return this.buf.getShort(DataPacket.CHANNEL_ID_OFFSET) & 0xFF_FF;
	}

	@Override
	public int getTypeCode() {
		return this.buf.getShort(DataPacket.TYPE_CODE_OFFSET) & 0xFF_FF;
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		buf.put(this.buf);
		return buf;
	}

	protected String stringAt(int pos) {
		int len = this.buf.get(pos);
		byte[] tmp = new byte[len];
		this.buf.get(tmp, pos + 1, len);
		return new String(tmp, StandardCharsets.UTF_8);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		byte[] data;
		if (buf.hasArray()) {
			data = buf.array();
		} else {
			data = new byte[buf.limit()];
			buf.get(data, 0, data.length);
		}
		out.writeInt(data.length);
		out.write(data);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int len = in.readInt();
		byte[] tmp = new byte[len];
		in.read(tmp);
		buf = ByteBuffer.wrap(tmp);
	}
}
