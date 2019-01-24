package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;
import java.util.Collection;

import com.moe365.moepi.net.channel.Property;

/**
 * 
 * @author mailmindlin
 */
public class PropertyEnumerationPacket extends AbstractMutableDataPacket {
	Collection<Property> properties;
	
	public PropertyEnumerationPacket() {
		this.setTypeCode(PacketTypeCode.PROPERTY_ENUMERATION);
	}
	
	public PropertyEnumerationPacket(Collection<Property> properties) {
		this();
		this.properties = properties;
	}
	
	public PropertyEnumerationPacket(ByteBuffer buf) {
		super(buf);
		//TODO parse
		//final int length = buf.getShort() & 0xFF_FF;
		
		this.setTypeCode(PacketTypeCode.PROPERTY_ENUMERATION);
	}

	public int getPropertiesCount() {
		return properties.size();
	}

	public Collection<Property> getProperties() {
		return properties;
	}


	@Override
	public int getLength() {
		int len = DataPacket.HEADER_LENGTH + 2 + properties.size() * 4;
		for (Property property : properties) {
			int strLen = property.getName().length();
			len += strLen;
			if (strLen > 0xFF)
				len += 2;
		}
		return len;
	}
	
	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putShort((short) properties.size());
		
		for (Property property : properties) {
			buf.putShort((short) property.getId())
				.put((byte)property.getType().ordinal());
			DataPacket.writeString(property.getName(), buf);
		}
		return buf;
	}
}
