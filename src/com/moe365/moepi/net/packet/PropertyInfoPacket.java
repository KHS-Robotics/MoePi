package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;
import java.util.List;

import com.moe365.moepi.net.channel.Property;
import com.moe365.moepi.net.channel.PropertyType;

public class PropertyInfoPacket extends AbstractMutableDataPacket {
	int id;
	PropertyType type;
	int min;
	int max;
	int step;
	int value;
	List<String> values;

	public PropertyInfoPacket(Property prop) {
		this.id = prop.getId();
		this.type = prop.getType();
		this.min = prop.getMin();
		this.max = prop.getMax();
		this.step = prop.getStep();
		this.values = prop.getValues();
	}

	@Override
	public int getLength() {
		int len = DataPacket.HEADER_LENGTH + 23;
		if (values == null)
			return len;
		for (String value : values) {
			int strlen = value.length();
			len += strlen + 1;
			if (strlen > 0xFF)
				len += 2;
		}
		return len;
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putInt(id);
		buf.put((byte) type.ordinal());
		buf.putInt(min);
		buf.putInt(max);
		buf.putInt(step);
		buf.putInt(value);
		if (values != null) {
			buf.putShort((short) values.size());
			for (String value : values)
				DataPacket.writeString(value, buf);
		} else {
			buf.putShort((short) 0);
		}
		return buf;
	}
}
