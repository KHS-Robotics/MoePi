package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;
import java.util.List;

import com.moe365.moepi.geom.PreciseRectangle;

public class OverlayPacket extends AbstractMutableDataPacket {
	List<PreciseRectangle> rectangles;

	public OverlayPacket(List<PreciseRectangle> rectangles) {
		this.rectangles = rectangles;
		super.typeCode = PacketTypeCode.STREAM_FRAME;
	}
	
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 4 + rectangles.size() * 32;
	}
	
	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putInt(rectangles.size());
		for (PreciseRectangle rect : rectangles) {
			buf.putDouble(rect.getX());
			buf.putDouble(rect.getY());
			buf.putDouble(rect.getWidth());
			buf.putDouble(rect.getHeight());
		}
		return buf;
	}
}
