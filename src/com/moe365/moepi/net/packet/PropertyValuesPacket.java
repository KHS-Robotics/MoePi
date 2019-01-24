package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.function.IntFunction;

import com.moe365.moepi.net.channel.Property;

@PacketTypeCode(PacketTypeCode.PROPERTY_VALUES)
public class PropertyValuesPacket extends AbstractMutableDataPacket {
	int[] propIds;
	int[] values;
	
	public PropertyValuesPacket(ByteBuffer buf) {
		super(buf);
		this.setTypeCode(PacketTypeCode.PROPERTY_VALUES);
		
		int len = buf.getShort() & 0xFF_FF;
		this.propIds = new int[len];
		this.values = new int[len];
		
		for (int i = 0; i < len; i++) {
			this.propIds[i] = buf.getShort() & 0xFF_FF;
			this.values[i] = buf.getInt();
		}
	}
	
	public PropertyValuesPacket(Collection<Property> properties) {
		this.propIds = new int[properties.size()];
		this.values = new int[properties.size()];
		
		int i = 0;
		for (Property property : properties) {
			this.propIds[i] = property.getId();
			this.values[i] = property.get();
			i++;
		}
	}
	
	public PropertyValuesPacket(Property...properties) {
		this.propIds = new int[properties.length];
		this.values = new int[properties.length];
		
		for (int i = 0; i < properties.length; i++) {
			Property property = properties[i];
			this.propIds[i] = property.getId();
			this.values[i] = property.get();
		}
	}
	
	public void doSet(IntFunction<Property> propertyLookup) {
		for (int i = 0; i < this.propIds.length; i++)
			propertyLookup.apply(this.propIds[i]).set(this.values[i]);
	}
	
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2 + 6 * propIds.length;
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putShort((short) propIds.length);
		for (int i = 0; i < propIds.length; i++) {
			buf.putShort((short) this.propIds[i])
				.putInt(this.values[i]);
		}
		return buf;
	}
	
	
	//TODO finish
}
