package com.moe365.moepi.net.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.moe365.moepi.net.channel.DataChannelClient;
import com.moe365.moepi.net.channel.DataChannelDirection;
import com.moe365.moepi.net.channel.DataChannelMediaType;
import com.moe365.moepi.net.channel.Property;
import com.moe365.moepi.net.packet.AckPacket;
import com.moe365.moepi.net.packet.DataPacket;
import com.moe365.moepi.net.packet.PacketTypeCode;
import com.moe365.moepi.net.packet.PropertyEnumerationPacket;
import com.moe365.moepi.net.packet.PropertyInfoPacket;
import com.moe365.moepi.net.packet.PropertyInfoRequestPacket;
import com.moe365.moepi.net.packet.PropertyValuesPacket;
import com.moe365.moepi.net.packet.PropertyValuesRequestPacket;

public class PropertyDataChannel extends AbstractWsDataChannel {
	Map<String, Property> properties = new HashMap<>();
	
	@Override
	public DataChannelMediaType getType() {
		return DataChannelMediaType.PROPERTY_ACCESS;
	}

	@Override
	public DataChannelDirection getDirection() {
		return DataChannelDirection.BOTH;
	}

	@Override
	public DataPacket parseNext(ByteBuffer buf, int typeCode) {
		switch (typeCode) {
			case PacketTypeCode.PROPERTY_ENUMERATION_REQUEST:
				return new PropertyEnumerationPacket(buf);
		}
		return null;
	}
	
	protected Property getPropertyById(int id) {
		return null;
	}

	@Override
	protected void onRecievePacket(DataPacket packet, DataChannelClient client) {
		switch (packet.getTypeCode()) {
			case PacketTypeCode.PROPERTY_ENUMERATION_REQUEST: {
				this.sendPacket(new PropertyEnumerationPacket(this.properties.values())
						.setAckId(packet.getId()),
						client);
			}
			case PacketTypeCode.PROPERTY_VALUES_REQUEST: {
				DataPacket response = new PropertyValuesPacket(
						Arrays.stream(((PropertyValuesRequestPacket) packet).getPropertyIds())
							.mapToObj(this::getPropertyById)
							.collect(Collectors.toList()))
						.setAckId(packet.getId());
				this.sendPacket(response, client);
				break;
			}
			case PacketTypeCode.PROPERTY_VALUES: {
				((PropertyValuesPacket)packet).doSet(this::getPropertyById);
				this.sendPacket(new AckPacket(packet.getAckId()), client);
				break;
			}
			case PacketTypeCode.PROPERTY_INFO_REQUEST: {
				Property prop = getPropertyById(((PropertyInfoRequestPacket) packet).getPropId());
				this.sendPacket(new PropertyInfoPacket(prop).setAckId(packet.getId()), client);
				break;
			}
		}
	}

}
