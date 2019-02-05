package com.moe365.moepi.client.packets;

import java.nio.ByteBuffer;

import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.geom.TargetType;

public class ThreeFoundRioPacket implements RioPacket {
    private final PreciseRectangle rect1, rect2, rect3;

    public ThreeFoundRioPacket(double left1, double top1, double width1, double height1, TargetType type1, 
                            double left2, double top2, double width2, double height2, TargetType type2, 
                            double left3, double top3, double width3, double height3, TargetType type3) {
		this(
            new PreciseRectangle(left1, top1, width1, height1, type1), 
            new PreciseRectangle(left2, top2, width2, height2, type2),
            new PreciseRectangle(left3, top3, width3, height3, type3)
        );
	}

    public ThreeFoundRioPacket(PreciseRectangle rect1, PreciseRectangle rect2, PreciseRectangle rect3) {
        this.rect1 = rect1;
        this.rect2 = rect2;
        this.rect3 = rect3;
    }

    @Override
    public int getStatus() {
        return RioPacket.STATUS_THREE_FOUND;
    }

    @Override
    public int getLength() {
        // 15 doubles
        return 15 * Double.BYTES;
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
        
        buffer.putDouble(this.rect3.getX());
		buffer.putDouble(this.rect3.getY());
		buffer.putDouble(this.rect3.getWidth());
		buffer.putDouble(this.rect3.getHeight());
		buffer.putDouble((double) this.rect3.getTargetType().getType());
    }
}
