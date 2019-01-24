package com.moe365.moepi.net.packet;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Packet structure is as follows:
 * 
 * <pre>
 * 0  - 3:	Length of packet
 * 4  - 7:	Packet ID
 * 8  - 11:	Ack Packet ID (-1 if not set)
 * 12 - 13:	Resource ID
 * 14 - 15:	Type Code
 * </pre>
 * 
 * @author mailmindlin
 */
public interface DataPacket extends Externalizable {

	public static void writeString(String str, ByteArrayOutputStream out) {
		byte[] data = ((str == null || str.isEmpty()) ? "" : str).getBytes(StandardCharsets.UTF_8);
		int len = data.length;
		if (len >= 0xFF) {
			out.write(0xFF);
			len -= 0xFF;
			if ((len >> 8) > 0xFF)
				throw new RuntimeException("Entry is too long (max size 65792b); was " + data.length + "b: " + str);
			out.write((len >> 8) & 0xFF);
		}
		out.write(len & 0xFF);
		out.write(data, 0, data.length);
	}

	public static void writeString(String str, ByteBuffer out) {
		byte[] data = ((str == null || str.isEmpty()) ? "" : str).getBytes(StandardCharsets.UTF_8);
		int len = data.length;
		if (len >= 0xFF) {
			out.put((byte) 0xFF);
			len -= 0xFF;
			len >>= 0xFF;
			if ((len >> 8) > 0xFF)
				throw new RuntimeException("Entry is too long (max size 65792b); was " + data.length + "b: " + str);
			out.put((byte) ((len >> 8) & 0xFF));
		}
		out.put((byte) (len & 0xFF));
		out.put(data, 0, data.length);
	}

	public static final int CHANNEL_ID_OFFSET = 0;
	public static final int TYPE_CODE_OFFSET = 2;
	public static final int PACKET_ID_OFFSET = 4;
	public static final int ACK_PACKET_ID_OFFSET = 8;
	@Deprecated
	public static final int DATA_OFFSET = 12;
	public static final int HEADER_LENGTH = 12;

	int getLength();

	int getId();

	int getAckId();

	int getChannelId();

	int getTypeCode();

	ByteBuffer writeTo(ByteBuffer buf);
}
