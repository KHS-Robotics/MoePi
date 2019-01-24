package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

public class StreamFramePacket extends AbstractMutableDataPacket {
	public static StreamFramePacket wrapImage(ByteBuffer imageData) {
		return new StreamFramePacket(false, imageData);
	}
	
	public static StreamFramePacket decodePacket(ByteBuffer packetData) {
		return new StreamFramePacket(true, packetData);
	}
	
	protected final ByteBuffer data;
	
	public StreamFramePacket(ByteBuffer packet) {
		this(true, packet);
	}
	
	protected StreamFramePacket(boolean parse, ByteBuffer data) {
		if (parse) {
			super.read(data);
			this.data = data.asReadOnlyBuffer();
		} else {
			this.typeCode = PacketTypeCode.STREAM_FRAME;
			this.data = data;
		}
	}
	
	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.put(this.data.duplicate());
		return buf;
	}
	
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + data.remaining();
	}
}
