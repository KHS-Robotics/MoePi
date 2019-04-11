package com.moe365.moepi.client.packets;

import java.nio.ByteBuffer;

import com.moe365.moepi.geom.PreciseRectangle;

/**
 * {@link RioPacket} that tells the Rio that we found one bounding box (and where it is, of course!)
 * @author mailmindlin
 */
public class OneFoundRioPacket implements RioPacket {
	double x;
	double y;
	double width;
	double height;
	
	public OneFoundRioPacket(PreciseRectangle rect) {
		this(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
	
	public OneFoundRioPacket(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public int getStatus() {
		return RioPacket.STATUS_ONE_FOUND;
	}

	@Override
	public int getLength() {
		// 4 doubles
		return 4 * Double.BYTES;
	}

	@Override
	public void writeTo(ByteBuffer buffer) {
		buffer.putDouble(this.x);
		buffer.putDouble(this.y);
		buffer.putDouble(this.width);
		buffer.putDouble(this.height);
	}
}
