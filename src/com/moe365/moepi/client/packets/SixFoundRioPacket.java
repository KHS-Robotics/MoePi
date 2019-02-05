package com.moe365.moepi.client.packets;

import java.nio.ByteBuffer;

import com.moe365.moepi.geom.PreciseRectangle;
import com.moe365.moepi.geom.TargetType;

public class SixFoundRioPacket implements RioPacket {
    private final PreciseRectangle rect1, rect2, rect3, rect4, rect5, rect6;

    public SixFoundRioPacket(double left1, double top1, double width1, double height1, TargetType type1, 
                            double left2, double top2, double width2, double height2, TargetType type2, 
                            double left3, double top3, double width3, double height3, TargetType type3,
                            double left4, double top4, double width4, double height4, TargetType type4,
                            double left5, double top5, double width5, double height5, TargetType type5,
                            double left6, double top6, double width6, double height6, TargetType type6) {
		this(
            new PreciseRectangle(left1, top1, width1, height1, type1), 
            new PreciseRectangle(left2, top2, width2, height2, type2),
            new PreciseRectangle(left3, top3, width3, height3, type3),
            new PreciseRectangle(left4, top4, width4, height4, type4),
            new PreciseRectangle(left5, top5, width5, height5, type5),
            new PreciseRectangle(left6, top6, width6, height6, type6)
        );
	}

    public SixFoundRioPacket(PreciseRectangle rect1, PreciseRectangle rect2, PreciseRectangle rect3, 
                                PreciseRectangle rect4, PreciseRectangle rect5, PreciseRectangle rect6) {
        this.rect1 = rect1;
        this.rect2 = rect2;
        this.rect3 = rect3;
        this.rect4 = rect4;
        this.rect5 = rect5;
        this.rect6 = rect6;
    }

    @Override
    public int getStatus() {
        return RioPacket.STATUS_SIX_FOUND;
    }

    @Override
    public int getLength() {
        // 30 doubles
        return 30 * Double.BYTES;
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
        
        buffer.putDouble(this.rect4.getX());
		buffer.putDouble(this.rect4.getY());
		buffer.putDouble(this.rect4.getWidth());
		buffer.putDouble(this.rect4.getHeight());
        buffer.putDouble((double) this.rect4.getTargetType().getType());
        
        buffer.putDouble(this.rect5.getX());
		buffer.putDouble(this.rect5.getY());
		buffer.putDouble(this.rect5.getWidth());
		buffer.putDouble(this.rect5.getHeight());
        buffer.putDouble((double) this.rect5.getTargetType().getType());
        
        buffer.putDouble(this.rect6.getX());
		buffer.putDouble(this.rect6.getY());
		buffer.putDouble(this.rect6.getWidth());
		buffer.putDouble(this.rect6.getHeight());
		buffer.putDouble((double) this.rect6.getTargetType().getType());
    }
}
