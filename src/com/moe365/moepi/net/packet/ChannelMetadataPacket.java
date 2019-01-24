package com.moe365.moepi.net.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ChannelMetadataPacket extends AbstractMutableDataPacket {
	transient byte[] data;
	int numEntries;
	Map<String, String> map;
	
	public ChannelMetadataPacket(Map<String, String> metadata) {
		super(PacketTypeCode.CHANNEL_METADATA);
		this.map = metadata;
		this.numEntries = map.entrySet().size();
	}
	
	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 2 + getData().length;
	}
	
	protected void writeString(String str, ByteArrayOutputStream out) {
		byte[] data = ((str == null || str.isEmpty()) ? "" : str).getBytes(StandardCharsets.UTF_8);
		int len = data.length;
		if (len >= 0xFF) {
			out.write(0xFF);
			len -= 0xFF;
			if ((len >> 8) > 0xFF)
				throw new RuntimeException("Entry is too long (max size 65792b); was " + data.length + "b: " + str);
			out.write((len >>  8) & 0xFF);
		}
		out.write(len & 0xFF);
		out.write(data, 0, data.length);
	}
	
	protected byte[] getData() {
		System.out.println(this.map);
		if (data != null)
			return this.data;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			for (Map.Entry<String, String> entry : this.map.entrySet()) {
				writeString(entry.getKey(), baos);
				writeString(entry.getValue(), baos);
			}
			return this.data = baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		super.writeTo(buf);
		buf.putShort((short)this.numEntries);
		buf.put(getData());
		return buf;
	}
}
