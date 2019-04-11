package com.moe365.moepi.client.packets;

import java.nio.ByteBuffer;

import com.moe365.moepi.geom.PreciseRectangle;

public class ThreeFoundRioPacket implements RioPacket {
    private final PreciseRectangle rect1, rect2, rect3;

    public ThreeFoundRioPacket(double left1, double top1, double width1, double height1, 
                            double left2, double top2, double width2, double height2, 
                            double left3, double top3, double width3, double height3) {
		this(
            new PreciseRectangle(left1, top1, width1, height1), 
            new PreciseRectangle(left2, top2, width2, height2),
            new PreciseRectangle(left3, top3, width3, height3)
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
        // 12 doubles
        return 12 * Double.BYTES;
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
        
        buffer.putDouble(this.rect3.getX());
		buffer.putDouble(this.rect3.getY());
		buffer.putDouble(this.rect3.getWidth());
		buffer.putDouble(this.rect3.getHeight());
    }
}
