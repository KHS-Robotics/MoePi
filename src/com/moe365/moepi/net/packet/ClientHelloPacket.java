package com.moe365.moepi.net.packet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

/**
 * A packet that is sent in response to the SERVER_HELLO packet.
 * 
 * @author mailmindlin
 */
@PacketTypeCode(PacketTypeCode.CLIENT_HELLO)
public class ClientHelloPacket extends AbstractMutableDataPacket {
	int protocolVersion;

	public ClientHelloPacket() {
		super();
		this.setTypeCode(PacketTypeCode.CLIENT_HELLO);
	}

	public ClientHelloPacket(int protocolVersion) {
		this();
		this.protocolVersion = protocolVersion;
	}

	public ClientHelloPacket(ByteBuffer buf) {
		super(buf);
		this.protocolVersion = buf.getInt();
		this.setTypeCode(PacketTypeCode.CLIENT_HELLO);
	}

	@Override
	public int getLength() {
		return DataPacket.HEADER_LENGTH + 4;
	}

	public ClientHelloPacket setProtocolVersion(int version) {
		this.protocolVersion = version;
		return this;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		this.protocolVersion = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(getProtocolVersion());
	}

	@Override
	public ByteBuffer writeTo(ByteBuffer buf) {
		return super.writeTo(buf)
			.putInt(this.protocolVersion);
	}
}
