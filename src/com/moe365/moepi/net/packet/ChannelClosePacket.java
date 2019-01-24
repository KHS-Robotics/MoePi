package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

public class ChannelClosePacket extends AbstractMutableDataPacket {
	int[] ids;
	
	public ChannelClosePacket() {
		super();
	}
	
	public ChannelClosePacket(int...ids) {
		this();
		this.ids = ids;
	}
	
	public ChannelClosePacket(ByteBuffer buf) {
		super(buf);
		int len = buf.getShort() & 0xFF_FF;
		this.ids = new int[len];
		for (int i = 0; i < len; i++)
			this.ids[i] = buf.getShort() & 0xFFFF;
	}
	
	public int[] getIds() {
		return ids;
	}
	
	public void setIds(int[] ids) {
		if (ids.length > Short.MAX_VALUE)
			throw new IllegalArgumentException("Illegal length " + ids.length);
		this.ids = ids;
	}
	
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2 + ids.length * 2;
	}
	
	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf)
			.putShort((short)this.ids.length);
		for (int i = 0; i < this.ids.length; i++)
			buf.putShort((short)ids[i]);
		return buf;
	}
}
