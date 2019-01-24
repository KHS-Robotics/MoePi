package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;

/**
 * Get the values of 1+ properties
 * 
 * @author mailmindlin
 */
@PacketTypeCode(PacketTypeCode.PROPERTY_VALUES_REQUEST)
public class PropertyValuesRequestPacket extends AbstractMutableDataPacket {
	int[] ids;

	public PropertyValuesRequestPacket() {
		this.setTypeCode(PacketTypeCode.PROPERTY_VALUES_REQUEST);
	}

	public PropertyValuesRequestPacket(int... ids) {
		this();
		this.ids = ids;
	}

	public PropertyValuesRequestPacket(ByteBuffer buf) {
		super(buf);
		final int length = buf.getShort() & 0xFF_FF;
		this.ids = new int[length];
		for (int i = 0; i < length; i++)
			this.ids[i] = buf.getShort() & 0xFF_FF;
		this.setTypeCode(PacketTypeCode.PROPERTY_VALUES_REQUEST);
	}

	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2 + ids.length * 2;
	}

	public int[] getPropertyIds() {
		return this.ids;
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putShort((short)ids.length);
		for (int id : ids)
			buf.putShort((short)id);
		return buf;
	}
}
