package com.moe365.moepi.net.packet;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.moe365.moepi.net.exception.ErrorCode;

@PacketTypeCode(PacketTypeCode.ERROR)
public interface ErrorPacket extends DataPacket {
	ErrorCode getCode();

	String getMessage();

	public static class MutableErrorPacket extends AbstractMutableDataPacket implements ErrorPacket {
		ErrorCode code;
		String message;
		int length = 0;

		public MutableErrorPacket() {
			this.setTypeCode(PacketTypeCode.ERROR);
		}

		public MutableErrorPacket(ByteBuffer buf) {
			super(buf);
			this.code = ErrorCode.values()[buf.get() & 0xFF];
			int msgLen = ((buf.get() & 0xFF) << 12) | (buf.getShort() & 0xFF_FF);
			byte[] data = new byte[msgLen];
			buf.get(data, 0, msgLen);
			this.message = new String(data, StandardCharsets.UTF_8);
			this.length = DataPacket.HEADER_LENGTH + 4 + msgLen;
			this.setTypeCode(PacketTypeCode.ERROR);
		}

		public MutableErrorPacket(ErrorCode code, String message) {
			this();
			this.code = code;
			this.message = message;
		}

		@Override
		public int getLength() {
			if (length < 1)
				length = DataPacket.HEADER_LENGTH + 4 + (message == null ? 0 : message.getBytes(StandardCharsets.UTF_8).length);
			return length;
		}

		@Override
		public ErrorCode getCode() {
			return this.code;
		}

		public MutableErrorPacket setCode(ErrorCode code) {
			this.code = code;
			return this;
		}

		@Override
		public String getMessage() {
			return this.message;
		}

		public MutableErrorPacket setMessage(String message) {
			this.message = message;
			this.length = 0;
			return this;
		}

		@Override
		public ByteBuffer writeTo(ByteBuffer buf) {
			super.writeTo(buf)
				.put((byte)code.ordinal());
			//Somewhat faster to calculate it this way, b/c caching
			byte[] msg = this.message == null ? new byte[0] : getMessage().getBytes(StandardCharsets.UTF_8);
			buf.put((byte)(msg.length >> 12))
				.putShort((short)(msg.length & 0xFF_FF))
				.put(msg, 0, msg.length);
			return buf;
		}
	}
}
