package com.moe365.moepi.client;

import java.nio.ByteBuffer;

import com.moe365.moepi.geom.PreciseRectangle;

public class TwoFoundRioPacket implements RioPacket {
	private final PreciseRectangle rect1, rect2;
	
	public TwoFoundRioPacket(double left1, double top1, double width1, double height1, double left2, double top2, double width2, double height2) {
		this(new PreciseRectangle(left1, top1, width1, height1), new PreciseRectangle(left2, top2, width2, height2));
	}
	
	public TwoFoundRioPacket(PreciseRectangle rect1, PreciseRectangle rect2) {
		this.rect1 = rect1;
		this.rect2 = rect2;
	}
	
	@Override
	public int getStatus() {
		return RioPacket.STATUS_TWO_FOUND;
	}

	@Override
	public int getLength() {
		//8 doubles
		return 8 * 8;
	}

	@Override
	public void writeTo(ByteBuffer buffer) {
		buffer.putDouble(this.rect1.getX());
		buffer.putDouble(this.rect1.getY());
		buffer.putDouble(this.rect1.getWidth());
		buffer.putDouble(this.rect1.getHeight());
		
		buffer.putDouble(this.rect2.getX());
		buffer.putDouble(this.rect2.getY());
		buffer.putDouble(this.rect2.getWidth());
		buffer.putDouble(this.rect2.getHeight());
	}
}
