package com.moe365.moepi.client.packets;

import java.nio.ByteBuffer;

import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.geom.TargetType;

public class TwoFoundRioPacket implements RioPacket {
	private final PreciseRectangle rect1, rect2;
	
	public TwoFoundRioPacket(double left1, double top1, double width1, double height1, TargetType type1, double left2, double top2, double width2, double height2, TargetType type2) {
		this(new PreciseRectangle(left1, top1, width1, height1, type1), new PreciseRectangle(left2, top2, width2, height2, type2));
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
		//10 doubles
		return 10 * Double.BYTES;
	}

	@Override
	public void writeTo(ByteBuffer buffer) {
		buffer.putDouble(this.rect1.getX());
		buffer.putDouble(this.rect1.getY());
		buffer.putDouble(this.rect1.getWidth());
		buffer.putDouble(this.rect1.getHeight());
		buffer.putDouble((double) this.rect1.getTargetType().getType());
		
		buffer.putDouble(this.rect2.getX());
		buffer.putDouble(this.rect2.getY());
		buffer.putDouble(this.rect2.getWidth());
		buffer.putDouble(this.rect2.getHeight());
		buffer.putDouble((double) this.rect2.getTargetType().getType());

	}
}
