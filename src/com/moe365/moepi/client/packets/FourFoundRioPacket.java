package com.moe365.moepi.client.packets;

import java.nio.ByteBuffer;

import com.moe365.moepi.geom.PreciseRectangle;

public class FourFoundRioPacket implements RioPacket {
    private final PreciseRectangle rect1, rect2, rect3, rect4;

    public FourFoundRioPacket(double left1, double top1, double width1, double height1, 
                            double left2, double top2, double width2, double height2, 
                            double left3, double top3, double width3, double height3,
                            double left4, double top4, double width4, double height4) {
		this(
            new PreciseRectangle(left1, top1, width1, height1), 
            new PreciseRectangle(left2, top2, width2, height2),
            new PreciseRectangle(left3, top3, width3, height3),
            new PreciseRectangle(left4, top4, width4, height4)
        );
	}

    public FourFoundRioPacket(PreciseRectangle rect1, PreciseRectangle rect2, PreciseRectangle rect3, PreciseRectangle rect4) {
        this.rect1 = rect1;
        this.rect2 = rect2;
        this.rect3 = rect3;
        this.rect4 = rect4;
    }

    @Override
    public int getStatus() {
        return RioPacket.STATUS_FOUR_FOUND;
    }

    @Override
    public int getLength() {
        // 16 doubles
        return 16 * Double.BYTES;
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
        
        buffer.putDouble(this.rect4.getX());
		buffer.putDouble(this.rect4.getY());
		buffer.putDouble(this.rect4.getWidth());
		buffer.putDouble(this.rect4.getHeight());
    }
}
